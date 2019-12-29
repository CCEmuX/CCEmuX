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
import java.util.HashSet;
import java.util.Optional;

import javax.swing.*;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dan200.computercraft.ComputerCraft;
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
		new Launcher(args).launch();
		System.exit(0);
	}

	private final CommandLine cli;
	private final Path dataDir;

	private Launcher(String args[]) {
		// parse cli options
		CommandLine _cli = null;
		try {
			_cli = new DefaultParser().parse(opts, args);
		} catch (ParseException e) {
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

		if (!source.getScheme().equals("file")) {
			throw new IllegalStateException("Incompatible CC location: " + source.toString());
		}

		return new File(source);
	}

	private void launch() {
		try {
			setSystemLAF();

			Files.createDirectories(dataDir);

			log.info("Loading user config");
			UserConfig cfg;
			try {
				getClass().getClassLoader().loadClass("dan200.computercraft.core.lua.CobaltLuaMachine");
				cfg = new UserConfigCCTweaked(dataDir);
			} catch (ReflectiveOperationException ignored) {
				cfg = new UserConfig(dataDir);
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
			dataDir.resolve("assets").resolve("computercraft").resolve("lua").toFile().mkdirs();

			ComputerCraft.log = LogManager.getLogger(ComputerCraft.class);
			cfg.setup();

			pluginMgr.setup();

			String renderer;
			if (cli.hasOption('r') && cli.getOptionValue('r') == null) {
				log.info("Available rendering methods:");
				pluginMgr.getRenderers().keySet().forEach(k -> log.info(" {}", k));
				System.exit(0);
				return;
			} else if (cli.hasOption('r')) {
				renderer = cli.getOptionValue('r');
			} else {
				renderer = cfg.renderer.get();
			}

			RendererFactory renderFactory = pluginMgr.getRenderers().get(renderer);
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

			pluginMgr.onInitializationCompleted();

			log.info("Setting up emulation environment");

			if (!GraphicsEnvironment.isHeadless()) {
				Optional.ofNullable(SplashScreen.getSplashScreen()).ifPresent(SplashScreen::close);
			}

			TerminalFont.loadImplicitFonts(getClass().getClassLoader());

			Path sessionPath = dataDir.resolve("session.json");
			CCEmuX emu = new CCEmuX(cfg, renderFactory, pluginMgr, getCCSource(), sessionPath);

			// Either restore the session or add a new computer
			SessionState session = cfg.restoreSession.get() ? SessionState.load(sessionPath) : null;
			if (session == null || session.computers.isEmpty()) {
				emu.createComputer();
			} else {
				for (SessionState.ComputerState computer : session.computers) {
					emu.createComputer(b -> b.id(computer.id).label(computer.label));
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
