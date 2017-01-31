package com.dslplatform.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.*;

/**
 * DslJson writes JSON into an byte[] target.
 * This class is used for growing such byte[] buffer
 * and providing other low level methods for JSON serialization.
 * <p>
 * After the processing is done, JSON can be copied to target OutputStream or resulting byte[] can be used directly.
 * <p>
 * For maximum performance JsonWriter instances should be reused.
 * They should not be shared across threads (concurrently) so for Thread reuse it's best to use patterns such as ThreadLocal.
 */
public final class JsonWriter extends Writer {

	private static final Charset UTF_8 = Charset.forName("UTF-8");

	final byte[] ensureCapacity(final int free) {
		if (position + free >= result.length) {
			result = Arrays.copyOf(result, result.length + (result.length << 1) + free);
		}
		return result;
	}

	void advance(int size) {
		position += size;
	}

	private int position;
	private byte[] result;

	private final UnknownSerializer unknownSerializer;

	/**
	 * Prefer creating JsonWriter through DslJson#newWriter
	 * This instance is safe to use when all type information is known and lookups to custom writers is not required.
	 */
	@Deprecated
	public JsonWriter() {
		this(512, null);
	}

	JsonWriter(final UnknownSerializer unknownSerializer) {
		this(512, unknownSerializer);
	}

	JsonWriter(final int size, final UnknownSerializer unknownSerializer) {
		this(new byte[size], unknownSerializer);
	}

	JsonWriter(final byte[] result, final UnknownSerializer unknownSerializer) {
		this.result = result;
		this.unknownSerializer = unknownSerializer;
	}

	public static final byte OBJECT_START = '{';
	public static final byte OBJECT_END = '}';
	public static final byte ARRAY_START = '[';
	public static final byte ARRAY_END = ']';
	public static final byte COMMA = ',';
	public static final byte SEMI = ':';
	public static final byte QUOTE = '"';
	public static final byte ESCAPE = '\\';

	public final void writeNull() {
		final int s = position;
		position += 4;
		if (position >= result.length) {
			result = Arrays.copyOf(result, result.length + result.length / 2);
		}
		final byte[] _result = result;
		_result[s] = 'n';
		_result[s + 1] = 'u';
		_result[s + 2] = 'l';
		_result[s + 3] = 'l';
	}

	public final void writeByte(final byte c) {
		if (position == result.length) {
			result = Arrays.copyOf(result, result.length + result.length / 2);
		}
		result[position++] = c;
	}

	public final void writeString(final String str) {
		final int len = str.length();
		if (position + (len << 2) + (len << 1) + 2 >= result.length) {
			result = Arrays.copyOf(result, result.length + result.length / 2 + (len << 2) + (len << 1) + 2);
		}
		final byte[] _result = result;
		_result[position] = QUOTE;
		int cur = position + 1;
		for (int i = 0; i < len; i++) {
			final char c = str.charAt(i);
			if (c > 31 && c != '"' && c != '\\' && c < 126) {
				_result[cur++] = (byte) c;
			} else {
				writeQuotedString(str, i, cur, len);
				return;
			}
		}
		_result[cur] = QUOTE;
		position = cur + 1;
	}

