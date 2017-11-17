package net.clgd.ccemux.rendering.javafx;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.control.TextArea;
import javafx.scene.input.*;
import lombok.Getter;
import net.clgd.ccemux.Utils;

/**
 * Captures keyboard input and handles it for a specific {@link ComputerPane}
 * 
 * @author apemanzilla
 *
 */
public class KeyboardCapturer extends TextArea {
	/**
	 * Time (in milliseconds) that a key combo must be held before triggering
	 */
	public static final long COMBO_TIME = 500;

	@Getter
	private final ComputerPane pane;

	/**
	 * Map of currently-pressed keys to the time (in milliseconds) that they
	 * were first pressed
	 */
	private final Map<KeyCode, Long> pressedKeys = new HashMap<>();

	@Getter
	private long blinkLockTime = -1;

	public KeyboardCapturer(ComputerPane pane) {
		this.pane = pane;

		managedProperty().bind(visibleProperty());
		setFocusTraversable(false);

		setOnKeyTyped(this::keyTyped);
		setOnKeyPressed(this::keyPressed);
		setOnKeyReleased(this::keyReleased);
	}

	/**
	 * @return Whether the cursor should be shown regardless of usual pattern
	 *         (due to a recent keypress)
	 */
	public boolean isBlinkLocked() {
		return blinkLockTime != -1 && System.currentTimeMillis() - blinkLockTime < 250;
	}

	/**
	 * @return Whether one of the standard control-combos is in progress
	 */
	public boolean isComboInProgress() {
		return pressedKeys.containsKey(KeyCode.CONTROL) && (pressedKeys.containsKey(KeyCode.T)
				|| pressedKeys.containsKey(KeyCode.R) || pressedKeys.containsKey(KeyCode.S));
	}

	private void keyTyped(KeyEvent e) {
		char c = e.getCharacter().charAt(0);
		if (Utils.isPrintableChar(c)) {
			pane.getComputer().pressChar(e.getCharacter().charAt(0));
		}
		e.consume();
	}

	private void keyPressed(KeyEvent e) {
		int ccCode = JFXKeyTranslator.translateToCC(e.getCode());
		if (ccCode == 0) return;

		if (pressedKeys.containsKey(e.getCode())) {

			if (isComboInProgress()) {
				// check if combo is complete
				long m = System.currentTimeMillis();

				if (m - pressedKeys.get(KeyCode.CONTROL) >= COMBO_TIME) {
					// handle combo action
					if (m - pressedKeys.getOrDefault(KeyCode.T, m) >= COMBO_TIME) {
						pane.getComputer().terminate();
					} else if (m - pressedKeys.getOrDefault(KeyCode.R, m) >= COMBO_TIME) {
						if (pane.getComputer().isOn()) {
							pane.getComputer().reboot();
						} else {
							pane.getComputer().turnOn();
						}
					} else if (m - pressedKeys.getOrDefault(KeyCode.S, m) >= COMBO_TIME) {
						pane.getComputer().shutdown();
					}

					// prevent the combo from triggering again for a while
					pressedKeys.replace(KeyCode.CONTROL, m);
				}
			} else {
				pane.getComputer().pressKey(ccCode, true);
			}
		} else {
			pane.getComputer().pressKey(ccCode, false);
			pressedKeys.put(e.getCode(), System.currentTimeMillis());
		}

		blinkLockTime = System.currentTimeMillis();
		e.consume();
	}

	private void keyReleased(KeyEvent e) {
		int ccCode = JFXKeyTranslator.translateToCC(e.getCode());
		if (ccCode == 0) return;

		pane.getComputer().releaseKey(ccCode);
		pressedKeys.remove(e.getCode());
		e.consume();
	}

	@Override
	public void paste() {
		pane.transferContents(Clipboard.getSystemClipboard());
	}
}
