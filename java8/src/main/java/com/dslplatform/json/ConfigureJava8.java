package com.dslplatform.json;

import com.dslplatform.json.runtime.OptionalAnalyzer;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.*;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public class ConfigureJava8 implements Configuration {
	private static final JsonWriter.WriteObject<Byte> ByteWriter = new JsonWriter.WriteObject<Byte>() {
		@Override
		public void write(JsonWriter writer, Byte value) {
			if (value == null) writer.writeNull();
			else NumberConverter.serialize(value, writer);
		}
	};
	static final JsonReader.ReadObject<Byte> ByteReader = new JsonReader.ReadObject<Byte>() {
		@Override
		public Byte read(JsonReader reader) throws IOException {
			return (byte)NumberConverter.deserializeInt(reader);
		}
	};
	static final JsonReader.ReadObject<Byte> NullableByteReader = new JsonReader.ReadObject<Byte>() {
		@Override
		public Byte read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : (byte)NumberConverter.deserializeInt(reader);
		}
	};
	private static final JsonWriter.WriteObject<Short> ShortWriter = new JsonWriter.WriteObject<Short>() {
		@Override
		public void write(JsonWriter writer, Short value) {
			if (value == null) writer.writeNull();
			else NumberConverter.serialize(value, writer);
		}
	};
	static final JsonReader.ReadObject<Short> ShortReader = new JsonReader.ReadObject<Short>() {
		@Override
		public Short read(JsonReader reader) throws IOException {
			return (short)NumberConverter.deserializeInt(reader);
		}
	};
	static final JsonReader.ReadObject<Short> NullableShortReader = new JsonReader.ReadObject<Short>() {
		@Override
		public Short read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : (short)NumberConverter.deserializeInt(reader);
		}
	};

	@Override
	public void configure(DslJson json) {
		json.registerReader(LocalDate.class, JavaTimeConverter.LOCAL_DATE_READER);
		json.registerWriter(LocalDate.class, JavaTimeConverter.LOCAL_DATE_WRITER);
		json.registerReader(LocalDateTime.class, JavaTimeConverter.LocalDateTimeReader);
		json.registerWriter(LocalDateTime.class, JavaTimeConverter.LocalDateTimeWriter);
		json.registerReader(OffsetDateTime.class, JavaTimeConverter.DATE_TIME_READER);
		json.registerWriter(OffsetDateTime.class, JavaTimeConverter.DATE_TIME_WRITER);
		json.registerReader(ZonedDateTime.class, JavaTimeConverter.ZonedDateTimeReader);
		json.registerWriter(ZonedDateTime.class, JavaTimeConverter.ZonedDateTimeWriter);
		json.registerReader(java.sql.Date.class, new JsonReader.ReadObject<java.sql.Date>() {
			@Override
			public Date read(JsonReader reader) throws IOException {
				return reader.wasNull() ? null : java.sql.Date.valueOf(JavaTimeConverter.deserializeLocalDate(reader));
			}
		});
		json.registerWriter(java.sql.Date.class, new JsonWriter.WriteObject<java.sql.Date>() {
			@Override
			public void write(JsonWriter writer, java.sql.Date value) {
				if (value == null) writer.writeNull();
				else JavaTimeConverter.serialize(value.toLocalDate(), writer);
			}
		});
		json.registerReader(java.sql.Timestamp.class, new JsonReader.ReadObject<Timestamp>() {
			@Override
			public Timestamp read(JsonReader reader) throws IOException {
				return reader.wasNull() ? null : java.sql.Timestamp.from(JavaTimeConverter.deserializeDateTime(reader).toInstant());
			}
		});
		json.registerWriter(java.sql.Timestamp.class, new JsonWriter.WriteObject<java.sql.Timestamp>() {
			@Override
			public void write(JsonWriter writer, java.sql.Timestamp value) {
				if (value == null) writer.writeNull();
				else JavaTimeConverter.serialize(OffsetDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault()), writer);
			}
		});
		json.registerReader(java.util.Date.class, new JsonReader.ReadObject<java.util.Date>() {
			@Override
			public java.util.Date read(JsonReader reader) throws IOException {
				return reader.wasNull() ? null : java.util.Date.from(JavaTimeConverter.deserializeDateTime(reader).toInstant());
			}
		});
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
		json.registerWriter(OptionalDouble.class, new JsonWriter.WriteObject<OptionalDouble>() {
			@Override
			public void write(JsonWriter writer, OptionalDouble value) {
				if (value != null && value.isPresent()) NumberConverter.serialize(value.getAsDouble(), writer);
				else writer.writeNull();
			}
		});
		json.registerReader(OptionalDouble.class, new JsonReader.ReadObject<OptionalDouble>() {
			@Override
			public OptionalDouble read(JsonReader reader) throws IOException {
				return reader.wasNull() ? OptionalDouble.empty() : OptionalDouble.of(NumberConverter.deserializeDouble(reader));
			}
		});
		json.registerDefault(OptionalDouble.class, OptionalDouble.empty());
		json.registerWriter(OptionalInt.class, new JsonWriter.WriteObject<OptionalInt>() {
			@Override
			public void write(JsonWriter writer, OptionalInt value) {
				if (value != null && value.isPresent()) NumberConverter.serialize(value.getAsInt(), writer);
				else writer.writeNull();
			}
		});
		json.registerReader(OptionalInt.class, new JsonReader.ReadObject<OptionalInt>() {
			@Override
			public OptionalInt read(JsonReader reader) throws IOException {
				return reader.wasNull() ? OptionalInt.empty() : OptionalInt.of(NumberConverter.deserializeInt(reader));
			}
		});
		json.registerDefault(OptionalInt.class, OptionalInt.empty());
		json.registerWriter(OptionalLong.class, new JsonWriter.WriteObject<OptionalLong>() {
			@Override
			public void write(JsonWriter writer, OptionalLong value) {
				if (value != null && value.isPresent()) NumberConverter.serialize(value.getAsLong(), writer);
				else writer.writeNull();
			}
		});
		json.registerReader(OptionalLong.class, new JsonReader.ReadObject<OptionalLong>() {
			@Override
			public OptionalLong read(JsonReader reader) throws IOException {
				return reader.wasNull() ? OptionalLong.empty() : OptionalLong.of(NumberConverter.deserializeLong(reader));
			}
		});
		json.registerDefault(OptionalLong.class, OptionalLong.empty());
		json.registerWriter(BigInteger.class, BigIntegerConverter.Writer);
		json.registerReader(BigInteger.class, BigIntegerConverter.Reader);
		json.registerDefault(Optional.class, Optional.empty());
		json.writerFactories.add(0, OptionalAnalyzer.WRITER);
		json.readerFactories.add(0, OptionalAnalyzer.READER);
	}
}
