package net.clgd.ccemux.rendering.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import dan200.computercraft.core.terminal.Terminal;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.Utils;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.plugins.builtin.GDXPlugin;

import java.awt.event.KeyEvent;

@Getter
@Slf4j
public class GDXInputProcessor implements InputProcessor {
	private final GDXPlugin plugin;
	private final GDXAdapter adapter;
	private final EmulatedComputer computer;
	private final Terminal terminal;
	
	private int lastKey, repeatedCounter, dragButton;
	private Vector2 lastDragSpot = new Vector2();
	
	GDXInputProcessor(GDXAdapter adapter) {
		this.plugin = adapter.getPlugin();
		this.adapter = adapter;
		this.computer = adapter.getComputer();
		this.terminal = adapter.getTerminal();
	}
	
	@Override
	public boolean keyDown(int keycode) {
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
			lastKey = keycode;
			computer.pressKey(KeyTranslator.translateToCC(keycode), false);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean keyUp(int keycode) {
		if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
			if (keycode == Input.Keys.S) adapter.setShutdownTimer(-1);
			if (keycode == Input.Keys.R) adapter.setRebootTimer(-1);
			if (keycode == Input.Keys.T) adapter.setTerminateTimer(-1);
		}
		
		lastKey = -1;
		repeatedCounter = 0;
		computer.releaseKey(KeyTranslator.translateToCC(keycode));
		
		return true;
	}
	
	@Override
	public boolean keyTyped(char c) {
		if (!adapter.allowKeyEvents()) return false;
		
		if (Utils.isPrintableChar(c)) {
			computer.pressChar(c);
			adapter.setBlinkLockedTime(0.25);
			
			return true;
		} else if (lastKey >= 0 && repeatedCounter++ > 0) {
			computer.pressKey(KeyTranslator.translateToCC(lastKey), true);
			computer.pressKey(KeyTranslator.translateToCC(lastKey), false);
		}
		
		return false;
	}
	
	private Vector2 mapPointToCC(Vector2 point) {
		int px = (int) (point.x - adapter.getMargin());
		int py = (int) (point.y - adapter.getMargin());
		
		int x = px / adapter.getPixelWidth();
		int y = py / adapter.getPixelHeight();
		
		return point.set(x + 1, y + 1);
	}
	
	private void fireMouseEvent(int screenX, int screenY, int button, boolean press) {
		Vector2 point = Pools.get(Vector2.class).obtain();
		mapPointToCC(point.set(screenX, screenY));
		computer.click(MouseTranslator.gdxToCC(button), (int) point.x, (int) point.y, !press);
		Pools.free(point);
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		fireMouseEvent(screenX, screenY, button, true);
		dragButton = MouseTranslator.gdxToCC(button);
		return true;
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		fireMouseEvent(screenX, screenY, button, false);
		return true;
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		Vector2 point = Pools.get(Vector2.class).obtain();
		mapPointToCC(point.set(screenX, screenY));
		
		if (point.equals(lastDragSpot)) return false;
		
		computer.drag(dragButton, (int) point.x, (int) point.y);
		lastDragSpot.set(point);
		
		Pools.free(point);
		return true;
	}
	
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}
	
	@Override
	public boolean scrolled(int amount) {
		int dir = amount > 0 ? 1 : -1;
		
		Vector2 point = Pools.get(Vector2.class).obtain();
		mapPointToCC(point.set(Gdx.input.getX(), Gdx.input.getY()));
		computer.scroll(dir, (int) point.x, (int) point.y);
		Pools.free(point);
		
		return true;
	}
}
