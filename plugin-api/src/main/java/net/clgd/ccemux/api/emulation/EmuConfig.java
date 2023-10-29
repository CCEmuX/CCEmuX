package net.clgd.ccemux.api.emulation;

import java.io.IOException;
import java.nio.file.Path;

import javax.annotation.Nonnull;

import net.clgd.ccemux.api.config.Config;
import net.clgd.ccemux.api.config.ConfigProperty;

/**
 * A base emulator config
 */
public abstract class EmuConfig extends Config {
	/**
	 * The directory to store emulator data in (e.g. configs, plugins, computer data)
	 */
	@Nonnull
	public abstract Path getDataDir();

	/**
	 * The directory a particular computer is stored in.
	 */
	@Nonnull
	public abstract Path getComputerDir(int id);

	@Nonnull
	public ConfigProperty<Double> termScale = property("termScale", double.class, 2.0)
		.setName("Terminal scale");

	@Nonnull
	public ConfigProperty<Integer> termHeight = property("termHeight", int.class, 19)
		.setName("Terminal height");

	@Nonnull
	public ConfigProperty<Integer> termWidth = property("termWidth", int.class, 51)
		.setName("Terminal width");

	@Nonnull
	public ConfigProperty<String> renderer = property("renderer", String.class, "AWT")
		.setName("Renderer");

	@Nonnull
	public ConfigProperty<Long> maxComputerCapacity = property("maxComputerCapacity", long.class, 2L * 1024 * 1024)
		.setName("Computer space limit")
		.setDescription("The disk space limit for computers in bytes");

	@Nonnull
	public ConfigProperty<Integer> maximumFilesOpen = property("maximumFilesOpen", int.class, 128)
		.setName("Maximum files open per computer")
		.setDescription("Set how many files a computer can have open at the same time. Set to 0 for unlimited.");

	@Nonnull
	public ConfigProperty<Boolean> httpEnabled = property("httpEnable", boolean.class, true)
		.setName("Enable HTTP API")
		.setDescription("Enable the \"http\" API on Computers (see \"httpWhitelist\" and \"httpBlacklist\" for more fine grained control than this)");

	@Nonnull
	public ConfigProperty<String[]> httpWhitelist = property("httpWhitelist", String[].class, new String[] { "*" })
		.setName("Allowed domains")
		.setDescription("A list of wildcards for domains or IP ranges that can be accessed through the \"http\" API on Computers.\n" +
			"Set this to \"*\" to access to the entire internet. Example: \"*.pastebin.com\" will restrict access to just subdomains of pastebin.com.\n" +
			"You can use domain names (\"pastebin.com\"), wilcards (\"*.pastebin.com\") or CIDR notation (\"127.0.0.0/8\").");

	@Nonnull
	public ConfigProperty<String[]> httpBlacklist = property("httpBlacklist", String[].class, new String[] { "$private" })
		.setName("Blocked domains")
		.setDescription("A list of wildcards for domains or IP ranges that cannot be accessed through the \"http\" API on Computers.\n" +
			"If this is empty then all whitelisted domains will be accessible. Example: \"*.github.com\" will block access to all subdomains of github.com.\n" +
			"You can use domain names (\"pastebin.com\"), wilcards (\"*.pastebin.com\") or CIDR notation (\"127.0.0.0/8\").");

	@Nonnull
	public ConfigProperty<String> defaultComputerSettings = property("defaultComputerSettings", String.class, "")
		.setName("Default computer settings")
		.setDescription("A comma seperated list of default system settings to set on new computers. Example: \"shell.autocomplete=false,lua.autocomplete=false,edit.autocomplete=false\" will disable all autocompletion");

	public ConfigProperty<Boolean> restoreSession = property("restoreSession", Boolean.class, false)
		.setName("Restore session")
		.setDescription("Restore computers from the previous session when starting the emulator");

	public EmuConfig() {
		getRoot().setName("CCEmuX Config");
	}

	public abstract void save() throws IOException;

	public abstract void load() throws IOException;
}
