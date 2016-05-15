package net.ceriat.clgd.ccemux;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

public class Graphics {
    /** A static vertex buffer containing a unit rectangle. To be used with GL_TRIANGLE_STRIP. */
    public int rectBuffer;

    /**
     * Creates a new Graphics object. Must be created in the presence of an OpenGL context.
     */
    public Graphics() {
        rectBuffer = createVBO(new Vertex[] {
            new Vertex(0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
            new Vertex(0.0f, 1.0f, 0.0f, 0.0f, 1.0f),
            new Vertex(1.0f, 0.0f, 0.0f, 1.0f, 0.0f),
            new Vertex(1.0f, 1.0f, 0.0f, 1.0f, 1.0f)
        }, GL_STATIC_DRAW);
    }

    /**
     * Initialise an orthographic coordinate system. Top left is (0, 0), bottom right is (w, h).
     * @param width The width of the framebuffer.
     * @param height The height of the framebuffer.
     */
    public void makeOrthographic(int width, int height) {
        // TODO: Replace this with core OpenGL code.
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0, (double)width, (double)height, 0.0, -1.0, 1.0);
    }

    /**
     * Creates an array of vertices on the GPU.
     * @param vertices The vertices.
     * @param usage How this buffer will be used.
     *              GL_STATIC_DRAW: This buffer will not change.
     *              GL_DYNAMIC_DRAW: This buffer will probably change.
     *              GL_STREAM_DRAW: This buffer is streamed.
     * @return
     */
    public int createVBO(Vertex[] vertices, int usage) {
        int buffer = glGenBuffers();

        FloatBuffer fbuf = BufferUtils.createFloatBuffer(vertices.length * Vertex.SIZE);

        for (Vertex v : vertices) {
            fbuf.put(new float[] {
                v.x, v.y, v.z,
                v.u, v.v
            });
        }

        fbuf.flip();

        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        glBufferData(GL_ARRAY_BUFFER, fbuf, usage);

        return buffer;
    }

    /**
     * Enable the attributes required for a draw.
     * @param buffer The buffer to draw with.
     */
    public void enableDrawAttribs(int buffer) {
        // TODO: Replace this with core OpenGL code.
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);

        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        glVertexPointer(3, GL_FLOAT, Vertex.SIZE, (long)0);
        glTexCoordPointer(2, GL_FLOAT, Vertex.SIZE, (long)3 * 4); // 3 * 4 means 3 floats into the buffer
    }

    /**
     * Disable the attributes required for a draw.
     */
    public void disableDrawAttribs() {
        // TODO: Replace this with core OpenGL code.
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);
    }
}
