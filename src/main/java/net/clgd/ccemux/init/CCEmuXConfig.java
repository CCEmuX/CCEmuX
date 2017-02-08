package net.clgd.ccemux.init;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class CCEmuXConfig {
	private static final Logger log = LoggerFactory.getLogger(CCEmuXConfig.class);

	public static final CCEmuXConfig defaults;

	public static final String CONFIG_FILE_NAME = "ccemux.json";

	static {
		Gson gson = new Gson();

		CCEmuXConfig tmp;
		try (Reader r = new InputStreamReader(CCEmuXConfig.class.getResourceAsStream("/ccemux.json"))) {
			tmp = gson.fromJson(r, CCEmuXConfig.class);
		} catch (NullPointerException | IOException e) {
			log.warn("Failed to get default config values", e);
			tmp = new CCEmuXConfig();
		}

		defaults = tmp;
	}

	public static CCEmuXConfig loadConfig(Path dataDir) {
		Gson gson = new Gson();

		File cfgFile = dataDir.resolve(CONFIG_FILE_NAME).toFile();

		CCEmuXConfig cfg;

		try {
			cfg = gson.fromJson(new FileReader(cfgFile), CCEmuXConfig.class);
		} catch (FileNotFoundException e) {
			log.warn("No user config file found, using defaults", e);
			cfg = new CCEmuXConfig();
		}

		cfg.dataDir = dataDir;

		return cfg;
	}

	private CCEmuXConfig() {

	}

	private transient Path dataDir;

	private String ccModule;

	private String ccRevision;

	private String ccExt;

	private String ccPatternRemote;

	private String ccPatternLocal;

	private Integer termWidth;

	private Integer termHeight;

	private Integer termScale;

	private String renderer;

	private Boolean apiEnabled;

	public URL getCCRemote() throws MalformedURLException {
		return new URL(getCCPatternRemote().replace("[module]", getCCModule()).replace("[revision]", getCCRevision())
				.replace("[ext]", getCCExt()));
	}

	public Path getCCLocal() {
		return Paths.get(getCCPatternLocal().replace("[module]", getCCModule()).replace("[revision]", getCCRevision())
				.replace("[ext]", getCCExt()));
	}

	public Path getDataDir() {
		return dataDir;
	}

	public String getCCModule() {
		if (this == defaults) return ccModule;

		return Optional.ofNullable(ccModule).orElse(defaults.ccModule);
	}

	public String getCCRevision() {
		if (this == defaults) return ccRevision;

		return Optional.ofNullable(ccRevision).orElse(defaults.ccRevision);
	}

	public String getCCExt() {
		if (this == defaults) return ccExt;

		return Optional.ofNullable(ccExt).orElse(defaults.ccExt);
	}

	public String getCCPatternRemote() {
		if (this == defaults) return ccPatternRemote;

		return Optional.ofNullable(ccPatternRemote).orElse(defaults.ccPatternRemote);
	}

	public String getCCPatternLocal() {
		if (this == defaults) return ccPatternLocal;

		return Optional.ofNullable(ccPatternLocal).orElse(defaults.ccPatternLocal);
	}

	public int getTermWidth() {
		if (this == defaults) return termWidth;

		return Optional.ofNullable(termWidth).orElse(defaults.termWidth);
	}

	public int getTermHeight() {
		if (this == defaults) return termHeight;

		return Optional.ofNullable(termHeight).orElse(defaults.termHeight);
	}

	public int getTermScale() {
		if (this == defaults) return termScale;

		return Optional.ofNullable(termScale).orElse(defaults.termScale);
	}

	public String getRenderer() {
		if (this == defaults) return renderer;

		return Optional.ofNullable(renderer).orElse(defaults.renderer);
	}

	public boolean isApiEnabled() {
		if (this == defaults) return apiEnabled;

		return Optional.ofNullable(apiEnabled).orElse(defaults.apiEnabled);
	}

	public void setCCModule(String ccModule) {
		this.ccModule = ccModule;
	}

	public void setCCRevision(String ccRevision) {
		this.ccRevision = ccRevision;
	}

	public void setCCExt(String ccExt) {
		this.ccExt = ccExt;
	}

	public void setCCPatternRemote(String ccPatternRemote) {
		this.ccPatternRemote = ccPatternRemote;
	}

	public void setCCPatternLocal(String ccPatternLocal) {
		this.ccPatternLocal = ccPatternLocal;
	}

	public void setTermWidth(int termWidth) {
		this.termWidth = termWidth;
	}

	public void setTermHeight(int termHeight) {
		this.termHeight = termHeight;
	}

	public void setTermScale(int termScale) {
		this.termScale = termScale;
	}

	public void setRenderer(String renderer) {
		this.renderer = renderer;
	}

	public void setApiEnabled(boolean apiEnabled) {
		this.apiEnabled = apiEnabled;
	}
}
