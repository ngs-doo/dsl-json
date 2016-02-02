package com.dslplatform.json;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;

public abstract class JavaTimeConverter {
	public static final LocalDateTime MIN_LOCAL_DATE_TIME = LocalDateTime.of(1, 1, 1, 0, 0, 0, 0);
	public static final OffsetDateTime MIN_DATE_TIME_UTC = OffsetDateTime.of(MIN_LOCAL_DATE_TIME, ZoneOffset.UTC);
	public static final LocalDate MIN_LOCAL_DATE = LocalDate.of(1, 1, 1);

	static final JsonReader.ReadObject<LocalDate> LocalDateReader = JavaTimeConverter::deserializeLocalDate;
	static final JsonWriter.WriteObject<LocalDate> LocalDateWriter = (writer, value) -> serializeNullable(value, writer);
	static final JsonReader.ReadObject<OffsetDateTime> DateTimeReader = JavaTimeConverter::deserializeDateTime;
	static final JsonWriter.WriteObject<OffsetDateTime> DateTimeWriter = (writer, value) -> serializeNullable(value, writer);
	static final JsonReader.ReadObject<LocalDateTime> LocalDateTimeReader = JavaTimeConverter::deserializeLocalDateTime;
	static final JsonWriter.WriteObject<LocalDateTime> LocalDateTimeWriter = (writer, value) -> serializeNullable(value, writer);

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
		final byte[] buf = sw.ensureCapacity(32);
		final int pos = sw.size();
		buf[pos] = '"';
		final int year = value.getYear();
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
		final int nano = value.getNano() / 100;
		if (nano != 0) {
			buf[pos + 20] = '.';
			final int div = nano / 100;
			final int div2 = div / 100;
			final int rem = nano - div * 100;
			int end;
			if (rem != 0) {
				NumberConverter.write3(div2, buf, pos + 21);
				NumberConverter.write2(div - div2 * 100, buf, pos + 24);
				NumberConverter.write2(rem, buf, pos + 26);
				end = 28;
			} else {
				final int rem2 = div - div2 * 100;
				if (rem2 != 0) {
					NumberConverter.write3(div2, buf, pos + 21);
					NumberConverter.write2(div - div2 * 100, buf, pos + 24);
					end = 26;
				} else {
					final int div3 = div2 / 100;
					if (div2 != div3 * 100) {
						NumberConverter.write3(div2, buf, pos + 21);
						end = 24;
					} else {
						buf[pos + 21] = (byte) (div3 + '0');
						end = 22;
					}
				}
			}
			writeTimezone(buf, end, value, sw);
		} else {
			writeTimezone(buf, 20, value, sw);
		}
	}

	public static void serialize(final LocalDateTime value, final JsonWriter sw) {
		final byte[] buf = sw.ensureCapacity(32);
		final int pos = sw.size();
		buf[pos] = '"';
		final int year = value.getYear();
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
		final int nano = value.getNano() / 100;
		if (nano != 0) {
			buf[pos + 20] = '.';
			final int div = nano / 100;
			final int div2 = div / 100;
			final int rem = nano - div * 100;
			int end;
			if (rem != 0) {
				NumberConverter.write3(div2, buf, pos + 21);
				NumberConverter.write2(div - div2 * 100, buf, pos + 24);
				NumberConverter.write2(rem, buf, pos + 26);
				end = 28;
			} else {
				final int rem2 = div - div2 * 100;
				if (rem2 != 0) {
					NumberConverter.write3(div2, buf, pos + 21);
					NumberConverter.write2(div - div2 * 100, buf, pos + 24);
					end = 26;
				} else {
					final int div3 = div2 / 100;
					if (div2 != div3 * 100) {
						NumberConverter.write3(div2, buf, pos + 21);
						end = 24;
					} else {
						buf[pos + 21] = (byte) (div3 + '0');
						end = 22;
					}
				}
			}
			buf[pos + end] = '"';
			sw.advance(end + 1);
		} else {
			buf[pos + 20] = '"';
			sw.advance(21);
		}
	}

	private static void writeTimezone(final byte[] buf, final int position, final OffsetDateTime dt, final JsonWriter sw) {
		final ZoneOffset zone = dt.getOffset();
		sw.advance(position);
		sw.writeAscii(zone.getId());
		sw.writeByte((byte) '"');
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
		//TODO: non utc
		if (len > 18 && len < 28 && tmp[len - 1] == 'Z' && tmp[4] == '-' && tmp[7] == '-'
				&& (tmp[10] == 'T' || tmp[10] == 't' || tmp[10] == ' ')
				&& tmp[13] == ':' && tmp[16] == ':' && allDigits(tmp, 20, len)) {
			final int year = NumberConverter.read4(tmp, 0);
			final int month = NumberConverter.read2(tmp, 5);
			final int day = NumberConverter.read2(tmp, 8);
			final int hour = NumberConverter.read2(tmp, 11);
			final int min = NumberConverter.read2(tmp, 14);
			final int sec = NumberConverter.read2(tmp, 17);
			if (tmp[19] == '.') {
				final int nanos;
				switch (len) {
					case 22:
						nanos = 100000 * (tmp[20] - 48);
						break;
					case 23:
						nanos = 100000 * (tmp[20] - 48) + 10000 * (tmp[21] - 48);
						break;
					case 24:
						nanos = 100000 * (tmp[20] - 48) + 10000 * (tmp[21] - 48) + 1000 * (tmp[22] - 48);
						break;
					case 25:
						nanos = 100000 * (tmp[20] - 48) + 10000 * (tmp[21] - 48) + 1000 * (tmp[22] - 48) + 100 * (tmp[23] - 48);
						break;
					case 26:
						nanos = 100000 * (tmp[20] - 48) + 10000 * (tmp[21] - 48) + 1000 * (tmp[22] - 48) + 100 * (tmp[23] - 48) + 10 * (tmp[24] - 48);
						break;
					default:
						nanos = 100000 * (tmp[20] - 48) + 10000 * (tmp[21] - 48) + 1000 * (tmp[22] - 48) + 100 * (tmp[23] - 48) + 10 * (tmp[24] - 48) + tmp[25] - 48;
						break;
				}
				return OffsetDateTime.of(year, month, day, hour, min, sec, nanos * 1000, ZoneOffset.UTC);
			}
			return OffsetDateTime.of(year, month, day, hour, min, sec, 0, ZoneOffset.UTC);
		} else if (len > 22 && len < 33 && tmp[len - 3] == ':'
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
				final int nanos;
				switch (len) {
					case 27:
						nanos = 100000 * (tmp[20] - 48);
						break;
					case 28:
						nanos = 100000 * (tmp[20] - 48) + 10000 * (tmp[21] - 48);
						break;
					case 29:
						nanos = 100000 * (tmp[20] - 48) + 10000 * (tmp[21] - 48) + 1000 * (tmp[22] - 48);
						break;
					case 30:
						nanos = 100000 * (tmp[20] - 48) + 10000 * (tmp[21] - 48) + 1000 * (tmp[22] - 48) + 100 * (tmp[23] - 48);
						break;
					case 31:
						nanos = 100000 * (tmp[20] - 48) + 10000 * (tmp[21] - 48) + 1000 * (tmp[22] - 48) + 100 * (tmp[23] - 48) + 10 * (tmp[24] - 48);
						break;
					default:
						nanos = 100000 * (tmp[20] - 48) + 10000 * (tmp[21] - 48) + 1000 * (tmp[22] - 48) + 100 * (tmp[23] - 48) + 10 * (tmp[24] - 48) + tmp[25] - 48;
						break;
				}
				return OffsetDateTime.of(year, month, day, hour, min, sec, nanos * 1000, offset);
			}
			return OffsetDateTime.of(year, month, day, hour, min, sec, 0, offset);
		} else {
			return OffsetDateTime.parse(new String(tmp, 0, len));
		}
	}

	public static LocalDateTime deserializeLocalDateTime(final JsonReader reader) throws IOException {
		final char[] tmp = reader.readSimpleQuote();
		final int len = reader.getCurrentIndex() - reader.getTokenStart() - 1;
		//TODO: non utc
		if (len > 18 && len < 27 && tmp[4] == '-' && tmp[7] == '-'
				&& (tmp[10] == 'T' || tmp[10] == 't' || tmp[10] == ' ')
				&& tmp[13] == ':' && tmp[16] == ':' && allDigits(tmp, 20, len)) {
			final int year = NumberConverter.read4(tmp, 0);
			final int month = NumberConverter.read2(tmp, 5);
			final int day = NumberConverter.read2(tmp, 8);
			final int hour = NumberConverter.read2(tmp, 11);
			final int min = NumberConverter.read2(tmp, 14);
			final int sec = NumberConverter.read2(tmp, 17);
			if (tmp[19] == '.') {
				final int nanos;
				switch (len) {
					case 21:
						nanos = 100000 * (tmp[20] - 48);
						break;
					case 22:
						nanos = 100000 * (tmp[20] - 48) + 10000 * (tmp[21] - 48);
						break;
					case 23:
						nanos = 100000 * (tmp[20] - 48) + 10000 * (tmp[21] - 48) + 1000 * (tmp[22] - 48);
						break;
					case 24:
						nanos = 100000 * (tmp[20] - 48) + 10000 * (tmp[21] - 48) + 1000 * (tmp[22] - 48) + 100 * (tmp[23] - 48);
						break;
					case 25:
						nanos = 100000 * (tmp[20] - 48) + 10000 * (tmp[21] - 48) + 1000 * (tmp[22] - 48) + 100 * (tmp[23] - 48) + 10 * (tmp[24] - 48);
						break;
					default:
						nanos = 100000 * (tmp[20] - 48) + 10000 * (tmp[21] - 48) + 1000 * (tmp[22] - 48) + 100 * (tmp[23] - 48) + 10 * (tmp[24] - 48) + tmp[25] - 48;
						break;
				}
				return LocalDateTime.of(year, month, day, hour, min, sec, nanos * 1000);
			}
			return LocalDateTime.of(year, month, day, hour, min, sec, 0);
		} else {
			return LocalDateTime.parse(new String(tmp, 0, len));
		}
	}

	public static ArrayList<OffsetDateTime> deserializeDateTimeCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(DateTimeReader);
	}

	public static void deserializeDateTimeCollection(final JsonReader reader, final Collection<OffsetDateTime> res) throws IOException {
		reader.deserializeCollection(DateTimeReader, res);
	}

	public static ArrayList<OffsetDateTime> deserializeDateTimeNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(DateTimeReader);
	}

	public static void deserializeDateTimeNullableCollection(final JsonReader reader, final Collection<OffsetDateTime> res) throws IOException {
		reader.deserializeNullableCollection(DateTimeReader, res);
	}

	public static ArrayList<LocalDateTime> deserializeLocalDateTimeCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(LocalDateTimeReader);
	}

	public static void deserializeLocalDateTimeCollection(final JsonReader reader, final Collection<LocalDateTime> res) throws IOException {
		reader.deserializeCollection(LocalDateTimeReader, res);
	}

	public static ArrayList<LocalDateTime> deserializeLocalDateTimeNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(LocalDateTimeReader);
	}

	public static void deserializeLocalDateTimeNullableCollection(final JsonReader reader, final Collection<LocalDateTime> res) throws IOException {
		reader.deserializeNullableCollection(LocalDateTimeReader, res);
	}

	public static void serializeNullable(final LocalDate value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serialize(value, sw);
		}
	}

	public static void serialize(final LocalDate value, final JsonWriter sw) {
		final byte[] buf = sw.ensureCapacity(12);
		final int pos = sw.size();
		buf[pos] = '"';
		final int year = value.getYear();
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
		return reader.deserializeCollection(LocalDateReader);
	}

	public static void deserializeLocalDateCollection(final JsonReader reader, final Collection<LocalDate> res) throws IOException {
		reader.deserializeCollection(LocalDateReader, res);
	}

	public static ArrayList<LocalDate> deserializeLocalDateNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(LocalDateReader);
	}

	public static void deserializeLocalDateNullableCollection(final JsonReader reader, final Collection<LocalDate> res) throws IOException {
		reader.deserializeNullableCollection(LocalDateReader, res);
	}
}
