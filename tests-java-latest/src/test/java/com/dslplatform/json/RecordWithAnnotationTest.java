package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RecordWithAnnotationTest {

	@Documented
	@Retention(RetentionPolicy.CLASS)
	@Target({ElementType.TYPE_USE})
	public @interface CustomAnnotation {
	}

	public enum SerializableEnum {
		YES,
		NO
	}

	public static class SerializableEnumConverter {
		private static final Map<String, SerializableEnum> stringToEnumMap = new ConcurrentHashMap<>();
		private static final Map<SerializableEnum, String> enumToStringMap = new ConcurrentHashMap<>();

		static {
			final String yes = "yes";
			final String no = "no";

			stringToEnumMap.put(yes, SerializableEnum.YES);
			stringToEnumMap.put(no, SerializableEnum.NO);

			enumToStringMap.put(SerializableEnum.YES, yes);
			enumToStringMap.put(SerializableEnum.NO, no);
		}

		public static final JsonReader.ReadObject<SerializableEnum> JSON_READER = reader -> {
			if (reader.wasNull()) return null;
			final String value = reader.readSimpleString();

			final SerializableEnum v = stringToEnumMap.get(value);
			if (v == null) {
				throw reader.newParseError("error description...");
			}

			return v;
		};

		public static final JsonWriter.WriteObject<SerializableEnum> JSON_WRITER = (writer, value) -> {
			if (value == null) {
				writer.writeNull();
			} else {
				writer.writeString(enumToStringMap.get(value));
			}
		};
	}

	@CompiledJson(onUnknown = CompiledJson.Behavior.FAIL)
	public record SerializableRecord(
			@CustomAnnotation
			@JsonAttribute(name = SIMPLE_FIELD)
			String simpleField,

			@CustomAnnotation
			@JsonAttribute(name = ENUM, converter = SerializableEnumConverter.class)
			SerializableEnum serializableEnum
	) {
		private static final String SIMPLE_FIELD = "simple_field";
		private static final String ENUM = "enum";
	}

	private final DslJson<Object> dslJson = new DslJson<>();

	@Test
	public void recordRoundtrip() throws IOException {
		SerializableRecord sr = new SerializableRecord("abc", SerializableEnum.YES);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(sr, os);
		Assert.assertEquals("{\"simple_field\":\"abc\",\"enum\":\"yes\"}", os.toString(StandardCharsets.UTF_8));
		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		SerializableRecord result = dslJson.deserialize(SerializableRecord.class, is);
		Assert.assertEquals(sr, result);
	}
}
