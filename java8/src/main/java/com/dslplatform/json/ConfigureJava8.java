package com.dslplatform.json;

import com.dslplatform.json.runtime.OptionalAnalyzer;

import java.io.IOException;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class ConfigureJava8 implements Configuration {
	private static final JsonWriter.WriteObject<Byte> ByteWriter = (writer, value) -> {
		if (value == null) writer.writeNull();
		else NumberConverter.serialize(value, writer);
	};
	static final JsonReader.ReadObject<Byte> ByteReader = reader -> (byte)NumberConverter.deserializeInt(reader);
	static final JsonReader.ReadObject<Byte> NullableByteReader = reader -> reader.wasNull() ? null : (byte)NumberConverter.deserializeInt(reader);
	private static final JsonWriter.WriteObject<Short> ShortWriter = (writer, value) -> {
		if (value == null) writer.writeNull();
		else NumberConverter.serialize(value, writer);
	};
	static final JsonReader.ReadObject<Short> ShortReader = reader -> (short)NumberConverter.deserializeInt(reader);
	static final JsonReader.ReadObject<Short> NullableShortReader = reader -> reader.wasNull() ? null : (short)NumberConverter.deserializeInt(reader);

	@Override
	public void configure(DslJson json) {
		json.registerReader(LocalDate.class, JavaTimeConverter.LocalDateReader);
		json.registerWriter(LocalDate.class, JavaTimeConverter.LocalDateWriter);
		json.registerReader(LocalDateTime.class, JavaTimeConverter.LocalDateTimeReader);
		json.registerWriter(LocalDateTime.class, JavaTimeConverter.LocalDateTimeWriter);
		json.registerReader(OffsetDateTime.class, JavaTimeConverter.DateTimeReader);
		json.registerWriter(OffsetDateTime.class, JavaTimeConverter.DateTimeWriter);
		json.registerReader(java.sql.Date.class, rdr -> rdr.wasNull() ? null : java.sql.Date.valueOf(JavaTimeConverter.deserializeLocalDate(rdr)));
		json.registerWriter(java.sql.Date.class, new JsonWriter.WriteObject<java.sql.Date>() {
			@Override
			public void write(JsonWriter writer, java.sql.Date value) {
				if (value == null) writer.writeNull();
				else JavaTimeConverter.serialize(value.toLocalDate(), writer);
			}
		});
		json.registerReader(java.sql.Timestamp.class, rdr -> rdr.wasNull() ? null : java.sql.Timestamp.from(JavaTimeConverter.deserializeDateTime(rdr).toInstant()));
		json.registerWriter(java.sql.Timestamp.class, new JsonWriter.WriteObject<java.sql.Timestamp>() {
			@Override
			public void write(JsonWriter writer, java.sql.Timestamp value) {
				if (value == null) writer.writeNull();
				else JavaTimeConverter.serialize(OffsetDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault()), writer);
			}
		});
		json.registerReader(java.util.Date.class, rdr -> rdr.wasNull() ? null : java.util.Date.from(JavaTimeConverter.deserializeDateTime(rdr).toInstant()));
		json.registerWriter(java.util.Date.class, new JsonWriter.WriteObject<java.util.Date>() {
			@Override
			public void write(JsonWriter writer, java.util.Date value) {
				if (value == null) writer.writeNull();
				else JavaTimeConverter.serialize(OffsetDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault()), writer);
			}
		});
		json.registerWriter(ResultSet.class, ResultSetConverter.Writer);
		json.registerWriter(byte.class, ByteWriter);
		json.registerReader(byte.class, ByteReader);
		json.registerWriter(Byte.class, ByteWriter);
		json.registerReader(Byte.class, NullableByteReader);
		json.registerWriter(short.class, ShortWriter);
		json.registerReader(short.class, ShortReader);
		json.registerWriter(Short.class, ShortWriter);
		json.registerReader(Short.class, NullableShortReader);
		json.writerFactories.add(OptionalAnalyzer.WRITER);
		json.readerFactories.add(OptionalAnalyzer.READER);
	}
}
