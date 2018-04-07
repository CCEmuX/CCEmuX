package net.clgd.ccemux.plugins.builtin;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import com.google.auto.service.AutoService;

import net.clgd.ccemux.api.plugins.Plugin;
import net.clgd.ccemux.api.plugins.PluginManager;
import net.clgd.ccemux.api.rendering.TerminalFont;

@AutoService(Plugin.class)
public class HDFontPlugin extends Plugin {
	@Override
	public String getName() {
		return "HD Terminal Font";
	}

	@Override
	public String getDescription() {
		return "Replaces the standard CC font with a double resolution font created by Bomb Bloke";
	}

	@Override
	public Optional<String> getVersion() {
		return Optional.empty();
	}

	@Override
	public Collection<String> getAuthors() {
		return Collections.singleton("BombBloke");
	}

	@Override
	public Optional<String> getWebsite() {
		return Optional
				.of("http://www.computercraft.info/forums2/index.php?/topic/25429-cc-176-enlarged-terminal-font/");
	}

	@Override
	public void setup(PluginManager manager) {
		TerminalFont.registerFont(HDFontPlugin.class.getResource("/img/hdfont.png"));
	}
}