	private void writeQuotedString(final String str, int i, int cur, final int len) {
		final byte[] _result = this.result;
		for (; i < len; i++) {
			final char c = str.charAt(i);
			if (c == '"') {
				_result[cur++] = ESCAPE;
				_result[cur++] = QUOTE;
			} else if (c == '\\') {
				_result[cur++] = ESCAPE;
				_result[cur++] = ESCAPE;
			} else if (c < 32) {
				if (c == 8) {
					_result[cur++] = ESCAPE;
					_result[cur++] = 'b';
				} else if (c == 9) {
					_result[cur++] = ESCAPE;
					_result[cur++] = 't';
				} else if (c == 10) {
					_result[cur++] = ESCAPE;
					_result[cur++] = 'n';
				} else if (c == 12) {
					_result[cur++] = ESCAPE;
					_result[cur++] = 'f';
				} else if (c == 13) {
					_result[cur++] = ESCAPE;
					_result[cur++] = 'r';
				} else {
					_result[cur] = ESCAPE;
					_result[cur + 1] = 'u';
					_result[cur + 2] = '0';
					_result[cur + 3] = '0';
					switch (c) {
						case 0:
							_result[cur + 4] = '0';
							_result[cur + 5] = '0';
							break;
						case 1:
							_result[cur + 4] = '0';
							_result[cur + 5] = '1';
							break;
						case 2:
							_result[cur + 4] = '0';
							_result[cur + 5] = '2';
							break;
						case 3:
							_result[cur + 4] = '0';
							_result[cur + 5] = '3';
							break;
						case 4:
							_result[cur + 4] = '0';
							_result[cur + 5] = '4';
							break;
						case 5:
							_result[cur + 4] = '0';
							_result[cur + 5] = '5';
							break;
						case 6:
							_result[cur + 4] = '0';
							_result[cur + 5] = '6';
							break;
						case 7:
							_result[cur + 4] = '0';
							_result[cur + 5] = '7';
							break;
						case 11:
							_result[cur + 4] = '0';
							_result[cur + 5] = 'B';
							break;
						case 14:
							_result[cur + 4] = '0';
							_result[cur + 5] = 'E';
							break;
						case 15:
							_result[cur + 4] = '0';
							_result[cur + 5] = 'F';
							break;
						case 16:
							_result[cur + 4] = '1';
							_result[cur + 5] = '0';
							break;
						case 17:
							_result[cur + 4] = '1';
							_result[cur + 5] = '1';
							break;
						case 18:
							_result[cur + 4] = '1';
							_result[cur + 5] = '2';
							break;
						case 19:
							_result[cur + 4] = '1';
							_result[cur + 5] = '3';
							break;
						case 20:
							_result[cur + 4] = '1';
							_result[cur + 5] = '4';
							break;
						case 21:
							_result[cur + 4] = '1';
							_result[cur + 5] = '5';
							break;
						case 22:
							_result[cur + 4] = '1';
							_result[cur + 5] = '6';
							break;
						case 23:
							_result[cur + 4] = '1';
							_result[cur + 5] = '7';
							break;
						case 24:
							_result[cur + 4] = '1';
							_result[cur + 5] = '8';
							break;
						case 25:
							_result[cur + 4] = '1';
							_result[cur + 5] = '9';
							break;
						case 26:
							_result[cur + 4] = '1';
							_result[cur + 5] = 'A';
							break;
						case 27:
							_result[cur + 4] = '1';
							_result[cur + 5] = 'B';
							break;
						case 28:
							_result[cur + 4] = '1';
							_result[cur + 5] = 'C';
							break;
						case 29:
							_result[cur + 4] = '1';
							_result[cur + 5] = 'D';
							break;
						case 30:
							_result[cur + 4] = '1';
							_result[cur + 5] = 'E';
							break;
						default:
							_result[cur + 4] = '1';
							_result[cur + 5] = 'F';
							break;
					}
					cur += 6;
				}
			} else if (c < 0x007F) {
				_result[cur++] = (byte) c;
			} else {
				final int cp = str.codePointAt(i);
				if (Character.isSupplementaryCodePoint(cp)) {
					i++;
				}
				if (cp == 0x007F) {
					_result[cur++] = (byte) cp;
				} else if (cp <= 0x7FF) {
					_result[cur++] = (byte) (0xC0 | ((cp >> 6) & 0x1F));
					_result[cur++] = (byte) (0x80 | (cp & 0x3F));
				} else if ((cp < 0xD800) || (cp > 0xDFFF && cp <= 0xFFFD)) {
					_result[cur++] = (byte) (0xE0 | ((cp >> 12) & 0x0F));
					_result[cur++] = (byte) (0x80 | ((cp >> 6) & 0x3F));
					_result[cur++] = (byte) (0x80 | (cp & 0x3F));
				} else if (cp >= 0x10000 && cp <= 0x10FFFF) {
					_result[cur++] = (byte) (0xF0 | ((cp >> 18) & 0x07));
					_result[cur++] = (byte) (0x80 | ((cp >> 12) & 0x3F));
					_result[cur++] = (byte) (0x80 | ((cp >> 6) & 0x3F));
					_result[cur++] = (byte) (0x80 | (cp & 0x3F));
				} else {
					throw new SerializationException("Unknown unicode codepoint in string! " + Integer.toHexString(cp));
				}
			}
		}
		_result[cur] = QUOTE;
		position = cur + 1;
	}

	@SuppressWarnings("deprecation")
	public final void writeAscii(final String str) {
		final int len = str.length();
		if (position + len >= result.length) {
			result = Arrays.copyOf(result, result.length + result.length / 2 + len);
		}
		str.getBytes(0, len, result, position);
		position += len;
	}

	@SuppressWarnings("deprecation")
	public final void writeAscii(final String str, final int len) {
		if (position + len >= result.length) {
			result = Arrays.copyOf(result, result.length + result.length / 2 + len);
		}
		str.getBytes(0, len, result, position);
		position += len;
	}

	public final void writeAscii(final byte[] buf) {
		final int len = buf.length;
		if (position + len >= result.length) {
			result = Arrays.copyOf(result, result.length + result.length / 2 + len);
		}
		final int p = position;
		final byte[] _result = result;
		for (int i = 0; i < buf.length; i++) {
			_result[p + i] = buf[i];
		}
		position += len;
	}

	public final void writeAscii(final byte[] buf, final int len) {
		if (position + len >= result.length) {
			result = Arrays.copyOf(result, result.length + result.length / 2 + len);
		}
		final int p = position;
		final byte[] _result = result;
		for (int i = 0; i < len; i++) {
			_result[p + i] = buf[i];
		}
		position += len;
	}

