package net.ceriat.clgd.ccemux.emulation;

import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.terminal.Terminal;

import net.ceriat.clgd.ccemux.CCEmuX;
import net.ceriat.clgd.ccemux.graphics.Colour;
import net.ceriat.clgd.ccemux.graphics.Point;
import net.ceriat.clgd.ccemux.graphics.TerminalRenderer;

import java.io.Closeable;

public class EmuComputer implements Closeable {
    public Computer computer;
    public Terminal terminal;
    public TerminalRenderer renderer;

    public EmuComputer(int termWidth, int termHeight, int pixelWidth, int pixelHeight) {
        terminal = new Terminal(termWidth, termHeight);
        computer = new Computer(new EmuEnvironment(true), terminal, 0);

        renderer = new TerminalRenderer(
            CCEmuX.instance.ccAssets.font,
            termWidth, termHeight,
            pixelWidth, pixelHeight
        );

        computer.turnOn();
    }

    private static final String BASE_16 = "0123456789abcdef";

    private static Colour getColourFromLineChar(char c) {
        int i = 15 - BASE_16.indexOf(Character.toLowerCase(c));
        dan200.computercraft.shared.util.Colour col = dan200.computercraft.shared.util.Colour.fromInt(i);

        if (col == null) {
            return Colour.WHITE;
        }

        return new Colour(col.getR(), col.getG(), col.getB());
    }

    public void syncWithRenderer() {
        renderer.startTextUpdate();

        for (int y = 0; y < terminal.getHeight(); ++y) {
            for (int x = 0; x < terminal.getWidth(); ++x) {
                char text = terminal.getLine(y).charAt(x);
                char textColour = terminal.getTextColourLine(y).charAt(x);
                renderer.updateText(new Point(x, y), getColourFromLineChar(textColour), text);
            }
        }

        renderer.stopTextUpdate();

        renderer.startPixelUpdate();

        for (int y = 0; y < terminal.getHeight(); ++y) {
            for (int x = 0; x < terminal.getWidth(); ++x) {
                char colour = terminal.getBackgroundColourLine(y).charAt(x);
                renderer.updatePixel(new Point(x, y), getColourFromLineChar(colour));
            }
        }

        renderer.stopPixelUpdate();
    }

    @Override
    public void close() {
        computer.shutdown();
    }
}
