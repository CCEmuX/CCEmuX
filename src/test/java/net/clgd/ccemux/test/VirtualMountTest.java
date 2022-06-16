package net.clgd.ccemux.test;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import net.clgd.ccemux.api.emulation.filesystem.VirtualDirectory;
import net.clgd.ccemux.api.emulation.filesystem.VirtualFile;
import net.clgd.ccemux.api.emulation.filesystem.VirtualMount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.nio.file.Paths.get;
import static org.junit.jupiter.api.Assertions.*;

public class VirtualMountTest {
	public VirtualMount rom;

	@BeforeEach
	public void setUp() {
		VirtualDirectory.Builder builder = new VirtualDirectory.Builder();

		builder.addEntry("file", new VirtualFile("hello"));
		builder.addEntry("folder/file2", new VirtualFile("hello2"));
		builder.addEntry("folder/folder2", new VirtualDirectory());

		rom = new VirtualMount(builder.build());
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testFollowRoot() {
		assertEquals(rom.getRoot(), rom.follow(""));

		assertEquals(rom.getRoot(), rom.follow(get("/")));
		assertEquals(rom.getRoot(), rom.follow(get("")));

		assertEquals(rom.getRoot(), rom.follow(get("/foo/..")));
		assertEquals(rom.getRoot(), rom.follow(get("abc/..")));
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testFollowEntries() {
		assertEquals(rom.getRoot().getEntry("folder"), rom.follow(get("/folder")));
		assertEquals(rom.getRoot().getEntry("folder"), rom.follow("folder"));

		assertEquals(((VirtualDirectory) rom.getRoot().getEntry("folder")).getEntry("folder2"),
			rom.follow(get("/folder/folder2")));
		assertEquals(((VirtualDirectory) rom.getRoot().getEntry("folder")).getEntry("folder2"),
			rom.follow("folder/folder2"));

		assertEquals(((VirtualDirectory) rom.getRoot().getEntry("folder")).getEntry("folder2"),
			rom.follow(get("/folder/../folder/folder2")));

		assertEquals(rom.getRoot().getEntry("file"), rom.follow("file"));
		assertEquals(((VirtualDirectory) rom.getRoot().getEntry("folder")).getEntry("file2"),
			rom.follow(get("/folder/folder2/../file2")));
	}

	@Test
	public void testFollowInvalidPath() {
		assertNull(rom.follow("nonexistent"));
		assertNull(rom.follow("file/child"));
	}

	@Test
	public void testExists() {
		assertTrue(rom.exists("file"));
		assertTrue(rom.exists("folder/file2"));

		assertFalse(rom.exists("nonexistent"));
		assertFalse(rom.exists("file/child"));
	}

	@Test
	public void testSize() throws IOException {
		assertEquals(5, rom.getSize("file"));
		assertEquals(6, rom.getSize("folder/file2"));

		assertEquals(0, rom.getSize("folder"));
	}

	@Test
	public void testDirectory() {
		assertTrue(rom.isDirectory("folder"));
		assertTrue(rom.isDirectory("folder/folder2"));
		assertFalse(rom.isDirectory("file"));
		assertFalse(rom.isDirectory("folder/nonexistent"));
	}

	@Test
	public void testList() throws IOException {
		List<String> names = new ArrayList<>();

		rom.list("folder", names);
		assertTrue(names.containsAll(ImmutableList.of("folder2", "file2")));
	}

	@Test
	public void testRead() throws IOException {
		assertEquals("hello", CharStreams.toString(Channels.newReader(rom.openForRead("file"), StandardCharsets.UTF_8.name())));
		assertEquals("hello2", CharStreams.toString(Channels.newReader(rom.openForRead("folder/file2"), StandardCharsets.UTF_8.name())));
	}
}
