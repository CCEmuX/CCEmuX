package net.ceriat.clgd.ccemux;

import org.joml.Matrix4f;

import java.awt.*;
import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;

public class TerminalRenderer implements IRenderer, Closeable {
    private Graphics graphics = CCEmuX.instance.graphics;
    private int pixelInstBuffer, pixelVAO;
    private int textInstBuffer, textVAO;
    private int width, height;
    private ByteBuffer pixelMappedBuf, textMappedBuf;

    private int rectVAO;

    private Texture font;

    public TerminalRenderer(Texture font, int width, int height, float pixelWidth, float pixelHeight) {
        this.width = width;
        this.height = height;
        this.font = font;

        orphan(width, height, pixelWidth, pixelHeight);
        pixelVAO = graphics.createVertexAttribs(graphics.rectBuffer, pixelInstBuffer);
        textVAO = graphics.createVertexAttribs(graphics.rectBuffer, textInstBuffer);
    }

    public void orphan(int width, int height, float pixelWidth, float pixelHeight) {
        Instance[] pixelInstances = new Instance[width * height];
        Instance[] textInstances = new Instance[width * height];

        Random rand = new Random();

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                Matrix4f posMat = new Matrix4f().translate(
                    (float)x * pixelWidth, (float)y * pixelHeight, 0.0f
                ).scale(
                    pixelWidth, pixelHeight, 1.0f
                );

                pixelInstances[y * width + x] = new Instance(posMat, rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), 1.0f);

                Instance textInst = new Instance(posMat, rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), 1.0f);

                int[] point = asciiToPoint((char)rand.nextInt(127), 6, 9, 96, 142);
                float[] relPoint = new float[] {
                    (float)point[0] / (float)font.getWidth(),
                    (float)point[1] / (float)font.getHeight()
                };

                textInst.uScale = 6.0f / font.getWidth();
                textInst.vScale = 9.0f / font.getHeight();
                textInst.uOffset = relPoint[0];
                textInst.vOffset = relPoint[1];
                textInstances[y * width + x] = textInst;
            }
        }

        pixelInstBuffer = graphics.createInstanceBuffer(pixelInstances, GL_DYNAMIC_DRAW);
        textInstBuffer = graphics.createInstanceBuffer(textInstances, GL_DYNAMIC_DRAW);
    }

    /**
     * Prepares the renderer for pixel changes.
     */
    public void startPixelUpdate() {
        glBindBuffer(GL_ARRAY_BUFFER, pixelInstBuffer);
        pixelMappedBuf = glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY, pixelMappedBuf);
    }

    /**
     * Changes a pixel. You must call {@link TerminalRenderer#startPixelUpdate()} first.
     * @param p The coords of the pixel to change.
     * @param colour The new colour of the pixel.
     */
    public void updatePixel(Point p, Color colour) {
        int idx = Instance.SIZE_FLOATS * (p.y * width + p.x);

        FloatBuffer fbuf = pixelMappedBuf.asFloatBuffer();
        fbuf.position(idx + 4 * 4);
        fbuf.put(colour.getRed() / 255.0f);
        fbuf.put(colour.getGreen() / 255.0f);
        fbuf.put(colour.getBlue() / 255.0f);
        fbuf.put(colour.getAlpha() / 255.0f);
    }

    /**
     * Stops updating pixels and submits the new pixel data to the GPU.
     */
    public void stopPixelUpdate() {
        pixelMappedBuf.flip();
        glUnmapBuffer(GL_ARRAY_BUFFER);
    }

    /**
     * Prepares the renderer for text changes.
     */
    public void startTextUpdate() {
        glBindBuffer(GL_ARRAY_BUFFER, textInstBuffer);
        textMappedBuf = glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY, textMappedBuf);
    }

    /**
     * Changes a character. You must call {@link TerminalRenderer#startTextUpdate()} first.
     * @param p The coords of the character to change.
     * @param colour The new colour of the character.
     * @param c The new character.
     */
    public void updateText(Point p, Color colour, char c) {
        int[] point = asciiToPoint(c, 6, 9, 150, 150);
        float[] relPoint = new float[] {
            (float)point[0] / (float)font.getWidth(),
            (float)point[1] / (float)font.getHeight()
        };

        int idx = Instance.SIZE_FLOATS * (p.y * width + p.x);

        FloatBuffer fbuf = textMappedBuf.asFloatBuffer();
        fbuf.position(idx + 4 * 4);
        fbuf.put(colour.getRed() / 255.0f);
        fbuf.put(colour.getGreen() / 255.0f);
        fbuf.put(colour.getBlue() / 255.0f);
        fbuf.put(colour.getAlpha() / 255.0f);
        fbuf.put(relPoint); // offset
        fbuf.put(6.0f / font.getWidth()).put(9.0f / font.getHeight());
    }

    /**
     * Stops updating chars and submits changes to the GPU.
     */
    public void stopTextUpdate() {
        textMappedBuf.flip();
        glUnmapBuffer(GL_ARRAY_BUFFER);
    }

    /**
     * Calculates the coordinates of a character in a character atlas.
     * @param c The character to find.
     * @param charWidth The width of a single char.
     * @param charHeight The height of a single char.
     * @param fontWidth The width of the font.
     * @param fontHeight The height of the font.
     * @return An array containing the x, y coords of the character.
     */
    public static int[] asciiToPoint(char c, int charWidth, int charHeight, int fontWidth, int fontHeight) {
        int columns = fontWidth / charWidth;
        int rows = fontHeight / charHeight;

        int ascii = (int)c;

        return new int[] {
            (ascii % columns) * charWidth,
            (ascii / rows) * charHeight
        };
    }

    /**
     * Renders the entire terminal.
     */
    public void render() {
        graphics.setRenderUniforms(graphics.shaderDefault);
        glBindVertexArray(pixelVAO);
        glBindTexture(GL_TEXTURE_2D, graphics.texWhite.getTextureHandle());
        glDrawArraysInstanced(GL_TRIANGLE_STRIP, 0, 4, width * height);

        glBindVertexArray(textVAO);
        glBindTexture(GL_TEXTURE_2D, font.getTextureHandle());
        glDrawArraysInstanced(GL_TRIANGLE_STRIP, 0, 4, width * height);
    }

    public void close() {
        glDeleteBuffers(pixelInstBuffer);
        glDeleteBuffers(textInstBuffer);
    }
}
