package net.clgd.ccemux.init;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import com.google.gson.Gson;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.plugins.Plugin;

@Slf4j
@EqualsAndHashCode
public class Config {
	public static final Config defaults;

	public static final String CONFIG_FILE_NAME = "ccemux.json";

	static {
		Gson gson = new Gson();

		Config tmp;
		try (Reader r = new InputStreamReader(Config.class.getResourceAsStream("/ccemux.json"))) {
			tmp = gson.fromJson(r, Config.class);
		} catch (NullPointerException | IOException e) {
			log.warn("Failed to get default config values", e);
			tmp = new Config();
		}

		defaults = tmp;
	}

	public static Config loadConfig(Path dataDir) {
		Gson gson = new Gson();

		File cfgFile = dataDir.resolve(CONFIG_FILE_NAME).toFile();

		Config cfg;

		try {
			cfg = gson.fromJson(new FileReader(cfgFile), Config.class);

			if (cfg == null) {
				cfg = new Config();
			}
		} catch (FileNotFoundException e) {
			log.warn("No user config file found, using defaults");
			cfg = new Config();
		}

		cfg.dataDir = dataDir;

		return cfg;
	}

	private Config() {

	}

	private transient Path dataDir;

	private Integer termWidth;

	private Integer termHeight;

	private Double termScale;

	private String renderer;

	private Long maxComputerCapacity;

	private Set<String> pluginBlacklist;

	public Path getDataDir() {
		return dataDir;
	}

	public int getTermWidth() {
		if (this == defaults) return termWidth;

		return Optional.ofNullable(termWidth).orElse(defaults.termWidth);
	}

	public int getTermHeight() {
		if (this == defaults) return termHeight;

		return Optional.ofNullable(termHeight).orElse(defaults.termHeight);
	}

	public double getTermScale() {
		if (this == defaults) return termScale;

		return Optional.ofNullable(termScale).orElse(defaults.termScale);
	}

	public String getRenderer() {
		if (this == defaults) return renderer;

		return Optional.ofNullable(renderer).orElse(defaults.renderer);
	}

	public long getMaxComputerCapaccity() {
		if (this == defaults) return maxComputerCapacity;

		return Optional.ofNullable(maxComputerCapacity).orElse(defaults.maxComputerCapacity);
	}

	public boolean isPluginBlacklisted(String className) {
		if (this == defaults) return Optional.ofNullable(pluginBlacklist).map(s -> s.contains(className)).orElse(false);

		return Optional.ofNullable(pluginBlacklist).map(s -> s.contains(className)).orElse(false)
				|| defaults.isPluginBlacklisted(className);
	}

	public boolean isPluginBlacklisted(Plugin plugin) {
		return isPluginBlacklisted(plugin.getClass().getName());
	}

	public void setTermWidth(int termWidth) {
		this.termWidth = termWidth;
	}

	public void setTermHeight(int termHeight) {
		this.termHeight = termHeight;
	}

	public void setTermScale(double termScale) {
		this.termScale = termScale;
	}

	public void setRenderer(String renderer) {
		this.renderer = renderer;
	}

	public void setMaxComputerCapacity(long capacity) {
		this.maxComputerCapacity = capacity;
	}
}
