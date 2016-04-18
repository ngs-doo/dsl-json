package com.dslplatform.json;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;

/**
 * Object for processing JSON from InputStream.
 * DSL-JSON works on byte level (instead of char level).
 * Deserialized instances can obtain TContext information provided with this reader.
 * <p>
 * Stream reader works by partially processing JSON in chunks.
 *
 * @param <TContext> context passed to deserialized object instances
 */
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

	private static class RereadStream extends InputStream {
		private final byte[] buffer;
		private final InputStream stream;
		private boolean usingBuffer;
		private int position;

		RereadStream(byte[] buffer, InputStream stream) {
			this.buffer = buffer;
			this.stream = stream;
			usingBuffer = true;
		}

		@Override
		public int read() throws IOException {
			if (usingBuffer) {
				if (position < buffer.length) {
					return buffer[position++];
				} else usingBuffer = false;
			}
			return stream.read();
		}

		@Override
		public int read(byte[] buf) throws IOException {
			if (usingBuffer) {
				return super.read(buf);
			}
			return stream.read(buf);
		}

		@Override
		public int read(byte[] buf, int off, int len) throws IOException {
			if (usingBuffer) {
				return super.read(buf, off, len);
			}
			return stream.read(buf, off, len);
		}
	}

	InputStream streamFromStart() throws IOException {
		return new RereadStream(this.buffer, this.stream);
	}
}
