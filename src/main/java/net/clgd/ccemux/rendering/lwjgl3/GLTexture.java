package net.clgd.ccemux.rendering.lwjgl3;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import static org.lwjgl.opengl.GL11.*;

public class GLTexture implements Closeable {
	private int handle;

	public GLTexture(String path) {
		this(GLTexture.class.getResourceAsStream(path));
	}

	public GLTexture(InputStream stream) {
		handle = glGenTextures();
	}

	@Override
	public void close() throws IOException {
		glDeleteTextures(handle);
	}
}
