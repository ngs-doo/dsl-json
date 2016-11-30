package com.dslplatform.json;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.eclipse.collections.impl.list.mutable.FastList;

/**
 * Object for processing JSON from byte[].
 * DSL-JSON works on byte level (instead of char level).
 * Deserialized instances can obtain TContext information provided with this reader.
 *
 * @param <TContext> context passed to deserialized object instances
 */
public class JsonReader<TContext> {

	private static final boolean[] WHITESPACE = new boolean[256];

	static {
		WHITESPACE[9 + 128] = true;
		WHITESPACE[10 + 128] = true;
		WHITESPACE[11 + 128] = true;
		WHITESPACE[12 + 128] = true;
		WHITESPACE[13 + 128] = true;
		WHITESPACE[32 + 128] = true;
		WHITESPACE[-96 + 128] = true;
		WHITESPACE[-31 + 128] = true;
		WHITESPACE[-30 + 128] = true;
		WHITESPACE[-29 + 128] = true;
	}

	private int tokenStart;
	private int nameEnd;
	protected int currentIndex = 0;
	private long currentPosition = 0;
	private byte last = ' ';

	private int length;
	private final char[] tmp;
	final int tmpLength;

	public final TContext context;
	protected final byte[] buffer;

	protected char[] chars;

	protected JsonReader(final char[] tmp, final byte[] buffer, final int length, final TContext context) {
		this.tmp = tmp;
		this.tmpLength = tmp.length;
		this.buffer = buffer;
		this.length = length;
		this.context = context;
		this.chars = tmp;
	}

	public JsonReader(final byte[] buffer, final TContext context) {
		this(new char[64], buffer, buffer.length, context);
	}

	public JsonReader(final byte[] buffer, final TContext context, final char[] tmp) {
		this(tmp, buffer, buffer.length, context);
		if (tmp == null) {
			throw new NullPointerException("tmp buffer provided as null.");
		}
	}

	public JsonReader(final byte[] buffer, final int length, final TContext context) throws IOException {
		this(buffer, length, context, new char[64]);
	}

	public JsonReader(final byte[] buffer, final int length, final TContext context, final char[] tmp) throws IOException {
		this(tmp, buffer, length, context);
		if (tmp == null) {
			throw new NullPointerException("tmp buffer provided as null.");
		}
		if (length > buffer.length) {
			throw new IOException("length can't be longer than buffer.length");
		} else if (length < buffer.length) {
			buffer[length] = '\0';
		}
	}

	public final int length() {
		return length;
	}

	void reset(int newLength) {
		currentPosition += currentIndex;
		currentIndex = 0;
		this.length = newLength;
	}

	private final static Charset UTF_8 = Charset.forName("UTF-8");

	@Override
	public String toString() {
		return new String(buffer, 0, length, UTF_8);
	}

	public byte read() throws IOException {
		if (currentIndex >= length) {
			throw new IOException("Unexpected end of JSON input");
		}
		return last = buffer[currentIndex++];
	}

	boolean isEndOfStream() throws IOException {
		return length == currentIndex;
	}

	public final byte last() {
		return last;
	}

	final IOException expecting(final String what) {
		return new IOException("Expecting '" + what + "' at position " + positionInStream() + ". Found " + (char) last);
	}

	final IOException expecting(final String what, final byte found) {
		return new IOException("Expecting '" + what + "' at position " + positionInStream() + ". Found " + (char) found);
	}

	public final int getTokenStart() {
		return tokenStart;
	}

	public final int getCurrentIndex() {
		return currentIndex;
	}

	public final char[] readNumber() {
		tokenStart = currentIndex - 1;
		tmp[0] = (char) last;
		int i = 1;
		int ci = currentIndex;
		byte bb = last;
		while (i < tmp.length && ci < length) {
			bb = buffer[ci++];
			if (bb == ',' || bb == '}' || bb == ']') break;
			tmp[i++] = (char) bb;
		}
		currentIndex += i - 1;
		last = bb;
		return tmp;
	}

