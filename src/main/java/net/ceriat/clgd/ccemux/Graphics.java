package net.ceriat.clgd.ccemux;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class Graphics {
    /** A static vertex buffer containing a unit rectangle. To be used with GL_TRIANGLE_STRIP. */
    public int rectBuffer;

    public Matrix4f projectionMat = new Matrix4f();
    public Matrix4f viewMat = new Matrix4f();
    public Matrix4f modelMat = new Matrix4f();

    public Shader shaderDefault = new Shader("default").compile().link();

    /**
     * Creates a new Graphics object. Must be created in the presence of an OpenGL context.
     */
    public Graphics() {
        String glVersion = glGetString(GL_VERSION);
        CCEmuX.instance.logger.info("OpenGL: " + glVersion);

        rectBuffer = createVBO(new Vertex[] {
            new Vertex(0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
            new Vertex(0.0f, 1.0f, 0.0f, 0.0f, 1.0f),
            new Vertex(1.0f, 0.0f, 0.0f, 1.0f, 0.0f),
            new Vertex(1.0f, 1.0f, 0.0f, 1.0f, 1.0f)
        }, GL_STATIC_DRAW);

        viewMat.identity();
        modelMat.identity();
    }

    /**
     * Initialise an orthographic coordinate system. Top left is (0, 0), bottom right is (w, h).
     * @param width The width of the framebuffer.
     * @param height The height of the framebuffer.
     */
    public void makeOrthographic(int width, int height) {
        projectionMat.identity();
        projectionMat.ortho2D(0.0f, (float)width, (float)height, 0.0f);
    }

    public Matrix4f getMVP() {
        Matrix4f mvp = projectionMat.mul(viewMat.mul(modelMat));
        return mvp;
    }

    /**
     * Creates an array of vertices on the GPU.
     * @param vertices The vertices.
     * @param usage How this buffer will be used.
     *              GL_STATIC_DRAW: This buffer will not change.
     *              GL_DYNAMIC_DRAW: This buffer will probably change.
     *              GL_STREAM_DRAW: This buffer is streamed.
     * @return A handle to the buffer.
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
     * Creates an array of instances on the GPU.
     * @param instances The instances.
     * @param usage How this buffer will be used.
     *              GL_STATIC_DRAW: This buffer will not change.
     *              GL_DYNAMIC_DRAW: This buffer will probably change.
     *              GL_STREAM_DRAW: This buffer is streamed.
     * @return A handle to the buffer.
     */
    public int createInstanceBuffer(Instance[] instances, int usage) {
        int buffer = glGenBuffers();

        FloatBuffer fbuf = BufferUtils.createFloatBuffer(instances.length * Instance.SIZE);

        for (Instance inst : instances) {
            float[] mat = new float[4 * 4];
            inst.transform.get(mat);

            fbuf.put(mat).put(new float[] {
                inst.r, inst.g, inst.b, inst.a
            });
        }

        fbuf.flip();

        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        glBufferData(GL_ARRAY_BUFFER, fbuf, usage);

        return buffer;
    }
}
