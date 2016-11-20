package net.clgd.ccemux;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

/**
 * A simple utility class to facilitate downloading, verifying, and loading the
 * CC jar dynamically. It is recommended that you set {@link #logger} to a
 * {@link org.slf4j.Logger Logger} instance to be used in the event that an
 * exception is caught.
 * 
 * @author apemanzilla
 *
 */
public class CCBootstrapper {
	public final File ccJar;
	public Logger logger;

	public CCBootstrapper(File ccJar) {
		this.ccJar = ccJar;
	}

	public boolean exists() {
		return ccJar.exists();
	}

	public boolean download(URL url) {
		try {
			FileUtils.copyURLToFile(url, ccJar);
			return true;
		} catch (IOException e) {
			if (logger != null)
				logger.error("Failed to download CC", e);
			return false;
		}
	}

	public boolean validate(String md5sum) {
		try (FileInputStream fis = new FileInputStream(ccJar)) {
			return DigestUtils.md5Hex(fis).equals(md5sum);
		} catch (IOException e) {
			if (logger != null)
				logger.error("Failed to validate CC", e);
			return false;
		}
	}

	public boolean load() {
		try {
			URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			Method m = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			m.setAccessible(true);
			m.invoke(loader, ccJar.toURI().toURL());
			return true;
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | MalformedURLException e) {
			if (logger != null)
				logger.error("Failed to load CC", e);
			return false;
		}
	}
}
