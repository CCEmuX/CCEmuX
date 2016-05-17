package net.ceriat.clgd.ccemux;

import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.Closeable;
import java.io.IOException;
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


        try {
            BufferedImage img = ImageIO.read(getClass().getResource(filename));

            handle = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, handle);

            int width = img.getWidth();
            int height = img.getHeight();

            // retrieve pixel data from bufferedimage
            byte[] pixels = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();
            upload(pixels, width, height);
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
        // put the pixels into a byte buffer so that it's directly allocated
        ByteBuffer bbuf = BufferUtils.createByteBuffer(pixels.length);
        bbuf.put(pixels);
        bbuf.flip();

        // upload the texture
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, bbuf);

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
