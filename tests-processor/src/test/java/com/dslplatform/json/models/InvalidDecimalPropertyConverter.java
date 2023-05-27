package com.dslplatform.json.models;

import com.dslplatform.json.*;

import java.io.IOException;
import java.math.BigDecimal;

@CompiledJson
public class InvalidDecimalPropertyConverter {
	@JsonAttribute(converter = FormatDecimal2.class)
	public BigDecimal d;

	public static abstract class FormatDecimal2 {
		public static final JsonReader.ReadObject<Long> JSON_READER = new JsonReader.ReadObject<Long>() {
			public Long read(JsonReader reader) throws IOException {
				return NumberConverter.deserializeLong(reader);
			}
		};
		public static final JsonWriter.WriteObject<BigDecimal> JSON_WRITER = new JsonWriter.WriteObject<BigDecimal>() {
			public void write(JsonWriter writer, BigDecimal value) {
				NumberConverter.serialize(value.setScale(2), writer);
			}
		};
	}
}
