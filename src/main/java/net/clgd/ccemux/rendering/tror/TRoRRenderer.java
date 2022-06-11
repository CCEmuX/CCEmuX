package net.clgd.ccemux.rendering.tror;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingDeque;

import javax.annotation.Nonnull;

import net.clgd.ccemux.api.Utils;
import net.clgd.ccemux.api.emulation.EmulatedComputer;
import net.clgd.ccemux.api.emulation.EmulatedPalette;
import net.clgd.ccemux.api.emulation.EmulatedTerminal;
import net.clgd.ccemux.api.rendering.Renderer;

public class TRoRRenderer implements Renderer, EmulatedTerminal.Listener, EmulatedPalette.ColorChangeListener {
	private final EmulatedComputer computer;

	private final List<Renderer.Listener> listeners = new ArrayList<>();

	private final BlockingDeque<InputProvider.InputPacket> events;

	private final Writer output;
	private boolean isVisible = true;

	public TRoRRenderer(EmulatedComputer computer) {
		this.computer = computer;

		output = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);

		computer.terminal.addListener(this);
		computer.terminal.getPalette().addListener(this);

		events = InputProvider.getStdinProvider().getQueue(computer);

		resize(computer.terminal.getWidth(), computer.terminal.getHeight());
	}

	@Override
	public void addListener(@Nonnull Renderer.Listener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(@Nonnull Renderer.Listener listener) {
		listeners.remove(listener);
	}

	@Override
	public boolean isVisible() {
		return isVisible;
	}

	@Override
	public void setVisible(boolean visible) {
		if (isVisible != visible) {
			isVisible = visible;

			if (visible) {
				// Broadcast the entire terminal state to ensure the remote is
				// in sync.
				EmulatedTerminal terminal = computer.terminal;
				setCursorPos(terminal.getCursorX(), terminal.getCursorY());
				setCursorBlink(terminal.getCursorBlink());
				resize(terminal.getWidth(), terminal.getHeight());

				StringBuilder builder = new StringBuilder();
				for (int y = 0; y < terminal.getHeight(); y++) {
					if (y > 0) {
						builder.append(':');
					}

					builder.append(terminal.getTextColourLine(y));
					builder.append(',');
					builder.append(terminal.getBackgroundColourLine(y));
					builder.append(',');
					builder.append(terminal.getLine(y));
				}
				sendLine("TV", builder.toString());

				EmulatedPalette palette = terminal.getPalette();
				for (int i = 0; i < 16; i++) {
					double[] colour = palette.getColour(i);
					setColour(i, colour[0], colour[1], colour[2]);
				}
			}
		}
	}

	@Override
	public void dispose() {
		sendLine("SC", "");
	}

	@Override
	public void onAdvance(double dt) {
		InputProvider.InputPacket packet;
		while ((packet = events.poll()) != null) {
			switch (packet.code) {
				case "EV": {
					String payload = packet.payload;
					int index = payload.indexOf(',');
					if (index < 0) {
						index = payload.length();
					}

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
									if (current == 'a') {
										builder.append((char) 0x7);
									} else if (current == 'b') {
										builder.append('\b');
									} else if (current == 'f') {
										builder.append('\f');
									} else if (current == 'n') {
										builder.append('\n');
									} else if (current == 'r') {
										builder.append('\r');
									} else if (current == 't') {
										builder.append('\t');
									} else if (current == 'v') {
										builder.append((char) 0xB);
									} else if (current == '\\') {
										builder.append('\\');
									} else if (current == '\'') {
										builder.append('\'');
									} else if (current == '\"') {
										builder.append('\"');
									}
									// TODO: Support for numeric escape codes.
								} else {
									builder.append(current);
								}
							}

							args.add(builder.toString());
							if (index >= payload.length() - 1 || payload.charAt(index) == ',') {
								index++;
							}
						} else {
							int next = payload.indexOf(',', index);
							if (next < 0) {
								next = payload.length();
							}
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
					break;
				}
				case "XA":
					switch (packet.payload.toLowerCase(Locale.ENGLISH)) {
						case "shutdown":
							computer.shutdown();
							break;
						case "reboot":
							computer.reboot();
							break;
						case "close":
							for (Listener listener : listeners) {
								listener.onClosed();
							}
							break;
					}
					break;
			}
		}
	}

	private void sendLine(String mode, String line) {
		if (!isVisible) {
			return;
		}

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
	public void write(@Nonnull String text) {
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
		sendLine("TB", blink ? "true" : "false");
	}

	@Override
	public void setTextColour(int colour) {
		sendLine("TF", Character.toString(Utils.intToBase16(colour)));
	}

	@Override
	public void setBackgroundColour(int colour) {
		sendLine("TK", Character.toString(Utils.intToBase16(colour)));
	}

	@Override
	public void setColour(int index, double r, double g, double b) {
		sendLine("TM", String.format("%c,%.4f,%.4f,%.4f", Utils.intToBase16(index), r, g, b));
	}

	@Override
	public void resize(int width, int height) {
		sendLine("TR", width + "," + height);
	}

	@Override
	public void blit(@Nonnull String text, @Nonnull String textColour, @Nonnull String backgroundColour) {
		sendLine("TY", textColour + "," + backgroundColour + "," + text.replace('\r', ' ').replace('\n', ' '));
	}
}
