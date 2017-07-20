package net.clgd.ccemux.init;

import static org.apache.commons.cli.Option.builder;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.squiddev.cctweaks.lua.launch.RewritingLoader;

import dan200.computercraft.ComputerCraft;
import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.OperatingSystem;
import net.clgd.ccemux.emulation.CCEmuX;
import net.clgd.ccemux.plugins.PluginManager;
import net.clgd.ccemux.rendering.RendererFactory;
import net.clgd.ccemux.rendering.TerminalFont;

@Slf4j
public class Launcher {
	private static final Options opts = new Options();

	// initialize cli options
	static {
		opts.addOption(builder("h").longOpt("help").desc("Shows this help information").build());

		opts.addOption(builder("d").longOpt("data-dir")
				.desc("Sets the data directory where plugins, configs, and other data are stored.").hasArg()
				.argName("path").build());

		opts.addOption(builder("r").longOpt("renderer")
				.desc("Sets the renderer to use. Run without a value to list all available renderers.").hasArg()
				.optionalArg(true).argName("renderer").build());

		opts.addOption(builder().longOpt("cc")
				.desc("Sepcifies a custom CC jar that will be used in place of the one specified by the config file.")
				.hasArg().argName("file").build());

		opts.addOption(builder().longOpt("plugin")
				.desc("Used to load additional plugins not present in the default plugin directory. Value should be a path to a .jar file.")
				.hasArg().argName("file").build());
	}

	private static void printHelp() {
		new HelpFormatter().printHelp("ccemux [args]", opts);
	}

	public static void main(String args[]) {
		System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36");

		try (final RewritingLoader loader = new RewritingLoader(
				((URLClassLoader) Launcher.class.getClassLoader()).getURLs())) {
			@SuppressWarnings("unchecked")
			final Class<Launcher> klass = (Class<Launcher>) loader.findClass(Launcher.class.getName());

			final Constructor<Launcher> constructor = klass.getDeclaredConstructor(String[].class);
			constructor.setAccessible(true);

			final Method launch = klass.getDeclaredMethod("launch");
			launch.setAccessible(true);
			launch.invoke(constructor.newInstance(new Object[] { args }));
		} catch (Exception e) {
			log.warn("Failed to setup rewriting classloader - some features may be unavailable", e);

			new Launcher(args).launch();
		}

		System.exit(0);
	}

	private final CommandLine cli;
	private final Path dataDir;

	private Launcher(String args[]) {
		// parse cli options
		CommandLine _cli = null;
		try {
			_cli = new DefaultParser().parse(opts, args);
		} catch (org.apache.commons.cli.ParseException e) {
			System.err.println(e.getLocalizedMessage());
			printHelp();
			System.exit(1);
		}

		cli = _cli;

		if (cli.hasOption('h')) {
			printHelp();
			System.exit(0);
		}

		log.info("Starting CCEmuX");
		log.debug("ClassLoader in use: {}", this.getClass().getClassLoader().getClass().getName());

		// set data dir
		if (cli.hasOption('d')) {
			dataDir = Paths.get(cli.getOptionValue('d'));
		} else {
			dataDir = OperatingSystem.get().getAppDataDir().resolve("ccemux");
		}
		log.info("Data directory is {}", dataDir.toString());
	}

	private void crashMessage(Throwable e) {
		CrashReport report = new CrashReport(e);
		log.error("Unexpected exception occurred!", e);
		log.error("CCEmuX has crashed!");

		if (!GraphicsEnvironment.isHeadless()) {
			JTextArea textArea = new JTextArea(12, 60);
			textArea.setEditable(false);
			textArea.setText(report.toString());

			JScrollPane scrollPane = new JScrollPane(textArea);
			scrollPane.setMaximumSize(new Dimension(600, 400));

			int result = JOptionPane.showConfirmDialog(null,
					new Object[] { "CCEmuX has crashed!", scrollPane,
							"Would you like to create a bug report on GitHub?" },
					"CCEmuX Crash", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);

			if (result == JOptionPane.YES_OPTION) {
				try {
					report.createIssue();
				} catch (URISyntaxException | IOException e1) {
					log.error("Failed to open GitHub to create issue", e1);
				}
			}
		}
	}

