package net.ceriat.clgd.ccemux;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

public class TerminalRenderer implements IRenderer {
    private Graphics graphics = CCEmuX.instance.graphics;
    private int instBuffer;
    private Instance[] pixelInstances;

    public TerminalRenderer() {
        instBuffer = graphics.createInstanceBuffer(pixelInstances, GL_DYNAMIC_DRAW);
    }

    public void render() {
    }
}
