package net.ceriat.clgd.ccemux;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

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

    public CCEmuX() throws Exception {
        window = new EmuWindow(EMU_TITLE, EMU_WIDTH, EMU_HEIGHT);
        window.show();

        window.makeContextActive();
        glClearColor(0.0f, 0.2f, 0.4f, 1.0f);

        graphics = new Graphics(); // must be created after context
        graphics.makeOrthographic(window.getWidth(), window.getHeight());

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
    }

    /**
     * Starts the emulator loop.
     */
    public void startLoop() {
        while (!window.shouldClose()) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glPushMatrix();
            {
                glScalef(64.0f, 64.0f, 1.0f);
                graphics.enableDrawAttribs(graphics.rectBuffer);
                glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
                graphics.disableDrawAttribs();
            }
            glPopMatrix();

            window.swapBuffers();
            EmuWindow.pollEvents();
        }
    }

    /** A CCEmuX instance */
    public static CCEmuX instance;

    public static void main(String[] args) throws Exception {
        initLibs();

        CCEmuX emux = new CCEmuX();
        CCEmuX.instance = emux;
        emux.startLoop();
    }

    private static void initLibs() throws Exception {
        if (glfwInit() == GLFW_FALSE) {
            throw new Exception("failed to initialise glfw!");
        }
    }
}
