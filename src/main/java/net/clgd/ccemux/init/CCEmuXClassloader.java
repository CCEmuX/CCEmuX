package net.clgd.ccemux.init;

import org.squiddev.cctweaks.lua.launch.DelegatingRewritingLoader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CCEmuXClassloader extends DelegatingRewritingLoader {
	public static final String CC_PREFIX = "dan200.computercraft";

	private boolean blockCC = true;

	CCEmuXClassloader(ClassLoader delegate) {
		super(delegate);
		addClassLoaderExclusion(CCEmuXClassloader.class.getName());
	}

	public void allowCC() {
		this.blockCC = false;
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		if (name.startsWith(CC_PREFIX) && blockCC) {
			log.warn("Blocked early access to CC class {}", name);
			throw new ClassNotFoundException(name);
		} else {
			return super.findClass(name);
		}
	}
}
