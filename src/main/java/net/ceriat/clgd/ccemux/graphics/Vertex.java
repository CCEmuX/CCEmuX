package net.ceriat.clgd.ccemux.graphics;

public class Vertex {
    /** The position of the vertex */
    public float x, y, z;

    /** The texture coordinates of the vertex */
    public float u, v;

    public Vertex(float x, float y, float z) {
        this(x, y, z, 0.0f, 0.0f);
    }

    public Vertex(float x, float y, float z, float u, float v) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.u = u;
        this.v = v;
    }

    /** The size of a vertex in bytes */
    public static final int SIZE =
        3 * 4 +     // x, y, z -> 3 floats * 4 bytes per float
        2 * 4;      // u, v -> 2 floats * 4 bytes per float
}
