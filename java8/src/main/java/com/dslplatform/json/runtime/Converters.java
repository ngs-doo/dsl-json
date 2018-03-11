package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.NumberConverter;

import java.io.IOException;

public abstract class Converters {
	public static void encodeInt(JsonWriter writer, int value) {
		NumberConverter.serialize(value, writer);
	}
	public static void encodeIntNullable(JsonWriter writer, Integer value) {
		NumberConverter.serialize(value, writer);
	}
	public static int decodeInt(JsonReader reader) throws IOException {
		return NumberConverter.deserializeInt(reader);
	}
	public static Integer decodeIntNullable(JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		return NumberConverter.deserializeInt(reader);
	}
	public static void encodeLong(JsonWriter writer, long value) {
		NumberConverter.serialize(value, writer);
	}
	public static void encodeLongNullable(JsonWriter writer, Long value) {
		NumberConverter.serialize(value, writer);
	}
	public static long decodeLong(JsonReader reader) throws IOException {
		return NumberConverter.deserializeLong(reader);
	}
	public static Long decodeLongNullable(JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		return NumberConverter.deserializeLong(reader);
	}
	public static void encodeFloat(JsonWriter writer, float value) {
		NumberConverter.serialize(value, writer);
	}
	public static void encodeFloatNullable(JsonWriter writer, Float value) {
		NumberConverter.serialize(value, writer);
	}
	public static float decodeFloat(JsonReader reader) throws IOException {
		return NumberConverter.deserializeFloat(reader);
	}
	public static Float decodeFloatNullable(JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		return NumberConverter.deserializeFloat(reader);
	}
	public static void encodeDouble(JsonWriter writer, double value) {
		NumberConverter.serialize(value, writer);
	}
	public static void encodeLongNullable(JsonWriter writer, Double value) {
		NumberConverter.serialize(value, writer);
	}
	public static double decodeDouble(JsonReader reader) throws IOException {
		return NumberConverter.deserializeDouble(reader);
	}
	public static Double decodeDoubleNullable(JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		return NumberConverter.deserializeDouble(reader);
	}
	public static void encodeStringNullable(JsonWriter writer, String value) {
		if (value == null) writer.writeNull();
		else writer.writeString(value);
	}
	public static String decodeStringNullable(JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		return reader.readString();
	}
}
