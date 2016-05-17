package net.ceriat.clgd.ccemux;

import org.lwjgl.BufferUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.IntBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL11.*;

public class Shader {
    private int fragShader, vertShader;
    private int program;
    private String pathPrefix, name;

    private HashMap<String, Integer> uniformCache = new HashMap<String, Integer>();

    public Shader(String name) {
        this.name = name;
        pathPrefix = "/shaders/" + name;

        fragShader = glCreateShader(GL_FRAGMENT_SHADER);
        vertShader = glCreateShader(GL_VERTEX_SHADER);

        program = glCreateProgram();
    }

    private void compile(String file, int handle) {
        InputStream is = getClass().getResourceAsStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        String all = "";

        String line;

        try {
            while ((line = reader.readLine()) != null) {
                all += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }

                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        glShaderSource(handle, all);
        glCompileShader(handle);

        IntBuffer status = BufferUtils.createIntBuffer(1);
        glGetShaderiv(handle, GL_COMPILE_STATUS, status);

        if (status.get() != GL_TRUE) {
            CCEmuX.instance.logger.severe(file + ": " + glGetShaderInfoLog(handle));
        }
    }

    public int getUniformLocation(String name) {
        if (uniformCache.containsKey(name)) {
            return uniformCache.get(name);
        } else {
            int l = glGetUniformLocation(program, name);
            uniformCache.put(name, l);
            return l;
        }
    }

    public Shader compile() {
        compile(pathPrefix + ".frag.glsl", fragShader);
        compile(pathPrefix + ".vert.glsl", vertShader);

        return this;
    }

    public Shader link() {
        glAttachShader(program, fragShader);
        glAttachShader(program, vertShader);

        glLinkProgram(program);

        IntBuffer status = BufferUtils.createIntBuffer(1);
        glGetProgramiv(program, GL_LINK_STATUS, status);

        if (status.get() != GL_TRUE) {
            CCEmuX.instance.logger.severe("Link error: " + glGetProgramInfoLog(program));

            glDetachShader(program, fragShader);
            glDetachShader(program, vertShader);

            glDeleteShader(fragShader);
            glDeleteShader(vertShader);

            glDeleteProgram(program);

            return this;
        }

        CCEmuX.instance.logger.fine("Compiled and linked shader \"" + name + "\"");

        glDetachShader(program, fragShader);
        glDetachShader(program, vertShader);

        glDeleteShader(fragShader);
        glDeleteShader(vertShader);

        return this;
    }

    /**
     * @return Returns the OpenGL handle to the program.
     */
    public int getProgramHandle() {
        return program;
    }
}
