package net.clgd.ccemux.rendering.gdx;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.plugins.builtin.GDXPlugin;

@Getter
@Slf4j
public class GDXInputProcessor implements InputProcessor {
	private final GDXPlugin plugin;
	private final GDXAdapter adapter;
	
	GDXInputProcessor(GDXAdapter adapter) {
		this.plugin = adapter.getPlugin();
		this.adapter = adapter;
	}
	
	@Override
	public boolean keyDown(int keycode) {
		log.debug("[KEY DOWN] {} ({})", keycode, Input.Keys.toString(keycode));
		return false;
	}
	
	@Override
	public boolean keyUp(int keycode) {
		log.debug("[KEY UP] {} ({})", keycode, Input.Keys.toString(keycode));
		return false;
	}
	
	@Override
	public boolean keyTyped(char character) {
		log.debug("[KEY TYPED] {}", character);
		return false;
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}
	
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}
	
	@Override
	public boolean scrolled(int amount) {
		return false;
	}
}