	private void setSystemLAF() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			log.warn("Failed to set system look and feel", e);
		}
	}

	private Config loadConfig() {
		log.debug("Loading config data");

		Config cfg = Config.loadConfig(dataDir);

		for (Field f : Config.class.getDeclaredFields()) {
			try {
				f.setAccessible(true);
				if (!(Modifier.isTransient(f.getModifiers()) || Modifier.isStatic(f.getModifiers())))
					log.trace(" {} -> {}", f.getName(), f.get(cfg));
			} catch (Exception e) {}
		}

		log.info("Config loaded");

		return cfg;
	}

	private PluginManager loadPlugins(Config cfg) throws ReflectiveOperationException {
		if (!(getClass().getClassLoader() instanceof URLClassLoader)) {
			throw new RuntimeException("Classloader in use is not a URLClassLoader");
		}

		URLClassLoader loader = (URLClassLoader) getClass().getClassLoader();

		File pd = dataDir.resolve("plugins").toFile();

		if (pd.isFile()) pd.delete();
		if (!pd.exists()) pd.mkdirs();

		HashSet<URL> urls = new HashSet<>();

		for (File f : pd.listFiles()) {
			if (f.isFile()) {
				log.debug("Adding plugin source '{}'", f.getName());
				try {
					urls.add(f.toURI().toURL());
				} catch (MalformedURLException e) {
					log.warn("Failed to add plugin source", e);
				}
			}
		}

		if (cli.hasOption("plugin")) {
			for (String s : cli.getOptionValues("plugin")) {
				File f = Paths.get(s).toFile();
				log.debug("Adding external plugin source '{}'", f.getName());
				try {
					urls.add(f.toURI().toURL());
				} catch (MalformedURLException e) {
					log.warn("Failed to add plugin source '{}'", f.getName());
				}
			}
		}

		Method m = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		m.setAccessible(true);

		for (URL u : urls) {
			m.invoke(loader, u);
		}

		return new PluginManager(loader, cfg);
	}

	private Optional<File> loadCC(Config cfg) throws MalformedURLException, ReflectiveOperationException {
		File jar;

		if (cli.hasOption("cc")) {
			jar = Paths.get(cli.getOptionValue("cc")).toFile();
		} else {
			jar = dataDir.resolve(cfg.getCCLocal()).toFile();

			if (!jar.exists()) {
				try {
					CCLoader.download(cfg.getCCRemote(), jar);
				} catch (IOException e) {
					log.error("Failed to download CC jar", e);
					if (!GraphicsEnvironment.isHeadless()) {
						JOptionPane.showMessageDialog(null,
								new JLabel("<html>CCEmuX failed to automatically download the ComputerCraft jar.<br />"
										+ "Please check your internet connection, or manually download the jar to the path below.<br />"
										+ "<pre>" + cfg.getCCLocal().toAbsolutePath().toString() + "</pre><br />"
										+ "If issues persist, please open a bug report.</html>"),
								"Failed to download CC jar", JOptionPane.ERROR_MESSAGE);
						return Optional.empty();
					}
				}
			}
		}

		CCLoader.load(jar);

		log.info("Loaded CC version {}", ComputerCraft.getVersion());
		if (!ComputerCraft.getVersion().equals(cfg.getCCRevision())) {
			log.warn("The expected CC version ({}) does not match the actual CC version ({}) - problems may occur!",
					cfg.getCCRevision(), ComputerCraft.getVersion());
		}

		return Optional.of(jar);
	}

	private void launch() {
		try {
			setSystemLAF();

			File dd = dataDir.toFile();
			if (dd.isFile()) dd.delete();
			if (!dd.exists()) dd.mkdirs();

			Config cfg = loadConfig();

			if (cfg.getTermScale() != (int) cfg.getTermScale())
				log.warn("Terminal scale is not an integer - stuff might look bad! Don't blame us!");

			PluginManager pluginMgr = loadPlugins(cfg);
			pluginMgr.loadConfigs();
			pluginMgr.loaderSetup(getClass().getClassLoader());

			if (getClass().getClassLoader() instanceof RewritingLoader) {
				((RewritingLoader) getClass().getClassLoader()).chain.finalise();
				log.warn("ClassLoader chain finalized");
			}

			File ccJar = loadCC(cfg).orElseThrow(FileNotFoundException::new);

			pluginMgr.setup();

			if (cli.hasOption('r') && cli.getOptionValue('r') == null) {
				log.info("Available rendering methods:");
				RendererFactory.implementations.keySet().stream().forEach(k -> log.info(" {}", k));
			}

			if (!RendererFactory.implementations.containsKey(cfg.getRenderer())) {
				log.error("Specified renderer '{}' does not exist - are you missing a plugin?", cfg.getRenderer());

				if (!GraphicsEnvironment.isHeadless()) {
					JOptionPane.showMessageDialog(null,
							"Specified renderer '" + cfg.getRenderer() + "' does not exist.\n"
									+ "Please double check your config file and plugin list.",
							"Configuration Error", JOptionPane.ERROR_MESSAGE);
				}
			}

			pluginMgr.onInitializationCompleted();

			TerminalFont.load();

			log.info("Setting up emulation environment");

			CCEmuX emu = new CCEmuX(cfg, pluginMgr, ccJar);
			emu.addComputer();
			emu.run();

			pluginMgr.onClosing(emu);
			log.info("Emulation complete, goodbye!");
		} catch (Throwable e) {
			crashMessage(e);
		}
	}
}
