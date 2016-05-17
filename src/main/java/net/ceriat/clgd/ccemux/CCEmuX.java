package net.ceriat.clgd.ccemux;

import org.joml.Matrix4f;

import java.awt.*;
import java.util.Random;
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

    private TerminalRenderer termRenderer;

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

        termRenderer = new TerminalRenderer(51, 19, 10.6f, 16.0f);
    }

    /**
     * Starts the emulator loop.
     */
    public void startLoop() {
        Random rand = new Random();

        while (!window.shouldClose()) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            termRenderer.startUpdate();

            for (int y = 5; y < 10; ++y) {
                for (int x = 5; x < 40; ++x) {
                    termRenderer.updatePixel(new Point(x, y), new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), 255));
                }
            }

            termRenderer.stopUpdate();

            termRenderer.render();

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
