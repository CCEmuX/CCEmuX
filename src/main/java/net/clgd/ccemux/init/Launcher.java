package net.clgd.ccemux.init;

import static org.apache.commons.cli.Option.builder;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;

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

	public static void main(String args[]) {
		// load cli options
		CommandLine cli = null;
		try {
			cli = new DefaultParser().parse(opts, args);
		} catch (org.apache.commons.cli.ParseException e) {
			System.err.println(e.getLocalizedMessage());

			printHelp();
			System.exit(1);
		}

		if (cli.hasOption('h')) {
			printHelp();
			System.exit(0);
		}

		// initialize logger
		String logLevel = cli.getOptionValue('l').trim();
		if (ImmutableSet.of("trace", "debug", "info", "warning", "error").contains(logLevel)) {
			System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", logLevel);
		} else {
			System.err.format("Invalid log level '%s'\n", logLevel);
		}
		Logger logger = LoggerFactory.getLogger("CCEmuX");
		logger.info("Starting CCEmuX");

		// get data dir
		Path dataDir;
		if (cli.hasOption('d')) {
			dataDir = Paths.get(cli.getOptionValue('d'));
		} else {
			dataDir = OperatingSystem.get().getAppDataDir().resolve("ccemux");
		}
		logger.info("Data directory is {}", dataDir.toString());

		// load config
		logger.debug("Loading config data");
		CCEmuXConfig cfg = new CCEmuXConfig(dataDir);
		try {
			cfg.loadConfig();
		} catch (ParseException e) {
			logger.error("Failed to load CCEmuX config data", e);
			System.exit(2);
		}
		logger.debug("Config loaded");

		// print out config values for debug purposes
		for (Field f : cfg.getClass().getDeclaredFields()) {
			if (f.isAnnotationPresent(ConfigOption.class)) {
				f.setAccessible(true);
				try {
					logger.trace("-> {} = {}", f.getName(), f.get(cfg));
				} catch (IllegalArgumentException | IllegalAccessException e) {
				}
			}
		}
	}
}
