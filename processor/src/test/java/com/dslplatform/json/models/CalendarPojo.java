package com.dslplatform.json.models;

import com.dslplatform.json.*;

import java.io.IOException;
import java.util.GregorianCalendar;

@CompiledJson
public class CalendarPojo {
	public GregorianCalendar c;

	@JsonConverter(target = GregorianCalendar.class)
	public static abstract class CalendarConverter {
		public static final JsonReader.ReadObject<GregorianCalendar> JSON_READER = new JsonReader.ReadObject<GregorianCalendar>() {
			public GregorianCalendar read(JsonReader reader) throws IOException {
				return new GregorianCalendar(0, 0, 0);
			}
		};
		public static final JsonWriter.WriteObject<GregorianCalendar> JSON_WRITER = new JsonWriter.WriteObject<GregorianCalendar>() {
			public void write(JsonWriter writer, @Nullable GregorianCalendar value) {
			}
		};
	}
}
