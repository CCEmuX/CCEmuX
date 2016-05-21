package net.ceriat.clgd.ccemux.emulation;

import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.terminal.Terminal;
import net.ceriat.clgd.ccemux.CCEmuX;
import net.ceriat.clgd.ccemux.graphics.Colour;
import net.ceriat.clgd.ccemux.graphics.Point;
import net.ceriat.clgd.ccemux.graphics.TerminalRenderer;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.io.Closeable;

public class EmuComputer implements Closeable {
    public Computer computer;
    public Terminal terminal;
    public TerminalRenderer renderer;

    public int termWidth, termHeight;
    public int pixelWidth, pixelHeight;

    private int lastDragX = 0, lastDragY = 0;

    public EmuComputer(int termWidth, int termHeight, int pixelWidth, int pixelHeight) {
        this.termWidth = termWidth;
        this.termHeight = termHeight;
        this.pixelWidth = pixelWidth;
        this.pixelHeight = pixelHeight;

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

        renderer.setCursorPos(terminal.getCursorX(), terminal.getCursorY());
        renderer.setDrawCursor(terminal.getCursorBlink() && CCEmuX.instance.globalCursorBlink);
    }

    public void pressChar(char c) {
        computer.queueEvent("char", new Object[] { String.valueOf(c) });
    }

    public void pressKey(int scancode, boolean up) {
        computer.queueEvent(up ? "key_up" : "key", new Object[] { scancode });
    }

    public void sendCtrlCombo(CCCtrlCommand cmd) {
        switch (cmd) {
            case SHUTDOWN:
                computer.shutdown();
                break;

            case REBOOT:
                computer.reboot();
                break;

            case TERMINATE:
                computer.queueEvent("terminate", new Object[] {});
                break;

            case PASTE: {
                Toolkit tk = Toolkit.getDefaultToolkit();
                String data = null;

                try {
                    data = (String)tk.getSystemClipboard().getData(DataFlavor.stringFlavor);
                    computer.queueEvent("paste", new Object[] { data });
                } catch (Exception e) {
                    // don't care
                }

                break;
            }
        }
    }

    public void mouseDrag(int button, double x, double y) {
        int realX = (int)x / pixelWidth + 1;
        int realY = (int)y / pixelHeight + 1;

        // prevents drag event spam
        if (realX == lastDragX && realY == lastDragY) {
            return;
        }

        computer.queueEvent("mouse_drag", new Object[] {
            button,
            realX,
            realY
        });

        lastDragX = realX;
        lastDragY = realY;
    }

    public void mousePress(int button, double x, double y, boolean release) {
        computer.queueEvent(release ? "mouse_up" : "mouse_click", new Object[] {
            button,
            (int)x / pixelWidth + 1,
            (int)y / pixelHeight + 1
        });
    }

    @Override
    public void close() {
        computer.shutdown();
    }
}
