package net.clgd.ccemux.emulation;

import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

import com.google.common.base.Objects;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import dan200.computercraft.api.filesystem.MountConstants;
import dan200.computercraft.api.filesystem.WritableMount;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.apis.IAPIEnvironment;
import dan200.computercraft.core.apis.handles.ArrayByteChannel;
import dan200.computercraft.core.apis.transfer.TransferredFile;
import dan200.computercraft.core.apis.transfer.TransferredFiles;
import dan200.computercraft.core.computer.ComputerSide;
import dan200.computercraft.core.filesystem.FileSystem;
import dan200.computercraft.core.filesystem.FileSystemException;
import net.clgd.ccemux.Utils;
import net.clgd.ccemux.api.emulation.EmulatedComputer;
import net.clgd.ccemux.api.emulation.EmulatedTerminal;
import net.clgd.ccemux.rendering.awt.AWTTerminalFont;
import net.clgd.ccemux.rendering.awt.TerminalRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a computer that can be emulated via CCEmuX
 */
public class EmulatedComputerImpl extends EmulatedComputer {
	private static final Logger log = LoggerFactory.getLogger(EmulatedComputerImpl.class);
	private static final ListeningExecutorService SCREENSHOTS = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

	/**
	 * A class used to create new {@link EmulatedComputer} instances
	 *
	 * @author apemanzilla
	 */
	public static class BuilderImpl implements Builder {
		private final CCEmuX emu;
		private final EmulatedTerminal term;
		private double termScale;

		private Integer id = null;

		private Supplier<WritableMount> rootMount = null;

		private String label = null;

		private transient final AtomicBoolean built = new AtomicBoolean();

