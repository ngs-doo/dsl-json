package com.dslplatform.json;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public abstract class NumberConverter {

	private final static int[] Digits = new int[100];
	private final static double[] POW_10 = new double[16];
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
		for (int i = 0; i < 100; i++) {
			Digits[i] = (i < 10 ? 1 << 16 : 0) + (((i / 10) + '0') << 8) + i % 10 + '0';
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
		final int q = value / 100;
		final int v1 = Digits[q];
		final int v2 = Digits[value - ((q << 6) + (q << 5) + (q << 2))];
		buf[pos] = (byte) (v1 >> 8);
		buf[pos + 1] = (byte) v1;
		buf[pos + 2] = (byte) (v2 >> 8);
		buf[pos + 3] = (byte) v2;
	}

	static void write2(final int value, final byte[] buf, final int pos) {
		final int v = Digits[value];
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

	private static BigDecimal parseNumberGeneric(final char[] buf, final int len, final int position) throws IOException {
		int end = len;
		while (end > 0 && Character.isWhitespace(buf[end - 1])) {
			end--;
		}
		try {
			return new BigDecimal(buf, 0, end);
		} catch (NumberFormatException nfe) {
			throw new IOException("Error parsing number at position: " + (position - len), nfe);
		}
	}

	public static void serialize(final double value, final JsonWriter sw) {
		if (Double.isNaN(value)) {
			sw.writeAscii("\"NaN\"");
		} else if (Double.isInfinite(value)) {
			final long bits = Double.doubleToLongBits(value);
			if((bits & -9223372036854775808L) != 0L) {
				sw.writeAscii("\"-Infinity\"");
			} else {
				sw.writeAscii("\"Infinity\"");
			}
		} else sw.writeAscii(Double.toString(value));
	}

	public static void serialize(final double[] value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else if (value.length == 0) {
			sw.writeAscii("[]");
		} else {
			sw.writeByte(JsonWriter.ARRAY_START);
			serialize(value[0], sw);
			for(int i = 1; i < value.length; i++) {
				sw.writeByte(JsonWriter.COMMA);
				serialize(value[i], sw);
			}
			sw.writeByte(JsonWriter.ARRAY_END);
		}
	}

	private static class NumberInfo {
		public final char[] buffer;
		public final int length;
		public NumberInfo(final char[] buffer, final int length) {
			this.buffer = buffer;
			this.length = length;
		}
	}

	private static NumberInfo readLongNumber(final JsonReader reader, final char[] buf) throws IOException {
		int i = buf.length;
		char[] tmp = Arrays.copyOf(buf, buf.length * 2);
		while (!reader.isEndOfStream()) {
			do {
				final char ch = (char) reader.read();
				tmp[i++] = ch;
				if (reader.isEndOfStream() || !(ch >= '0' && ch < '9' || ch == '-' || ch == '+' || ch == '.' || ch == 'e' || ch == 'E')) {
					return new NumberInfo(tmp, reader.isEndOfStream() ? i : i - 1);
				}
			} while (i < tmp.length);
			tmp = Arrays.copyOf(tmp, tmp.length * 2);
		}
		return new NumberInfo(tmp, i);
	}

	public static double deserializeDouble(final JsonReader reader) throws IOException {
		if (reader.last() == '"') {
			final int position = reader.getCurrentIndex();
			final char[] buf = reader.readSimpleQuote();
			return parseDoubleGeneric(buf, reader.getCurrentIndex() - position - 1, position + 1);
		}
		final char[] buf = reader.readNumber();
		final int position = reader.getCurrentIndex();
		final int len = position - reader.getTokenStart();
		if (len > 18) {
			if (len == buf.length) {
				final NumberInfo tmp = readLongNumber(reader, buf);
				return parseDoubleGeneric(tmp.buffer, tmp.length, position);
			} else {
				return parseDoubleGeneric(buf, len, position);
			}
		}
		final char ch = buf[0];
		if (ch == '-') {
			return parseNegativeDouble(buf, position, len, 1);
		} else if (ch == '+') {
			return parsePositiveDouble(buf, position, len, 1);
		}
		return parsePositiveDouble(buf, position, len, 0);
	}

	private static double parsePositiveDouble(final char[] buf, final int position, final int len, int i) throws IOException {
		long value = 0;
		char ch = ' ';
		for (; i < len; i++) {
			ch = buf[i];
			if (ch == '.') break;
			final int ind = buf[i] - 48;
			value = (value << 3) + (value << 1) + ind;
			if (ind < 0 || ind > 9) {
				return parseDoubleGeneric(buf, len, position);
			}
		}
		if (i == len) return value;
		else if (ch == '.') {
			i++;
			long div = 1;
			for (; i < len; i++) {
				final int ind = buf[i] - 48;
				div = (div << 3) + (div << 1);
				value = (value << 3) + (value << 1) + ind;
				if (ind < 0 || ind > 9) {
					return parseDoubleGeneric(buf, len, position);
				}
			}
			return value / (double) div;
		}
		return value;
	}

	private static double parseNegativeDouble(final char[] buf, final int position, final int len, int i) throws IOException {
		long value = 0;
		char ch = ' ';
		for (; i < len; i++) {
			ch = buf[i];
			if (ch == '.') break;
			final int ind = buf[i] - 48;
			value = (value << 3) + (value << 1) - ind;
			if (ind < 0 || ind > 9) {
				return parseDoubleGeneric(buf, len, position);
			}
		}
		if (i == len) return value;
		else if (ch == '.') {
			i++;
			long div = 1;
			for (; i < len; i++) {
				final int ind = buf[i] - 48;
				div = (div << 3) + (div << 1);
				value = (value << 3) + (value << 1) - ind;
				if (ind < 0 || ind > 9) {
					return parseDoubleGeneric(buf, len, position);
				}
			}
			return value / (double) div;
		}
		return value;
	}

	private static double parseDoubleGeneric(final char[] buf, final int len, final int position) throws IOException {
		int end = len;
		while (end > 0 && Character.isWhitespace(buf[end - 1])) {
			end--;
		}
		try {
			return Double.parseDouble(new String(buf, 0, end));
		} catch (NumberFormatException nfe) {
			throw new IOException("Error parsing float number at position: " + (position - len), nfe);
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
		if (Float.isNaN(value)) {
			sw.writeAscii("\"NaN\"");
		} else if (Float.isInfinite(value)) {
			final int bits = Float.floatToIntBits(value);
			if ((bits & -2147483648) != 0) {
				sw.writeAscii("\"-Infinity\"");
			} else {
				sw.writeAscii("\"Infinity\"");
			}
		} else sw.writeAscii(Float.toString(value)); //TODO: better implementation required
	}

	public static void serialize(final float[] value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else if (value.length == 0) {
			sw.writeAscii("[]");
		} else {
			sw.writeByte(JsonWriter.ARRAY_START);
			serialize(value[0], sw);
			for(int i = 1; i < value.length; i++) {
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
			return parseFloatGeneric(buf, reader.getCurrentIndex() - position - 1, position + 1);
		}
		final char[] buf = reader.readNumber();
		final int position = reader.getCurrentIndex();
		final int len = position - reader.getTokenStart();
		if (len > 18) {
			if (len == buf.length) {
				final NumberInfo tmp = readLongNumber(reader, buf);
				return parseFloatGeneric(tmp.buffer, tmp.length, position);
			} else {
				return parseFloatGeneric(buf, len, position);
			}
		}
		final char ch = buf[0];
		if (ch == '-') {
			return parseNegativeFloat(buf, position, len, 1);
		} else if (ch == '+') {
			return parsePositiveFloat(buf, position, len, 1);
		}
		return parsePositiveFloat(buf, position, len, 0);
	}

	private static float parsePositiveFloat(final char[] buf, final int position, final int len, int i) throws IOException {
		long value = 0;
		char ch = ' ';
		for (; i < len; i++) {
			ch = buf[i];
			if (ch == '.') break;
			final int ind = buf[i] - 48;
			value = (value << 3) + (value << 1) + ind;
			if (ind < 0 || ind > 9) {
				return parseFloatGeneric(buf, len, position);
			}
		}
		if (ch == '.') {
			i++;
			int div = 1;
			for (; i < len; i++) {
				final int ind = buf[i] - 48;
				div = (div << 3) + (div << 1);
				value = (value << 3) + (value << 1) + ind;
				if (ind < 0 || ind > 9) {
					return parseFloatGeneric(buf, len, position);
				}
			}
			return value / (float) div;
		}
		return value;
	}

	private static float parseNegativeFloat(final char[] buf, final int position, final int len, int i) throws IOException {
		long value = 0;
		char ch = ' ';
		for (; i < len; i++) {
			ch = buf[i];
			if (ch == '.') break;
			final int ind = buf[i] - 48;
			value = (value << 3) + (value << 1) - ind;
			if (ind < 0 || ind > 9) {
				return parseFloatGeneric(buf, len, position);
			}
		}
		if (ch == '.') {
			i++;
			int div = 1;
			for (; i < len; i++) {
				final int ind = buf[i] - 48;
				div = (div << 3) + (div << 1);
				value = (value << 3) + (value << 1) - ind;
				if (ind < 0 || ind > 9) {
					return parseFloatGeneric(buf, len, position);
				}
			}
			return value / (float) div;
		}
		return value;
	}

	private static float parseFloatGeneric(final char[] buf, final int len, final int position) throws IOException {
		int end = len;
		while (end > 0 && Character.isWhitespace(buf[end - 1])) {
			end--;
		}
		try {
			return Float.parseFloat(new String(buf, 0, end));
		} catch (NumberFormatException nfe) {
			throw new IOException("Error parsing float number at position: " + (position - len), nfe);
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

	public static void serialize(final int value, final JsonWriter sw) {
		if (value == Integer.MIN_VALUE) {
			sw.writeAscii("-2147483648");
		} else {
			final byte[] buf = sw.tmp;
			int q, r;
			int charPos = 10;
			int i;
			if (value < 0) {
				i = -value;
				sw.writeByte(MINUS);
			} else {
				i = value;
			}

			int v = 0;
			while (charPos > 1) {
				q = i / 100;
				r = i - ((q << 6) + (q << 5) + (q << 2));
				i = q;
				v = Digits[r];
				buf[charPos--] = (byte) v;
				buf[charPos--] = (byte) (v >> 8);
				if (i == 0) break;
			}

			sw.writeBuffer(charPos + 1 + (v >> 16), 11);
		}
	}

	public static void serialize(final int[] value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else if (value.length == 0) {
			sw.writeAscii("[]");
		} else {
			sw.writeByte(JsonWriter.ARRAY_START);
			serialize(value[0], sw);
			for(int i = 1; i < value.length; i++) {
				sw.writeByte(JsonWriter.COMMA);
				serialize(value[i], sw);
			}
			sw.writeByte(JsonWriter.ARRAY_END);
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
			for(int i = 1; i < value.length; i++) {
				sw.writeByte(JsonWriter.COMMA);
				serialize(value[i], sw);
			}
			sw.writeByte(JsonWriter.ARRAY_END);
		}
	}

	public static int deserializeInt(final JsonReader reader) throws IOException {
		final char[] buf = reader.readNumber();
		final int position = reader.getCurrentIndex();
		final int len = position - reader.getTokenStart();
		final char ch = buf[0];
		if (ch == '-') {
			return parseNegativeInt(buf, position, len, 1);
		} else if (ch == '+') {
			return parsePositiveInt(buf, position, len, 1);
		}
		return parsePositiveInt(buf, position, len, 0);
	}

	private static int parsePositiveInt(final char[] buf, final int position, final int len, int i) throws IOException {
		int value = 0;
		for (; i < len; i++) {
			final int ind = buf[i] - 48;
			value = (value << 3) + (value << 1) + ind;
			if (ind < 0 || ind > 9) {
				BigDecimal v = parseNumberGeneric(buf, len, position);
				if (v.scale() <= 0) return v.intValue();
				throw new IOException("Error parsing int number at position: " + (position - len) + ". Found decimal value: " + v);
			}
		}
		return value;
	}

	private static int parseNegativeInt(final char[] buf, final int position, final int len, int i) throws IOException {
		int value = 0;
		for (; i < len; i++) {
			final int ind = buf[i] - 48;
			value = (value << 3) + (value << 1) - ind;
			if (ind < 0 || ind > 9) {
				BigDecimal v = parseNumberGeneric(buf, len, position);
				if (v.scale() <= 0) return v.intValue();
				throw new IOException("Error parsing int number at position: " + (position - len) + ". Found decimal value: " + v);
			}
		}
		return value;
	}

	public static ArrayList<Integer> deserializeIntCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(IntReader);
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

	public static void serialize(final long value, final JsonWriter sw) {
		if (value == Long.MIN_VALUE) {
			sw.writeAscii("-9223372036854775808");
		} else {
			final byte[] buf = sw.tmp;
			long q;
			int r;
			int charPos = 20;
			long i;
			if (value < 0) {
				i = -value;
				sw.writeByte(MINUS);
			} else {
				i = value;
			}

			int v = 0;
			while (charPos > 1) {
				q = i / 100;
				r = (int) (i - ((q << 6) + (q << 5) + (q << 2)));
				i = q;
				v = Digits[r];
				buf[charPos--] = (byte) v;
				buf[charPos--] = (byte) (v >> 8);
				if (i == 0) break;
			}

			sw.writeBuffer(charPos + 1 + (v >> 16), 21);
		}
	}

	public static void serialize(final long[] value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else if (value.length == 0) {
			sw.writeAscii("[]");
		} else {
			sw.writeByte(JsonWriter.ARRAY_START);
			serialize(value[0], sw);
			for(int i = 1; i < value.length; i++) {
				sw.writeByte(JsonWriter.COMMA);
				serialize(value[i], sw);
			}
			sw.writeByte(JsonWriter.ARRAY_END);
		}
	}

	public static long deserializeLong(final JsonReader reader) throws IOException {
		final char[] buf = reader.readNumber();
		final int position = reader.getCurrentIndex();
		final int len = position - reader.getTokenStart();
		final char ch = buf[0];
		int i = 0;
		long value = 0;
		if (ch == '-') {
			i = 1;
			for (; i < len; i++) {
				final int ind = buf[i] - 48;
				value = (value << 3) + (value << 1) - ind;
				if (ind < 0 || ind > 9) {
					return parseLongGeneric(buf, position, len);
				}
			}
			return value;
		} else if (ch == '+') {
			i = 1;
		}
		for (; i < len; i++) {
			final int ind = buf[i] - 48;
			value = (value << 3) + (value << 1) + ind;
			if (ind < 0 || ind > 9) {
				return parseLongGeneric(buf, position, len);
			}
		}
		return value;
	}

	private static long parseLongGeneric(final char[] buf, final int position, final int len) throws IOException {
		BigDecimal v = parseNumberGeneric(buf, len, position);
		if (v.scale() <= 0) return v.longValue();
		throw new IOException("Error parsing long number at position: " + (position - len) + ". Found decimal value: " + v);
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
		final char[] buf = reader.readNumber();
		final int position = reader.getCurrentIndex();
		final int len = position - reader.getTokenStart();
		if (len > 18) {
			if (len == buf.length) {
				final NumberInfo tmp = readLongNumber(reader, buf);
				return parseNumberGeneric(tmp.buffer, tmp.length, position);
			} else {
				return parseNumberGeneric(buf, len, position);
			}
		}
		final char ch = buf[0];
		if (ch == '-') {
			return parseNegativeDecimal(buf, position, len);
		} else if (ch == '+') {
			return parsePositiveDecimal(buf, position, len, 1);
		}
		return parsePositiveDecimal(buf, position, len, 0);
	}

	private static BigDecimal parsePositiveDecimal(final char[] buf, final int position, final int len, int i) throws IOException {
		long value = 0;
		char ch = ' ';
		for (; i < len; i++) {
			ch = buf[i];
			if (ch == '.' || ch == 'e' || ch == 'E') break;
			final int ind = ch - 48;
			value = (value << 3) + (value << 1) + ind;
			if (ind < 0 || ind > 9) {
				return parseNumberGeneric(buf, len, position);
			}
		}
		if (i == len) return BigDecimal.valueOf(value);
		else if (ch == '.') {
			i++;
			int dp = i;
			for (; i < len; i++) {
				ch = buf[i];
				if (ch == 'e' || ch == 'E') break;
				final int ind = ch - 48;
				value = (value << 3) + (value << 1) + ind;
				if (ind < 0 || ind > 9) {
					return parseNumberGeneric(buf, len, position);
				}
			}
			if (i == len) return BigDecimal.valueOf(value, len - dp);
			else if (ch == 'e' || ch == 'E') {
				final int ep = i;
				i++;
				ch = buf[i];
				final int exp;
				if (ch == '-') {
					exp = parseNegativeInt(buf, position, len, i + 1);
				} else if (ch == '+') {
					exp = parsePositiveInt(buf, position, len, i + 1);
				} else {
					exp = parsePositiveInt(buf, position, len, i);
				}
				return BigDecimal.valueOf(value, ep - dp - exp);
			}
			return BigDecimal.valueOf(value, len - dp);
		} else if (ch == 'e' || ch == 'E') {
			i++;
			ch = buf[i];
			final int exp;
			if (ch == '-') {
				exp = parseNegativeInt(buf, position, len, i + 1);
			} else if (ch == '+') {
				exp = parsePositiveInt(buf, position, len, i + 1);
			} else {
				exp = parsePositiveInt(buf, position, len, i);
			}
			return BigDecimal.valueOf(value, -exp);
		}
		return BigDecimal.valueOf(value);
	}

	private static BigDecimal parseNegativeDecimal(final char[] buf, final int position, final int len) throws IOException {
		long value = 0;
		char ch = ' ';
		int i = 1;
		for (; i < len; i++) {
			ch = buf[i];
			if (ch == '.' || ch == 'e' || ch == 'E') break;
			final int ind = ch - 48;
			value = (value << 3) + (value << 1) - ind;
			if (ind < 0 || ind > 9) {
				return parseNumberGeneric(buf, len, position);
			}
		}
		if (i == len) return BigDecimal.valueOf(value);
		else if (ch == '.') {
			i++;
			int dp = i;
			for (; i < len; i++) {
				ch = buf[i];
				if (ch == 'e' || ch == 'E') break;
				final int ind = ch - 48;
				value = (value << 3) + (value << 1) - ind;
				if (ind < 0 || ind > 9) {
					return parseNumberGeneric(buf, len, position);
				}
			}
			if (i == len) return BigDecimal.valueOf(value, len - dp);
			else if (ch == 'e' || ch == 'E') {
				final int ep = i;
				i++;
				ch = buf[i];
				final int exp;
				if (ch == '-') {
					exp = parseNegativeInt(buf, position, len, i + 1);
				} else if (ch == '+') {
					exp = parsePositiveInt(buf, position, len, i + 1);
				} else {
					exp = parsePositiveInt(buf, position, len, i);
				}
				return BigDecimal.valueOf(value, ep - dp - exp);
			}
			return BigDecimal.valueOf(value, len - dp);
		} else if (ch == 'e' || ch == 'E') {
			i++;
			ch = buf[i];
			final int exp;
			if (ch == '-') {
				exp = parseNegativeInt(buf, position, len, i + 1);
			} else if (ch == '+') {
				exp = parsePositiveInt(buf, position, len, i + 1);
			} else {
				exp = parsePositiveInt(buf, position, len, i);
			}
			return BigDecimal.valueOf(value, -exp);
		}
		return BigDecimal.valueOf(value);
	}

	public static Number deserializeNumber(final JsonReader reader) throws IOException {
		final char[] buf = reader.readNumber();
		final int position = reader.getCurrentIndex();
		final int len = position - reader.getTokenStart();
		if (len > 18) {
			if (len == buf.length) {
				final NumberInfo tmp = readLongNumber(reader, buf);
				return parseNumberGeneric(tmp.buffer, tmp.length, position);
			} else {
				return parseNumberGeneric(buf, len, position);
			}
		}
		final char ch = buf[0];
		if (ch == '-') {
			return parseNegativeNumber(buf, position, len);
		} else if (ch == '+') {
			return parsePositiveNumber(buf, position, len, 1);
		}
		return parsePositiveNumber(buf, position, len, 0);
	}

	private static Number parsePositiveNumber(final char[] buf, final int position, final int len, int i) throws IOException {
		long value = 0;
		char ch = ' ';
		for (; i < len; i++) {
			ch = buf[i];
			if (ch == '.' || ch == 'e' || ch == 'E') break;
			final int ind = ch - 48;
			value = (value << 3) + (value << 1) + ind;
			if (ind < 0 || ind > 9) {
				return parseNumberGeneric(buf, len, position);
			}
		}
		if (i == len) return value;
		else if (ch == '.') {
			i++;
			int dp = i;
			for (; i < len; i++) {
				ch = buf[i];
				if (ch == 'e' || ch == 'E') break;
				final int ind = ch - 48;
				value = (value << 3) + (value << 1) + ind;
				if (ind < 0 || ind > 9) {
					return parseNumberGeneric(buf, len, position);
				}
			}
			if (i == len) return value / POW_10[len - dp];
			else if (ch == 'e' || ch == 'E') {
				final int ep = i;
				i++;
				ch = buf[i];
				final int exp;
				if (ch == '-') {
					exp = parseNegativeInt(buf, position, len, i + 1);
				} else if (ch == '+') {
					exp = parsePositiveInt(buf, position, len, i + 1);
				} else {
					exp = parsePositiveInt(buf, position, len, i);
				}
				return BigDecimal.valueOf(value, ep - dp - exp);
			}
			return BigDecimal.valueOf(value, len - dp);
		} else if (ch == 'e' || ch == 'E') {
			i++;
			ch = buf[i];
			final int exp;
			if (ch == '-') {
				exp = parseNegativeInt(buf, position, len, i + 1);
			} else if (ch == '+') {
				exp = parsePositiveInt(buf, position, len, i + 1);
			} else {
				exp = parsePositiveInt(buf, position, len, i);
			}
			return BigDecimal.valueOf(value, -exp);
		}
		return BigDecimal.valueOf(value);
	}

	private static Number parseNegativeNumber(final char[] buf, final int position, final int len) throws IOException {
		long value = 0;
		char ch = ' ';
		int i = 1;
		for (; i < len; i++) {
			ch = buf[i];
			if (ch == '.' || ch == 'e' || ch == 'E') break;
			final int ind = ch - 48;
			value = (value << 3) + (value << 1) - ind;
			if (ind < 0 || ind > 9) {
				return parseNumberGeneric(buf, len, position);
			}
		}
		if (i == len) return value;
		else if (ch == '.') {
			i++;
			int dp = i;
			for (; i < len; i++) {
				ch = buf[i];
				if (ch == 'e' || ch == 'E') break;
				final int ind = ch - 48;
				value = (value << 3) + (value << 1) - ind;
				if (ind < 0 || ind > 9) {
					return parseNumberGeneric(buf, len, position);
				}
			}
			if (i == len) return value / POW_10[len - dp];
			else if (ch == 'e' || ch == 'E') {
				final int ep = i;
				i++;
				ch = buf[i];
				final int exp;
				if (ch == '-') {
					exp = parseNegativeInt(buf, position, len, i + 1);
				} else if (ch == '+') {
					exp = parsePositiveInt(buf, position, len, i + 1);
				} else {
					exp = parsePositiveInt(buf, position, len, i);
				}
				return BigDecimal.valueOf(value, ep - dp - exp);
			}
			return BigDecimal.valueOf(value, len - dp);
		} else if (ch == 'e' || ch == 'E') {
			i++;
			ch = buf[i];
			final int exp;
			if (ch == '-') {
				exp = parseNegativeInt(buf, position, len, i + 1);
			} else if (ch == '+') {
				exp = parsePositiveInt(buf, position, len, i + 1);
			} else {
				exp = parsePositiveInt(buf, position, len, i);
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
