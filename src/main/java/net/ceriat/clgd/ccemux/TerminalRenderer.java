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
    private int instBuffer, vao;
    private int width, height;
    private ByteBuffer mappedBuf;

    private int rectVAO;

    private Texture font;

    public TerminalRenderer(Texture font, int width, int height, float pixelWidth, float pixelHeight) {
        this.width = width;
        this.height = height;
        this.font = font;

        orphan(width, height, pixelWidth, pixelHeight);
        vao = graphics.createVertexAttribs(graphics.rectBuffer, instBuffer);
        rectVAO = graphics.createVertexAttribs(graphics.rectBuffer, 0);
    }

    public void orphan(int width, int height, float pixelWidth, float pixelHeight) {
        Instance[] pixelInstances = new Instance[width * height];

        Random rand = new Random();

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                Matrix4f posMat = new Matrix4f().translate(
                    (float)x * pixelWidth, (float)y * pixelHeight, 0.0f
                ).scale(
                    pixelWidth, pixelHeight, 1.0f
                );

                pixelInstances[y * width + x] = new Instance(posMat, rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), 1.0f);
            }
        }

        instBuffer = graphics.createInstanceBuffer(pixelInstances, GL_DYNAMIC_DRAW);
    }

    public void startUpdate() {
        glBindBuffer(GL_ARRAY_BUFFER, instBuffer);
        mappedBuf = glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY);
    }

    public void updatePixel(Point p, Color colour) {
        int idx = Instance.SIZE_FLOATS * (p.y * width + p.x);

        FloatBuffer fbuf = mappedBuf.asFloatBuffer();
        fbuf.position(idx + 4 * 4);
        fbuf.put(colour.getRed() / 255.0f);
        fbuf.put(colour.getGreen() / 255.0f);
        fbuf.put(colour.getBlue() / 255.0f);
        fbuf.put(colour.getAlpha() / 255.0f);
    }

    public void stopUpdate() {
        mappedBuf.flip();
        glBindBuffer(GL_ARRAY_BUFFER, instBuffer);
        glUnmapBuffer(GL_ARRAY_BUFFER);
    }

    public void render() {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        graphics.setRenderUniforms(graphics.shaderDefault);
        glBindVertexArray(vao);
        glBindTexture(GL_TEXTURE_2D, graphics.texWhite.getTextureHandle());
        //glDrawArraysInstanced(GL_TRIANGLE_STRIP, 0, 4, width * height);

        graphics.modelviewMat.pushMatrix();
        graphics.modelviewMat.translate(640.0f, 0.0f, 0.0f);
        graphics.modelviewMat.scale(64.0f, 64.0f, 1.0f);

        graphics.setRenderUniforms(graphics.shaderDefault);

        glBindVertexArray(rectVAO);
        glBindTexture(GL_TEXTURE_2D, font.getTextureHandle());
        glDrawArraysInstanced(GL_TRIANGLE_STRIP, 0, 4, 1);

        graphics.modelviewMat.popMatrix();
    }

    public void close() {
        glDeleteBuffers(instBuffer);
    }
}
