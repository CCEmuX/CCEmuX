package net.clgd.ccemux.rendering.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import dan200.computercraft.core.terminal.Terminal;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.plugins.builtin.GDXPlugin;

import java.awt.event.KeyEvent;

@Getter
@Slf4j
public class GDXInputProcessor implements InputProcessor {
	private static boolean isPrintableChar(char c) {
		Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
		return !Character.isISOControl(c) && c != KeyEvent.CHAR_UNDEFINED && block != null
			&& block != Character.UnicodeBlock.SPECIALS;
	}
	
	private final GDXPlugin plugin;
	private final GDXAdapter adapter;
	private final EmulatedComputer computer;
	private final Terminal terminal;
	
	GDXInputProcessor(GDXAdapter adapter) {
		this.plugin = adapter.getPlugin();
		this.adapter = adapter;
		this.computer = adapter.getComputer();
		this.terminal = adapter.getTerminal();
	}
	
	@Override
	public boolean keyDown(int keycode) {
		log.debug("[KEY DOWN] {} ({})", keycode, Input.Keys.toString(keycode));
		
		if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
			if (keycode == Input.Keys.S && adapter.getShutdownTimer() < 0) adapter.setShutdownTimer(0);
			if (keycode == Input.Keys.R && adapter.getRebootTimer() < 0) adapter.setRebootTimer(0);
			if (keycode == Input.Keys.T && adapter.getTerminateTimer() < 0) adapter.setTerminateTimer(0);
			
			if (keycode == Input.Keys.V) {
				String contents = Gdx.app.getClipboard().getContents();
				computer.paste(contents);
				
				return true;
			}
		}
		
		if (adapter.allowKeyEvents()) {
			computer.pressKey(KeyTranslator.translateToCC(keycode), false);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean keyUp(int keycode) {
		log.debug("[KEY UP] {} ({})", keycode, Input.Keys.toString(keycode));
		
		if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
			if (keycode == Input.Keys.S) adapter.setShutdownTimer(-1);
			if (keycode == Input.Keys.R) adapter.setRebootTimer(-1);
			if (keycode == Input.Keys.T) adapter.setTerminateTimer(-1);
		}
		
		computer.pressKey(KeyTranslator.translateToCC(keycode), true);
		
		return true;
	}
	
	@Override
	public boolean keyTyped(char c) {
		log.debug("[KEY TYPED] {}", c);
		
		if (isPrintableChar(c) && adapter.allowKeyEvents()) {
			computer.pressChar(c);
			adapter.setBlinkLockedTime(0.25);
		}
		
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
