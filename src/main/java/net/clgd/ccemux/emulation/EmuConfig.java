package net.clgd.ccemux.emulation;

import java.nio.file.Path;

import com.google.gson.Gson;
import net.clgd.ccemux.config.Config;
import net.clgd.ccemux.config.JsonAdapter;
import net.clgd.ccemux.config.Property;

public abstract class EmuConfig extends Config {
	protected final JsonAdapter adapter;

	public EmuConfig(Gson gson) {
		adapter = new JsonAdapter(gson);
	}

	public abstract Path getDataDir();

	public Property<Double> termScale = property("termScale", Double.class, 2.0)
			.setName("Terminal scale");
	public Property<Integer> termHeight = property("termHeight", Integer.class, 19)
			.setName("Terminal height");
	public Property<Integer> termWidth = property("termWidth", Integer.class, 52)
			.setName("Terminal width");
	public Property<String> renderer = property("renderer", String.class, "AWT")
			.setName("Renderer");

	public Property<Long> maxComputerCapacity = property("maxComputerCapacity", Long.class, 2L * 1024 * 1024)
			.setName("Max computer capacity");
	public Property<Boolean> httpEnabled = property("httpEnable", Boolean.class, true)
			.setName("Enable HTTP API")
			.setDescription("Enable the \"http\" API on Computers (see \"httpWhitelist\" and \"httpBlacklist\" for more fine grained control than this)");
	public Property<String[]> httpWhitelist = property("httpWhitelist", String[].class, new String[]{"*"})
			.setName("HTTP whitelist")
			.setDescription("A list of wildcards for domains or IP ranges that can be accessed through the \"http\" API on Computers.\n" +
					"Set this to \"*\" to access to the entire internet. Example: \"*.pastebin.com\" will restrict access to just subdomains of pastebin.com.\n" +
					"You can use domain names (\"pastebin.com\"), wilcards (\"*.pastebin.com\") or CIDR notation (\"127.0.0.0/8\").");
	public Property<String[]> httpBlacklist = property("httpBlacklist", String[].class, new String[]{"127.0.0.0/8", "10.0.0.0/8", "172.16.0.0/12", "192.168.0.0/16", "fd00::/8"})
			.setName("HTTP blacklist")
			.setDescription("A list of wildcards for domains or IP ranges that cannot be accessed through the \"http\" API on Computers.\n" +
					"If this is empty then all whitelisted domains will be accessible. Example: \"*.github.com\" will block access to all subdomains of github.com.\n" +
					"You can use domain names (\"pastebin.com\"), wilcards (\"*.pastebin.com\") or CIDR notation (\"127.0.0.0/8\").");
	public Property<Boolean> disableLua51Features = property("disableLua51Features", Boolean.class, false)
			.setName("Disable Lua 5.1 features")
			.setDescription("Set this to true to disable Lua 5.1 functions that will be removed in a future update. Useful for ensuring forward compatibility of your programs now.");
	public Property<String> defaultComputerSettings = property("defaultComputerSettings", String.class, "")
			.setName("Default computer settings")
			.setDescription("A comma seperated list of default system settings to set on new computers. Example: \"shell.autocomplete=false,lua.autocomplete=false,edit.autocomplete=false\" will disable all autocompletion");
}
