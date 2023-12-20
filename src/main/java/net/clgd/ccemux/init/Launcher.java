package net.clgd.ccemux.init;

import static org.apache.commons.cli.Option.builder;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.SplashScreen;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.*;

import dan200.computercraft.core.ComputerContext;
import dan200.computercraft.core.filesystem.WritableFileMount;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.clgd.ccemux.api.OperatingSystem;
import net.clgd.ccemux.api.rendering.RendererFactory;
import net.clgd.ccemux.api.rendering.TerminalFont;
import net.clgd.ccemux.emulation.CCEmuX;
import net.clgd.ccemux.emulation.SessionState;
import net.clgd.ccemux.plugins.PluginManager;

public class Launcher {
	private static final Logger log = LoggerFactory.getLogger(Launcher.class);
	private static final Options opts = new Options();

	// initialize cli options
	static {
		opts.addOption(builder("h").longOpt("help").desc("Shows this help information").build());

		opts.addOption(builder("d").longOpt("data-dir")
			.desc("Sets the data directory where plugins, configs, and other data are stored.").hasArg()
			.argName("path").build());

		opts.addOption(builder("a").longOpt("assets-dir")
			.desc("Sets the directory where assets are located. This should contain a 'computercraft/lua' folder.").hasArg()
			.argName("path").build());

		opts.addOption(builder("C").longOpt("computers-dir")
			.desc("Sets the directory where computer files are stored").hasArg()
			.argName("path").build());

		opts.addOption(builder("c").longOpt("start-dir")
			.desc("Start a computer whose root is this directory").hasArg()
			.argName("path").build());

		opts.addOption(builder("t").longOpt("term-size")
			.desc("Set the size of the initial computer's terminal. Later computers will use the default config. This should be a string of the form [width]x[height], for instance 51x19.").hasArg()
			.argName("termSize").build());

		opts.addOption(builder("r").longOpt("renderer")
			.desc("Sets the renderer to use. Run without a value to list all available renderers.").hasArg()
			.optionalArg(true).argName("renderer").build());

		opts.addOption(builder().longOpt("plugin").desc(
			"Used to load additional plugins not present in the default plugin directory. Value should be a path to a .jar file.")
			.hasArg().argName("file").build());
	}

	private static void printHelp() {
		new HelpFormatter().printHelp("ccemux [args]", opts);
	}

	public static void main(String[] args) {
		// parse cli options
		CommandLine cli;
		try {
			cli = new DefaultParser().parse(opts, args);
		} catch (ParseException e) {
			System.err.println(e.getLocalizedMessage());
			printHelp();
			System.exit(1);
			return;
		}

		if (cli.hasOption('h')) {
			printHelp();
			System.exit(0);
			return;
		}

		log.info("Starting CCEmuX");
		log.debug("ClassLoader in use: {}", Launcher.class.getClassLoader().getClass().getName());

		// Parse options
		Path dataDir = cli.hasOption("data-dir")
			? Paths.get(cli.getOptionValue("data-dir"))
			: OperatingSystem.get().getAppDataDir().resolve("ccemux");

		Path assetDir = cli.hasOption('a') ? Paths.get(cli.getOptionValue('a')) : dataDir.resolve("data");
		Path computerDir = cli.hasOption('C') ? Paths.get(cli.getOptionValue('C')) : dataDir.resolve("computer");

		List<Path> startIn = cli.hasOption("start-dir")
			? Arrays.stream(cli.getOptionValues("start-dir")).map(Paths::get).collect(Collectors.toList())
			: Collections.emptyList();

		List<Path> plugins = cli.hasOption("plugin")
			? Arrays.stream(cli.getOptionValues("plugin")).map(Paths::get).collect(Collectors.toList())
			: Collections.emptyList();

		boolean listRenderers = cli.hasOption('r') && cli.getOptionValue('r') == null;
		String renderer = cli.getOptionValue('r');

		String termSize = cli.getOptionValue("term-size");

		new Launcher(dataDir, assetDir, computerDir, startIn, listRenderers, renderer, termSize, plugins).launch();
		System.exit(0);
	}

