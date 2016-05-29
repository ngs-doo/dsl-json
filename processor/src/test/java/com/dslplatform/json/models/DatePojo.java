package com.dslplatform.json.models;

import com.dslplatform.json.*;

import java.io.IOException;
import java.util.Date;

@CompiledJson
public class DatePojo {
	public Date d;

	@JsonConverter(target = Date.class)
	public static abstract class DateConverter {
		public static final JsonReader.ReadObject<Date> JSON_READER = new JsonReader.ReadObject<Date>() {
			public Date read(JsonReader reader) throws IOException {
				return new Date(NumberConverter.deserializeLong(reader));
			}
		};
		public static final JsonWriter.WriteObject<Date> JSON_WRITER = new JsonWriter.WriteObject<Date>() {
			public void write(JsonWriter writer, Date value) {
				NumberConverter.serialize(value.getTime(), writer);
			}
		};
	}
}
