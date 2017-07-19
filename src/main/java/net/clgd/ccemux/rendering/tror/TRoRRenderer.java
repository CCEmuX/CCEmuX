package net.clgd.ccemux.rendering.tror;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;

import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.emulation.EmulatedPalette;
import net.clgd.ccemux.emulation.EmulatedTerminal;
import net.clgd.ccemux.rendering.Renderer;
import net.clgd.ccemux.rendering.RendererConfig;

public class TRoRRenderer implements Renderer, EmulatedTerminal.Listener, EmulatedPalette.Listener {
	private static final String[] COLOURS = new String[]{
			"0", "1", "2", "3", "4", "5", "6", "7",
			"8", "9", "a", "b", "c", "d", "e", "f"
	};

	private final EmulatedComputer computer;
	private final RendererConfig config;

	private final List<Listener> listeners = new ArrayList<>();

	private final BlockingDeque<InputProvider.InputPacket> events;

	private final Writer output;

	public TRoRRenderer(EmulatedComputer computer, RendererConfig config) {
		this.computer = computer;
		this.config = config;

		try {
			output = new OutputStreamWriter(System.out, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		computer.terminal.addListener(this);
		computer.terminal.getEmulatedPalette().addListener(this);

		events = InputProvider.getStdinProvider().getQueue(computer);
	}

	@Override
	public void addListener(Renderer.Listener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(Renderer.Listener listener) {
		listeners.remove(listener);
	}

	@Override
	public boolean isVisible() {
		return false;
	}

	@Override
	public void setVisible(boolean visible) {

	}

	@Override
	public void dispose() {
	}

	@Override
	public void onAdvance(double dt) {
		InputProvider.InputPacket packet;
		while ((packet = events.poll()) != null) {
			switch (packet.code) {
				case "EV": {
					String payload = packet.payload;
					int index = payload.indexOf(',');
					if (index < 0) index = payload.length();

					String event = payload.substring(0, index);
					List<Object> args = new ArrayList<>();
					index++;
					while (index < payload.length()) {
						char startChar = payload.charAt(index);
						if (startChar == '"' || startChar == '\'') {
							StringBuilder builder = new StringBuilder();
							index++;
							while (index < payload.length() - 1) {
								char current = payload.charAt(index++);

								if (current == startChar) {
									break;
								} else if (current == '\\') {
									current = payload.charAt(index++);
									if (current == 'a') builder.append((char) 0x7);
									else if (current == 'b') builder.append('\b');
									else if (current == 'f') builder.append('\f');
									else if (current == 'n') builder.append('\n');
									else if (current == 'r') builder.append('\r');
									else if (current == 't') builder.append('\t');
									else if (current == 'v') builder.append((char) 0xB);
									else if (current == '\\') builder.append('\\');
									else if (current == '\'') builder.append('\'');
									else if (current == '\"') builder.append('\"');

									// TODO: Support for numeric escape codes.
								} else {
									builder.append(current);
								}
							}

							args.add(builder.toString());
							if (index >= payload.length() - 1 || payload.charAt(index) == ',') index++;
						} else {
							int next = payload.indexOf(',', index);
							if (next < 0) next = payload.length();
							String arg = payload.substring(index, next);
							switch (arg) {
								case "nil":
									args.add(null);
									break;
								case "true":
									args.add(true);
									break;
								case "false":
									args.add(false);
									break;
								default:
									try {
										args.add(Double.parseDouble(arg));
									} catch (NumberFormatException e) {
										args.add(null);
									}
									break;
							}

							index = next + 1;
						}
					}

					computer.queueEvent(event, args.toArray());
				}
			}
		}
	}

	private void sendLine(String mode, String line) {
		try {
			output.write(mode);
			output.write(':');
			output.write(Integer.toString(computer.getID()));
			output.write(';');
			output.write(line);
			output.write('\n');
			output.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void write(String text) {
		sendLine("TW", text.replace('\r', ' ').replace('\n', ' '));
	}

	@Override
	public void setCursorPos(int x, int y) {
		sendLine("TC", x + "," + y);
	}

	@Override
	public void clear() {
		sendLine("TE", "");
	}

	@Override
	public void clearLine() {
		sendLine("TL", "");
	}

	@Override
	public void scroll(int yDiff) {
		sendLine("TS", "");
	}

	@Override
	public void setCursorBlink(boolean blink) {
		sendLine("TB", blink ? "true" : "faalse");
	}

	@Override
	public void setTextColour(int colour) {
		sendLine("TF", COLOURS[colour]);
	}

	@Override
	public void setBackgroundColour(int colour) {
		sendLine("TK", COLOURS[colour]);
	}

	@Override
	public void setColour(int index, double r, double g, double b) {
		sendLine("TM", String.format("%s,%.4f,%.4f,%.4f", COLOURS[index], r, g, b));
	}

	@Override
	public void resize(int width, int height) {
		sendLine("TR", width + "," + height);
	}

	@Override
	public void blit(String text, String textColour, String backgroundColour) {
		sendLine("TY", textColour + "," + backgroundColour + "," + text.replace('\r', ' ').replace('\n', ' '));
	}
}
