package net.clgd.ccemux.init;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import lombok.*;
import net.clgd.ccemux.emulation.EmuConfig;

@EqualsAndHashCode(callSuper = true)
public class UserConfig extends EmuConfig {
	public static final String CONFIG_FILE_NAME = "ccemux.json";

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static UserConfig loadConfig(Path dataDir) throws IOException {
		return new UserConfig(dataDir);
	}

	private static Map<String, JsonElement> loadMap(Path file) throws IOException {
		if (Files.exists(file)) {
			try (BufferedReader r = Files.newBufferedReader(file)) {
				return gson.fromJson(r, new TypeToken<Map<String, JsonElement>>() {}.getType());
			}
		} else {
			return Collections.emptyMap();
		}
	}

	@Getter
	private final Path dataDir;

	private UserConfig(Path dataDir) throws IOException {
		super(gson, loadMap(dataDir.resolve(CONFIG_FILE_NAME)));

		this.dataDir = dataDir;
	}

	public void saveConfig() throws IOException {
		Files.write(dataDir.resolve(CONFIG_FILE_NAME), gson.toJson(getData()).getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public String toString() {
		return getData().toString();
	}
}
