package net.clgd.ccemux.rendering.gdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowConfiguration;
import com.badlogic.gdx.math.Vector2;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.rendering.Renderer;

public class GDXRenderer extends ApplicationAdapter {
	private static GDXRenderer renderer;

	private GDXRenderer(GDXWindow window) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

		Vector2 windowSize = window.getWindowSize();
		config.setWindowSizeLimits((int) windowSize.x, (int) windowSize.y, (int) windowSize.x, (int) windowSize.y);

		new Lwjgl3Application(window, config);
	}

	public static Renderer newInstance(EmulatedComputer computer) {
		GDXWindow window = new GDXWindow(computer);

		if (renderer == null) {
			new Thread(() -> {
				renderer = new GDXRenderer(window);
			}).start();
		} else {
			new Thread(() -> {
				// fucking fix this shit

				Lwjgl3Application app = (Lwjgl3Application) Gdx.app;
				Lwjgl3WindowConfiguration config = new Lwjgl3WindowConfiguration();

				Vector2 windowSize = window.getWindowSize();
				config.setWindowSizeLimits(
					(int) windowSize.x,
				   	(int) windowSize.y,
				   	(int) windowSize.x,
				    (int) windowSize.y
				);

				Lwjgl3Window lwjgl3Window = app.newWindow(window, config);
				window.setWindow(lwjgl3Window);
				lwjgl3Window.setSizeLimits(
					(int) windowSize.x,
					(int) windowSize.y,
					(int) windowSize.x,
					(int) windowSize.y
				);
			}).start();
		}

		return window;
	}
}
