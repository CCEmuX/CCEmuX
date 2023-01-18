package net.clgd.ccemux.emulation;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import dan200.computercraft.api.filesystem.FileOperationException;
import dan200.computercraft.api.filesystem.Mount;

public class ComboMount implements Mount {
	private final Mount[] mounts;

	public ComboMount(Mount[] mounts) {this.mounts = mounts;}

	@Override
	public boolean exists(String path) throws IOException {
		for (var mount : mounts) {
			if (mount.exists(path)) return true;
		}
		return false;
	}

	@Override
	public boolean isDirectory(String path) throws IOException {
		for (var mount : mounts) {
			if (mount.isDirectory(path)) return true;
		}
		return false;
	}

	@Override
	public void list(String path, List<String> contents) throws IOException {
		List<String> foundFiles = null;
		for (var mount : mounts) {
			if (!mount.isDirectory(path)) continue;

			if (foundFiles == null) foundFiles = new ArrayList<>();
			mount.list(path, foundFiles);
		}

		if (foundFiles == null) throw new FileOperationException(path, "Not a directory");
		foundFiles.stream().distinct().forEach(contents::add);
	}

	@Override
	public long getSize(String path) throws IOException {
		for (var mount : mounts) {
			if (mount.exists(path)) return mount.getSize(path);
		}
		throw new FileOperationException(path, "No such file");
	}

	@Override
	public SeekableByteChannel openForRead(String path) throws IOException {
		for (var mount : mounts) {
			if (mount.exists(path)) return mount.openForRead(path);
		}
		throw new FileOperationException(path, "No such file");
	}

	@Override
	public BasicFileAttributes getAttributes(String path) throws IOException {
		for (var mount : mounts) {
			if (mount.exists(path)) return mount.getAttributes(path);
		}
		throw new FileOperationException(path, "No such file");
	}
}
