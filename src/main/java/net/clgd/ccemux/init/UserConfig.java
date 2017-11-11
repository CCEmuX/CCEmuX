package net.clgd.ccemux.init;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.clgd.ccemux.emulation.EmuConfig;

@EqualsAndHashCode(callSuper = true)
public class UserConfig extends EmuConfig {
	public static final String CONFIG_FILE_NAME = "ccemux.json";

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Getter
	private final Path dataDir;

	public UserConfig(Path dataDir) {
		super(gson);
		this.dataDir = dataDir;
		getRoot().setName("CCEmuX Config");
	}

	public void load() throws IOException {
		try (Reader reader = Files.newBufferedReader(dataDir.resolve(CONFIG_FILE_NAME), StandardCharsets.UTF_8)) {
			adapter.fromJson(gson.fromJson(reader, JsonElement.class));
		}
	}

	public void save() throws IOException {
		try (Writer writer = Files.newBufferedWriter(dataDir.resolve(CONFIG_FILE_NAME), StandardCharsets.UTF_8)) {
			gson.toJson(adapter.toJson(), writer);
		}
	}
}