	private final Path dataDir;
	private final Path assetDir;
	private final Path computerDir;
	private final List<Path> startDirs;
	private final boolean listRenderers;
	private final String renderer;
	private final String termSize;
	private final List<Path> plugins;

	public Launcher(Path dataDir, Path assetDir, Path computerDir, List<Path> startDirs, boolean listRenderers, String renderer, String termSize, List<Path> plugins) {
		this.dataDir = dataDir;
		this.assetDir = assetDir;
		this.computerDir = computerDir;
		this.startDirs = startDirs;
		this.listRenderers = listRenderers;
		this.renderer = renderer;
		this.termSize = termSize;
		this.plugins = plugins;
	}

	private void crashMessage(Throwable e) {
		CrashReport report = new CrashReport(e);
		log.error("Unexpected exception occurred!", e);
		log.error("CCEmuX has crashed!");

		if (!GraphicsEnvironment.isHeadless()) {
			JTextArea textArea = new JTextArea(12, 60);
			textArea.setEditable(false);
			textArea.setText(report.toString());

			JScrollPane scrollPane = new JScrollPane(textArea);
			scrollPane.setMaximumSize(new Dimension(600, 400));

			int result = JOptionPane.showConfirmDialog(null,
				new Object[] { "CCEmuX has crashed!", scrollPane,
					"Would you like to create a bug report on GitHub?" },
				"CCEmuX Crash", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);

			if (result == JOptionPane.YES_OPTION) {
				try {
					report.createIssue();
				} catch (URISyntaxException | IOException e1) {
					log.error("Failed to open GitHub to create issue", e1);
				}
			}
		}
	}

