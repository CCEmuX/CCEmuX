package net.clgd.ccemux.init;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.clgd.ccemux.api.emulation.EmuConfig;
import net.clgd.ccemux.config.JsonAdapter;

@EqualsAndHashCode(callSuper = true)
public class UserConfig extends EmuConfig {
	public static final String CONFIG_FILE_NAME = "ccemux.json";

	public static final String DEFAULT_FILE_NAME = "ccemux.default.json";

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Getter
	private final Path dataDir;

	private final JsonAdapter adapter;
	
	public UserConfig(Path dataDir) {
		adapter = new JsonAdapter(gson, this);
		this.dataDir = dataDir;
		getRoot().setName("CCEmuX Config");
	}

	public void load() throws IOException {
		Path configPath = dataDir.resolve(CONFIG_FILE_NAME);
		if (Files.exists(configPath)) {
			try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
				adapter.fromJson(gson.fromJson(reader, JsonElement.class));
			}
		} else {
			JsonObject elements = new JsonObject();
			elements.add("_comment", new JsonPrimitive("This is the config file for CCEmuX. Refer to ccemux.default.json for the various options."));
			adapter.fromJson(elements);
			save();
		}
	}

	public void save() throws IOException {
		try (Writer writer = Files.newBufferedWriter(dataDir.resolve(CONFIG_FILE_NAME), StandardCharsets.UTF_8)) {
			gson.toJson(adapter.toJson(), writer);
		}
	}

	public void saveDefault() throws IOException {
		try (Writer writer = Files.newBufferedWriter(dataDir.resolve(DEFAULT_FILE_NAME), StandardCharsets.UTF_8)) {
			gson.toJson(adapter.toDefaultJson(), writer);
		}
	}
}
