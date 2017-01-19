package net.clgd.ccemux.config;

import java.util.ArrayList;

import net.clgd.ccemux.config.ConfigWriter.ConfigEntry;

public class ConfigWriter extends ArrayList<ConfigEntry> {
	private static final long serialVersionUID = -1094423794774837106L;

	public static class ConfigEntry {
		public final String comment;
		public final String key;
		public final String value;

		public ConfigEntry(String comment, String key, String value) {
			this.comment = comment;
			this.key = key;
			this.value = value;
		}

		public ConfigEntry(String key, String value) {
			this("", key, value);
		}

		@Override
		public String toString() {
			String output = "";

			if (comment != null && !comment.isEmpty()) {
				output = output + "# " + comment.replace("\n", "\n# ") + "\n";
			}

			if (key != null && value != null && !(key.isEmpty() || value.isEmpty())) {
				output = output + key.replace("=", "\\=").replace("\n", "\\n") + " = " + value.replace("\n", "\\n");
			}

			return output;
		}
	}
}
