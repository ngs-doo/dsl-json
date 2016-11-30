package com.dslplatform.json;

import java.io.IOException;
import java.util.List;
import java.util.Collection;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public abstract class JodaTimeConverter {
	public static final DateTime MIN_DATE_TIME = DateTime.parse("0001-01-01T00:00:00Z");
	public static final LocalDate MIN_LOCAL_DATE = new LocalDate(1, 1, 1);
	private static final DateTimeFormatter localDateParser = ISODateTimeFormat.localDateParser();
	private static final DateTimeFormatter dateTimeParser = ISODateTimeFormat.dateTimeParser().withOffsetParsed();
	private static final DateTimeZone utcZone = DateTimeZone.UTC;

	static final JsonReader.ReadObject<LocalDate> LocalDateReader = new JsonReader.ReadObject<LocalDate>() {
		@Override
		public LocalDate read(final JsonReader reader) throws IOException {
			return deserializeLocalDate(reader);
		}
	};
	static final JsonWriter.WriteObject<LocalDate> LocalDateWriter = new JsonWriter.WriteObject<LocalDate>() {
		@Override
		public void write(JsonWriter writer, LocalDate value) {
			serializeNullable(value, writer);
		}
	};
	static final JsonReader.ReadObject<DateTime> DateTimeReader = new JsonReader.ReadObject<DateTime>() {
		@Override
		public DateTime read(JsonReader reader) throws IOException {
			return deserializeDateTime(reader);
		}
	};
	static final JsonWriter.WriteObject<DateTime> DateTimeWriter = new JsonWriter.WriteObject<DateTime>() {
		@Override
		public void write(JsonWriter writer, DateTime value) {
			serializeNullable(value, writer);
		}
	};

	public static void serializeNullable(final DateTime value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serialize(value, sw);
		}
	}

	public static void serialize(final DateTime value, final JsonWriter sw) {
		final byte[] buf = sw.ensureCapacity(32);
		final int pos = sw.size();
		buf[pos] = '"';
		final int year = value.getYear();
		NumberConverter.write4(year, buf, pos + 1);
		buf[pos + 5] = '-';
		NumberConverter.write2(value.getMonthOfYear(), buf, pos + 6);
		buf[pos + 8] = '-';
		NumberConverter.write2(value.getDayOfMonth(), buf, pos + 9);
		buf[pos + 11] = 'T';
		NumberConverter.write2(value.getHourOfDay(), buf, pos + 12);
		buf[pos + 14] = ':';
		NumberConverter.write2(value.getMinuteOfHour(), buf, pos + 15);
		buf[pos + 17] = ':';
		NumberConverter.write2(value.getSecondOfMinute(), buf, pos + 18);
		final int milis = value.getMillisOfSecond();
		final int offset;
		if (milis != 0) {
			buf[pos + 20] = '.';
			final int hi = milis / 100;
			final int lo = milis - hi * 100;
			buf[pos + 21] = (byte) (hi + 48);
			if (lo != 0) {
				NumberConverter.write2(lo, buf, pos + 22);
				offset = 24 + writeTimezone(buf, pos + 24, value, sw);
			} else {
				offset = 22 + writeTimezone(buf, pos + 22, value, sw);
			}
		} else {
			offset = 20 + writeTimezone(buf, pos + 20, value, sw);
		}
		sw.advance(offset);
	}

	private static int writeTimezone(final byte[] buf, final int position, final DateTime dt, final JsonWriter sw) {
		final DateTimeZone zone = dt.getZone();
		if (utcZone.equals(zone) || zone == null) {
			buf[position] = 'Z';
			buf[position + 1] = '"';
			return 2;
		} else {
			final long ms = dt.getMillis();
			int off = zone.getOffset(ms);
			if (off < 0) {
				buf[position] = '-';
				off = -off;
			} else {
				buf[position] = '+';
			}
			final int hours = off / 3600000;
			final int remainder = off - hours * 3600000;
			NumberConverter.write2(hours, buf, position + 1);
			buf[position + 3] = ':';
			NumberConverter.write2(remainder / 60000, buf, position + 4);
			buf[position + 6] = '"';
			return 7;
		}
	}

	public static DateTime deserializeDateTime(final JsonReader reader) throws IOException {
		final char[] tmp = reader.readSimpleQuote();
		final int len = reader.getCurrentIndex() - reader.getTokenStart() - 1;
		//TODO: non utc
		if (len > 18 && len < 25 && tmp[len - 1] == 'Z' && tmp[4] == '-' && tmp[7] == '-'
				&& (tmp[10] == 'T' || tmp[10] == 't' || tmp[10] == ' ')
				&& tmp[13] == ':' && tmp[16] == ':') {
			final int year = NumberConverter.read4(tmp, 0);
			final int month = NumberConverter.read2(tmp, 5);
			final int day = NumberConverter.read2(tmp, 8);
			final int hour = NumberConverter.read2(tmp, 11);
			final int min = NumberConverter.read2(tmp, 14);
			final int sec = NumberConverter.read2(tmp, 17);
			if (tmp[19] == '.') {
				final int milis;
				switch (len) {
					case 22:
						milis = 100 * (tmp[20] - 48);
						break;
					case 23:
						milis = 100 * (tmp[20] - 48) + 10 * (tmp[21] - 48);
						break;
					default:
						milis = 100 * (tmp[20] - 48) + 10 * (tmp[21] - 48) + tmp[22] - 48;
						break;
				}
				return new DateTime(year, month, day, hour, min, sec, milis, utcZone);
			}
			return new DateTime(year, month, day, hour, min, sec, 0, utcZone);
		} else {
			return dateTimeParser.parseDateTime(new String(tmp, 0, len));
		}
	}

	public static List<DateTime> deserializeDateTimeCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(DateTimeReader);
	}

	public static void deserializeDateTimeCollection(final JsonReader reader, final Collection<DateTime> res) throws IOException {
		reader.deserializeCollection(DateTimeReader, res);
	}

	public static List<DateTime> deserializeDateTimeNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(DateTimeReader);
	}

	public static void deserializeDateTimeNullableCollection(final JsonReader reader, final Collection<DateTime> res) throws IOException {
		reader.deserializeNullableCollection(DateTimeReader, res);
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
		NumberConverter.write2(value.getMonthOfYear(), buf, pos + 6);
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
			return new LocalDate(year, month, day);
		} else {
			return localDateParser.parseLocalDate(new String(tmp, 0, len));
		}
	}

	public static List<LocalDate> deserializeLocalDateCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(LocalDateReader);
	}

	public static void deserializeLocalDateCollection(final JsonReader reader, final Collection<LocalDate> res) throws IOException {
		reader.deserializeCollection(LocalDateReader, res);
	}

	public static List<LocalDate> deserializeLocalDateNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(LocalDateReader);
	}

	public static void deserializeLocalDateNullableCollection(final JsonReader reader, final Collection<LocalDate> res) throws IOException {
		reader.deserializeNullableCollection(LocalDateReader, res);
	}
}