	public final void writeBinary(final byte[] buf) {
		if (position + (buf.length << 1) + 2 >= result.length) {
			result = Arrays.copyOf(result, result.length + result.length / 2 + (buf.length << 1) + 2);
		}
		result[position++] = '"';
		position += Base64.encodeToBytes(buf, result, position);
		result[position++] = '"';
	}

	@Override
	public String toString() {
		return new String(result, 0, position, UTF_8);
	}

	public final byte[] toByteArray() {
		return Arrays.copyOf(result, position);
	}

	public final void toStream(final OutputStream stream) throws IOException {
		stream.write(result, 0, position);
	}

	public final byte[] getByteBuffer() {
		return result;
	}

	public final int size() {
		return position;
	}

	public final void reset() {
		position = 0;
	}

	@Override
	public void write(int c) throws IOException {
		if (c < 127) {
			writeByte((byte) c);
		} else {
			write(new char[]{(char) c}, 0, 1);
		}
	}

	@Override
	public void write(char[] cbuf, int off, int len) {
		String append = new String(cbuf, off, len);
		writeAscii(append.getBytes(UTF_8));
	}

	@Override
	public void write(String str, int off, int len) {
		String append = str.substring(off, off + len);
		writeAscii(append.getBytes(UTF_8));
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void close() throws IOException {
		position = 0;
	}

	/**
	 * Custom objects can be serialized based on the implementation specified through this interface.
	 * Annotation processor creates custom deserializers at compile time and registers them into DslJson.
	 *
	 * @param <T> type
	 */
	public interface WriteObject<T> {
		void write(JsonWriter writer, T value);
	}

	public <T extends JsonObject> void serialize(final T[] array) {
		writeByte(ARRAY_START);
		if (array.length != 0) {
			array[0].serialize(this, false);
			for (int i = 1; i < array.length; i++) {
				writeByte(COMMA);
				array[i].serialize(this, false);
			}
		}
		writeByte(ARRAY_END);
	}

	public <T extends JsonObject> void serialize(final T[] array, final int len) {
		writeByte(ARRAY_START);
		if (array.length != 0 && len != 0) {
			array[0].serialize(this, false);
			for (int i = 1; i < len; i++) {
				writeByte(COMMA);
				array[i].serialize(this, false);
			}
		}
		writeByte(ARRAY_END);
	}

	public <T extends JsonObject> void serialize(final List<T> list) {
		writeByte(ARRAY_START);
		if (list.size() != 0) {
			list.get(0).serialize(this, false);
			for (int i = 1; i < list.size(); i++) {
				writeByte(COMMA);
				list.get(i).serialize(this, false);
			}
		}
		writeByte(ARRAY_END);
	}

	public <T> void serialize(final T[] array, final WriteObject<T> writer) {
		if (array == null) {
			writeNull();
			return;
		}
		writeByte(ARRAY_START);
		if (array.length != 0) {
			T item = array[0];
			if (item != null) {
				writer.write(this, item);
			} else {
				writeNull();
			}
			for (int i = 1; i < array.length; i++) {
				writeByte(COMMA);
				item = array[i];
				if (item != null) {
					writer.write(this, item);
				} else {
					writeNull();
				}
			}
		}
		writeByte(ARRAY_END);
	}

	public <T> void serialize(final List<T> list, final WriteObject<T> writer) {
		if (list == null) {
			writeNull();
			return;
		}
		writeByte(ARRAY_START);
		if (list.size() != 0) {
			T item = list.get(0);
			if (item != null) {
				writer.write(this, item);
			} else {
				writeNull();
			}
			for (int i = 1; i < list.size(); i++) {
				writeByte(COMMA);
				item = list.get(i);
				if (item != null) {
					writer.write(this, item);
				} else {
					writeNull();
				}
			}
		}
		writeByte(ARRAY_END);
	}

	public <T> void serialize(final Collection<T> collection, final WriteObject<T> writer) {
		if (collection == null) {
			writeNull();
			return;
		}
		writeByte(ARRAY_START);
		if (!collection.isEmpty()) {
			final Iterator<T> it = collection.iterator();
			T item = it.next();
			if (item != null) {
				writer.write(this, item);
			} else {
				writeNull();
			}
			while (it.hasNext()) {
				writeByte(COMMA);
				item = it.next();
				if (item != null) {
					writer.write(this, item);
				} else {
					writeNull();
				}
			}
		}
		writeByte(ARRAY_END);
	}

	public void serializeObject(final Object value) {
		if (value == null) {
			writeNull();
		} else if (unknownSerializer != null) {
			try {
				unknownSerializer.serialize(this, value);
			} catch (IOException ex) { //serializing unknown stuff can fail in various ways ;(
				throw new SerializationException(ex);
			}
		} else {
			throw new SerializationException("Unable to serialize: " + value.getClass() + ".\n" +
					"Check that JsonWriter was created through DslJson#newWriter.");
		}
	}
}
