package net.ceriat.clgd.ccemux;

import org.joml.Matrix4f;
import org.joml.MatrixStackf;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL33.*;

public class Graphics {
    /** A static vertex buffer containing a unit rectangle. To be used with GL_TRIANGLE_STRIP. */
    public int rectBuffer;

    public MatrixStackf projectionMat = new MatrixStackf(1);
    public MatrixStackf modelviewMat = new MatrixStackf(3);

    public Shader shaderDefault = new Shader("default").compile().link();

    private int defaultInstBuffer;

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

        defaultInstBuffer = createInstanceBuffer(new Instance[] {
            new Instance(new Matrix4f().identity(), 1.0f, 1.0f, 1.0f, 1.0f)
        }, GL_STATIC_DRAW);

        projectionMat.identity();
        modelviewMat.identity();
    }

    /**
     * Initialise an orthographic coordinate system. Top left is (0, 0), bottom right is (w, h).
     * @param width The width of the framebuffer.
     * @param height The height of the framebuffer.
     */
    public void makeOrthographic(int width, int height) {
        projectionMat.identity();
        projectionMat.ortho(0.0f, (float)width, (float)height, 0.0f, -1.0f, 1.0f);
    }

    /**
     * Reinitialises the projection matrix to fit the new resolution.
     * @param width The new framebuffer width.
     * @param height The new framebuffer height.
     */
    public void refresh(int width, int height) {
        makeOrthographic(width, height);
    }

    /**
     * Sets uniforms required for rendering, as well as binding the shader.
     * @param shader
     */
    public void setRenderUniforms(Shader shader) {
        glUseProgram(shader.getProgramHandle());

        FloatBuffer fb = BufferUtils.createFloatBuffer(16);

        int u_MVMatrix = shaderDefault.getUniformLocation("u_MVMatrix");
        modelviewMat.get(fb);
        glUniformMatrix4fv(u_MVMatrix, false, fb);

        int u_PMatrix = shaderDefault.getUniformLocation("u_PMatrix");
        projectionMat.get(fb);
        glUniformMatrix4fv(u_PMatrix, false, fb);
    }

    /**
     * Creates an array of vertices on the GPU. Will bind the buffer as a side effect.
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
     * Creates an array of instances on the GPU. Will bind the buffer as a side effect.
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

    /**
     * Creates an OpenGL VAO for buffers created with {@link Graphics#createVBO(Vertex[], int)}.
     * Will bind the buffer as a side effect.
     *
     * @param buffer The buffer to use for the VAO.
     * @param instanceBuffer The instance buffer to use for the VAO. Pass 0 if none is used.
     * @return The VAO.
     */
    public int createVertexAttribs(int buffer, int instanceBuffer) {
        int vao = glGenVertexArrays();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, buffer);

        // position
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, Vertex.SIZE, 0L);

        // tex coords
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, Vertex.SIZE, 3L * 4L); // 3 floats in

        if (instanceBuffer > 0) {
            glBindBuffer(GL_ARRAY_BUFFER, instanceBuffer);
        } else {
            glBindBuffer(GL_ARRAY_BUFFER, defaultInstBuffer);
        }

        // instance matrix
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 4, GL_FLOAT, false, Instance.SIZE, (4L * 0L) * 4L);
        glVertexAttribDivisor(2, 1);

        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 4, GL_FLOAT, false, Instance.SIZE, (4L * 1L) * 4L);
        glVertexAttribDivisor(3, 1);

        glEnableVertexAttribArray(4);
        glVertexAttribPointer(4, 4, GL_FLOAT, false, Instance.SIZE, (4L * 2L) * 4L);
        glVertexAttribDivisor(4, 1);

        glEnableVertexAttribArray(5);
        glVertexAttribPointer(5, 4, GL_FLOAT, false, Instance.SIZE, (4L * 3L) * 4L);
        glVertexAttribDivisor(5, 1);

        // instance colour
        glEnableVertexAttribArray(6);
        glVertexAttribPointer(6, 4, GL_FLOAT, false, Instance.SIZE, (4L * 4L) * 4L);
        glVertexAttribDivisor(6, 1);

        return vao;
    }
}
