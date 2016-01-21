package com.dslplatform.json;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;

public class JsonStreamReader<TContext> extends JsonReader<TContext> {

	private InputStream stream;
	private int halfLength;

	public JsonStreamReader(final InputStream stream, final byte[] buffer, final TContext context) throws IOException {
		super(new char[64], buffer, readFully(buffer, stream, 0), context);
		if (stream == null) {
			throw new NullPointerException("stream provided as null.");
		}
		if (buffer == null) {
			throw new NullPointerException("buffer provided as null.");
		}
		this.stream = stream;
		this.halfLength = length / 2;
	}

	public static int readFully(final byte[] buffer, final InputStream stream, final int offset) throws IOException {
		int read = stream.read(buffer, offset, buffer.length - offset);
		int position = read + offset;
		while (position < buffer.length
				&& (read = stream.read(buffer, position, buffer.length - position)) != -1) {
			position += read;
		}
		return position;
	}

	public void reset(int newLength) {
		halfLength = newLength / 2;
		super.reset(newLength);
	}

	public byte read() throws IOException {
		if (currentIndex > halfLength) {
			final int len = buffer.length - currentIndex;
			System.arraycopy(buffer, currentIndex, buffer, 0, len);
			int position = readFully(buffer, stream, len);
			reset(position);
		}
		return super.read();
	}

	boolean isEndOfStream() throws IOException {
		if (length != currentIndex) {
			return false;
		}
		final int len = buffer.length - currentIndex;
		System.arraycopy(buffer, currentIndex, buffer, 0, len);
		int position = readFully(buffer, stream, len);
		reset(position);
		return super.isEndOfStream();
	}

	protected String readLargeString(final int startIndex) throws IOException {

		int soFar = --currentIndex - startIndex;
		char[] chars = new char[soFar + 256];

		for (int i = soFar - 1; i >= 0; i--) {
			chars[i] = (char) buffer[startIndex + i];
		}

		while (!isEndOfStream()) {
			int bc = read();
			if (bc == '"') {
				return new String(chars, 0, soFar);
			}

			// if we're running out of space, double the buffer capacity
			if (soFar >= chars.length - 3) {
				final char[] newChars = new char[chars.length << 1];
				System.arraycopy(chars, 0, newChars, 0, soFar);
				chars = newChars;
			}

			if (bc == '\\') {
				bc = buffer[currentIndex++];

				switch (bc) {
					case 'b':
						bc = '\b';
						break;
					case 't':
						bc = '\t';
						break;
					case 'n':
						bc = '\n';
						break;
					case 'f':
						bc = '\f';
						break;
					case 'r':
						bc = '\r';
						break;
					case '"':
					case '/':
					case '\\':
						break;
					case 'u':
						bc = (hexToInt(buffer[currentIndex++]) << 12) +
								(hexToInt(buffer[currentIndex++]) << 8) +
								(hexToInt(buffer[currentIndex++]) << 4) +
								hexToInt(buffer[currentIndex++]);
						break;

					default:
						throw new IOException("Could not parse String, got invalid escape combination '\\" + bc + "'");
				}
			} else if ((bc & 0x80) != 0) {
				final int u2 = buffer[currentIndex++];
				if ((bc & 0xE0) == 0xC0) {
					bc = ((bc & 0x1F) << 6) + (u2 & 0x3F);
				} else {
					final int u3 = buffer[currentIndex++];
					if ((bc & 0xF0) == 0xE0) {
						bc = ((bc & 0x0F) << 12) + ((u2 & 0x3F) << 6) + (u3 & 0x3F);
					} else {
						final int u4 = buffer[currentIndex++];
						if ((bc & 0xF8) == 0xF0) {
							bc = ((bc & 0x07) << 18) + ((u2 & 0x3F) << 12) + ((u3 & 0x3F) << 6) + (u4 & 0x3F);
						} else {
							// there are legal 5 & 6 byte combinations, but none are _valid_
							throw new IOException();
						}

						if (bc >= 0x10000) {
							// check if valid unicode
							if (bc >= 0x110000) throw new IOException();

							// split surrogates
							final int sup = bc - 0x10000;
							chars[soFar++] = (char) ((sup >>> 10) + 0xd800);
							chars[soFar++] = (char) ((sup & 0x3ff) + 0xdc00);
						}
					}
				}
			}

			chars[soFar++] = (char) bc;
		}
		throw new IOException("JSON string was not closed with a double quote!");
	}

	public byte[] readBase64() throws IOException {
		if (Base64.findEnd(buffer, currentIndex) == buffer.length) {
			final String input = readString();
			return DatatypeConverter.parseBase64Binary(input);
		}
		return super.readBase64();
	}
}
