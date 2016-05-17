package net.ceriat.clgd.ccemux;

import org.joml.Matrix4f;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;

public class CCEmuX {
    // Constants
    private static final String EMU_TITLE = "CCEmuX";
    private static final int EMU_WIDTH = 1280;
    private static final int EMU_HEIGHT = 720;

    // Emulator components

    /** The emulator's window */
    public EmuWindow window;

    /** A helper object that aids with graphical stuff */
    public Graphics graphics;

    public Logger logger = Logger.getLogger("CCEmuX");

    private int testVAO, testInsts;

    public CCEmuX() throws Exception {
        CCEmuX.instance = this;

        logger.setUseParentHandlers(false);

        for (Handler h : logger.getHandlers()) {
            logger.removeHandler(h);
        }

        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.ALL);
        ch.setFormatter(new LogFormatter());

        logger.addHandler(ch);

        logger.setLevel(Level.ALL);

        window = new EmuWindow(EMU_TITLE, EMU_WIDTH, EMU_HEIGHT);
        window.show();

        window.makeContextActive();
        glClearColor(0.0f, 0.2f, 0.4f, 1.0f);

        graphics = new Graphics(); // must be created after context
        graphics.makeOrthographic(window.getWidth(), window.getHeight());

        testInsts = graphics.createInstanceBuffer(new Instance[] {
            new Instance(new Matrix4f().translate(128.0f, 64.0f, 0.0f).scale(128.0f, 64.0f, 1.0f), 1.0f, 0.0f, 0.0f, 1.0f),
            new Instance(new Matrix4f().translate(0.0f, 0.0f, 0.0f).scale(64.0f, 128.0f, 1.0f), 0.0f, 1.0f, 0.0f, 1.0f),
            new Instance(new Matrix4f().translate(512.0f, 128.0f, 0.0f).scale(64.0f, 64.0f, 1.0f).rotateZ((float)Math.toRadians(260.0f)), 0.0f, 0.0f, 1.0f, 1.0f),
            new Instance(new Matrix4f().translate(64.0f, 256.0f, 0.0f).scale(64.0f, 64.0f, 1.0f).rotateZ((float)Math.toRadians(34.0f)), 1.0f, 0.0f, 1.0f, 1.0f)
        }, GL_STATIC_DRAW);

        testVAO = graphics.createVertexAttribs(graphics.rectBuffer, testInsts);
    }

    /**
     * Starts the emulator loop.
     */
    public void startLoop() {
        while (!window.shouldClose()) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            graphics.modelviewMat.pushMatrix();

            glBindVertexArray(testVAO);
            graphics.setRenderUniforms(graphics.shaderDefault);
            glDrawArraysInstanced(GL_TRIANGLE_STRIP, 0, 4, 4);

            graphics.modelviewMat.popMatrix();

            window.swapBuffers();
            EmuWindow.pollEvents();
        }
    }

    /** A CCEmuX instance */
    public static CCEmuX instance;

    public static void main(String[] args) throws Exception {
        initLibs();

        CCEmuX emux = new CCEmuX();
        emux.startLoop();
    }

    private static void initLibs() throws Exception {
        if (glfwInit() == GLFW_FALSE) {
            throw new Exception("failed to initialise glfw!");
        }
    }
}
