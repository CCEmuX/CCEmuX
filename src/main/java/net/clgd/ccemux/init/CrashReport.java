package net.clgd.ccemux.init;

import static java.lang.System.getProperty;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.common.base.MoreObjects;

import lombok.Data;
import lombok.val;
import net.clgd.ccemux.emulation.CCEmuX;

/**
 * Used to collect system information when an uncaught exception causes CCEmuX
 * to crash
 * 
 * @author apemanzilla
 *
 */
@Data
public class CrashReport {
	private final Throwable throwable;

	public static Map<String, String> collectInfo() {
		val info = new LinkedHashMap<String, String>();

		try {
			info.put("CCEmuX version", MoreObjects.firstNonNull(CCEmuX.getVersion(), "unknown"));
		} catch (Throwable t) {
			info.put("CCEmuX version", t.toString());
		}

		info.put("OS name", getProperty("os.name", "unknown"));
		info.put("OS version", getProperty("os.version", "unknown"));
		info.put("OS architecture", getProperty("os.arch", "unknown"));

		info.put("JRE vendor", getProperty("java.vendor", "unknown"));
		info.put("JRE version", getProperty("java.version", "unknown"));
		info.put("JRE architecture", getProperty("sun.arch.data.model", "unknown"));

		return info;
	}

	@Override
	public String toString() {
		return collectInfo().entrySet().stream().map(e -> e.getKey() + ": " + e.getValue() + "\n").reduce("",
				(a, b) -> a + b) + "\n" + ExceptionUtils.getStackTrace(getThrowable());
	}

	public void createIssue() throws URISyntaxException, IOException {
		Desktop.getDesktop()
				.browse(new URI(String.format("https://github.com/Lignumm/CCEmuX/issues/new?title=%s&body=%s",
						URLEncoder.encode("Unexpected " + getThrowable().getClass().toString(), "UTF-8"),
						URLEncoder.encode("```\n" + toString() + "```", "UTF-8"))));
	}
}
