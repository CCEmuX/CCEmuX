package net.ceriat.clgd.ccemux;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.io.Closeable;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.system.MemoryUtil.*;

public class EmuWindow implements Closeable {
    private long handle;

    public EmuWindow(String title, int width, int height) throws Exception {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        handle = glfwCreateWindow(width, height, title, NULL, NULL);

        if (handle == NULL) {
            throw new Exception("could not create emulator window");
        }

        // centre the window
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(handle, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);

        glfwMakeContextCurrent(handle);
        glfwSwapInterval(1); // vsync, set to 0 for immediate swap

        GL.createCapabilities();
    }

    public void makeContextActive() {
        glfwMakeContextCurrent(handle);
    }

    public void show() {
        glfwShowWindow(handle);
    }

    public int getWidth() {
        IntBuffer widthBuf = BufferUtils.createIntBuffer(1);
        glfwGetWindowSize(handle, widthBuf, null);
        return widthBuf.get();
    }

    public int getHeight() {
        IntBuffer heightBuf = BufferUtils.createIntBuffer(1);
        glfwGetWindowSize(handle, null, heightBuf);
        return heightBuf.get();
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(handle) == GLFW_TRUE;
    }

    public void swapBuffers() {
        glfwSwapBuffers(handle);
    }

    public static void pollEvents() {
        glfwPollEvents();
    }

    public void close() {
        glfwDestroyWindow(handle);
    }
}
