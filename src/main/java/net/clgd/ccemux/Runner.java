package net.clgd.ccemux;

import java.awt.SplashScreen;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.squiddev.cctweaks.lua.lib.ApiRegister;

import net.clgd.ccemux.emulation.CCEmuX;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.rendering.Renderer;
import net.clgd.ccemux.rendering.RenderingMethod;

public class Runner {
	public static void launch(Logger logger, Config config, List<Path> saveDirs, int count) {
		CCEmuX emu = new CCEmuX(logger, config);
		HashMap<EmulatedComputer, List<Renderer>> computers = new HashMap<>();

		for (int i = 0; i < count; i++) {
			EmulatedComputer ec = emu.createEmulatedComputer(-1, saveDirs.size() > 0 ? saveDirs.remove(0) : null);
			computers.put(ec, emu.conf.getRenderer().stream().map(r -> RenderingMethod.create(r, emu, ec))
					.collect(Collectors.toList()));
		}
		
		if (SplashScreen.getSplashScreen() != null) SplashScreen.getSplashScreen().close();
		
		computers.forEach((ec, r) -> r.forEach(r2 -> r2.setVisible(true)));
		
		emu.run();
	}

	public static void launchCCTweaks(Logger logger, Config config, List<Path> saveDirs, int count) {
		config.addListener(new Runnable() {
			@Override
			public void run() {
				config.entrySet().stream().map(e -> new SimpleEntry<>((String) e.getKey(), (String) e.getValue()))
						.filter(e -> e.getKey().startsWith("cctweaks"))
						.forEach(e -> System.setProperty(e.getKey(), e.getValue()));
			}
		});

		org.squiddev.patcher.Logger.instance = new org.squiddev.patcher.Logger() {
			@Override
			public void doDebug(String message) {
				logger.debug(message);
			}

			@Override
			public void doWarn(String message) {
				logger.warn(message);
			}

			@Override
			public void doError(String message, Throwable t) {
				logger.error(message, t);
			}
		};

		ApiRegister.init();

		launch(logger, config, saveDirs, count);
	}
}
