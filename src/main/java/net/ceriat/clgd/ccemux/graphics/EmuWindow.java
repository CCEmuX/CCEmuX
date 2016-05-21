package net.ceriat.clgd.ccemux.graphics;

import net.ceriat.clgd.ccemux.CCEmuX;
import net.ceriat.clgd.ccemux.emulation.CCCtrlCommand;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;

import javax.swing.*;
import java.io.Closeable;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class EmuWindow implements Closeable {
    private long handle;

    private static final HashMap<Integer, Integer> keycodeTranslationMap = new HashMap<Integer, Integer>();

    static {
        keycodeTranslationMap.put(328, 200);
        keycodeTranslationMap.put(336, 208);
        keycodeTranslationMap.put(331, 203);
        keycodeTranslationMap.put(333, 205);
        keycodeTranslationMap.put(335, 207);
        keycodeTranslationMap.put(327, 199);
    }

    private GLFWWindowSizeCallback callbackSize = new GLFWWindowSizeCallback() {
        @Override
        public void invoke(long window, int width, int height) {
            glViewport(0, 0, width, height);

            if (CCEmuX.instance.graphics != null) {
                CCEmuX.instance.graphics.refresh(width, height);
            }
        }
    };

    private GLFWCharModsCallback callbackChar = new GLFWCharModsCallback() {
        @Override
        public void invoke(long window, int codepoint, int mods) {
            CCEmuX.instance.computer.pressChar((char) codepoint);
        }
    };

    private GLFWKeyCallback callbackKey = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            int code = scancode;

            if (keycodeTranslationMap.containsKey(scancode)) {
                code = keycodeTranslationMap.get(scancode);
            }

            if (action == GLFW_RELEASE && (mods & GLFW_MOD_CONTROL) == GLFW_MOD_CONTROL) {
                for (CCCtrlCommand cmd : CCCtrlCommand.values()) {
                    if (cmd.triggerKey == key) {
                        CCEmuX.instance.computer.sendCtrlCombo(cmd);
                        return;
                    }
                }
            }

            CCEmuX.instance.computer.pressKey(code, action == GLFW_RELEASE);
        }
    };

    private GLFWMouseButtonCallback callbackMouseBtn = new GLFWMouseButtonCallback() {
        @Override
        public void invoke(long window, int button, int action, int mods) {
            if (action == GLFW_PRESS) {
                int ccbutton = 0;

                switch (button) {
                    case GLFW_MOUSE_BUTTON_LEFT:
                        ccbutton = 1;
                        break;

                    case GLFW_MOUSE_BUTTON_RIGHT:
                        ccbutton = 2;
                        break;

                    case GLFW_MOUSE_BUTTON_MIDDLE:
                        ccbutton = 3;
                        break;
                }

                DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
                DoubleBuffer y = BufferUtils.createDoubleBuffer(1);
                glfwGetCursorPos(window, x, y);

                CCEmuX.instance.computer.mousePress(ccbutton, x.get(0), y.get(0));
            }
        }
    };

    /**
     * Creates a new window and OpenGL context. It will not show until {@link EmuWindow#show()} is called.
     * @param title The window's title.
     * @param width The width of the window.
     * @param height The height of the window.
     * @throws Exception
     */
    public EmuWindow(String title, int width, int height) throws Exception {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        // TODO: Disable debug mode on release somehow.
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        handle = glfwCreateWindow(width, height, title, NULL, NULL);

        if (handle == NULL) {
            throw new Exception("could not create emulator window");
        }

        // centre the window
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(handle, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);

        glfwMakeContextCurrent(handle);
        glfwSwapInterval(1); // vsync, set to 0 for immediate swap

        GL.createCapabilities(true);

        if (!GL.getCapabilities().OpenGL33) {
            JOptionPane.showMessageDialog(
                null,
                "This application requires at least OpenGL 3.3.\n" +
                "Try updating your graphics drivers.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
            return;
        }

        glfwSetWindowSizeCallback(handle, callbackSize);
        glfwSetCharModsCallback(handle, callbackChar);
        glfwSetKeyCallback(handle, callbackKey);
        glfwSetMouseButtonCallback(handle, callbackMouseBtn);
    }

    /**
     * When this is called, all OpenGL calls in the current thread will go towards this window.
     */
    public void makeContextActive() {
        glfwMakeContextCurrent(handle);
    }

    /**
     * Makes the window visible.
     */
    public void show() {
        glfwShowWindow(handle);
    }

    /**
     * @return The width of the window.
     */
    public int getWidth() {
        IntBuffer widthBuf = BufferUtils.createIntBuffer(1);
        glfwGetWindowSize(handle, widthBuf, null);
        return widthBuf.get();
    }

    /**
     * @return The height of the window.
     */
    public int getHeight() {
        IntBuffer heightBuf = BufferUtils.createIntBuffer(1);
        glfwGetWindowSize(handle, null, heightBuf);
        return heightBuf.get();
    }

    /**
     * @return true if the close button has been clicked, false if not.
     */
    public boolean shouldClose() {
        return glfwWindowShouldClose(handle) == GLFW_TRUE;
    }

    /**
     * Swaps front buffer and back buffer.
     */
    public void swapBuffers() {
        glfwSwapBuffers(handle);
    }

    /**
     * Polls events so that event callbacks may be called.
     */
    public static void pollEvents() {
        glfwPollEvents();
    }

    /**
     * Closes the window. Any actions performed on this object after close() has been called are undefined.
     */
    public void close() {
        glfwDestroyWindow(handle);
    }
}
