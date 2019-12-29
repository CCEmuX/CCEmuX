package net.clgd.ccemux.emulation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SessionState {
	private static final Logger log = LoggerFactory.getLogger(SessionState.class);

	private static final Gson gson = new GsonBuilder().create();

	/**
	 * The list of active computers in the session
	 */
	public List<ComputerState> computers = Collections.emptyList();

	public SessionState() {
	}

	public SessionState(List<ComputerState> computers) {
		this.computers = computers;
	}

	/**
	 * The serialised form of a computer in the current session
	 */
	public static final class ComputerState {
		/**
		 * The ID for this computer
		 */
		public int id;

		/**
		 * The (optional) label of this computer
		 */
		@Nullable
		public String label;

		public ComputerState() {}

		public ComputerState(int id, @Nullable String label) {
			this.id = id;
			this.label = label;
		}
	}

	public void save(Path destination) {
		log.info("Saving session to " + destination);
		try (BufferedWriter writer = Files.newBufferedWriter(destination, StandardCharsets.UTF_8)) {
			gson.toJson(this, writer);
		} catch (IOException e) {
			log.error("Cannot save session state", e);
		}
	}

	@Nullable
	public static SessionState load(Path destination) {
		if (!Files.isRegularFile(destination)) return null;

		try (BufferedReader reader = Files.newBufferedReader(destination, StandardCharsets.UTF_8)) {
			return gson.fromJson(reader, SessionState.class);
		} catch (IOException e) {
			log.error("Cannot load session state", e);
			return null;
		}
	}
}
