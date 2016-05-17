package net.ceriat.clgd.ccemux;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;

import java.io.Closeable;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

import static org.lwjgl.opengl.GL11.*;

public class EmuWindow implements Closeable {
    private long handle;

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

        glfwSetWindowSizeCallback(handle, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                glViewport(0, 0, width, height);
                CCEmuX.instance.graphics.refresh(width, height);
            }
        });
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
