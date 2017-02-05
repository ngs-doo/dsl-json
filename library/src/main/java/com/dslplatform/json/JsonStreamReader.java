package com.dslplatform.json;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Object for processing JSON from InputStream.
 * DSL-JSON works on byte level (instead of char level).
 * Deserialized instances can obtain TContext information provided with this reader.
 * <p>
 * Stream reader works by partially processing JSON in chunks.
 *
 * @param <TContext> context passed to deserialized object instances
 */
public final class JsonStreamReader<TContext> extends JsonReader<TContext> {

	private InputStream stream;
	private int readLimit;

	/**
	 * Create reusable stream reader.
	 * Prefer creating stream reader through DslJson#newReader since it will pass several arguments (such as key/string value cache)
	 * First chunk will be populated.
	 * First byte will not be read.
	 * It will allocate new char[64] for string buffer
	 *
	 * @param stream input stream to read from
	 * @param buffer buffer to hold chunk of stream
	 * @param context available context
	 * @throws IOException unable to read from stream
	 */
	public JsonStreamReader(final InputStream stream, final byte[] buffer, final TContext context) throws IOException {
		this(stream, buffer, context, null, null);
	}

	JsonStreamReader(final InputStream stream, final byte[] buffer, final TContext context, StringCache keyCache, StringCache valuesCache) throws IOException {
		super(new char[64], buffer, readFully(buffer, stream, 0), context, keyCache, valuesCache);
		if (stream == null) {
			throw new IllegalArgumentException("stream provided as null.");
		}
		if (buffer == null) {
			throw new IllegalArgumentException("buffer provided as null.");
		}
		this.stream = stream;
		this.readLimit = length() >> 1;
	}

	/**
	 * Populate buffer until the end of buffer or stream.
	 *
	 * @param buffer buffer to fill
	 * @param stream stream to read
	 * @param offset populate buffer after specified offset
	 * @return how many bytes have been read
	 * @throws IOException unable to read from stream
	 */
	public static int readFully(final byte[] buffer, final InputStream stream, final int offset) throws IOException {
		int read;
		int position = offset;
		while (position < buffer.length
				&& (read = stream.read(buffer, position, buffer.length - position)) != -1) {
			position += read;
		}
		return position;
	}

	/**
	 * Prepare JsonStreamReader for processing another stream.
	 * First chunk will be populated.
	 * First byte will not be read.
	 *
	 * @param stream new stream to process
	 * @throws IOException unable to read from stream
	 */
	public void reset(InputStream stream) throws IOException {
		if (stream == null) {
			throw new IllegalArgumentException("stream provided as null.");
		}
		final int available = readFully(buffer, stream, 0);
		readLimit = available >> 1;
		currentPosition = 0;
		reset(available);
	}

	/**
	 * Read next byte from JSON input (buffer/stream).
	 * If buffer has been processed, fill the buffer with next chunk;
	 * otherwise get next byte from the buffer
	 *
	 * @return next byte to process
	 * @throws IOException unable to read from stream
	 */
	public byte read() throws IOException {
		if (currentIndex > readLimit) {
			final int len = buffer.length - currentIndex;
			System.arraycopy(buffer, currentIndex, buffer, 0, len);
			final int available = readFully(buffer, stream, len);
			currentPosition += currentIndex;
			if (available == len) {
				readLimit = length() - currentIndex;
				reset(readLimit);
			} else {
				readLimit = available >> 1;
				reset(available);
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
		currentPosition += currentIndex;
		reset(position);
		return position == 0;
	}

	public byte[] readBase64() throws IOException {
		if (Base64.findEnd(buffer, currentIndex) == buffer.length) {
			final int len = parseString();
			return DatatypeConverter.parseBase64Binary(new String(chars, 0, len));
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

	public <T> Iterator<T> iterateOver(final JsonReader.ReadObject<T> reader) throws IOException {
		return new StreamWithReader<T>(reader, this);
	}

	public <T extends JsonObject> Iterator<T> iterateOver(final JsonReader.ReadJsonObject<T> reader) throws IOException {
		return new StreamWithObjectReader<T>(reader, this);
	}

	private class StreamWithReader<T> implements Iterator<T> {
		private final JsonReader.ReadObject<T> reader;
		private final JsonStreamReader json;

		private boolean hasNext;

		StreamWithReader(
				JsonReader.ReadObject<T> reader,
				JsonStreamReader json) {
			this.reader = reader;
			this.json = json;
			hasNext = true;
		}

		@Override
		public boolean hasNext() {
			return hasNext;
		}

		@Override
		public void remove() {
		}

		@Override
		public T next() {
			try {
				byte nextToken = json.last();
				final T instance;
				if (nextToken == 'n') {
					if (json.wasNull()) {
						instance = null;
					} else {
						throw json.expecting("null");
					}
				} else {
					instance = reader.read(json);
				}
				hasNext = json.getNextToken() == ',';
				if (hasNext) {
					json.getNextToken();
				} else {
					if (json.last() != ']') {
						throw json.expecting("]");
					}
				}
				return instance;
			} catch (IOException e) {
				throw new SerializationException(e);
			}
		}
	}

	private static class StreamWithObjectReader<T extends JsonObject> implements Iterator<T> {
		private final JsonReader.ReadJsonObject<T> reader;
		private final JsonStreamReader json;

		private boolean hasNext;

		StreamWithObjectReader(
				JsonReader.ReadJsonObject<T> reader,
				JsonStreamReader json) {
			this.reader = reader;
			this.json = json;
			hasNext = true;
		}

		@Override
		public boolean hasNext() {
			return hasNext;
		}

		@Override
		public void remove() {
		}

		@Override
		public T next() {
			try {
				byte nextToken = json.last();
				final T instance;
				if (nextToken == 'n') {
					if (json.wasNull()) {
						instance = null;
					} else {
						throw json.expecting("null");
					}
				} else if (nextToken == '{') {
					json.getNextToken();
					instance = reader.deserialize(json);
				} else {
					throw json.expecting("{");
				}
				hasNext = json.getNextToken() == ',';
				if (hasNext) {
					json.getNextToken();
				} else {
					if (json.last() != ']') {
						throw json.expecting("]");
					}
				}
				return instance;
			} catch (IOException e) {
				throw new SerializationException(e);
			}
		}
	}
}
