package net.clgd.ccemux.init;

import java.awt.Desktop;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Optional;

/**
 * Used to collect system information when an uncaught exception causes CCEmuX to crash
 * @author apemanzilla
 *
 */
public class CrashReport {
	public final String exceptionClass;
	public final String trace;

	public final String osName;
	public final String osVersion;
	public final String osArch;

	public final String jreVendor;
	public final String jreVersion;
	public final String jreArch;

	public CrashReport(Throwable t) {
		exceptionClass = t.getClass().getName();
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		trace = sw.toString();

		osName = Optional.of(System.getProperty("os.name")).orElse("unknown");
		osVersion = Optional.of(System.getProperty("os.version")).orElse("unknown");
		osArch = Optional.of(System.getProperty("os.arch")).orElse("unknown");

		jreVendor = Optional.of(System.getProperty("java.vendor")).orElse("unknown");
		jreVersion = Optional.of(System.getProperty("java.version")).orElse("unknown");
		jreArch = Optional.of(System.getProperty("sun.arch.data.model")).orElse("unknown");
	}

	@Override
	public String toString() {
		return "OS name: " + osName + "\n" + "OS version: " + osVersion + "\n" + "OS architecture: " + osArch + "\n\n"
				+ "JRE vendor: " + jreVendor + "\n" + "JRE version: " + jreVersion + "\n" + "JRE architecture: "
				+ jreArch + "\n\n" + "Stack trace: " + trace;
	}

	public void createIssue() throws URISyntaxException, IOException {
		Desktop.getDesktop()
				.browse(new URI(String.format("https://github.com/Lignumm/CCEmuX/issues/new?title=%s&body=%s",
						URLEncoder.encode("Unexpected " + exceptionClass, "UTF-8"),
						URLEncoder.encode("```\n" + toString() + "```", "UTF-8"))));
	}
}
