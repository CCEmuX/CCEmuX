package net.clgd.ccemux.init;

import static org.apache.commons.cli.Option.builder;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.SplashScreen;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;

import javax.swing.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.core.apis.AddressPredicate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.clgd.ccemux.OperatingSystem;
import net.clgd.ccemux.emulation.CCEmuX;
import net.clgd.ccemux.plugins.PluginManager;
import net.clgd.ccemux.rendering.RendererFactory;
import net.clgd.ccemux.rendering.TerminalFonts;

@Slf4j
public class Launcher {
	private static final Options opts = new Options();

	// initialize cli options
	static {
		opts.addOption(builder("h").longOpt("help").desc("Shows this help information").build());

		opts.addOption(builder("d").longOpt("data-dir")
				.desc("Sets the data directory where plugins, configs, and other data are stored.").hasArg()
				.argName("path").build());

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

	public static void main(String args[]) {
		if (System.getProperty("ccemux.forceDirectLaunch") != null) {
			log.info("Skipping custom classloader, some features may be unavailable");
			new Launcher(args).launch();
		} else {
			try (final CCEmuXClassloader loader = new CCEmuXClassloader(
					((URLClassLoader) Launcher.class.getClassLoader()).getURLs())) {
				@SuppressWarnings("unchecked") final Class<Launcher> klass = (Class<Launcher>) loader.findClass(Launcher.class.getName());

				final Constructor<Launcher> constructor = klass.getDeclaredConstructor(String[].class);
				constructor.setAccessible(true);

				final Method launch = klass.getDeclaredMethod("launch");
				launch.setAccessible(true);
				launch.invoke(constructor.newInstance(new Object[]{args}));
			} catch (Exception e) {
				log.warn("Failed to setup rewriting classloader - some features may be unavailable", e);
				new Launcher(args).launch();
			}
		}

		System.exit(0);
	}

	private final CommandLine cli;
	private final Path dataDir;

	private Launcher(String args[]) {
		// parse cli options
		CommandLine _cli = null;
		try {
			_cli = new DefaultParser().parse(opts, args);
		} catch (org.apache.commons.cli.ParseException e) {
			System.err.println(e.getLocalizedMessage());
			printHelp();
			System.exit(1);
		}

		cli = _cli;

		if (cli.hasOption('h')) {
			printHelp();
			System.exit(0);
		}

		log.info("Starting CCEmuX");
		log.debug("ClassLoader in use: {}", this.getClass().getClassLoader().getClass().getName());

		// set data dir
		if (cli.hasOption('d')) {
			dataDir = Paths.get(cli.getOptionValue('d'));
		} else {
			dataDir = OperatingSystem.get().getAppDataDir().resolve("ccemux");
		}
		log.info("Data directory is {}", dataDir.toString());
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
					new Object[]{"CCEmuX has crashed!", scrollPane,
							"Would you like to create a bug report on GitHub?"},
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
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			log.warn("Failed to set system look and feel", e);
		}
	}

	private ClassLoader buildLoader() throws ReflectiveOperationException {
		File pd = dataDir.resolve("plugins").toFile();

		if (pd.isFile())
			pd.delete();
		if (!pd.exists())
			pd.mkdirs();

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

		if (cli.hasOption("plugin")) {
			for (String s : cli.getOptionValues("plugin")) {
				File f = Paths.get(s).toFile();
				log.debug("Adding external plugin source '{}'", f.getName());
				try {
					urls.add(f.toURI().toURL());
				} catch (MalformedURLException e) {
					log.warn("Failed to add plugin source '{}'", f.getName());
				}
			}
		}

		return new URLClassLoader(urls.toArray(new URL[urls.size()]), this.getClass().getClassLoader());
	}

	private File getCCSource() throws URISyntaxException {
		URI source = Optional.ofNullable(ComputerCraft.class.getProtectionDomain().getCodeSource())
				.orElseThrow(() -> new IllegalStateException("Cannot locate CC")).getLocation().toURI();

		log.debug("CC is loaded from {}", source);

		if (!source.getScheme().equals("file"))
			throw new IllegalStateException("Incompatible CC location: " + source.toString());

		return new File(source);
	}

	private void launch() {
		try {
			setSystemLAF();

			Files.createDirectories(dataDir);

			log.info("Loading user config");
			UserConfig cfg = new UserConfig(dataDir);
			log.debug("Config: {}", cfg);

			if (cfg.termScale.get() != cfg.termScale.get().intValue())
				log.warn("Terminal scale is not an integer - stuff might look bad! Don't blame us!");

			PluginManager pluginMgr = new PluginManager(cfg);
			pluginMgr.gatherCandidates(buildLoader());
			cfg.load();
			cfg.saveDefault();
			pluginMgr.gatherEnabled();

			pluginMgr.loaderSetup(getClass().getClassLoader());

			if (getClass().getClassLoader() instanceof CCEmuXClassloader) {
				val loader = (CCEmuXClassloader) getClass().getClassLoader();
				loader.chain.finalise();
				log.warn("ClassLoader chain finalized");
				loader.allowCC();
				log.debug("CC access now allowed");
			} else {
				log.warn("Incompatible classloader type: {}", getClass().getClassLoader().getClass());
			}

			ComputerCraft.log = LogManager.getLogger(ComputerCraft.class);

			// Setup the properties to sync with the original.
			// computerSpaceLimit isn't technically needed, but we do it for consistency's sake.
			cfg.maxComputerCapacity.addAndFireListener((o, n) -> ComputerCraft.computerSpaceLimit = n.intValue());
			cfg.httpEnabled.addAndFireListener((o, n) -> ComputerCraft.http_enable = n);
			cfg.httpWhitelist.addAndFireListener((o, n) -> ComputerCraft.http_whitelist = new AddressPredicate(n));
			cfg.httpBlacklist.addAndFireListener((o, n) -> ComputerCraft.http_blacklist = new AddressPredicate(n));
			cfg.disableLua51Features.addAndFireListener((o, n) -> ComputerCraft.disable_lua51_features = n);
			cfg.defaultComputerSettings.addAndFireListener((o, n) -> ComputerCraft.default_computer_settings = n);

			pluginMgr.setup();

			if (cli.hasOption('r') && cli.getOptionValue('r') == null) {
				log.info("Available rendering methods:");
				RendererFactory.implementations.keySet().stream().forEach(k -> log.info(" {}", k));
				System.exit(0);
			} else if (cli.hasOption('r')) {
				// TODO: figure out this
			}

			RendererFactory renderFactory = RendererFactory.implementations.get(cfg.renderer.get());
			if (renderFactory == null) {
				log.error("Specified renderer '{}' does not exist - are you missing a plugin?", cfg.renderer.get());

				if (!GraphicsEnvironment.isHeadless()) {
					JOptionPane.showMessageDialog(null,
							"Specified renderer '" + cfg.renderer.get() + "' does not exist.\n"
									+ "Please double check your config file and plugin list.",
							"Configuration Error", JOptionPane.ERROR_MESSAGE);
				}

				System.exit(1);
			}

			pluginMgr.onInitializationCompleted();

			log.info("Setting up emulation environment");

			if (!GraphicsEnvironment.isHeadless())
				Optional.ofNullable(SplashScreen.getSplashScreen()).ifPresent(SplashScreen::close);

			TerminalFonts.loadImplicitFonts();

			CCEmuX emu = new CCEmuX(cfg, renderFactory, pluginMgr, getCCSource());
			emu.createComputer();
			emu.run();

			pluginMgr.onClosing(emu);
			log.info("Emulation complete, goodbye!");
		} catch (Throwable e) {
			crashMessage(e);
		}
	}
}
