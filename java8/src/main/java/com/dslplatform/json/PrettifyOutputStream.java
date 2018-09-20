package com.dslplatform.json;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;


public final class PrettifyOutputStream extends OutputStream {
	private static final int INDENT_CACHE_SIZE = 257;

	private static final boolean[] WHITESPACE = new boolean[256];
	static {
		WHITESPACE[9] = true;
		WHITESPACE[10] = true;
		WHITESPACE[13] = true;
		WHITESPACE[32] = true;
	}

	private final OutputStream out;
	private final IndentType indentType;
	private final int indentLength;

	private int currentIndent = 0;
	private boolean inString = false;
	private int lastByteInString = 0;
	private boolean beginObjectOrList = false;

	public enum IndentType {
		SPACES((byte) ' '),
		TABS((byte) '\t');

		private final byte[] cache;

		IndentType(byte b) {
			cache = new byte[INDENT_CACHE_SIZE];
			cache[0] = '\n';
			Arrays.fill(cache, 1, cache.length, b);
		}
	}

	public PrettifyOutputStream(OutputStream out) {
		this(out, IndentType.SPACES, 2);
	}

	public PrettifyOutputStream(OutputStream out, IndentType indentType, int indentLength) {
		if (out == null) throw new IllegalArgumentException("'out' must be not null");
		if (indentType == null) throw new IllegalArgumentException("'indentType' must be not null");
		if (indentLength < 1) throw new IllegalArgumentException("'indentLength' must be >= 1");

		this.out = out;
		this.indentType = indentType;
		this.indentLength = indentLength;
	}

	@Override
	public final void write(final byte[] bytes, final int off, final int len) throws IOException {
		int start = off;

		for (int i = off; i < off + len; i++) {
			int b = bytes[i];

			if (inString) {
				if (b == '"' && lastByteInString != '\\') {
					inString = false;
				} else {
					lastByteInString = b;
				}
			} else if (b == '"') {
				inString = true;
				if (beginObjectOrList) {
					writeNewLineWithIndent();
					beginObjectOrList = false;
				}
			} else if (b == ',') {
				out.write(bytes, start, i - start + 1);
				start = i + 1;
				writeNewLineWithIndent();
			} else if (b == ':') {
				out.write(bytes, start, i - start + 1);
				start = i + 1;
				out.write(' ');
			} else if (b == '{' || b == '[') {
				if (beginObjectOrList) {
					writeNewLineWithIndent();
				}
				beginObjectOrList = true;
				currentIndent += indentLength;
				out.write(bytes, start, i - start + 1);
				start = i + 1;
			} else if (b == '}' || b == ']') {
				currentIndent -= indentLength;
				out.write(bytes, start, i - start);
				if (beginObjectOrList) {
					beginObjectOrList = false;
				} else {
					writeNewLineWithIndent();
				}
				out.write(b);
				start = i + 1;
			} else if(WHITESPACE[b]) {
				out.write(bytes, start, i - start);
				start = i + 1;
			} else if (beginObjectOrList) {
				writeNewLineWithIndent();
				beginObjectOrList = false;
			}
		}

		int remaining = off + len - start;
		if (remaining > 0) {
			out.write(bytes, start, remaining);
		}
	}

	@Override
	public final void write(final int b) throws IOException {
		if (inString) {
			if (b == '"' && lastByteInString != '\\') {
				inString = false;
			} else {
				lastByteInString = b;
			}
			out.write(b);
		} else if (b == '"') {
			inString = true;
			if (beginObjectOrList) {
				writeNewLineWithIndent();
				beginObjectOrList = false;
			}
			out.write(b);
		} else if (b == ',') {
			out.write(',');
			writeNewLineWithIndent();
		} else if (b == ':') {
			out.write(':');
			out.write(' ');
		} else if (b == '{' || b == '[') {
			if (beginObjectOrList) {
				writeNewLineWithIndent();
			}
			beginObjectOrList = true;
			currentIndent += indentLength;
			out.write(b);
		} else if (b == '}' || b == ']') {
			currentIndent -= indentLength;
			if (beginObjectOrList) {
				beginObjectOrList = false;
			} else {
				writeNewLineWithIndent();
			}
			out.write(b);
		} else if (!WHITESPACE[b]) {
			if (beginObjectOrList) {
				writeNewLineWithIndent();
				beginObjectOrList = false;
			}
			out.write(b);
		}
	}

	private void writeNewLineWithIndent() throws IOException {
		final int size = currentIndent + 1;
		if (size < INDENT_CACHE_SIZE) {
			out.write(indentType.cache, 0, size);
		} else {
			final byte[] cache = indentType.cache;
			out.write(cache);
			int remaining = size - INDENT_CACHE_SIZE;
			while (true) {
				if (remaining < INDENT_CACHE_SIZE) {
					out.write(cache, 1, remaining);
					break;
				} else {
					out.write(cache, 1, INDENT_CACHE_SIZE - 1);
					remaining -= INDENT_CACHE_SIZE - 1;
				}
			}
		}
	}
}
