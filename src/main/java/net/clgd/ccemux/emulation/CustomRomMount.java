package net.clgd.ccemux.emulation;

import static dan200.computercraft.core.filesystem.FileSystem.getDirectory;
import static dan200.computercraft.core.filesystem.FileSystem.getName;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;

import dan200.computercraft.api.filesystem.IMount;

public class CustomRomMount implements IMount {

	private static String trimSlash(String path) {
		return path.replaceAll("/$", "");
	}
	
	private final Map<String, Map<String, byte[]>> folders;
	
	public CustomRomMount(ZipInputStream is) {
		folders = new HashMap<>();
		
		folders.put("", new HashMap<>()); // root folder
		
		try {
			ZipEntry entry;
			while ((entry = is.getNextEntry()) != null) {
				if (entry.isDirectory()) {
					folders.put(trimSlash(entry.getName()), new HashMap<>());
				} else {
					String n = trimSlash(entry.getName());
					byte[] data = new byte[(int)entry.getSize()];
					IOUtils.read(is, data);
					folders.get(getDirectory(n)).put(getName(n), data);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean exists(String path) {
		return folders.containsKey(path)
				|| folders.containsKey(getDirectory(path)) && folders.get(getDirectory(path)).containsKey(getName(path));
	}
	
	@Override
	public long getSize(String path) {
		if (folders.containsKey(getDirectory(path)) && folders.get(getDirectory(path)).containsKey(getName(path))) {
			return folders.get(getDirectory(path)).get(getName(path)).length;
		} else {
			return 0;
		}
	}
	
	@Override
	public boolean isDirectory(String path) {
		return folders.containsKey(path);
	}
	
	@Override
	public void list(String path, List<String> contents) {
		if (folders.containsKey(path)) {
			folders.get(path).keySet().forEach(f -> {
				if (!contents.contains(f)) contents.add(f);
			});
		}
	}
	
	@Override
	public InputStream openForRead(String path) {
		if (folders.containsKey(getDirectory(path))
				&& folders.get(getDirectory(path)).containsKey(getName(path))) {
			return new ByteArrayInputStream(folders.get(getDirectory(path)).get(getName(path)));
		} else {
			return null;
		}
	}
}
