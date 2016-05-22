package net.ceriat.clgd.ccemux;

import dan200.computercraft.core.computer.ComputerThread;
import net.ceriat.clgd.ccemux.emulation.CCAssets;
import net.ceriat.clgd.ccemux.emulation.EmuComputer;
import net.ceriat.clgd.ccemux.graphics.EmuWindow;

import javax.swing.*;
import java.io.File;
import java.util.Random;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class CCEmuX {
    // Constants
    public static final String VERSION = "1.0.0";

    private static final String EMU_TITLE = "CCEmuX v" + VERSION;
    private static final int EMU_WIDTH = 918;
    private static final int EMU_HEIGHT = 513;

    // Emulator components

    /** The emulator's window */
    public EmuWindow window;

    /** A helper object that aids with graphical stuff */
    public net.ceriat.clgd.ccemux.graphics.Graphics graphics;

    public Logger logger = Logger.getLogger("CCEmuX");

    public File assetsDir = new File("assets");
    public File ccJarFile = new File(assetsDir, "ComputerCraft.jar");
    public File romCustomDir = new File(assetsDir, "rom_custom");
    public File configFile = new File(assetsDir, "config.properties");

    public CCEmuXConfig config = new CCEmuXConfig(configFile);

    public CCAssets ccAssets;

    public int ticksSinceStart;
    public float timeSinceStart;
    public boolean globalCursorBlink;

    public float deltaTime;

    public EmuComputer computer;

    public CCEmuX() throws Exception {
        CCEmuX.instance = this;

        if (!assetsDir.exists()) {
            assetsDir.mkdir();
        }

        if (!romCustomDir.exists()) {
            romCustomDir.mkdirs();
        }

        if (!ccJarFile.exists()) {
            JOptionPane.showMessageDialog(
                null,
                "There is no ComputerCraft.jar file present.\n" +
                "Please download one from computercraft.info and put it in the assets directory with the name \"ComputerCraft.jar\".",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );

            System.exit(1);
            return;
        }

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

        graphics = new net.ceriat.clgd.ccemux.graphics.Graphics(); // must be created after context
        graphics.makeOrthographic(window.getWidth(), window.getHeight());

        ccAssets = new CCAssets(ccJarFile);
        computer = new EmuComputer(51, 19, 18, 27);
    }

    public void tick() {
        computer.advance(deltaTime);
        computer.syncWithRenderer();

        globalCursorBlink = ticksSinceStart / 8 % 2 == 0;
    }

    /**
     * Starts the emulator loop.
     */
    public void startLoop() {
        Random rand = new Random();
        int tickTimer = 0;

        long lastTime = System.currentTimeMillis();

        while (!window.shouldClose()) {
            long now = System.currentTimeMillis();
            long dtMs = now - lastTime;
            float dt = dtMs / 1000.0f;
            timeSinceStart += dt;
            deltaTime = dt;

            tickTimer += dtMs;

            if (tickTimer >= 50) {
                ticksSinceStart++;
                tickTimer = 0;

                tick();
            }

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            computer.renderer.render();

            lastTime = System.currentTimeMillis();

            window.swapBuffers();
            EmuWindow.pollEvents();
        }

        cleanup();
    }

    public void cleanup() {
        ccAssets.close();
        computer.close();
        graphics.close();

        ComputerThread.stop();
    }

    /** A CCEmuX instance */
    public static CCEmuX instance;

    public static void main(String[] args) throws Exception {
        initLibs();

        CCEmuX emux = new CCEmuX();
        emux.startLoop();

        glfwTerminate();

        // TODO: Find a way to terminate CC's million threads
        System.exit(0);
    }

    private static void initLibs() throws Exception {
        if (glfwInit() == GLFW_FALSE) {
            throw new Exception("failed to initialise glfw!");
        }
    }
}
