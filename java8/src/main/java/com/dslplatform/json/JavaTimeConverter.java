package com.dslplatform.json;

import java.io.IOException;
import java.time.*;
import java.util.ArrayList;
import java.util.Collection;

public abstract class JavaTimeConverter {
	public static final LocalDateTime MIN_LOCAL_DATE_TIME = LocalDateTime.of(1, 1, 1, 0, 0, 0, 0);
	public static final OffsetDateTime MIN_DATE_TIME_UTC = OffsetDateTime.of(MIN_LOCAL_DATE_TIME, ZoneOffset.UTC);
	public static final LocalDate MIN_LOCAL_DATE = LocalDate.of(1, 1, 1);

	public static final JsonReader.ReadObject<LocalDate> LOCAL_DATE_READER = new JsonReader.ReadObject<LocalDate>() {
		@Override
		public LocalDate read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : JavaTimeConverter.deserializeLocalDate(reader);
		}
	};
	public static final JsonWriter.WriteObject<LocalDate> LOCAL_DATE_WRITER = new JsonWriter.WriteObject<LocalDate>() {
		@Override
		public void write(JsonWriter writer, LocalDate value) {
			serializeNullable(value, writer);
		}
	};
	public static final JsonReader.ReadObject<OffsetDateTime> DATE_TIME_READER = new JsonReader.ReadObject<OffsetDateTime>() {
		@Override
		public OffsetDateTime read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : JavaTimeConverter.deserializeDateTime(reader);
		}
	};
	public static final JsonWriter.WriteObject<OffsetDateTime> DATE_TIME_WRITER = new JsonWriter.WriteObject<OffsetDateTime>() {
		@Override
		public void write(JsonWriter writer, OffsetDateTime value) {
			serializeNullable(value, writer);
		}
	};
	public static final JsonReader.ReadObject<LocalDateTime> LOCAL_DATE_TIME_READER = new JsonReader.ReadObject<LocalDateTime>() {
		@Override
		public LocalDateTime read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : JavaTimeConverter.deserializeLocalDateTime(reader);
		}
	};
	public static final JsonWriter.WriteObject<LocalDateTime> LOCAL_DATE_TIME_WRITER = new JsonWriter.WriteObject<LocalDateTime>() {
		@Override
		public void write(JsonWriter writer, LocalDateTime value) {
			serializeNullable(value, writer);
		}
	};
	public static final JsonReader.ReadObject<ZonedDateTime> ZONED_DATE_TIME_READER = new JsonReader.ReadObject<ZonedDateTime>() {
		@Override
		public ZonedDateTime read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : JavaTimeConverter.deserializeDateTime(reader).toZonedDateTime();
		}
	};
	public static final JsonWriter.WriteObject<ZonedDateTime> ZONED_DATE_TIME_WRITER = new JsonWriter.WriteObject<ZonedDateTime>() {
		@Override
		public void write(JsonWriter writer, ZonedDateTime value) {
			if (value == null) writer.writeNull();
			else serialize(value.toOffsetDateTime(), writer);
		}
	};

	public static void serializeNullable(final OffsetDateTime value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serialize(value, sw);
		}
	}

	public static void serializeNullable(final LocalDateTime value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serialize(value, sw);
		}
	}

	public static void serialize(final OffsetDateTime value, final JsonWriter sw) {
		final int year = value.getYear();
		if (year < 0) {
			throw new SerializationException("Negative dates are not supported.");
		} else if (year > 9999) {
			sw.writeByte(JsonWriter.QUOTE);
			sw.writeAscii(value.toString());
			sw.writeByte(JsonWriter.QUOTE);
			return;
		}
		final byte[] buf = sw.ensureCapacity(32);
		final int pos = sw.size();
		buf[pos] = '"';
		NumberConverter.write4(year, buf, pos + 1);
		buf[pos + 5] = '-';
		NumberConverter.write2(value.getMonthValue(), buf, pos + 6);
		buf[pos + 8] = '-';
		NumberConverter.write2(value.getDayOfMonth(), buf, pos + 9);
		buf[pos + 11] = 'T';
		NumberConverter.write2(value.getHour(), buf, pos + 12);
		buf[pos + 14] = ':';
		NumberConverter.write2(value.getMinute(), buf, pos + 15);
		buf[pos + 17] = ':';
		NumberConverter.write2(value.getSecond(), buf, pos + 18);
		final int nano = value.getNano();
		if (nano != 0) {
			final int end = writeNano(buf, pos, nano);
			writeTimezone(end, value, sw);
		} else {
			writeTimezone(20, value, sw);
		}
	}

	private static int writeNano(final byte[] buf, int pos, final int nano) {
		buf[pos + 20] = '.';
		final int div = nano / 1000;
		final int div2 = div / 1000;
		final int rem1 = nano - div * 1000;
		int end;
		if (rem1 != 0) {
			NumberConverter.write3(div2, buf, pos + 21);
			NumberConverter.write3(div - div2 * 1000, buf, pos + 24);
			NumberConverter.write3(rem1, buf, pos + 27);
			end = 30;
		} else {
			final int rem2 = div - div2 * 1000;
			if (rem2 != 0) {
				NumberConverter.write3(div2, buf, pos + 21);
				NumberConverter.write3(rem2, buf, pos + 24);
				end = 27;
			} else {
				NumberConverter.write3(div2, buf, pos + 21);
				end = 24;
			}
		}
		if (buf[end + pos - 1] == '0') end--;
		if (buf[end + pos - 1] == '0') end--;
		return end;
	}

	public static void serialize(final LocalDateTime value, final JsonWriter sw) {
		final int year = value.getYear();
		if (year < 0) {
			throw new SerializationException("Negative dates are not supported.");
		} else if (year > 9999) {
			sw.writeByte(JsonWriter.QUOTE);
			sw.writeAscii(value.toString());
			sw.writeByte(JsonWriter.QUOTE);
			return;
		}
		final byte[] buf = sw.ensureCapacity(32);
		final int pos = sw.size();
		buf[pos] = '"';
		NumberConverter.write4(year, buf, pos + 1);
		buf[pos + 5] = '-';
		NumberConverter.write2(value.getMonthValue(), buf, pos + 6);
		buf[pos + 8] = '-';
		NumberConverter.write2(value.getDayOfMonth(), buf, pos + 9);
		buf[pos + 11] = 'T';
		NumberConverter.write2(value.getHour(), buf, pos + 12);
		buf[pos + 14] = ':';
		NumberConverter.write2(value.getMinute(), buf, pos + 15);
		buf[pos + 17] = ':';
		NumberConverter.write2(value.getSecond(), buf, pos + 18);
		final int nano = value.getNano();
		if (nano != 0) {
			final int end = writeNano(buf, pos, nano);
			buf[pos + end] = '"';
			sw.advance(end + 1);
		} else {
			buf[pos + 20] = '"';
			sw.advance(21);
		}
	}

	private static void writeTimezone(final int position, final OffsetDateTime dt, final JsonWriter sw) {
		final ZoneOffset zone = dt.getOffset();
		sw.advance(position);
		sw.writeAscii(zone.getId());
		sw.writeByte(JsonWriter.QUOTE);
	}

	private static boolean allDigits(char[] buffer, int start, int end) {
		for (int i = start; i < end; i++) {
			if (buffer[i] < '0' || buffer[i] > '9') return false;
		}
		return true;
	}

	public static OffsetDateTime deserializeDateTime(final JsonReader reader) throws IOException {
		final char[] tmp = reader.readSimpleQuote();
		final int len = reader.getCurrentIndex() - reader.getTokenStart() - 1;
		if (len > 18 && len < 31 && tmp[len - 1] == 'Z' && tmp[4] == '-' && tmp[7] == '-'
				&& (tmp[10] == 'T' || tmp[10] == 't' || tmp[10] == ' ')
				&& tmp[13] == ':' && tmp[16] == ':' && allDigits(tmp, 20, len - 1)) {
			final int year = NumberConverter.read4(tmp, 0);
			final int month = NumberConverter.read2(tmp, 5);
			final int day = NumberConverter.read2(tmp, 8);
			final int hour = NumberConverter.read2(tmp, 11);
			final int min = NumberConverter.read2(tmp, 14);
			final int sec = NumberConverter.read2(tmp, 17);
			if (tmp[19] == '.') {
				final int nanos = readNanos(tmp, len - 1);
				return OffsetDateTime.of(year, month, day, hour, min, sec, nanos, ZoneOffset.UTC);
			}
			return OffsetDateTime.of(year, month, day, hour, min, sec, 0, ZoneOffset.UTC);
		} else if (len > 22 && len < 36 && tmp[len - 3] == ':'
				&& (tmp[len - 6] == '+' || tmp[len - 6] == '-')
				&& tmp[4] == '-' && tmp[7] == '-'
				&& (tmp[10] == 'T' || tmp[10] == 't' || tmp[10] == ' ')
				&& tmp[13] == ':' && tmp[16] == ':'
				&& allDigits(tmp, len - 2, len) && allDigits(tmp, len - 5, len - 3)) {
			final int year = NumberConverter.read4(tmp, 0);
			final int month = NumberConverter.read2(tmp, 5);
			final int day = NumberConverter.read2(tmp, 8);
			final int hour = NumberConverter.read2(tmp, 11);
			final int min = NumberConverter.read2(tmp, 14);
			final int sec = NumberConverter.read2(tmp, 17);
			final int offHour = NumberConverter.read2(tmp, len - 5);
			final int offMin = NumberConverter.read2(tmp, len - 2);
			final ZoneOffset offset = ZoneOffset.ofHoursMinutes(tmp[len - 6] == '+' ? offHour : -offHour, offMin);
			if (tmp[19] == '.') {
				final int nanos = readNanos(tmp, len - 6);
				return OffsetDateTime.of(year, month, day, hour, min, sec, nanos, offset);
			}
			return OffsetDateTime.of(year, month, day, hour, min, sec, 0, offset);
		} else {
			return OffsetDateTime.parse(new String(tmp, 0, len));
		}
	}

	private static int readNanos(final char[] tmp, final int len) {
		switch (len) {
			case 21:
				return 100000000 * (tmp[20] - 48);
			case 22:
				return 100000000 * (tmp[20] - 48) + 10000000 * (tmp[21] - 48);
			case 23:
				return 100000000 * (tmp[20] - 48) + 10000000 * (tmp[21] - 48) + 1000000 * (tmp[22] - 48);
			case 24:
				return 100000000 * (tmp[20] - 48) + 10000000 * (tmp[21] - 48) + 1000000 * (tmp[22] - 48) + 100000 * (tmp[23] - 48);
			case 25:
				return 100000000 * (tmp[20] - 48) + 10000000 * (tmp[21] - 48) + 1000000 * (tmp[22] - 48) + 100000 * (tmp[23] - 48) + 10000 * (tmp[24] - 48);
			case 26:
				return 100000000 * (tmp[20] - 48) + 10000000 * (tmp[21] - 48) + 1000000 * (tmp[22] - 48) + 100000 * (tmp[23] - 48) + 10000 * (tmp[24] - 48) + 1000 * (tmp[25] - 48);
			case 27:
				return 100000000 * (tmp[20] - 48) + 10000000 * (tmp[21] - 48) + 1000000 * (tmp[22] - 48) + 100000 * (tmp[23] - 48) + 10000 * (tmp[24] - 48) + 1000 * (tmp[25] - 48) + 100 * (tmp[26] - 48);
			case 28:
				return 100000000 * (tmp[20] - 48) + 10000000 * (tmp[21] - 48) + 1000000 * (tmp[22] - 48) + 100000 * (tmp[23] - 48) + 10000 * (tmp[24] - 48) + 1000 * (tmp[25] - 48) + 100 * (tmp[26] - 48) + 10 * (tmp[27] - 48);
			default:
				return 100000000 * (tmp[20] - 48) + 10000000 * (tmp[21] - 48) + 1000000 * (tmp[22] - 48) + 100000 * (tmp[23] - 48) + 10000 * (tmp[24] - 48) + 1000 * (tmp[25] - 48) + 100 * (tmp[26] - 48) + 10 * (tmp[27] - 48) + tmp[28] - 48;
		}
	}

	public static LocalDateTime deserializeLocalDateTime(final JsonReader reader) throws IOException {
		final char[] tmp = reader.readSimpleQuote();
		final int len = reader.getCurrentIndex() - reader.getTokenStart() - 1;
		if (len > 18 && len < 30 && tmp[4] == '-' && tmp[7] == '-'
				&& (tmp[10] == 'T' || tmp[10] == 't' || tmp[10] == ' ')
				&& tmp[13] == ':' && tmp[16] == ':' && allDigits(tmp, 20, len)) {
			final int year = NumberConverter.read4(tmp, 0);
			final int month = NumberConverter.read2(tmp, 5);
			final int day = NumberConverter.read2(tmp, 8);
			final int hour = NumberConverter.read2(tmp, 11);
			final int min = NumberConverter.read2(tmp, 14);
			final int sec = NumberConverter.read2(tmp, 17);
			if (tmp[19] == '.') {
				final int nanos = readNanos(tmp, len);
				return LocalDateTime.of(year, month, day, hour, min, sec, nanos);
			}
			return LocalDateTime.of(year, month, day, hour, min, sec, 0);
		} else {
			return LocalDateTime.parse(new String(tmp, 0, len));
		}
	}

	public static ArrayList<OffsetDateTime> deserializeDateTimeCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(DATE_TIME_READER);
	}

	public static void deserializeDateTimeCollection(final JsonReader reader, final Collection<OffsetDateTime> res) throws IOException {
		reader.deserializeCollection(DATE_TIME_READER, res);
	}

	public static ArrayList<OffsetDateTime> deserializeDateTimeNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(DATE_TIME_READER);
	}

	public static void deserializeDateTimeNullableCollection(final JsonReader reader, final Collection<OffsetDateTime> res) throws IOException {
		reader.deserializeNullableCollection(DATE_TIME_READER, res);
	}

	public static ArrayList<LocalDateTime> deserializeLocalDateTimeCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(LOCAL_DATE_TIME_READER);
	}

	public static void deserializeLocalDateTimeCollection(final JsonReader reader, final Collection<LocalDateTime> res) throws IOException {
		reader.deserializeCollection(LOCAL_DATE_TIME_READER, res);
	}

	public static ArrayList<LocalDateTime> deserializeLocalDateTimeNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(LOCAL_DATE_TIME_READER);
	}

	public static void deserializeLocalDateTimeNullableCollection(final JsonReader reader, final Collection<LocalDateTime> res) throws IOException {
		reader.deserializeNullableCollection(LOCAL_DATE_TIME_READER, res);
	}

	public static void serializeNullable(final LocalDate value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serialize(value, sw);
		}
	}

	public static void serialize(final LocalDate value, final JsonWriter sw) {
		final int year = value.getYear();
		if (year < 0) {
			throw new SerializationException("Negative dates are not supported.");
		} else if (year > 9999) {
			sw.writeByte(JsonWriter.QUOTE);
			NumberConverter.serialize(year, sw);
			sw.writeByte((byte)'-');
			NumberConverter.serialize(value.getMonthValue(), sw);
			sw.writeByte((byte)'-');
			NumberConverter.serialize(value.getDayOfMonth(), sw);
			sw.writeByte(JsonWriter.QUOTE);
			return;
		}
		final byte[] buf = sw.ensureCapacity(12);
		final int pos = sw.size();
		buf[pos] = '"';
		NumberConverter.write4(year, buf, pos + 1);
		buf[pos + 5] = '-';
		NumberConverter.write2(value.getMonthValue(), buf, pos + 6);
		buf[pos + 8] = '-';
		NumberConverter.write2(value.getDayOfMonth(), buf, pos + 9);
		buf[pos + 11] = '"';
		sw.advance(12);
	}

	public static LocalDate deserializeLocalDate(final JsonReader reader) throws IOException {
		final char[] tmp = reader.readSimpleQuote();
		final int len = reader.getCurrentIndex() - reader.getTokenStart() - 1;
		if (len == 10 && tmp[4] == '-' && tmp[7] == '-') {
			final int year = NumberConverter.read4(tmp, 0);
			final int month = NumberConverter.read2(tmp, 5);
			final int day = NumberConverter.read2(tmp, 8);
			return LocalDate.of(year, month, day);
		} else if (len == 8 && tmp[4] == '-' && tmp[6] == '-') {
			final int year = NumberConverter.read4(tmp, 0);
			final int month = tmp[5] - 48;
			final int day = tmp[7] - 48;
			return LocalDate.of(year, month, day);
		} else if (len == 9 && tmp[4] == '-') {
			final int year = NumberConverter.read4(tmp, 0);
			final int month;
			final int day;
			if (tmp[6] == '-') {
				month = tmp[5] - 48;
				day = NumberConverter.read2(tmp, 7);
			} else if (tmp[7] == '-') {
				month = NumberConverter.read2(tmp, 5);
				day = tmp[8] - 48;
			} else {
				return LocalDate.parse(new String(tmp, 0, len));
			}
			return LocalDate.of(year, month, day);
		} else {
			return LocalDate.parse(new String(tmp, 0, len));
		}
	}

	public static ArrayList<LocalDate> deserializeLocalDateCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(LOCAL_DATE_READER);
	}

	public static void deserializeLocalDateCollection(final JsonReader reader, final Collection<LocalDate> res) throws IOException {
		reader.deserializeCollection(LOCAL_DATE_READER, res);
	}

	public static ArrayList<LocalDate> deserializeLocalDateNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(LOCAL_DATE_READER);
	}

	public static void deserializeLocalDateNullableCollection(final JsonReader reader, final Collection<LocalDate> res) throws IOException {
		reader.deserializeNullableCollection(LOCAL_DATE_READER, res);
	}
}
