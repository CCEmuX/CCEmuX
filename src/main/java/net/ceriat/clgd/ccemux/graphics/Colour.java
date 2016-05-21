package net.ceriat.clgd.ccemux.graphics;

public class Colour {
    public float r, g, b, a;

    public Colour(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Colour(float r, float g, float b) {
        this(r, g, b, 1.0f);
    }

    public static final Colour WHITE = new Colour(1.0f, 1.0f, 1.0f);
    public static final Colour BLACK = new Colour(0.0f, 0.0f, 0.0f);
}
