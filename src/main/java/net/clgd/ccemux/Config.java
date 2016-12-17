package net.clgd.ccemux;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

public class Config extends Properties {
	private static final long serialVersionUID = 930020960006777817L;

	public static final String CONFIG_FILE_NAME = "ccemux.properties";
	public static final Properties DEFAULTS = new Properties();

	static {
		try {
			DEFAULTS.load(Config.class.getResourceAsStream("/default.properties"));
			DEFAULTS.load(Config.class.getResourceAsStream("/cc.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final Path dataDir;
	private final File configFile;

	public Path getDataDir() {
		return dataDir;
	}

	public File getConfigFile() {
		return configFile;
	}

	private final List<Runnable> listeners = new ArrayList<>();

	public Config(Path dataDir) {
		super(DEFAULTS);

		this.dataDir = dataDir;
		this.configFile = dataDir.resolve(CONFIG_FILE_NAME).toFile();

		dataDir.toFile().mkdirs();

		if (!configFile.exists()) {
			try (FileOutputStream fos = new FileOutputStream(configFile)) {
				IOUtils.copy(Config.class.getResourceAsStream("/default.properties"), fos);
				fos.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			load(new FileInputStream(configFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void addListener(Runnable listener) {
		listeners.add(listener);
		listener.run();
	}

	@Override
	public synchronized Object setProperty(String key, String value) {
		Object prev = super.setProperty(key, value);
		for (Runnable listener : listeners) listener.run();
		return prev;
	}

	public int getTermWidth() {
		return Integer.parseInt(getProperty("termWidth"));
	}

	public void setTermWidth(Integer width) {
		setProperty("termWidth", width.toString());
	}

	public int getTermHeight() {
		return Integer.parseInt(getProperty("termHeight"));
	}

	public void setTermHeight(Integer height) {
		setProperty("termHeight", height.toString());
	}

	public int getTermScale() {
		return Integer.parseInt(getProperty("termScale"));
	}

	public int getFramerate() {
		return Integer.parseInt(getProperty("framerate"));
	}

	public boolean isApiEnabled() {
		return Boolean.parseBoolean(getProperty("apiEnabled"));
	}

	public List<String> getRenderer() {
		return Arrays.stream(getProperty("renderer").split(","))
				.map(r -> r.trim())
				.collect(Collectors.toList());
	}

	public String getCCModule() {
		return getProperty("ccModule");
	}

	public String getCCRevision() {
		return getProperty("ccRevision");
	}

	public String getCCExt() {
		return getProperty("ccExt");
	}

	public String getCCChecksum() {
		return getProperty("ccChecksum");
	}

	public String getCCPatternRemote() {
		return getProperty("ccPatternRemote");
	}

	public String getCCPatternLocal() {
		return getProperty("ccPatternLocal");
	}

	public String getCCRemote() {
		return getCCPatternRemote()
				.replace("[module]", getCCModule())
				.replace("[revision]", getCCRevision())
				.replace("[ext]", getCCExt());
	}

	public String getCCLocal() {
		return getCCPatternLocal()
				.replace("[module]", getCCModule())
				.replace("[revision]", getCCRevision())
				.replace("[ext]", getCCExt());
	}

	public boolean getCCTweaks() {
		return Boolean.parseBoolean(getProperty("cctweaks.enabled"));
	}

	public void saveProperties() throws IOException {
		try (FileOutputStream fos = new FileOutputStream(configFile)) {
			store(fos, null);
			fos.flush();
		}
	}
}
