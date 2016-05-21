package net.ceriat.clgd.ccemux.emulation;

import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.terminal.Terminal;
import net.ceriat.clgd.ccemux.CCEmuX;
import net.ceriat.clgd.ccemux.graphics.TerminalRenderer;

import java.awt.*;
import java.io.Closeable;

public class EmuComputer implements Closeable {
    public Computer computer;
    public Terminal terminal;
    public TerminalRenderer renderer;

    public EmuComputer(int termWidth, int termHeight, int pixelWidth, int pixelHeight) {
        terminal = new Terminal(termWidth, termHeight);
        computer = new Computer(new EmuEnvironment(false), terminal, 0);

        renderer = new TerminalRenderer(
            CCEmuX.instance.ccAssets.font,
            termWidth, termHeight,
            pixelWidth, pixelHeight
        );

        computer.turnOn();
    }

    public void syncWithRenderer() {
        renderer.startTextUpdate();

        for (int y = 0; y < terminal.getHeight(); ++y) {
            for (int x = 0; x < terminal.getWidth(); ++x) {
                char text = terminal.getLine(y).charAt(x);
                renderer.updateText(new Point(x, y), Color.WHITE, text);
            }
        }

        renderer.stopTextUpdate();
    }

    @Override
    public void close() {
        computer.shutdown();
    }
}
