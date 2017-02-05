package com.dslplatform.json;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

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
	protected long currentPosition = 0;
	private byte last = ' ';

	private int length;
	private final char[] tmp;

	public final TContext context;
	protected final byte[] buffer;

	protected char[] chars;

	private final StringCache keyCache;
	private final StringCache valuesCache;

	protected JsonReader(final char[] tmp, final byte[] buffer, final int length, final TContext context, final StringCache keyCache, final StringCache valuesCache) {
		this.tmp = tmp;
		this.buffer = buffer;
		this.length = length;
		this.context = context;
		this.chars = tmp;
		this.keyCache = keyCache;
		this.valuesCache = valuesCache;
	}

	/**
	 * Prefer creating reader through DslJson#newReader since it will pass several arguments (such as key/string value cache)
	 * First byte will not be read.
	 * It will allocate new char[64] for string buffer.
	 * Key and string vales cache will be null.
	 *
	 * @param buffer input JSON
	 * @param context context
	 */
	public JsonReader(final byte[] buffer, final TContext context) {
		this(new char[64], buffer, buffer.length, context, null, null);
	}

	public JsonReader(final byte[] buffer, final TContext context, StringCache keyCache, StringCache valuesCache) {
		this(new char[64], buffer, buffer.length, context, keyCache, valuesCache);
	}

	public JsonReader(final byte[] buffer, final TContext context, final char[] tmp) {
		this(tmp, buffer, buffer.length, context, null, null);
		if (tmp == null) {
			throw new IllegalArgumentException("tmp buffer provided as null.");
		}
	}

	public JsonReader(final byte[] buffer, final int length, final TContext context) {
		this(buffer, length, context, new char[64]);
	}

	public JsonReader(final byte[] buffer, final int length, final TContext context, final char[] tmp) {
		this(buffer, length, context, tmp, null, null);
	}

	public JsonReader(final byte[] buffer, final int length, final TContext context, final char[] tmp, final StringCache keyCache, final StringCache valuesCache) {
		this(tmp, buffer, length, context, keyCache, valuesCache);
		if (tmp == null) {
			throw new IllegalArgumentException("tmp buffer provided as null.");
		}
		if (length > buffer.length) {
			throw new IllegalArgumentException("length can't be longer than buffer.length");
		} else if (length < buffer.length) {
			buffer[length] = '\0';
		}
	}

	/**
	 * Valid length of the input buffer.
	 *
	 * @return size of JSON input
	 */
	public final int length() {
		return length;
	}

	void reset(int newLength) {
		currentIndex = 0;
		this.length = newLength;
	}

	private final static Charset UTF_8 = Charset.forName("UTF-8");

	@Override
	public String toString() {
		return new String(buffer, 0, length, UTF_8);
	}

	/**
	 * Read next byte from the JSON input.
	 * If buffer has been read in full IOException will be thrown
	 *
	 * @return next byte
	 * @throws IOException when end of JSON input
	 */
	public byte read() throws IOException {
		if (currentIndex >= length) {
			throw new IOException("Unexpected end of JSON input");
		}
		return last = buffer[currentIndex++];
	}

	boolean isEndOfStream() throws IOException {
		return length == currentIndex;
	}

	/**
	 * Which was last byte read from the JSON input.
	 * JsonReader doesn't allow to go back, but it remembers previously read byte
	 *
	 * @return which was the last byte read
	 */
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

	/**
	 * will be removed. not used anymore
	 *
	 * @return parsed chars from a number
	 */
	@Deprecated
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
		while (ci < length) {
			bb = buffer[ci++];
			if (bb == ',' || bb == '}' || bb == ']') break;
			i++;
		}
		currentIndex += i - 1;
		last = bb;
		return tokenStart;
	}

	final char[] prepareBuffer(final int start) {
		final int remaining = length - start;
		while (chars.length < remaining) {
			chars = Arrays.copyOf(chars, chars.length * 2);
		}
		final char[] _tmp = chars;
		final byte[] _buf = buffer;
		for (int i = 0; i < remaining; i++) {
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

	/**
	 * Read simple ascii string. Will not use values cache to create instance.
	 *
	 * @return parsed string
	 * @throws IOException unable to parse string
	 */
	public final String readSimpleString() throws IOException {
		if (last != '"') {
			throw new IOException("Expecting '\"' at position " + positionInStream() + ". Found " + (char) last);
		}
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

	/**
	 * Read simple "ascii string" into temporary buffer.
	 * String length must be obtained through getTokenStart and getCurrentToken
	 *
	 * @return temporary buffer
	 * @throws IOException unable to parse string
	 */
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

	/**
	 * Read string from JSON input.
	 * If values cache is used, string will be looked up from the cache.
	 * <p>
	 * String value must start and end with a double quote (").
	 *
	 * @return parsed string
	 * @throws IOException error reading string input
	 */
	public final String readString() throws IOException {
		final int len = parseString();
		return valuesCache == null ? new String(chars, 0, len) : valuesCache.get(chars, len);
	}

	final int parseString() throws IOException {
		final int startIndex = currentIndex;
		if (last != '"') {
			//TODO: count special chars in separate counter
			throw new IOException("JSON string must start with a double quote at: " + positionInStream());
		}

		byte bb;
		int ci = currentIndex;
		char[] _tmp = chars;
		try {
			for (int i = 0; i < _tmp.length; i++) {
				bb = buffer[ci++];
				if (bb == '"') {
					currentIndex = ci;
					return i;
				}
				// If we encounter a backslash, which is a beginning of an escape sequence
				// or a high bit was set - indicating an UTF-8 encoded multibyte character,
				// there is no chance that we can decode the string without instantiating
				// a temporary buffer, so quit this loop
				if ((bb ^ '\\') < 1) break;
				_tmp[i] = (char) bb;
			}
			if (ci >= length) {
				throw new IOException("JSON string was not closed with a double quote at: " + (currentPosition + length));
			}
		} catch (ArrayIndexOutOfBoundsException ignore) {
			if (length != buffer.length) {
				throw new IOException("JSON string was not closed with a double quote at: " + (currentPosition + length));
			}
			ci--;
			_tmp = chars = Arrays.copyOf(chars, chars.length * 2);
		}
		int _tmpLen = _tmp.length;
		currentIndex = ci;
		int soFar = --currentIndex - startIndex;

		while (!isEndOfStream()) {
			int bc = read();
			if (bc == '"') {
				return soFar;
			}

			if (bc == '\\') {
				if (soFar >= _tmpLen - 6) {
					_tmp = chars = Arrays.copyOf(chars, chars.length * 2);
					_tmpLen = _tmp.length;
				}
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
				if (soFar >= _tmpLen - 4) {
					_tmp = chars = Arrays.copyOf(chars, chars.length * 2);
					_tmpLen = _tmp.length;
				}
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
							if (bc >= 0x110000) {
								throw new IOException("Invalid unicode character detected at: " + positionInStream());
							}

							// split surrogates
							final int sup = bc - 0x10000;
							_tmp[soFar++] = (char) ((sup >>> 10) + 0xd800);
							_tmp[soFar++] = (char) ((sup & 0x3ff) + 0xdc00);
						}
					}
				}
			} else if (soFar >= _tmpLen) {
				_tmp = chars = Arrays.copyOf(chars, chars.length * 2);
				_tmpLen = _tmp.length;
			}

			_tmp[soFar++] = (char) bc;
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

	/**
	 * Read next token (byte) from input JSON.
	 * Whitespace will be skipped and next non-whitespace byte will be returned.
	 *
	 * @return next non-whitespace byte in the JSON input
	 * @throws IOException unable to get next byte (end of stream, ...)
	 */
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
		return new String(buffer, tokenStart, nameEnd - tokenStart - 1, "UTF-8");
	}

	private byte skipString() throws IOException {
		byte c = read();
		byte prev = c;
		boolean inEscape = false;
		while (c != '"' || inEscape) {
			prev = c;
			inEscape = !inEscape && prev == '\\';
			c = read();
		}
		return getNextToken();
	}

	/**
	 * Skip to next non-whitespace token (byte)
	 * Will not allocate memory while skipping over JSON input.
	 *
	 * @return next non-whitespace byte
	 * @throws IOException unable to read next byte (end of stream, invalid JSON, ...)
	 */
	public final byte skip() throws IOException {
		if (last == '"') return skipString();
		if (last == '{') {
			byte nextToken = getNextToken();
			if (nextToken == '}') return getNextToken();
			if (nextToken == '"') {
				nextToken = skipString();
			} else {
				throw new IOException("Expecting '\"' at position " + positionInStream() + ". Found " + (char) nextToken);
			}
			if (nextToken != ':') {
				throw new IOException("Expecting ':' at position " + positionInStream() + ". Found " + (char) nextToken);
			}
			getNextToken();
			nextToken = skip();
			while (nextToken == ',') {
				nextToken = getNextToken();
				if (nextToken == '"') {
					nextToken = skipString();
				} else {
					throw new IOException("Expecting '\"' at position " + positionInStream() + ". Found " + (char) nextToken);
				}
				if (nextToken != ':') {
					throw new IOException("Expecting ':' at position " + positionInStream() + ". Found " + (char) nextToken);
				}
				getNextToken();
				nextToken = skip();
			}
			if (nextToken != '}') {
				throw new IOException("Expecting '}' at position " + positionInStream() + ". Found " + (char) nextToken);
			}
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

	/**
	 * will be removed
	 *
	 * @return not used anymore
	 * @throws IOException throws if invalid JSON detected
	 */
	@Deprecated
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
	 * Read key value of JSON input.
	 * If key cache is used, it will be looked up from there.
	 *
	 * @return parsed key value
	 * @throws IOException unable to parse string input
	 */
	public String readKey() throws IOException {
		final int len = parseString();
		final String key = keyCache != null ? keyCache.get(chars, len) : new String(chars, 0, len);
		if (getNextToken() != ':') {
			throw new IOException("Expecting ':' at position " + positionInStream() + ". Found " + (char) last);
		}
		getNextToken();
		return key;
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

	/**
	 * Checks if 'null' value is at current position.
	 * This means last read byte was 'n' and 'ull' are next three bytes.
	 * If last byte was n but next three are not 'ull' it will throw since that is not a valid JSON construct.
	 *
	 * @return true if 'null' value is at current position
	 * @throws IOException invalid 'null' value detected
	 */
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

	/**
	 * Checks if 'true' value is at current position.
	 * This means last read byte was 't' and 'rue' are next three bytes.
	 * If last byte was t but next three are not 'rue' it will throw since that is not a valid JSON construct.
	 *
	 * @return true if 'true' value is at current position
	 * @throws IOException invalid 'true' value detected
	 */
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

	/**
	 * Checks if 'false' value is at current position.
	 * This means last read byte was 'f' and 'alse' are next four bytes.
	 * If last byte was f but next four are not 'alse' it will throw since that is not a valid JSON construct.
	 *
	 * @return true if 'false' value is at current position
	 * @throws IOException invalid 'false' value detected
	 */
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
			if (currentIndex >= length)
				throw new IOException("Unexpected end of JSON in collection at: " + positionInStream());
			else throw new IOException("Expecting ']' at position " + positionInStream() + ". Found " + (char) last);
		}
	}

	public final <T, S extends T> ArrayList<T> deserializeCollection(final ReadObject<S> readObject) throws IOException {
		final ArrayList<T> res = new ArrayList<T>(4);
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

	public final <T, S extends T> ArrayList<T> deserializeNullableCollection(final ReadObject<S> readObject) throws IOException {
		final ArrayList<T> res = new ArrayList<T>(4);
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

	public final <T extends JsonObject> ArrayList<T> deserializeCollection(final ReadJsonObject<T> readObject) throws IOException {
		final ArrayList<T> res = new ArrayList<T>(4);
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

	public final <T extends JsonObject> ArrayList<T> deserializeNullableCollection(final ReadJsonObject<T> readObject) throws IOException {
		final ArrayList<T> res = new ArrayList<T>(4);
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
