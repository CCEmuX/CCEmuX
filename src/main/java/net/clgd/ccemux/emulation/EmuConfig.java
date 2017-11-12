package net.clgd.ccemux.emulation;

import java.io.IOException;
import java.nio.file.Path;

import com.google.gson.Gson;
import net.clgd.ccemux.config.Config;
import net.clgd.ccemux.config.JsonAdapter;
import net.clgd.ccemux.config.ConfigProperty;

public abstract class EmuConfig extends Config {
	protected final JsonAdapter adapter;

	public EmuConfig(Gson gson) {
		adapter = new JsonAdapter(gson, this);
	}

	public abstract Path getDataDir();

	public ConfigProperty<Double> termScale = property("termScale", double.class, 2.0)
			.setName("Terminal scale");
	public ConfigProperty<Integer> termHeight = property("termHeight", int.class, 19)
			.setName("Terminal height");
	public ConfigProperty<Integer> termWidth = property("termWidth", int.class, 52)
			.setName("Terminal width");
	public ConfigProperty<String> renderer = property("renderer", String.class, "AWT")
			.setName("Renderer");

	public ConfigProperty<Long> maxComputerCapacity = property("maxComputerCapacity", long.class, 2L * 1024 * 1024)
			.setName("Max computer capacity");
	public ConfigProperty<Boolean> httpEnabled = property("httpEnable", boolean.class, true)
			.setName("Enable HTTP API")
			.setDescription("Enable the \"http\" API on Computers (see \"httpWhitelist\" and \"httpBlacklist\" for more fine grained control than this)");
	public ConfigProperty<String[]> httpWhitelist = property("httpWhitelist", String[].class, new String[]{"*"})
			.setName("HTTP whitelist")
			.setDescription("A list of wildcards for domains or IP ranges that can be accessed through the \"http\" API on Computers.\n" +
					"Set this to \"*\" to access to the entire internet. Example: \"*.pastebin.com\" will restrict access to just subdomains of pastebin.com.\n" +
					"You can use domain names (\"pastebin.com\"), wilcards (\"*.pastebin.com\") or CIDR notation (\"127.0.0.0/8\").");
	public ConfigProperty<String[]> httpBlacklist = property("httpBlacklist", String[].class, new String[]{"127.0.0.0/8", "10.0.0.0/8", "172.16.0.0/12", "192.168.0.0/16", "fd00::/8"})
			.setName("HTTP blacklist")
			.setDescription("A list of wildcards for domains or IP ranges that cannot be accessed through the \"http\" API on Computers.\n" +
					"If this is empty then all whitelisted domains will be accessible. Example: \"*.github.com\" will block access to all subdomains of github.com.\n" +
					"You can use domain names (\"pastebin.com\"), wilcards (\"*.pastebin.com\") or CIDR notation (\"127.0.0.0/8\").");
	public ConfigProperty<Boolean> disableLua51Features = property("disableLua51Features", boolean.class, false)
			.setName("Disable Lua 5.1 features")
			.setDescription("Set this to true to disable Lua 5.1 functions that will be removed in a future update. Useful for ensuring forward compatibility of your programs now.");
	public ConfigProperty<String> defaultComputerSettings = property("defaultComputerSettings", String.class, "")
			.setName("Default computer settings")
			.setDescription("A comma seperated list of default system settings to set on new computers. Example: \"shell.autocomplete=false,lua.autocomplete=false,edit.autocomplete=false\" will disable all autocompletion");

	public abstract void save() throws IOException;

	public abstract void load() throws IOException;
}