		private BuilderImpl(CCEmuX emu, EmulatedTerminal term) {
			this.emu = emu;
			this.term = term;
			this.termScale = emu.getConfig().termScale.get();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see net.clgd.ccemux.emulation.Builder#id(java.lang.Integer)
		 */
		@Nonnull
		@Override
		public BuilderImpl id(Integer num) {
			id = num;
			return this;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see net.clgd.ccemux.emulation.Builder#rootMount(dan200.computercraft.api.
		 * filesystem.WritableMount)
		 */
		@Nonnull
		@Override
		public Builder rootMount(Supplier<WritableMount> rootMount) {
			this.rootMount = rootMount;
			return this;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see net.clgd.ccemux.emulation.Builder#label(java.lang.String)
		 */
		@Nonnull
		@Override
		public Builder label(String label) {
			this.label = label;
			return this;
		}

		@Override
		public Builder termSize(int width, int height) {
			if (term.getWidth() != width || term.getHeight() != height) term.resize(width, height);
			return this;
		}

		@Override
		public Builder termScale(double scale) {
			this.termScale = scale;
			return this;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see net.clgd.ccemux.emulation.Builder#build()
		 */
		@Nonnull
		@Override
		public EmulatedComputer build() {
			if (built.getAndSet(true)) throw new IllegalStateException("This computer has already been built!");

			int id = Optional.ofNullable(this.id).orElse(-1);
			if (id < 0) id = emu.assignNewID();

			EmulatedComputer ec = new EmulatedComputerImpl(emu, term, id, termScale, rootMount);
			ec.setLabel(label);
			return ec;
		}
	}

	/**
	 * Gets a new builder to create an {@link EmulatedComputer} instance.
	 *
	 * @return The new builder
	 */
	public static Builder builder(CCEmuX emu, EmulatedTerminal term) {
		return new BuilderImpl(emu, term);
	}

	/**
	 * The emulator this computer belongs to
	 */
	private final CCEmuX emulator;

	private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();

	private final double termScale;

	private EmulatedComputerImpl(CCEmuX emulator, EmulatedTerminal terminal, int id, double termScale, Supplier<WritableMount> mount) {
		super(emulator.context(), new ComputerEnvironmentImpl(emulator, id, mount), terminal, id);
		this.emulator = emulator;
		this.termScale = termScale;
	}

	@Override
	public boolean addListener(@Nonnull Listener l) {
		return listeners.add(l);
	}

	@Override
	public boolean removeListener(@Nonnull Listener l) {
		return listeners.remove(l);
	}

	@Override
	public double getTermScale() {
		return termScale;
	}

	@Override
	public void tick() {
		super.tick();

		IAPIEnvironment environment = getAPIEnvironment();
		for (ComputerSide side : ComputerSide.values()) {
			IPeripheral peripheral = environment.getPeripheral(side);
			if (peripheral instanceof Listener) ((Listener) peripheral).onAdvance(0.05);
		}

		listeners.forEach(l -> l.onAdvance(0.05));
	}

	@Override
	public void transferFiles(@Nonnull Iterable<File> files) throws IOException {
		List<TransferredFile> toTransfer = new ArrayList<>();
		for (var file : files) {
			if (file.isFile()) {
				toTransfer.add(new TransferredFile(file.getName(), new ArrayByteChannel(Files.readAllBytes(file.toPath()))));
			}
		}

		if (!toTransfer.isEmpty()) {
			queueEvent(TransferredFiles.EVENT, new Object[]{new TransferredFiles(toTransfer, () -> {
			})});
		}
	}

	private void doCopyFiles(@Nonnull Iterable<File> files, @Nonnull String location) throws IOException, FileSystemException {
		FileSystem mount = this.getAPIEnvironment().getFileSystem();
		Path base = Paths.get(location);

		for (File f : files) {
			String pathName = base.resolve(f.getName()).toString();

			if (f.isFile()) {
				if (f.length() > mount.getFreeSpace("")) {
					throw new IOException("Not enough space on computer");
				}

				try (var s = mount.openForWrite(pathName, MountConstants.WRITE_OPTIONS);
					 FileChannel o = FileChannel.open(f.toPath())) {
					ByteStreams.copy(o, s.get());
				}
			} else {
				mount.makeDir(pathName);
				doCopyFiles(Arrays.asList(f.listFiles()), pathName);
			}
		}
	}

	@Override
	public void copyFiles(@Nonnull Iterable<File> files, @Nonnull String location) throws IOException {
		try {
			doCopyFiles(files, location);
		} catch (FileSystemException e) {
			throw new IOException(e);
		}
	}

	@Nonnull
	@Override
	public ListenableFuture<File> screenshot() {
		return SCREENSHOTS.submit(() -> {
			Path screenshotDir = emulator.getConfig().getDataDir().resolve("screenshots");
			Files.createDirectories(screenshotDir);

			LocalDateTime instant = LocalDateTime.now();
			File file = Utils.createUniqueFile(screenshotDir.toFile(), String.format("%04d-%02d-%02d-%02d_%02d_%02d",
				instant.get(ChronoField.YEAR), instant.get(ChronoField.MONTH_OF_YEAR), instant.get(ChronoField.DAY_OF_MONTH),
				instant.get(ChronoField.HOUR_OF_DAY), instant.get(ChronoField.MINUTE_OF_HOUR), instant.get(ChronoField.SECOND_OF_MINUTE)
			), ".png");

			AWTTerminalFont font = AWTTerminalFont.getBest(AWTTerminalFont::new);
			TerminalRenderer renderer = new TerminalRenderer(terminal, termScale);
			try {
				synchronized (terminal) {
					Dimension dimension = renderer.getSize();
					BufferedImage image = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_3BYTE_BGR);

					Graphics graphics = image.getGraphics();
					renderer.render(font, graphics);
					graphics.dispose();

					ImageIO.write(image, "png", file);
				}

				log.info("Saved screenshot to {}", file.getAbsolutePath());

				return file;
			} catch (IOException e) {
				file.delete();
				throw e;
			}
		});
	}

	@Override
	public void setLabel(String label) {
		if (!Objects.equal(label, getLabel())) {
			super.setLabel(label);
			emulator.sessionStateChanged();
		}
	}
}
