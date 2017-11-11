package net.clgd.ccemux.plugins.builtin;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import com.google.auto.service.AutoService;

import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.emulation.EmuConfig;
import net.clgd.ccemux.plugins.Plugin;
import net.clgd.ccemux.rendering.TerminalFonts;

@Slf4j
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
	public void setup(EmuConfig cfg) {
		try {
			TerminalFonts.registerFont(HDFontPlugin.class.getResource("/img/hdfont.png"));
		} catch (IOException e) {
			log.error("Failed to load HD font!", e);
		}
	}
}