	public final int scanNumber() {
		tokenStart = currentIndex - 1;
		int i = 1;
		int ci = currentIndex;
		byte bb = last;
		while (i < tmp.length && ci < length) {
			bb = buffer[ci++];
			if (bb == ',' || bb == '}' || bb == ']') break;
			i++;
		}
		currentIndex += i - 1;
		last = bb;
		return tokenStart;
	}

	final char[] prepareBuffer(final int start) {
		final char[] _tmp = tmp;
		final byte[] _buf = buffer;
		final int max = Math.min(_tmp.length, _buf.length - start);
		for (int i = 0; i < max; i++) {
			_tmp[i] = (char) _buf[start + i];
		}
		return _tmp;
	}

	final boolean allWhitespace(final int start, final int end) {
		final byte[] _buf = buffer;
		for (int i = start; i < end; i++) {
			if (!WHITESPACE[_buf[i] + 128]) return false;
		}
		return true;
	}

	final int findNonWhitespace(final int end) {
		final byte[] _buf = buffer;
		for (int i = end - 1; i > 0; i--) {
			if (!WHITESPACE[_buf[i] + 128]) return i + 1;
		}
		return 0;
	}

	public final String readSimpleString() throws IOException {
		if (last != '"')
			throw new IOException("Expecting '\"' at position " + positionInStream() + ". Found " + (char) last);
		int i = 0;
		int ci = currentIndex;
		try {
			while (i < tmp.length) {
				final byte bb = buffer[ci++];
				if (bb == '"') break;
				tmp[i++] = (char) bb;
			}
		} catch (ArrayIndexOutOfBoundsException ignore) {
			throw new IOException("JSON string was not closed with a double quote at: " + positionInStream());
		}
		if (ci > length) {
			throw new IOException("JSON string was not closed with a double quote at: " + (currentPosition + length));
		}
		currentIndex = ci;
		return new String(tmp, 0, i);
	}

	public final char[] readSimpleQuote() throws IOException {
		if (last != '"') {
			throw new IOException("Expecting '\"' at position " + positionInStream() + ". Found " + (char) last);
		}
		int ci = tokenStart = currentIndex;
		try {
			for (int i = 0; i < tmp.length; i++) {
				final byte bb = buffer[ci++];
				if (bb == '"') break;
				tmp[i] = (char) bb;
			}
		} catch (ArrayIndexOutOfBoundsException ignore) {
			throw new IOException("JSON string was not closed with a double quote at: " + positionInStream());
		}
		if (ci > length) {
			throw new IOException("JSON string was not closed with a double quote at: " + (currentPosition + length));
		}
		currentIndex = ci;
		return tmp;
	}

