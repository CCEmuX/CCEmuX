package net.clgd.ccemux.init;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import dan200.computercraft.ComputerCraft;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CCLoader {
	private static final Logger log = LoggerFactory.getLogger(CCLoader.class);

	private CCLoader() {
	}

	public static boolean isLoaded() {
		try {
			Class.forName("dan200.computercraft.ComputerCraft", false, CCLoader.class.getClassLoader());
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public static void download(URL remote, File jar) throws IOException {
		log.info("Downloading CC jar from {} to {}", remote, jar);

		if (jar.exists())
			jar.delete();

		FileUtils.copyURLToFile(remote, jar, 5000, 5000);

		log.info("Downloaded CC successfully");
	}

	public static void load(File jar) throws ReflectiveOperationException, MalformedURLException {
		if (!jar.isFile())
			throw new RuntimeException("Given jar is not a file or does not exist and cannot be added to classloader");

		log.info("Adding CC jar to classloader");

		Method m = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		m.setAccessible(true);
		m.invoke(CCLoader.class.getClassLoader(), jar.toURI().toURL());
		ComputerCraft.logPeripheralErrors = true;
		ComputerCraft.log = LogManager.getLogger("ComputerCraft");
	}
}