	private void setSystemLAF() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			log.warn("Failed to set system look and feel", e);
		}
	}

	private ClassLoader buildLoader() {
		File pd = dataDir.resolve("plugins").toFile();

		if (pd.isFile()) pd.delete();
		if (!pd.exists()) pd.mkdirs();

		HashSet<URL> urls = new HashSet<>();

		for (File f : pd.listFiles()) {
			if (f.isFile()) {
				log.debug("Adding plugin source '{}'", f.getName());
				try {
					urls.add(f.toURI().toURL());
				} catch (MalformedURLException e) {
					log.warn("Failed to add plugin source", e);
				}
			}
		}

		for (Path plugin : plugins) {
			File f = plugin.toFile();
			log.debug("Adding external plugin source '{}'", f.getName());
			try {
				urls.add(f.toURI().toURL());
			} catch (MalformedURLException e) {
				log.warn("Failed to add plugin source '{}'", f.getName());
			}
		}

		return new URLClassLoader(urls.toArray(new URL[urls.size()]), this.getClass().getClassLoader());
	}

	private File getCCSource() throws URISyntaxException {
		URI source = Optional.ofNullable(ComputerContext.class.getProtectionDomain().getCodeSource())
			.orElseThrow(() -> new IllegalStateException("Cannot locate CC")).getLocation().toURI();

		log.debug("Loaded ComputerCraft from {}", source);

		if (!source.getScheme().equals("file")) {
			throw new IllegalStateException("Incompatible CC location: " + source.toString());
		}

		return new File(source);
	}

	private void launch() {
		log.info("Data directory is {}", dataDir);
		log.info("Loading assets from {}", assetDir);
		log.info("Loading computers from {}", computerDir);

		try {
			setSystemLAF();

			Files.createDirectories(dataDir);
			Files.createDirectories(computerDir);

			log.info("Loading user config");
			UserConfig cfg;
			try {
				getClass().getClassLoader().loadClass("dan200.computercraft.core.lua.CobaltLuaMachine");
				cfg = new UserConfigCCTweaked(dataDir, assetDir, computerDir);
			} catch (ReflectiveOperationException ignored) {
				cfg = new UserConfig(dataDir, assetDir, computerDir);
			}
			log.debug("Config: {}", cfg);

			if (cfg.termScale.get() != cfg.termScale.get().intValue()) {
				log.warn("Terminal scale is not an integer - stuff might look bad! Don't blame us!");
			}

			PluginManager pluginMgr = new PluginManager(cfg);
			pluginMgr.gatherCandidates(buildLoader());
			cfg.load();
			cfg.saveDefault();
			pluginMgr.gatherEnabled();

			// Create a stub for the assets directory if needed.
			//noinspection ResultOfMethodCallIgnored
			assetDir.resolve("computercraft").resolve("lua").toFile().mkdirs();

			cfg.setup();
			pluginMgr.setup();

			String renderer;
			if (listRenderers) {
				log.info("Available rendering methods:");
				pluginMgr.getRenderers().keySet().forEach(k -> log.info(" {}", k));
				System.exit(0);
				return;
			} else if (this.renderer != null) {
				renderer = this.renderer;
			} else {
				renderer = cfg.renderer.get();
			}

			RendererFactory<?> renderFactory = pluginMgr.getRenderers().get(renderer);
			if (renderFactory == null) {
				log.error("Specified renderer '{}' does not exist - are you missing a plugin?", renderer);

				if (!GraphicsEnvironment.isHeadless()) {
					JOptionPane.showMessageDialog(null,
						"Specified renderer '" + renderer + "' does not exist.\n"
							+ "Please double check your config file and plugin list.",
						"Configuration Error", JOptionPane.ERROR_MESSAGE);
				}

				System.exit(1);
			}

			int termWidth, termHeight;
			if(termSize != null) {
				Matcher result = Pattern.compile("^(\\d+)x(\\d+)$").matcher(termSize);
				if(!result.matches()) {
					log.error("Cannot parse terminal size '{}', should be of the form [width]x[height].", termSize);
					System.exit(1);
				}

				termWidth = Integer.parseInt(result.group(1));
				termHeight = Integer.parseInt(result.group(2));
				if(termWidth <= 0 || termHeight <= 0) {
					log.error("Width and height should both be positive.");
					System.exit(1);
				}
			} else {
				termWidth = cfg.termWidth.get();
				termHeight = cfg.termHeight.get();
			}

			pluginMgr.onInitializationCompleted();

			log.info("Setting up emulation environment");

			if (!GraphicsEnvironment.isHeadless()) {
				Optional.ofNullable(SplashScreen.getSplashScreen()).ifPresent(SplashScreen::close);
			}

			TerminalFont.loadImplicitFonts(getClass().getClassLoader());

			Path sessionPath = dataDir.resolve("session.json");
			CCEmuX emu = new CCEmuX(cfg, renderFactory, pluginMgr, getCCSource(), sessionPath);

			// Either load the requested computers, restore the session or add a new computer
			if (startDirs.size() > 0) {
				for (Path dir : startDirs) {
					emu.createComputer(b -> b
						.rootMount(() -> new WritableFileMount(dir.toFile(), emu.getConfig().getMaxComputerCapacity()))
						.termSize(termWidth, termHeight)
					);
				}
			} else {
				SessionState session = cfg.restoreSession.get() ? SessionState.load(sessionPath) : null;
				if (session == null || session.computers.isEmpty()) {
					emu.createComputer(b -> b.termSize(termWidth, termHeight));
				} else {
					for (SessionState.ComputerState computer : session.computers) {
						emu.createComputer(b -> b.id(computer.id).label(computer.label).termSize(termWidth, termHeight));
					}
				}
			}

			emu.run();

			pluginMgr.onClosing(emu);
			log.info("Emulation complete, goodbye!");
		} catch (Throwable e) {
			crashMessage(e);
		}
	}
}
