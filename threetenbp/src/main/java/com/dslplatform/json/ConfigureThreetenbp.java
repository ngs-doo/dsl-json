package com.dslplatform.json;

import dsl_json.org.threeten.bp.*;

public class ConfigureThreetenbp implements Configuration {
	@Override
	public void configure(DslJson json) {
		new LocalDateDslJsonConverter().configure(json);
		new LocalDateTimeDslJsonConverter().configure(json);
		new OffsetDateTimeDslJsonConverter().configure(json);
		new ZonedDateTimeDslJsonConverter().configure(json);
	}
}
