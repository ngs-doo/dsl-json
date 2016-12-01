package com.dslplatform.json;

public class ConfigureJodaTime implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerReader(org.joda.time.LocalDate.class, JodaTimeConverter.LocalDateReader);
		json.registerWriter(org.joda.time.LocalDate.class, JodaTimeConverter.LocalDateWriter);
		json.registerReader(org.joda.time.DateTime.class, JodaTimeConverter.DateTimeReader);
		json.registerWriter(org.joda.time.DateTime.class, JodaTimeConverter.DateTimeWriter);
	}
}
