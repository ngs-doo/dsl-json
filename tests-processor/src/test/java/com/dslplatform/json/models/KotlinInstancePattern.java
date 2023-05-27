package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonConverter;

import java.util.GregorianCalendar;

@CompiledJson
public class KotlinInstancePattern {
	public GregorianCalendar x;

	@JsonConverter(target = GregorianCalendar.class)
	public static final class CalendarConverter {
		private static final com.dslplatform.json.JsonReader.ReadObject<GregorianCalendar> JSON_READER = null;
		private static final com.dslplatform.json.JsonWriter.WriteObject<GregorianCalendar> JSON_WRITER = null;
		public static final CalendarConverter INSTANCE = null;
		public final com.dslplatform.json.JsonReader.ReadObject<GregorianCalendar> getJSON_READER() {
			return null;
		}
		public final com.dslplatform.json.JsonWriter.WriteObject<GregorianCalendar> getJSON_WRITER() {
			return null;
		}
		private CalendarConverter() {
			super();
		}
	}

}
