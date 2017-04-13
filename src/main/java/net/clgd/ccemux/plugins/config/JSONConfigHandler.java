package net.clgd.ccemux.plugins.config;

import com.google.gson.Gson;

public abstract class JSONConfigHandler<T> implements PluginConfigHandler<T> {
	protected final Gson gson;
	protected final T defaults;

	public JSONConfigHandler(T defaults, Gson gson) {
		this.gson = gson;
		this.defaults = defaults;
	}

	public JSONConfigHandler(T defaults) {
		this(defaults, new Gson());
	}

	@Override
	public String getConfigFileExtension() {
		return "json";
	}

	@Override
	public T getDefaultConfig() {
		return defaults;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T loadConfig(String data) {
		return (T) gson.fromJson(data, defaults.getClass());
	}

	@Override
	public String writeConfig(T config) {
		return gson.toJson(config);
	}
}
