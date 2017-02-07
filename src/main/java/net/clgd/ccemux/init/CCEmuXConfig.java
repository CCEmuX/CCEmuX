package net.clgd.ccemux.init;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.clgd.ccemux.config.Config;
import net.clgd.ccemux.config.ConfigBindingException;
import net.clgd.ccemux.config.ConfigOption;
import net.clgd.ccemux.config.parsers.BooleanParser;
import net.clgd.ccemux.config.parsers.IntegerParser;
import net.clgd.ccemux.config.parsers.ParseException;
import net.clgd.ccemux.config.parsers.StringParser;

public class CCEmuXConfig implements Config {
	private static final Logger log = LoggerFactory.getLogger(CCEmuXConfig.class);

	public static final String CONFIG_FILE_NAME = "ccemux.properties";

	public final Path dataDir;

	@ConfigOption(key = "ccModule", parser = StringParser.class, defaultValue = "")
	private String ccModule;

	@ConfigOption(key = "ccRevision", parser = StringParser.class, defaultValue = "")
	private String ccRevision;

	@ConfigOption(key = "ccExt", parser = StringParser.class, defaultValue = "")
	private String ccExt;

	@ConfigOption(key = "ccPatternRemote", parser = StringParser.class, defaultValue = "")
	private String ccPatternRemote;

	@ConfigOption(key = "ccPatternLocal", parser = StringParser.class, defaultValue = "")
	private String ccPatternLocal;

	/**
	 * The width of the terminal for emulated computers
	 */
	@ConfigOption(key = "termWidth", parser = IntegerParser.class, defaultValue = "51")
	private int termWidth;

	/**
	 * The height of the terminal for emulated computers
	 */
	@ConfigOption(key = "termHeight", parser = IntegerParser.class, defaultValue = "19")
	private int termHeight;

	/**
	 * The scale of the terminal for renderers - may be ignored depending on
	 * implementation
	 */
	@ConfigOption(key = "termScale", parser = IntegerParser.class, defaultValue = "3")
	private int termScale;

	/**
	 * The renderer to use
	 */
	@ConfigOption(key = "renderer", parser = StringParser.class, defaultValue = "AWT")
	private String renderer;

	/**
	 * Whether the <code>ccemux</code> Lua API is enabled for emulated computers
	 * - allows access to potentially abusable functions
	 */
	@ConfigOption(key = "apiEnabled", parser = BooleanParser.class, defaultValue = "true")
	private boolean apiEnabled;

	/**
	 * Gets the link to the CC jar
	 */
	public URL getCCRemote() throws MalformedURLException {
		return new URL(ccPatternRemote.replace("[module]", ccModule).replace("[revision]", ccRevision).replace("[ext]",
				ccExt));
	}

	/**
	 * Gets the path to the CC jar saved locally
	 */
	public Path getCCLocal() {
		return dataDir.resolve(
				ccPatternLocal.replace("[module]", ccModule).replace("[revision]", ccRevision).replace("[ext]", ccExt));
	}
	
	public String getCCRevision() {
		return ccRevision;
	}

	/**
	 * The width of the terminal for emulated computers
	 */
	public int getTermWidth() {
		return termWidth;
	}

	/**
	 * The height of the terminal for emulated computers
	 */
	public int getTermHeight() {
		return termHeight;
	}

	/**
	 * The scale of the terminal for emulated computers
	 */
	public int getTermScale() {
		return termScale;
	}

	/**
	 * The renderer to use for emulated computers
	 */
	public String getRenderer() {
		return renderer;
	}

	/**
	 * Whether the <code>ccemux</code> Lua API is available for emulated
	 * computers.
	 */
	public boolean isApiEnabled() {
		return apiEnabled;
	}

	/**
	 * Creates a new instance with the specified data directory (which will be
	 * used for config, saves, etc)
	 * 
	 * @param dataDir
	 */
	public CCEmuXConfig(Path dataDir) {
		this.dataDir = dataDir;
	}

	/**
	 * Loads the config from embedded resources and from the data dir
	 * 
	 * @throws ConfigBindingException
	 *             Thrown when there is a problem binding data to the object
	 * @throws ParseException
	 *             Thrown when one of the supplied values cannot be parsed
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void loadConfig() throws ConfigBindingException, ParseException {
		Properties props = new Properties();

		// load embedded defaults
		try {
			props.load(CCEmuXConfig.class.getResourceAsStream("/cc.properties"));
			props.load(CCEmuXConfig.class.getResourceAsStream("/default.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// load user config if present
		File cfgFile = dataDir.resolve(CONFIG_FILE_NAME).toFile();

		if (cfgFile.exists()) {
			try (FileReader r = new FileReader(cfgFile)) {
				props.load(r);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		this.bindConfigOptions((Map) props);
	}
}