	public final String readString() throws IOException {
		final int startIndex = currentIndex;
		if (last != '"') {
			//TODO: count special chars in separate counter
			throw new IOException("JSON string must start with a double quote at: " + positionInStream());
		}

		byte bb = 0;
		int ci = currentIndex;
		try {
			for (int i = 0; i < chars.length; i++) {
				bb = buffer[ci++];
				if (bb == '"') {
					currentIndex = ci;
					return new String(chars, 0, i);
				}
				// If we encounter a backslash, which is a beginning of an escape sequence
				// or a high bit was set - indicating an UTF-8 encoded multibyte character,
				// there is no chance that we can decode the string without instantiating
				// a temporary buffer, so quit this loop
				if ((bb ^ '\\') < 1) break;
				chars[i] = (char) bb;
			}
		} catch (ArrayIndexOutOfBoundsException ignore) {
			throw new IOException("JSON string was not closed with a double quote at: " + positionInStream());
		}
		if (ci >= length) {
			throw new IOException("JSON string was not closed with a double quote at: " + (currentPosition + length));
		}

		currentIndex = ci;

		int soFar = --currentIndex - startIndex;

		while (!isEndOfStream()) {
			int bc = read();
			if (bc == '"') {
				return new String(chars, 0, soFar);
			}

			if (soFar >= chars.length - 3) {
				chars = Arrays.copyOf(chars, chars.length * 2);
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
						throw new IOException("Could not parse String at position: " + positionInStream() + ". Invalid escape combination detected: '\\" + bc + "'");
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
							throw new IOException("Invalid unicode character detected at: " + positionInStream());
						}

						if (bc >= 0x10000) {
							// check if valid unicode
							if (bc >= 0x110000) throw new IOException("Invalid unicode character detected at: " + positionInStream());

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
		throw new IOException("JSON string was not closed with a double quote at: " + positionInStream());
	}

	private static int hexToInt(final byte value) throws IOException {
		if (value >= '0' && value <= '9') return value - 0x30;
		if (value >= 'A' && value <= 'F') return value - 0x37;
		if (value >= 'a' && value <= 'f') return value - 0x57;
		throw new IOException("Could not parse unicode escape, expected a hexadecimal digit, got '" + value + "'");
	}

	private boolean wasWhiteSpace() {
		switch (last) {
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 32:
			case -96:
				return true;
			case -31:
				if (currentIndex + 1 < length && buffer[currentIndex] == -102 && buffer[currentIndex + 1] == -128) {
					currentIndex += 2;
					last = ' ';
					return true;
				}
				return false;
			case -30:
				if (currentIndex + 1 < length) {
					final byte b1 = buffer[currentIndex];
					final byte b2 = buffer[currentIndex + 1];
					if (b1 == -127 && b2 == -97) {
						currentIndex += 2;
						last = ' ';
						return true;
					}
					if (b1 != -128) return false;
					switch (b2) {
						case -128:
						case -127:
						case -126:
						case -125:
						case -124:
						case -123:
						case -122:
						case -121:
						case -120:
						case -119:
						case -118:
						case -88:
						case -87:
						case -81:
							currentIndex += 2;
							last = ' ';
							return true;
						default:
							return false;
					}
				} else {
					return false;
				}
			case -29:
				if (currentIndex + 1 < length && buffer[currentIndex] == -128 && buffer[currentIndex + 1] == -128) {
					currentIndex += 2;
					last = ' ';
					return true;
				}
				return false;
			default:
				return false;
		}
	}

	public final byte getNextToken() throws IOException {
		read();
		if (WHITESPACE[last + 128]) {
			while (wasWhiteSpace()) {
				read();
			}
		}
		return last;
	}

	public final long positionInStream() {
		return currentPosition + currentIndex;
	}

	public final long positionInStream(final int offset) {
		return currentPosition + currentIndex - offset;
	}

	public final int fillName() throws IOException {
		final int hash = calcHash();
		if (read() != ':') {
			if (!wasWhiteSpace() || getNextToken() != ':') {
				throw new IOException("Expecting ':' at position " + positionInStream() + ". Found " + (char) last);
			}
		}
		return hash;
	}

	public final int calcHash() throws IOException {
		if (last != '"') {
			throw new IOException("Expecting '\"' at position " + positionInStream() + ". Found " + (char) last);
		}
		tokenStart = currentIndex;
		int ci = currentIndex;
		long hash = 0x811c9dc5;
		while (ci < buffer.length) {
			final byte b = buffer[ci++];
			if (b == '"') break;
			hash ^= b;
			hash *= 0x1000193;
		}
		nameEnd = currentIndex = ci;
		return (int) hash;
	}

	public final boolean wasLastName(final String name) {
		if (name.length() != nameEnd - tokenStart - 1) {
			return false;
		}
		for (int i = 0; i < name.length(); i++) {
			if (name.charAt(i) != buffer[tokenStart + i]) {
				return false;
			}
		}
		return true;
	}

	public final String getLastName() throws IOException {
		return new String(buffer, tokenStart, nameEnd - tokenStart - 1, "ISO-8859-1");
	}

	private byte skipString() throws IOException {
		byte c = read();
		byte prev = c;
		while (c != '"' || prev == '\\') {
			prev = c;
			c = read();
		}
		return getNextToken();
	}

	public final byte skip() throws IOException {
		if (last == '"') return skipString();
		if (last == '{') {
			byte nextToken = getNextToken();
			if (nextToken == '}') return getNextToken();
			if (nextToken == '"') nextToken = skipString();
			else
				throw new IOException("Expecting '\"' at position " + positionInStream() + ". Found " + (char) nextToken);
			if (nextToken != ':')
				throw new IOException("Expecting ':' at position " + positionInStream() + ". Found " + (char) nextToken);
			getNextToken();
			nextToken = skip();
			while (nextToken == ',') {
				nextToken = getNextToken();
				if (nextToken == '"') nextToken = skipString();
				else
					throw new IOException("Expecting '\"' at position " + positionInStream() + ". Found " + (char) nextToken);
				if (nextToken != ':')
					throw new IOException("Expecting ':' at position " + positionInStream() + ". Found " + (char) nextToken);
				getNextToken();
				nextToken = skip();
			}
			if (nextToken != '}')
				throw new IOException("Expecting '}' at position " + positionInStream() + ". Found " + (char) nextToken);
			return getNextToken();
		}
		if (last == '[') {
			getNextToken();
			byte nextToken = skip();
			while (nextToken == ',') {
				getNextToken();
				nextToken = skip();
			}
			if (nextToken != ']') {
				throw new IOException("Expecting ']' at position " + positionInStream() + ". Found " + (char) nextToken);
			}
			return getNextToken();
		}
		if (last == 'n') {
			if (!wasNull()) {
				throw new IOException("Expecting 'null' at position " + positionInStream());
			}
			return getNextToken();
		}
		if (last == 't') {
			if (!wasTrue()) {
				throw new IOException("Expecting 'true' at position " + positionInStream());
			}
			return getNextToken();
		}
		if (last == 'f') {
			if (!wasFalse()) {
				throw new IOException("Expecting 'false' at position " + positionInStream());
			}
			return getNextToken();
		}
		while (last != ',' && last != '}' && last != ']') {
			read();
		}
		return last;
	}

	public String readNext() throws IOException {
		final int start = currentIndex - 1;
		skip();
		return new String(buffer, start, currentIndex - start - 1, "UTF-8");
	}

	public byte[] readBase64() throws IOException {
		if (last != '"') {
			throw new IOException("Expecting '\"' at position " + positionInStream() + " at base64 start. Found " + (char) last);
		}
		final int start = currentIndex;
		currentIndex = Base64.findEnd(buffer, start);
		last = buffer[currentIndex++];
		if (last != '"') {
			throw new IOException("Expecting '\"' at position " + positionInStream() + " at base64 end. Found " + (char) last);
		}
		return Base64.decodeFast(buffer, start, currentIndex - 1);
	}

	/**
	 * Custom objects can be deserialized based on the implementation specified through this interface.
	 * Annotation processor creates custom deserializers at compile time and registers them into DslJson.
	 *
	 * @param <T> type
	 */
	public interface ReadObject<T> {
		T read(JsonReader reader) throws IOException;
	}

	public interface ReadJsonObject<T extends JsonObject> {
		T deserialize(JsonReader reader) throws IOException;
	}

	public final boolean wasNull() throws IOException {
		if (last == 'n') {
			if (currentIndex + 2 < length && buffer[currentIndex] == 'u'
					&& buffer[currentIndex + 1] == 'l' && buffer[currentIndex + 2] == 'l') {
				currentIndex += 3;
				last = 'l';
				return true;
			}
			throw new IOException("Invalid null value found at: " + positionInStream());
		}
		return false;
	}

	public final boolean wasTrue() throws IOException {
		if (last == 't') {
			if (currentIndex + 2 < length && buffer[currentIndex] == 'r'
					&& buffer[currentIndex + 1] == 'u' && buffer[currentIndex + 2] == 'e') {
				currentIndex += 3;
				last = 'e';
				return true;
			}
			throw new IOException("Invalid boolean value found at: " + positionInStream());
		}
		return false;
	}

	public final boolean wasFalse() throws IOException {
		if (last == 'f') {
			if (currentIndex + 3 < length && buffer[currentIndex] == 'a'
					&& buffer[currentIndex + 1] == 'l' && buffer[currentIndex + 2] == 's'
					&& buffer[currentIndex + 3] == 'e') {
				currentIndex += 4;
				last = 'e';
				return true;
			}
			throw new IOException("Invalid boolean value found at: " + positionInStream());
		}
		return false;
	}

	public final void checkArrayEnd() throws IOException {
		if (last != ']') {
			if (currentIndex >= length) throw new IOException("Unexpected end of JSON in collection at: " + positionInStream());
			else throw new IOException("Expecting ']' at position " + positionInStream() + ". Found " + (char) last);
		}
	}

	public final <T, S extends T> List<T> deserializeCollection(final ReadObject<S> readObject) throws IOException {
		final List<T> res = FastList.newList(4);
		deserializeCollection(readObject, res);
		return res;
	}

	public final <T, S extends T> void deserializeCollection(final ReadObject<S> readObject, final Collection<T> res) throws IOException {
		res.add(readObject.read(this));
		while (getNextToken() == ',') {
			getNextToken();
			res.add(readObject.read(this));
		}
		checkArrayEnd();
	}

	public final <T, S extends T> List<T> deserializeNullableCollection(final ReadObject<S> readObject) throws IOException {
		final List<T> res = FastList.newList(4);
		deserializeNullableCollection(readObject, res);
		return res;
	}

	public final <T, S extends T> void deserializeNullableCollection(final ReadObject<S> readObject, final Collection<T> res) throws IOException {
		if (wasNull()) {
			res.add(null);
		} else {
			res.add(readObject.read(this));
		}
		while (getNextToken() == ',') {
			getNextToken();
			if (wasNull()) {
				res.add(null);
			} else {
				res.add(readObject.read(this));
			}
		}
		checkArrayEnd();
	}

	public final <T extends JsonObject> List<T> deserializeCollection(final ReadJsonObject<T> readObject) throws IOException {
		final List<T> res = FastList.newList(4);
		deserializeCollection(readObject, res);
		return res;
	}

	public final <T extends JsonObject> void deserializeCollection(final ReadJsonObject<T> readObject, final Collection<T> res) throws IOException {
		if (last == '{') {
			getNextToken();
			res.add(readObject.deserialize(this));
		} else throw new IOException("Expecting '{' at position " + positionInStream() + ". Found " + (char) last);
		while (getNextToken() == ',') {
			if (getNextToken() == '{') {
				getNextToken();
				res.add(readObject.deserialize(this));
			} else throw new IOException("Expecting '{' at position " + positionInStream() + ". Found " + (char) last);
		}
		checkArrayEnd();
	}

	public final <T extends JsonObject> List<T> deserializeNullableCollection(final ReadJsonObject<T> readObject) throws IOException {
		final List<T> res = FastList.newList(4);
		deserializeNullableCollection(readObject, res);
		return res;
	}

	public final <T extends JsonObject> void deserializeNullableCollection(final ReadJsonObject<T> readObject, final Collection<T> res) throws IOException {
		if (last == '{') {
			getNextToken();
			res.add(readObject.deserialize(this));
		} else if (wasNull()) {
			res.add(null);
		} else throw new IOException("Expecting '{' at position " + positionInStream() + ". Found " + (char) last);
		while (getNextToken() == ',') {
			if (getNextToken() == '{') {
				getNextToken();
				res.add(readObject.deserialize(this));
			} else if (wasNull()) {
				res.add(null);
			} else throw new IOException("Expecting '{' at position " + positionInStream() + ". Found " + (char) last);
		}
		checkArrayEnd();
	}
}
