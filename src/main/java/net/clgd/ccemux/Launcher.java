package net.clgd.ccemux;

import static org.apache.commons.cli.Option.builder;

import java.awt.GraphicsEnvironment;
import java.awt.SplashScreen;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squiddev.cctweaks.lua.launch.RewritingLoader;

import com.google.common.collect.ImmutableSet;

import dan200.computercraft.ComputerCraft;
import net.clgd.ccemux.rendering.RenderingMethod;

public class Launcher {

	private static final Options opts = new Options();

	static {
		opts.addOption(builder("h").longOpt("help").desc("Shows the help information").build());

		opts.addOption(builder("d").longOpt("data-dir")
				.desc("Overrides the data directory where the CC jar, config, and default save directory are stored")
				.hasArg().optionalArg(true).argName("path").build());

		opts.addOption(builder("l").longOpt("log-level")
				.desc("Manually specify the logging level. Valid options are 'trace', 'debug', 'info', 'warning', and 'error'.")
				.hasArg().argName("level").build());

		opts.addOption(builder("r").longOpt("renderer")
				.desc("Sets the renderer to use. Run without an argument to show available renderers.").hasArg()
				.optionalArg(true).argName("type").build());

		opts.addOption(builder("c").longOpt("count").desc("How many emulated computers to create").hasArg()
				.argName("count").build());

		opts.addOption(builder("s").longOpt("save-dir")
				.desc("Overrides the save directory where CC computers save their files, separated by commas.").hasArg()
				.argName("paths").build());
	}

	public static void main(String args[]) {
		try {
			new Launcher(args).start();
		} catch (Throwable t) {
			t.printStackTrace();
			System.err.println("Uncaught exception!");

			if (!GraphicsEnvironment.isHeadless()) {
				String message = "CCEmuX has crashed!\n\n" + t.toString();

				Throwable t2 = t;
				while ((t2 = t2.getCause()) != null)
					message += '\n' + t2.toString();

				message += "\n\nCheck console for more details. If this continues, please report this as a bug.";

				if (SplashScreen.getSplashScreen() != null)
					SplashScreen.getSplashScreen().close();

				JOptionPane.showMessageDialog(null, message, "Fatal Error!", JOptionPane.ERROR_MESSAGE);
			}
		} finally {
			System.exit(-1);
		}
	}

	private final String[] args;
	private Path dataDir;
	private Logger logger;
	private Config config;

	private Launcher(String[] args) {
		this.args = args;
	}

	private void printHelp() {
		HelpFormatter hf = new HelpFormatter();
		String location;

		try {
			location = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getName();
		} catch (URISyntaxException e) {
			location = "idfk.jar";
		}

		hf.printHelp("java -jar " + location + " <args>", opts);
	}

	private void start() throws Exception {
		CommandLine cmd = new DefaultParser().parse(opts, args);

		if (cmd.hasOption('h')) {
			printHelp();
			System.exit(1);
		}

		String logLevel = cmd.getOptionValue('l', "info").trim();
		if (ImmutableSet.of("trace", "debug", "info", "warning", "error").contains(logLevel)) {
			System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", logLevel);
		} else {
			System.err.format("Invalid log level '%s'\n", logLevel);
		}

		logger = LoggerFactory.getLogger("CCEmuX");
		logger.info("Starting CCEmuX");

		dataDir = cmd.hasOption('d') ? Paths.get(cmd.getOptionValue('d'))
				: OperatingSystem.get().getAppDataDir().resolve("ccemux");

		dataDir.toFile().mkdirs();
		logger.info("Data directory is {}", dataDir.toAbsolutePath());

		logger.debug("Loading config");
		config = new Config(dataDir);
		config.forEach((k, v) -> logger.trace("-> {} = {}", k, v));
		logger.info("Loaded configuration data");

		CCBootstrapper b = new CCBootstrapper(dataDir.resolve(config.getCCLocal()).toFile());
		b.logger = logger;
		
		if (!b.exists()) {
			URL dl = new URL(config.getCCRemote());
			logger.info("Downloading CC from {}", dl.toString());
			b.download(dl);
		}

		if (b.exists()) {
			if (!config.getCCChecksum().isEmpty()) {
				logger.warn("No checksum provided, skipping CC jar validation");
			} else {
				if (!b.validate(config.getCCChecksum())) {
					logger.warn("CC jar validation failed, attempting redownload");
					b.ccJar.delete();
					b.download(new URL(config.getCCRemote()));
				} else {
					logger.debug("Successfully validated CC jar");
				}
			}
		}

		if (b.exists() && (config.getCCChecksum() == null || b.validate(config.getCCChecksum()))) {
			logger.debug("Loading CC jar");
			if (b.load()) {
				logger.info("Loaded CC version {}", ComputerCraft.getVersion());
			} else {
				System.exit(3);
			}
		} else {
			logger.error("Failed to load CC jar: File not found");
		}
		
		if (cmd.hasOption('r') && cmd.getOptionValue('r', "").isEmpty()) {
			System.out.format("Available rendering methods: %s\n",
					Arrays.stream(RenderingMethod.getMethods()).map(r -> r.name()).reduce((p1, p2) -> p1 + ", " + p2).orElse(""));
			System.exit(2);
		}

		if (cmd.hasOption('r')) {
			config.setProperty("renderer", cmd.getOptionValue('r').trim());
		}

		int count = 0;

		try {
			count = Integer.parseInt(cmd.getOptionValue('c', "1"));
		} catch (NumberFormatException e) {
			logger.debug("Failed to parse count option", e);
		} finally {
			if (count < 1) {
				logger.warn("Invalid computer count '{}', defaulting to 1", cmd.getOptionValue('c'));
				count = 1;
			}
		}

		List<Path> saveDirs = new ArrayList<>();
		
		if (cmd.hasOption('s'))
			saveDirs = Arrays.stream(cmd.getOptionValue('s', "").split(","))
				.map(s -> Paths.get(s).toAbsolutePath()).collect(Collectors.toList());

		if (config.getCCTweaks()) {
			logger.info("Injecting CCTweaks classloader");

			RewritingLoader loader = org.squiddev.cctweaks.lua.launch.Launcher.setupLoader();

			ImmutableSet.of("net.clgd.ccemux.Config", "net.clgd.ccemux.Launcher", "net.clgd.ccemux.CCBootstrapper",
					"org.slf4j.", "javax.", "org.apache.").forEach(s -> loader.addClassLoaderExclusion(s));

			loader.chain.finalise();

			loader.loadClass("net.clgd.ccemux.Runner")
					.getMethod("launchCCTweaks", Logger.class, Config.class, List.class, int.class)
					.invoke(null, logger, config, saveDirs, count);
		} else {
			Runner.launch(logger, config, saveDirs, count);
		}

		logger.info("Exiting CCEmuX");
		System.exit(0);
	}
}
