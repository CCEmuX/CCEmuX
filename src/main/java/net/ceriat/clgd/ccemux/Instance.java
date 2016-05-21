package net.ceriat.clgd.ccemux;

import org.joml.Matrix4f;

public class Instance {
    /** This instance's transform */
    public Matrix4f transform;

    /** The colour of this instance */
    public float r, g, b, a;

    /** Absolute offset of the tex coords */
    public float uOffset = 0.0f, vOffset = 0.0f;

    /** Relative offset of the tex coords */
    public float uScale = 1.0f, vScale = 1.0f;

    public Instance(Matrix4f transform, float r, float g, float b, float a) {
        this.transform = transform;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Instance(Matrix4f transform) {
        this(transform, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    /** The size of one Instance in bytes */
    public static final int SIZE = (4 * 4) * 4 + 4 * 4 + 4 * 2 + 4 * 2;

    /** The size of one Instance in floats */
    public static final int SIZE_FLOATS = SIZE / 4;
}
