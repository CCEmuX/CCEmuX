package net.ceriat.clgd.ccemux;

import de.matthiasmann.twl.utils.PNGDecoder;
import org.lwjgl.BufferUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Texture implements Closeable {
    private int handle = 0;
    private int width, height;

    /**
     * Creates and loads a texture from a file.
     * @param filename The path to the texture in the classpath.
     */
    public Texture(String filename) {
        this(Texture.class.getResourceAsStream(filename));
    }

    public Texture(InputStream stream) {
        try {
            handle = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, handle);
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            PNGDecoder decoder = new PNGDecoder(stream);
            int width = decoder.getWidth();
            int height = decoder.getHeight();

            final int channels = 4; // RGBA

            ByteBuffer bbuf = BufferUtils.createByteBuffer(width * height * channels);
            decoder.decode(bbuf, width * channels, PNGDecoder.Format.RGBA);
            bbuf.flip();

            upload(bbuf, width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Texture(byte[] pixels, int width, int height) {
        handle = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, handle);

        upload(pixels, width, height);
    }

    private void upload(byte[] pixels, int width, int height) {
        ByteBuffer bbuf = BufferUtils.createByteBuffer(pixels.length);
        bbuf.put(pixels);
        bbuf.flip();

        upload(bbuf, width, height);
    }

    private void upload(ByteBuffer pixels, int width, int height) {
        // upload the texture
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        this.width = width;
        this.height = height;
    }

    /**
     * @return The OpenGL handle of the texture.
     */
    public int getTextureHandle() {
        return handle;
    }

    /**
     * @return The width of the texture.
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return The height of the texture.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Calls glDeleteTexture on the texture handle.
     * The object may no longer be used after calling this. Behaviour will be undefined.
     */
    public void close() {
        glDeleteTextures(handle);
    }
}
