package com.dslplatform.json;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public abstract class NumberConverter {

	private final static int[] DIGITS = new int[1000];
	private final static double[] POW_10 = new double[18];
	static final JsonReader.ReadObject<Double> DoubleReader = new JsonReader.ReadObject<Double>() {
		@Override
		public Double read(JsonReader reader) throws IOException {
			return deserializeDouble(reader);
		}
	};
	static final JsonWriter.WriteObject<Double> DoubleWriter = new JsonWriter.WriteObject<Double>() {
		@Override
		public void write(JsonWriter writer, Double value) {
			serializeNullable(value, writer);
		}
	};
	static final JsonReader.ReadObject<Float> FloatReader = new JsonReader.ReadObject<Float>() {
		@Override
		public Float read(JsonReader reader) throws IOException {
			return deserializeFloat(reader);
		}
	};
	static final JsonWriter.WriteObject<Float> FloatWriter = new JsonWriter.WriteObject<Float>() {
		@Override
		public void write(JsonWriter writer, Float value) {
			serializeNullable(value, writer);
		}
	};
	static final JsonReader.ReadObject<Integer> IntReader = new JsonReader.ReadObject<Integer>() {
		@Override
		public Integer read(JsonReader reader) throws IOException {
			return deserializeInt(reader);
		}
	};
	static final JsonWriter.WriteObject<Integer> IntWriter = new JsonWriter.WriteObject<Integer>() {
		@Override
		public void write(JsonWriter writer, Integer value) {
			serializeNullable(value, writer);
		}
	};
	static final JsonReader.ReadObject<Long> LongReader = new JsonReader.ReadObject<Long>() {
		@Override
		public Long read(JsonReader reader) throws IOException {
			return deserializeLong(reader);
		}
	};
	static final JsonWriter.WriteObject<Long> LongWriter = new JsonWriter.WriteObject<Long>() {
		@Override
		public void write(JsonWriter writer, Long value) {
			serializeNullable(value, writer);
		}
	};
	static final JsonReader.ReadObject<BigDecimal> DecimalReader = new JsonReader.ReadObject<BigDecimal>() {
		@Override
		public BigDecimal read(JsonReader reader) throws IOException {
			return deserializeDecimal(reader);
		}
	};
	static final JsonWriter.WriteObject<BigDecimal> DecimalWriter = new JsonWriter.WriteObject<BigDecimal>() {
		@Override
		public void write(JsonWriter writer, BigDecimal value) {
			serializeNullable(value, writer);
		}
	};
	static final JsonReader.ReadObject<Number> NumberReader = new JsonReader.ReadObject<Number>() {
		@Override
		public Number read(JsonReader reader) throws IOException {
			return deserializeNumber(reader);
		}
	};

	static {
		for (int i = 0; i < 1000; i++) {
			DIGITS[i] = (i < 10 ? (2 << 24) : i < 100 ? (1 << 24) : 0)
					+ (((i / 100) + '0') << 16)
					+ ((((i / 10) % 10) + '0') << 8)
					+ i % 10 + '0';
		}
		long tenPow = 1;
		for (int i = 0; i < POW_10.length; i++) {
			POW_10[i] = tenPow;
			tenPow = tenPow * 10;
		}
	}

	static void write4(final int value, final byte[] buf, final int pos) {
		if (value > 9999) {
			throw new IllegalArgumentException("Only 4 digits numbers are supported. Provided: " + value);
		}
		final int q = value / 1000;
		final int v = DIGITS[value - q * 1000];
		buf[pos] = (byte) (q + '0');
		buf[pos + 1] = (byte) (v >> 16);
		buf[pos + 2] = (byte) (v >> 8);
		buf[pos + 3] = (byte) v;
	}

	static void write3(final int number, final byte[] buf, int pos) {
		final int v = DIGITS[number];
		buf[pos] = (byte) (v >> 16);
		buf[pos + 1] = (byte) (v >> 8);
		buf[pos + 2] = (byte) v;
	}

	static void write2(final int value, final byte[] buf, final int pos) {
		final int v = DIGITS[value];
		buf[pos] = (byte) (v >> 8);
		buf[pos + 1] = (byte) v;
	}

	static int read2(final char[] buf, final int pos) {
		final int v1 = buf[pos] - 48;
		return (v1 << 3) + (v1 << 1) + buf[pos + 1] - 48;
	}

	static int read4(final char[] buf, final int pos) {
		final int v2 = buf[pos + 1] - 48;
		final int v3 = buf[pos + 2] - 48;
		return (buf[pos] - 48) * 1000
				+ (v2 << 6) + (v2 << 5) + (v2 << 2)
				+ (v3 << 3) + (v3 << 1)
				+ buf[pos + 3] - 48;
	}

	public static void serializeNullable(final Double value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serialize(value, sw);
		}
	}

	private static BigDecimal parseNumberGeneric(final char[] buf, final int len, final JsonReader reader) throws IOException {
		int end = len;
		while (end > 0 && Character.isWhitespace(buf[end - 1])) {
			end--;
		}
		try {
			return new BigDecimal(buf, 0, end);
		} catch (NumberFormatException nfe) {
			throw new IOException("Error parsing number at position: " + reader.positionInStream(len), nfe);
		}
	}

	public static void serialize(final double value, final JsonWriter sw) {
		if (value == Double.POSITIVE_INFINITY) {
			sw.writeAscii("\"Infinity\"");
		} else if (value == Double.NEGATIVE_INFINITY) {
			sw.writeAscii("\"-Infinity\"");
		} else if (value != value) {
			sw.writeAscii("\"NaN\"");
		} else {
			sw.writeAscii(Double.toString(value));//TODO: better implementation required
		}
	}

	public static void serialize(final double[] value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else if (value.length == 0) {
			sw.writeAscii("[]");
		} else {
			sw.writeByte(JsonWriter.ARRAY_START);
			serialize(value[0], sw);
			for (int i = 1; i < value.length; i++) {
				sw.writeByte(JsonWriter.COMMA);
				serialize(value[i], sw);
			}
			sw.writeByte(JsonWriter.ARRAY_END);
		}
	}

	private static class NumberInfo {
		final char[] buffer;
		final int length;

		NumberInfo(final char[] buffer, final int length) {
			this.buffer = buffer;
			this.length = length;
		}
	}

	private static NumberInfo readLongNumber(final JsonReader reader, final int start) throws IOException {
		char[] tmp = reader.prepareBuffer(start);
		int i = reader.length() - start;
		while (!reader.isEndOfStream()) {
			while (i < tmp.length) {
				final char ch = (char) reader.read();
				tmp[i++] = ch;
				if (reader.isEndOfStream() || !(ch >= '0' && ch < '9' || ch == '-' || ch == '+' || ch == '.' || ch == 'e' || ch == 'E')) {
					return new NumberInfo(tmp, reader.isEndOfStream() ? i : i - 1);
				}
			}
			tmp = Arrays.copyOf(tmp, tmp.length * 2);
		}
		return new NumberInfo(tmp, i);
	}

	public static double deserializeDouble(final JsonReader reader) throws IOException {
		if (reader.last() == '"') {
			final int position = reader.getCurrentIndex();
			final char[] buf = reader.readSimpleQuote();
			return parseDoubleGeneric(buf, reader.getCurrentIndex() - position - 1, reader);
		}
		final int start = reader.scanNumber();
		final int end = reader.getCurrentIndex();
		final int len = end - start;
		if (len > 18) {
			if (end == reader.length()) {
				final NumberInfo tmp = readLongNumber(reader, start);
				return parseDoubleGeneric(tmp.buffer, tmp.length, reader);
			}
			return parseDoubleGeneric(reader.prepareBuffer(start), len, reader);
		}
		final byte[] buf = reader.buffer;
		final byte ch = buf[start];
		if (ch == '-') {
			return parseNegativeDouble(buf, reader, start, end, start + 1);
		} else if (ch == '+') {
			return parsePositiveDouble(buf, reader, start, end, start + 1);
		}
		return parsePositiveDouble(buf, reader, start, end, start);
	}

	private static double parsePositiveDouble(final byte[] buf, final JsonReader reader, final int start, final int end, int i) throws IOException {
		long value = 0;
		byte ch = ' ';
		for (; i < end; i++) {
			ch = buf[i];
			if (ch == '.') break;
			final int ind = buf[i] - 48;
			value = (value << 3) + (value << 1) + ind;
			if (ind < 0 || ind > 9) {
				return parseDoubleGeneric(reader.prepareBuffer(start), end - start, reader);
			}
		}
		if (i == end) return value;
		else if (ch == '.') {
			i++;
			long div = 1;
			for (; i < end; i++) {
				final int ind = buf[i] - 48;
				div = (div << 3) + (div << 1);
				value = (value << 3) + (value << 1) + ind;
				if (ind < 0 || ind > 9) {
					return parseDoubleGeneric(reader.prepareBuffer(start), end - start, reader);
				}
			}
			return value / (double) div;
		}
		return value;
	}

	private static double parseNegativeDouble(final byte[] buf, final JsonReader reader, final int start, final int end, int i) throws IOException {
		long value = 0;
		byte ch = ' ';
		for (; i < end; i++) {
			ch = buf[i];
			if (ch == '.') break;
			final int ind = buf[i] - 48;
			value = (value << 3) + (value << 1) - ind;
			if (ind < 0 || ind > 9) {
				return parseDoubleGeneric(reader.prepareBuffer(start), end - start, reader);
			}
		}
		if (i == end) return value;
		else if (ch == '.') {
			i++;
			long div = 1;
			for (; i < end; i++) {
				final int ind = buf[i] - 48;
				div = (div << 3) + (div << 1);
				value = (value << 3) + (value << 1) - ind;
				if (ind < 0 || ind > 9) {
					return parseDoubleGeneric(reader.prepareBuffer(start), end - start, reader);
				}
			}
			return value / (double) div;
		}
		return value;
	}

	private static double parseDoubleGeneric(final char[] buf, final int len, final JsonReader reader) throws IOException {
		int end = len;
		while (end > 0 && Character.isWhitespace(buf[end - 1])) {
			end--;
		}
		try {
			return Double.parseDouble(new String(buf, 0, end));
		} catch (NumberFormatException nfe) {
			throw new IOException("Error parsing float number at position: " + reader.positionInStream(len), nfe);
		}
	}

	public static ArrayList<Double> deserializeDoubleCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(DoubleReader);
	}

	public static void deserializeDoubleCollection(final JsonReader reader, final Collection<Double> res) throws IOException {
		reader.deserializeCollection(DoubleReader, res);
	}

	public static ArrayList<Double> deserializeDoubleNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(DoubleReader);
	}

	public static void deserializeDoubleNullableCollection(final JsonReader reader, final Collection<Double> res) throws IOException {
		reader.deserializeNullableCollection(DoubleReader, res);
	}

	public static void serializeNullable(final Float value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serialize(value.floatValue(), sw);
		}
	}

	public static void serialize(final float value, final JsonWriter sw) {
		if (value == Float.POSITIVE_INFINITY) {
			sw.writeAscii("\"Infinity\"");
		} else if (value == Float.NEGATIVE_INFINITY) {
			sw.writeAscii("\"-Infinity\"");
		} else if (value != value) {
			sw.writeAscii("\"NaN\"");
		} else {
			sw.writeAscii(Float.toString(value));//TODO: better implementation required
		}
	}

	public static void serialize(final float[] value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else if (value.length == 0) {
			sw.writeAscii("[]");
		} else {
			sw.writeByte(JsonWriter.ARRAY_START);
			serialize(value[0], sw);
			for (int i = 1; i < value.length; i++) {
				sw.writeByte(JsonWriter.COMMA);
				serialize(value[i], sw);
			}
			sw.writeByte(JsonWriter.ARRAY_END);
		}
	}

	public static float deserializeFloat(final JsonReader reader) throws IOException {
		if (reader.last() == '"') {
			final int position = reader.getCurrentIndex();
			final char[] buf = reader.readSimpleQuote();
			return parseFloatGeneric(buf, reader.getCurrentIndex() - position - 1, reader);
		}
		final int start = reader.scanNumber();
		final int end = reader.getCurrentIndex();
		final int len = end - start;
		if (len > 18) {
			if (end == reader.length()) {
				final NumberInfo tmp = readLongNumber(reader, start);
				return parseFloatGeneric(tmp.buffer, tmp.length, reader);
			} else {
				return parseFloatGeneric(reader.prepareBuffer(start), len, reader);
			}
		}
		final byte[] buf = reader.buffer;
		final byte ch = buf[start];
		if (ch == '-') {
			return parseNegativeFloat(buf, reader, start, end, start + 1);
		} else if (ch == '+') {
			return parsePositiveFloat(buf, reader, start, end, start + 1);
		}
		return parsePositiveFloat(buf, reader, start, end, start);
	}

	private static float parsePositiveFloat(final byte[] buf, final JsonReader reader, final int start, final int end, int i) throws IOException {
		long value = 0;
		byte ch = ' ';
		for (; i < end; i++) {
			ch = buf[i];
			if (ch == '.') break;
			final int ind = buf[i] - 48;
			value = (value << 3) + (value << 1) + ind;
			if (ind < 0 || ind > 9) {
				return parseFloatGeneric(reader.prepareBuffer(start), end - start, reader);
			}
		}
		if (ch == '.') {
			i++;
			int div = 1;
			for (; i < end; i++) {
				final int ind = buf[i] - 48;
				div = (div << 3) + (div << 1);
				value = (value << 3) + (value << 1) + ind;
				if (ind < 0 || ind > 9) {
					return parseFloatGeneric(reader.prepareBuffer(start), end - start, reader);
				}
			}
			return value / (float) div;
		}
		return value;
	}

	private static float parseNegativeFloat(final byte[] buf, final JsonReader reader, final int start, final int end, int i) throws IOException {
		long value = 0;
		byte ch = ' ';
		for (; i < end; i++) {
			ch = buf[i];
			if (ch == '.') break;
			final int ind = buf[i] - 48;
			value = (value << 3) + (value << 1) - ind;
			if (ind < 0 || ind > 9) {
				return parseFloatGeneric(reader.prepareBuffer(start), end - start, reader);
			}
		}
		if (ch == '.') {
			i++;
			int div = 1;
			for (; i < end; i++) {
				final int ind = buf[i] - 48;
				div = (div << 3) + (div << 1);
				value = (value << 3) + (value << 1) - ind;
				if (ind < 0 || ind > 9) {
					return parseFloatGeneric(reader.prepareBuffer(start), end - start, reader);
				}
			}
			return value / (float) div;
		}
		return value;
	}

	private static float parseFloatGeneric(final char[] buf, final int len, final JsonReader reader) throws IOException {
		int end = len;
		while (end > 0 && Character.isWhitespace(buf[end - 1])) {
			end--;
		}
		try {
			return Float.parseFloat(new String(buf, 0, end));
		} catch (NumberFormatException nfe) {
			throw new IOException("Error parsing float number at position: " + reader.positionInStream(len), nfe);
		}
	}

	public static ArrayList<Float> deserializeFloatCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(FloatReader);
	}

	public static void deserializeFloatCollection(final JsonReader reader, Collection<Float> res) throws IOException {
		reader.deserializeCollection(FloatReader, res);
	}

	public static ArrayList<Float> deserializeFloatNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(FloatReader);
	}

	public static void deserializeFloatNullableCollection(final JsonReader reader, final Collection<Float> res) throws IOException {
		reader.deserializeNullableCollection(FloatReader, res);
	}

	public static void serializeNullable(final Integer value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serialize(value.intValue(), sw);
		}
	}

	private static final byte MINUS = '-';
	private static final byte[] MIN_INT = "-2147483648".getBytes();

	public static void serialize(final int value, final JsonWriter sw) {
		final byte[] buf = sw.ensureCapacity(11);
		final int position = sw.size();
		int current = serialize(buf, position, value);
		sw.advance(current - position);
	}

	private static int serialize(final byte[] buf, int pos, final int value) {
		int i;
		if (value < 0) {
			if (value == Integer.MIN_VALUE) {
				for (int x = 0; x < MIN_INT.length; x++) {
					buf[pos + x] = MIN_INT[x];
				}
				return pos + MIN_INT.length;
			}
			i = -value;
			buf[pos++] = MINUS;
		} else {
			i = value;
		}
		final int q1 = i / 1000;
		if (q1 == 0) {
			pos += writeFirstBuf(buf, DIGITS[i], pos);
			return pos;
		}
		final int r1 = i - q1 * 1000;
		final int q2 = q1 / 1000;
		if (q2 == 0) {
			final int v1 = DIGITS[r1];
			final int v2 = DIGITS[q1];
			int off = writeFirstBuf(buf, v2, pos);
			writeBuf(buf, v1, pos + off);
			return pos + 3 + off;
		}
		final int r2 = q1 - q2 * 1000;
		final long q3 = q2 / 1000;
		final int v1 = DIGITS[r1];
		final int v2 = DIGITS[r2];
		if (q3 == 0) {
			pos += writeFirstBuf(buf, DIGITS[q2], pos);
		} else {
			final int r3 = (int) (q2 - q3 * 1000);
			buf[pos++] = (byte) (q3 + '0');
			writeBuf(buf, DIGITS[r3], pos);
			pos += 3;
		}
		writeBuf(buf, v2, pos);
		writeBuf(buf, v1, pos + 3);
		return pos + 6;
	}

	public static void serialize(final int[] values, final JsonWriter sw) {
		if (values == null) {
			sw.writeNull();
		} else if (values.length == 0) {
			sw.writeAscii("[]");
		} else {
			final byte[] buf = sw.ensureCapacity(values.length * 11 + 2);
			int position = sw.size();
			buf[position++] = '[';
			position = serialize(buf, position, values[0]);
			for (int i = 1; i < values.length; i++) {
				buf[position++] = ',';
				position = serialize(buf, position, values[i]);
			}
			buf[position++] = ']';
			sw.advance(position - sw.size());
		}
	}

	public static void serialize(final short[] value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else if (value.length == 0) {
			sw.writeAscii("[]");
		} else {
			sw.writeByte(JsonWriter.ARRAY_START);
			serialize(value[0], sw);
			for (int i = 1; i < value.length; i++) {
				sw.writeByte(JsonWriter.COMMA);
				serialize(value[i], sw);
			}
			sw.writeByte(JsonWriter.ARRAY_END);
		}
	}

	public static int deserializeInt(final JsonReader reader) throws IOException {
		if (reader.last() == '"') {
			final int position = reader.getCurrentIndex();
			final char[] buf = reader.readSimpleQuote();
			try {
				return parseNumberGeneric(buf, reader.getCurrentIndex() - position - 1, reader).intValueExact();
			} catch (ArithmeticException ignore) {
				throw new IOException("Integer overflow detected at position: " + (reader.currentPosition + position));
			}
		}
		final int start = reader.scanNumber();
		final int end = reader.getCurrentIndex();
		final byte[] buf = reader.buffer;
		final byte ch = buf[start];
		if (ch == '-') {
			return parseNegativeInt(buf, reader, start, end, start + 1);
		} else if (ch == '+') {
			return parsePositiveInt(buf, reader, start, end, start + 1);
		}
		return parsePositiveInt(buf, reader, start, end, start);
	}

	private static int parsePositiveInt(final byte[] buf, final JsonReader reader, final int start, final int end, int i) throws IOException {
		int value = 0;
		for (; i < end; i++) {
			final int ind = buf[i] - 48;
			if (ind < 0 || ind > 9) {
				if (reader.allWhitespace(i, end)) return value;
				BigDecimal v = parseNumberGeneric(reader.prepareBuffer(start), end - start, reader);
				if (v.scale() <= 0) return v.intValue();
				throw new IOException("Error parsing int number at position: " + reader.positionInStream(end - start) + ". Found decimal value: " + v);
			}
			value = (value << 3) + (value << 1) + ind;
			if (value < 0) {
				throw new IOException("Integer overflow detected at position: " + reader.positionInStream(end - start));
			}
		}
		return value;
	}

	private static int parseNegativeInt(final byte[] buf, final JsonReader reader, final int start, final int end, int i) throws IOException {
		int value = 0;
		for (; i < end; i++) {
			final int ind = buf[i] - 48;
			if (ind < 0 || ind > 9) {
				if (reader.allWhitespace(i, end)) return value;
				BigDecimal v = parseNumberGeneric(reader.prepareBuffer(start), end - start, reader);
				if (v.scale() <= 0) return v.intValue();
				throw new IOException("Error parsing int number at position: " + reader.positionInStream(end - start) + ". Found decimal value: " + v);
			}
			value = (value << 3) + (value << 1) - ind;
			if (value > 0) {
				throw new IOException("Integer overflow detected at position: " + reader.positionInStream(end - start));
			}
		}
		return value;
	}

	public static ArrayList<Integer> deserializeIntCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(IntReader);
	}

	public static int[] deserializeIntArray(final JsonReader reader) throws IOException {
		if (reader.last() == ']') {
			return new int[0];
		}
		int[] buffer = new int[4];
		buffer[0] = deserializeInt(reader);
		int i = 1;
		while (reader.getNextToken() == ',') {
			reader.getNextToken();
			if (i == buffer.length) {
				buffer = Arrays.copyOf(buffer, buffer.length << 1);
			}
			buffer[i++] = deserializeInt(reader);
		}
		reader.checkArrayEnd();
		return Arrays.copyOf(buffer, i);
	}

	public static long[] deserializeLongArray(final JsonReader reader) throws IOException {
		if (reader.last() == ']') {
			return new long[0];
		}
		long[] buffer = new long[4];
		buffer[0] = deserializeLong(reader);
		int i = 1;
		while (reader.getNextToken() == ',') {
			reader.getNextToken();
			if (i == buffer.length) {
				buffer = Arrays.copyOf(buffer, buffer.length << 1);
			}
			buffer[i++] = deserializeLong(reader);
		}
		reader.checkArrayEnd();
		return Arrays.copyOf(buffer, i);
	}

	public static float[] deserializeFloatArray(final JsonReader reader) throws IOException {
		if (reader.last() == ']') {
			return new float[0];
		}
		float[] buffer = new float[4];
		buffer[0] = deserializeFloat(reader);
		int i = 1;
		while (reader.getNextToken() == ',') {
			reader.getNextToken();
			if (i == buffer.length) {
				buffer = Arrays.copyOf(buffer, buffer.length << 1);
			}
			buffer[i++] = deserializeFloat(reader);
		}
		reader.checkArrayEnd();
		return Arrays.copyOf(buffer, i);
	}

	public static double[] deserializeDoubleArray(final JsonReader reader) throws IOException {
		if (reader.last() == ']') {
			return new double[0];
		}
		double[] buffer = new double[4];
		buffer[0] = deserializeDouble(reader);
		int i = 1;
		while (reader.getNextToken() == ',') {
			reader.getNextToken();
			if (i == buffer.length) {
				buffer = Arrays.copyOf(buffer, buffer.length << 1);
			}
			buffer[i++] = deserializeDouble(reader);
		}
		reader.checkArrayEnd();
		return Arrays.copyOf(buffer, i);
	}

	public static void deserializeIntCollection(final JsonReader reader, final Collection<Integer> res) throws IOException {
		reader.deserializeCollection(IntReader, res);
	}

	public static ArrayList<Integer> deserializeIntNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(IntReader);
	}

	public static void deserializeIntNullableCollection(final JsonReader reader, final Collection<Integer> res) throws IOException {
		reader.deserializeNullableCollection(IntReader, res);
	}

	public static void serializeNullable(final Long value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serialize(value.longValue(), sw);
		}
	}

	private static int writeFirstBuf(final byte[] buf, final int v, int pos) {
		final int start = v >> 24;
		if (start == 0) {
			buf[pos++] = (byte) (v >> 16);
			buf[pos++] = (byte) (v >> 8);
		} else if (start == 1) {
			buf[pos++] = (byte) (v >> 8);
		}
		buf[pos] = (byte) v;
		return 3 - start;
	}

	private static void writeBuf(final byte[] buf, final int v, int pos) {
		buf[pos] = (byte) (v >> 16);
		buf[pos + 1] = (byte) (v >> 8);
		buf[pos + 2] = (byte) v;
	}

	private static final byte[] MIN_LONG = "-9223372036854775808".getBytes();

	public static void serialize(final long value, final JsonWriter sw) {
		final byte[] buf = sw.ensureCapacity(21);
		final int position = sw.size();
		int current = serialize(buf, position, value);
		sw.advance(current - position);
	}

	private static int serialize(final byte[] buf, int pos, final long value) {
		long i;
		if (value < 0) {
			if (value == Long.MIN_VALUE) {
				for (int x = 0; x < MIN_LONG.length; x++) {
					buf[pos + x] = MIN_LONG[x];
				}
				return pos + MIN_LONG.length;
			}
			i = -value;
			buf[pos++] = MINUS;
		} else {
			i = value;
		}
		final long q1 = i / 1000;
		if (q1 == 0) {
			pos += writeFirstBuf(buf, DIGITS[(int) i], pos);
			return pos;
		}
		final int r1 = (int) (i - q1 * 1000);
		final long q2 = q1 / 1000;
		if (q2 == 0) {
			final int v1 = DIGITS[r1];
			final int v2 = DIGITS[(int) q1];
			int off = writeFirstBuf(buf, v2, pos);
			writeBuf(buf, v1, pos + off);
			return pos + 3 + off;
		}
		final int r2 = (int) (q1 - q2 * 1000);
		final long q3 = q2 / 1000;
		if (q3 == 0) {
			final int v1 = DIGITS[r1];
			final int v2 = DIGITS[r2];
			final int v3 = DIGITS[(int) q2];
			pos += writeFirstBuf(buf, v3, pos);
			writeBuf(buf, v2, pos);
			writeBuf(buf, v1, pos + 3);
			return pos + 6;
		}
		final int r3 = (int) (q2 - q3 * 1000);
		final int q4 = (int) (q3 / 1000);
		if (q4 == 0) {
			final int v1 = DIGITS[r1];
			final int v2 = DIGITS[r2];
			final int v3 = DIGITS[r3];
			final int v4 = DIGITS[(int) q3];
			pos += writeFirstBuf(buf, v4, pos);
			writeBuf(buf, v3, pos);
			writeBuf(buf, v2, pos + 3);
			writeBuf(buf, v1, pos + 6);
			return pos + 9;
		}
		final int r4 = (int) (q3 - q4 * 1000);
		final int q5 = q4 / 1000;
		if (q5 == 0) {
			final int v1 = DIGITS[r1];
			final int v2 = DIGITS[r2];
			final int v3 = DIGITS[r3];
			final int v4 = DIGITS[r4];
			final int v5 = DIGITS[q4];
			pos += writeFirstBuf(buf, v5, pos);
			writeBuf(buf, v4, pos);
			writeBuf(buf, v3, pos + 3);
			writeBuf(buf, v2, pos + 6);
			writeBuf(buf, v1, pos + 9);
			return pos + 12;
		}
		final int r5 = q4 - q5 * 1000;
		final int q6 = q5 / 1000;
		final int v1 = DIGITS[r1];
		final int v2 = DIGITS[r2];
		final int v3 = DIGITS[r3];
		final int v4 = DIGITS[r4];
		final int v5 = DIGITS[r5];
		if (q6 == 0) {
			pos += writeFirstBuf(buf, DIGITS[q5], pos);
		} else {
			final int r6 = q5 - q6 * 1000;
			buf[pos++] = (byte) (q6 + '0');
			writeBuf(buf, DIGITS[r6], pos);
			pos += 3;
		}
		writeBuf(buf, v5, pos);
		writeBuf(buf, v4, pos + 3);
		writeBuf(buf, v3, pos + 6);
		writeBuf(buf, v2, pos + 9);
		writeBuf(buf, v1, pos + 12);
		return pos + 15;
	}

	public static void serialize(final long[] values, final JsonWriter sw) {
		if (values == null) {
			sw.writeNull();
		} else if (values.length == 0) {
			sw.writeAscii("[]");
		} else {
			final byte[] buf = sw.ensureCapacity(values.length * 21 + 2);
			int position = sw.size();
			buf[position++] = '[';
			position = serialize(buf, position, values[0]);
			for (int i = 1; i < values.length; i++) {
				buf[position++] = ',';
				position = serialize(buf, position, values[i]);
			}
			buf[position++] = ']';
			sw.advance(position - sw.size());
		}
	}

	public static long deserializeLong(final JsonReader reader) throws IOException {
		if (reader.last() == '"') {
			final int position = reader.getCurrentIndex();
			final char[] buf = reader.readSimpleQuote();
			try {
				return parseNumberGeneric(buf, reader.getCurrentIndex() - position - 1, reader).longValueExact();
			} catch (ArithmeticException ignore) {
				throw new IOException("Long overflow detected at position: " + (reader.currentPosition + position));
			}
		}
		final int start = reader.scanNumber();
		final int end = reader.getCurrentIndex();
		final byte[] buf = reader.buffer;
		final byte ch = buf[start];
		int i = start;
		long value = 0;
		if (ch == '-') {
			i = start + 1;
			for (; i < end; i++) {
				final int ind = buf[i] - 48;
				if (ind < 0 || ind > 9) {
					if (reader.allWhitespace(i, end)) return value;
					return parseLongGeneric(reader, start, end);
				}
				value = (value << 3) + (value << 1) - ind;
				if (value > 0) {
					throw new IOException("Long overflow detected at position: " + reader.positionInStream(end - start));
				}
			}
			return value;
		} else if (ch == '+') {
			i = start + 1;
		}
		for (; i < end; i++) {
			final int ind = buf[i] - 48;
			if (ind < 0 || ind > 9) {
				if (reader.allWhitespace(i, end)) return value;
				return parseLongGeneric(reader, start, end);
			}
			value = (value << 3) + (value << 1) + ind;
			if (value < 0) {
				throw new IOException("Long overflow detected at position: " + reader.positionInStream(end - start));
			}
		}
		return value;
	}

	private static long parseLongGeneric(final JsonReader reader, final int start, final int end) throws IOException {
		BigDecimal v = parseNumberGeneric(reader.prepareBuffer(start), end - start, reader);
		if (v.scale() <= 0) return v.longValue();
		throw new IOException("Error parsing long number at position: " + reader.positionInStream(end - start) + ". Found decimal value: " + v);
	}

	public static ArrayList<Long> deserializeLongCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(LongReader);
	}

	public static void deserializeLongCollection(final JsonReader reader, final Collection<Long> res) throws IOException {
		reader.deserializeCollection(LongReader, res);
	}

	public static ArrayList<Long> deserializeLongNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(LongReader);
	}

	public static void deserializeLongNullableCollection(final JsonReader reader, final Collection<Long> res) throws IOException {
		reader.deserializeNullableCollection(LongReader, res);
	}

	public static void serializeNullable(final BigDecimal value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			sw.writeAscii(value.toString());
		}
	}

	public static void serialize(final BigDecimal value, final JsonWriter sw) {
		sw.writeAscii(value.toString());
	}

	public static BigDecimal deserializeDecimal(final JsonReader reader) throws IOException {
		if (reader.last() == '"') {
			final int len = reader.parseString();
			return parseNumberGeneric(reader.chars, len, reader);
		}
		final int start = reader.scanNumber();
		int end = reader.getCurrentIndex();
		int len = end - start;
		if (len > 18) {
			end = reader.findNonWhitespace(end);
			len = end - start;
			if (end == reader.length()) {
				final NumberInfo info = readLongNumber(reader, start);
				return parseNumberGeneric(info.buffer, info.length, reader);
			} else if (len > 18) {
				return parseNumberGeneric(reader.prepareBuffer(start), len, reader);
			}
		}
		final byte[] buf = reader.buffer;
		final byte ch = buf[start];
		if (ch == '-') {
			return parseNegativeDecimal(buf, reader, start, end);
		} else if (ch == '+') {
			return parsePositiveDecimal(buf, reader, start, end, start + 1);
		}
		return parsePositiveDecimal(buf, reader, start, end, start);
	}

	private static BigDecimal parsePositiveDecimal(final byte[] buf, final JsonReader reader, final int start, final int end, int i) throws IOException {
		long value = 0;
		byte ch = ' ';
		for (; i < end; i++) {
			ch = buf[i];
			if (ch == '.' || ch == 'e' || ch == 'E') break;
			final int ind = ch - 48;
			value = (value << 3) + (value << 1) + ind;
			if (ind < 0 || ind > 9) {
				return parseNumberGeneric(reader.prepareBuffer(start), end - start, reader);
			}
		}
		if (i == end) return BigDecimal.valueOf(value);
		else if (ch == '.') {
			i++;
			int dp = i;
			for (; i < end; i++) {
				ch = buf[i];
				if (ch == 'e' || ch == 'E') break;
				final int ind = ch - 48;
				value = (value << 3) + (value << 1) + ind;
				if (ind < 0 || ind > 9) {
					return parseNumberGeneric(reader.prepareBuffer(start), end - start, reader);
				}
			}
			if (i == end) return BigDecimal.valueOf(value, end - dp);
			else if (ch == 'e' || ch == 'E') {
				final int ep = i;
				i++;
				ch = buf[i];
				final int exp;
				if (ch == '-') {
					exp = parseNegativeInt(buf, reader, start, end, i + 1);
				} else if (ch == '+') {
					exp = parsePositiveInt(buf, reader, start, end, i + 1);
				} else {
					exp = parsePositiveInt(buf, reader, start, end, i);
				}
				return BigDecimal.valueOf(value, ep - dp - exp);
			}
			return BigDecimal.valueOf(value, end - dp);
		} else if (ch == 'e' || ch == 'E') {
			i++;
			ch = buf[i];
			final int exp;
			if (ch == '-') {
				exp = parseNegativeInt(buf, reader, start, end, i + 1);
			} else if (ch == '+') {
				exp = parsePositiveInt(buf, reader, start, end, i + 1);
			} else {
				exp = parsePositiveInt(buf, reader, start, end, i);
			}
			return BigDecimal.valueOf(value, -exp);
		}
		return BigDecimal.valueOf(value);
	}

	private static BigDecimal parseNegativeDecimal(final byte[] buf, final JsonReader reader, final int start, final int end) throws IOException {
		long value = 0;
		byte ch = ' ';
		int i = start + 1;
		for (; i < end; i++) {
			ch = buf[i];
			if (ch == '.' || ch == 'e' || ch == 'E') break;
			final int ind = ch - 48;
			if (ind < 0 || ind > 9) {
				if (reader.allWhitespace(i, end)) return BigDecimal.valueOf(value);
				return parseNumberGeneric(reader.prepareBuffer(start), end - start, reader);
			}
			value = (value << 3) + (value << 1) - ind;
		}
		if (i == end) return BigDecimal.valueOf(value);
		else if (ch == '.') {
			i++;
			int dp = i;
			for (; i < end; i++) {
				ch = buf[i];
				if (ch == 'e' || ch == 'E') break;
				final int ind = ch - 48;
				value = (value << 3) + (value << 1) - ind;
				if (ind < 0 || ind > 9) {
					return parseNumberGeneric(reader.prepareBuffer(start), end - start, reader);
				}
			}
			if (i == end) return BigDecimal.valueOf(value, end - dp);
			else if (ch == 'e' || ch == 'E') {
				final int ep = i;
				i++;
				ch = buf[i];
				final int exp;
				if (ch == '-') {
					exp = parseNegativeInt(buf, reader, start, end, i + 1);
				} else if (ch == '+') {
					exp = parsePositiveInt(buf, reader, start, end, i + 1);
				} else {
					exp = parsePositiveInt(buf, reader, start, end, i);
				}
				return BigDecimal.valueOf(value, ep - dp - exp);
			}
			return BigDecimal.valueOf(value, end - dp);
		} else if (ch == 'e' || ch == 'E') {
			i++;
			ch = buf[i];
			final int exp;
			if (ch == '-') {
				exp = parseNegativeInt(buf, reader, start, end, i + 1);
			} else if (ch == '+') {
				exp = parsePositiveInt(buf, reader, start, end, i + 1);
			} else {
				exp = parsePositiveInt(buf, reader, start, end, i);
			}
			return BigDecimal.valueOf(value, -exp);
		}
		return BigDecimal.valueOf(value);
	}

	private static final BigDecimal BD_MAX_LONG = BigDecimal.valueOf(Long.MAX_VALUE);
	private static final BigDecimal BD_MIN_LONG = BigDecimal.valueOf(Long.MIN_VALUE);

	private static Number tryLongFromBigDecimal(final BigDecimal num) {
		if (num.scale() == 0 && num.precision() <= 19) {
			if (num.signum() == 1) {
				if (num.compareTo(BD_MAX_LONG) <= 0) {
					return num.longValue();
				}
			} else if (num.compareTo(BD_MIN_LONG) >= 0) {
				return num.longValue();
			}
		}
		return num;
	}

	public static Number deserializeNumber(final JsonReader reader) throws IOException {
		final int start = reader.scanNumber();
		int end = reader.getCurrentIndex();
		int len = end - start;
		if (len > 18) {
			end = reader.findNonWhitespace(end);
			len = end - start;
			if (end == reader.length()) {
				final NumberInfo tmp = readLongNumber(reader, start);
				return tryLongFromBigDecimal(parseNumberGeneric(tmp.buffer, tmp.length, reader));
			} else if (len > 18) {
				return tryLongFromBigDecimal(parseNumberGeneric(reader.prepareBuffer(start), len, reader));
			}
		}
		final byte[] buf = reader.buffer;
		final byte ch = buf[start];
		if (ch == '-') {
			return parseNegativeNumber(buf, reader, start, end);
		} else if (ch == '+') {
			return parsePositiveNumber(buf, reader, start, end, start + 1);
		}
		return parsePositiveNumber(buf, reader, start, end, start);
	}

	private static Number parsePositiveNumber(final byte[] buf, final JsonReader reader, final int start, final int end, int i) throws IOException {
		long value = 0;
		byte ch = ' ';
		for (; i < end; i++) {
			ch = buf[i];
			if (ch == '.' || ch == 'e' || ch == 'E') break;
			final int ind = ch - 48;
			if (ind < 0 || ind > 9) {
				if (reader.allWhitespace(i, end)) return value;
				return parseNumberGeneric(reader.prepareBuffer(start), end - start, reader);
			}
			value = (value << 3) + (value << 1) + ind;
		}
		if (i == end) return value;
		else if (ch == '.') {
			i++;
			int dp = i;
			for (; i < end; i++) {
				ch = buf[i];
				if (ch == 'e' || ch == 'E') break;
				final int ind = ch - 48;
				value = (value << 3) + (value << 1) + ind;
				if (ind < 0 || ind > 9) {
					return parseNumberGeneric(reader.prepareBuffer(start), end - start, reader);
				}
			}
			if (i == end) return BigDecimal.valueOf(value, end - dp);
			else if (ch == 'e' || ch == 'E') {
				final int ep = i;
				i++;
				ch = buf[i];
				final int exp;
				if (ch == '-') {
					exp = parseNegativeInt(buf, reader, start, end, i + 1);
				} else if (ch == '+') {
					exp = parsePositiveInt(buf, reader, start, end, i + 1);
				} else {
					exp = parsePositiveInt(buf, reader, start, end, i);
				}
				return BigDecimal.valueOf(value, ep - dp - exp);
			}
			return BigDecimal.valueOf(value, end - dp);
		} else if (ch == 'e' || ch == 'E') {
			i++;
			ch = buf[i];
			final int exp;
			if (ch == '-') {
				exp = parseNegativeInt(buf, reader, start, end, i + 1);
			} else if (ch == '+') {
				exp = parsePositiveInt(buf, reader, start, end, i + 1);
			} else {
				exp = parsePositiveInt(buf, reader, start, end, i);
			}
			return BigDecimal.valueOf(value, -exp);
		}
		return BigDecimal.valueOf(value);
	}

	private static Number parseNegativeNumber(final byte[] buf, final JsonReader reader, final int start, final int end) throws IOException {
		long value = 0;
		byte ch = ' ';
		int i = start + 1;
		for (; i < end; i++) {
			ch = buf[i];
			if (ch == '.' || ch == 'e' || ch == 'E') break;
			final int ind = ch - 48;
			if (ind < 0 || ind > 9) {
				if (reader.allWhitespace(i, end)) return value;
				return parseNumberGeneric(reader.prepareBuffer(start), end - start, reader);
			}
			value = (value << 3) + (value << 1) - ind;
		}
		if (i == end) return value;
		else if (ch == '.') {
			i++;
			int dp = i;
			for (; i < end; i++) {
				ch = buf[i];
				if (ch == 'e' || ch == 'E') break;
				final int ind = ch - 48;
				value = (value << 3) + (value << 1) - ind;
				if (ind < 0 || ind > 9) {
					return parseNumberGeneric(reader.prepareBuffer(start), end - start, reader);
				}
			}
			if (i == end) return BigDecimal.valueOf(value, end - dp);
			else if (ch == 'e' || ch == 'E') {
				final int ep = i;
				i++;
				ch = buf[i];
				final int exp;
				if (ch == '-') {
					exp = parseNegativeInt(buf, reader, start, end, i + 1);
				} else if (ch == '+') {
					exp = parsePositiveInt(buf, reader, start, end, i + 1);
				} else {
					exp = parsePositiveInt(buf, reader, start, end, i);
				}
				return BigDecimal.valueOf(value, ep - dp - exp);
			}
			return BigDecimal.valueOf(value, end - dp);
		} else if (ch == 'e' || ch == 'E') {
			i++;
			ch = buf[i];
			final int exp;
			if (ch == '-') {
				exp = parseNegativeInt(buf, reader, start, end, i + 1);
			} else if (ch == '+') {
				exp = parsePositiveInt(buf, reader, start, end, i + 1);
			} else {
				exp = parsePositiveInt(buf, reader, start, end, i);
			}
			return BigDecimal.valueOf(value, -exp);
		}
		return BigDecimal.valueOf(value);
	}

	public static ArrayList<BigDecimal> deserializeDecimalCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(DecimalReader);
	}

	public static void deserializeDecimalCollection(final JsonReader reader, final Collection<BigDecimal> res) throws IOException {
		reader.deserializeCollection(DecimalReader, res);
	}

	public static ArrayList<BigDecimal> deserializeDecimalNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(DecimalReader);
	}

	public static void deserializeDecimalNullableCollection(final JsonReader reader, final Collection<BigDecimal> res) throws IOException {
		reader.deserializeNullableCollection(DecimalReader, res);
	}
}