package net.clgd.ccemux.init;

import org.apache.commons.cli.Options;

import static org.apache.commons.cli.Option.builder;

public class LauncherOptions extends Options {
	private static final long serialVersionUID = -5388705196065266712L;

	public LauncherOptions() {
		addOption(builder("h").longOpt("help").desc("Shows this help information").build());
		addOption(builder("d").longOpt("data-dir")
				.desc("Sets the data directory where plugins, configs, and other data are stored.").hasArg()
				.argName("path").build());
		addOption(builder("l").longOpt("log-level").desc(
				"Manually specify the logging level. Valid options are 'trace', 'debug', 'info', 'warning', and 'error'.")
				.hasArg().argName("level").build());
		addOption(builder("r").longOpt("renderer")
				.desc("Sets the renderer to use. Run without a value to list all available renderers.").hasArg()
				.optionalArg(true).argName("renderer").build());
		addOption(builder().longOpt("plugin").desc(
				"Used to load additional plugins outside the default plugin directory. Value should be a path to a .jar file.")
				.hasArg().build());
	}
}
