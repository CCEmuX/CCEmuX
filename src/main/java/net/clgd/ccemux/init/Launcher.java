package net.clgd.ccemux.init;

import static org.apache.commons.cli.Option.builder;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import net.clgd.ccemux.OperatingSystem;
import net.clgd.ccemux.config.ConfigOption;
import net.clgd.ccemux.config.parsers.ParseException;
import net.clgd.ccemux.plugins.CCEmuXPlugin;

public class Launcher {
	private static final Options opts = new Options();

	static {
		opts.addOption(builder("h").longOpt("help").desc("Shows this help information").build());

		opts.addOption(builder("d").longOpt("data-dir")
				.desc("Sets the data directory where plugins, configs, and other data are stored.").hasArg()
				.argName("path").build());

		opts.addOption(builder("l").longOpt("log-level").desc(
				"Manually specify the logging level. Valid options are 'trace', 'debug', 'info', 'warning', and 'error'.")
				.hasArg().argName("level").build());

		opts.addOption(builder("r").longOpt("renderer")
				.desc("Sets the renderer to use. Run without a value to list all available renderers.").hasArg()
				.optionalArg(true).argName("renderer").build());

		opts.addOption(builder().longOpt("plugin").desc(
				"Used to load additional plugins not present in the default plugin directory. Value should be a path to a .jar file.")
				.hasArg().build());
	}

	private static void printHelp() {
		new HelpFormatter().printHelp("ccemux [args]", opts);
	}

	private final CommandLine cli;
	private final Logger logger;
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

		// initialize logging
		String logLevel = cli.getOptionValue('l').trim();
		if (ImmutableSet.of("trace", "debug", "info", "warning", "error").contains(logLevel)) {
			System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", logLevel);
		} else {
			System.err.format("Invalid log level '%s'\n", logLevel);
		}
		logger = LoggerFactory.getLogger("CCEmuX");
		logger.info("Starting CCEmuX");

		// set data dir
		if (cli.hasOption('d')) {
			dataDir = Paths.get(cli.getOptionValue('d'));
		} else {
			dataDir = OperatingSystem.get().getAppDataDir().resolve("ccemux");
		}
		logger.info("Data directory is {}", dataDir.toString());
	}

	private void crashMessage(Throwable e) {
		logger.error("Unexpected exception occurred!", e);
		logger.error("CCEmuX has crashed!");

		if (!GraphicsEnvironment.isHeadless()) {
			Throwable t = e;
			String trace = "";

			do {
				trace = trace + t.toString() + "\n";
			} while ((t = t.getCause()) != null);

			String niceMessage = String.format("CCEmuX has crashed due to an unexpected error.\n"
					+ "If this issue persists please create a bug report.\n\n" + "%s\n"
					+ "More details can be found in the log.", trace);

			JOptionPane.showMessageDialog(null, niceMessage, "CCEmuX crash", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void setSystemLAF() {
		if (!GraphicsEnvironment.isHeadless()) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				logger.warn("Could not set system look and feel", e);
			}
		}
	}

	private CCEmuXConfig loadConfig() throws ParseException {
		logger.debug("Loading config data");

		CCEmuXConfig cfg = new CCEmuXConfig(dataDir);
		cfg.loadConfig();

		logger.info("Config loaded");

		// print out config values for debugging purposes
		for (Field f : cfg.getClass().getDeclaredFields()) {
			if (f.isAnnotationPresent(ConfigOption.class)) {
				f.setAccessible(true);
				try {
					logger.trace("-> {} = {}", f.getName(), f.get(cfg));
				} catch (IllegalAccessException | IllegalArgumentException e) {
				}
			}
		}

		return cfg;
	}

	// TODO: Extract to separate class?
	private Set<CCEmuXPlugin> loadPlugins() {
		logger.debug("Collecting plugin sources");
		File pd = dataDir.resolve("plugins").toFile();

		if (pd.isFile())
			pd.delete();
		if (!pd.exists())
			pd.mkdirs();

		Set<URL> urls = new HashSet<>();

		for (File f : pd.listFiles()) {
			logger.debug("Adding plugin source '{}'", f.getName());
			try {
				urls.add(f.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.warn("Failed to add plugin source", e);
			}
		}

		if (cli.hasOption("plugin")) {
			for (String s : cli.getOptionValues("plugin")) {
				File f = Paths.get(s).toFile();
				logger.debug("Adding external plugin source '{}'", f.getName());
				try {
					urls.add(f.toURI().toURL());
				} catch (MalformedURLException e) {
					logger.warn("Failed to add plugin source '{}'", f.getName());
				}
			}
		}

		logger.info("Loading plugins");

		URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0]), getClass().getClassLoader());
		Set<CCEmuXPlugin> plugins = new HashSet<>();
		ServiceLoader.load(CCEmuXPlugin.class, classLoader).forEach(p -> {
			logger.debug("Discovered plugin: {}", p.getName());
			plugins.add(p);
		});

		return plugins;
	}

	private void launch() {
		try {
			File dd = dataDir.toFile();

			if (dd.isFile())
				dd.delete();

			if (!dd.exists())
				dd.mkdirs();

			setSystemLAF();

			CCEmuXConfig cfg = loadConfig();

			loadPlugins();
		} catch (Throwable e) {
			crashMessage(e);
			System.exit(2);
		}
	}

	public static void main(String args[]) {
		new Launcher(args).launch();
	}
}
