package com.dslplatform.json;

import dsl_json.org.joda.time.*;

public class ConfigureJodaTime implements Configuration {
	@Override
	public void configure(DslJson json) {
		new LocalDateDslJsonConverter().configure(json);
		new DateTimeDslJsonConverter().configure(json);
	}
}
