package com.dslplatform.json;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

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
		this.halfLength = length() / 2;
	}

	public static int readFully(final byte[] buffer, final InputStream stream, final int offset) throws IOException {
		int read;
		int position = offset;
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
			if (position == len) {
				super.reset(length() - currentIndex);
			} else {
				super.reset(position);
			}
		}
		return super.read();
	}

	boolean isEndOfStream() throws IOException {
		if (length() != currentIndex) {
			return false;
		}
		final int len = buffer.length - currentIndex;
		System.arraycopy(buffer, currentIndex, buffer, 0, len);
		int position = readFully(buffer, stream, len);
		if (position == len) {
			return true;
		}
		super.reset(position);
		return super.isEndOfStream();
	}

	public byte[] readBase64() throws IOException {
		if (Base64.findEnd(buffer, currentIndex) == buffer.length) {
			final String input = readString();
			return DatatypeConverter.parseBase64Binary(input);
		}
		return super.readBase64();
	}
}
