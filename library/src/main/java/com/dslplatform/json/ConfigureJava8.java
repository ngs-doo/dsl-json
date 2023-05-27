package com.dslplatform.json;

import dsl_json.java.lang.ByteDslJsonConverter;
import dsl_json.java.math.BigIntegerDslJsonConverter;
import dsl_json.java.time.*;
import dsl_json.java.util.*;

public class ConfigureJava8 implements Configuration {

	@Override
	public void configure(DslJson json) {
		new LocalDateDslJsonConverter().configure(json);
		new LocalDateTimeDslJsonConverter().configure(json);
		new LocalTimeDslJsonConverter().configure(json);
		new OffsetDateTimeDslJsonConverter().configure(json);
		new OffsetTimeDslJsonConverter().configure(json);
		new ZonedDateTimeDslJsonConverter().configure(json);
		new dsl_json.java.sql.DateDslJsonConverter().configure(json);
		new dsl_json.java.sql.TimestampDslJsonConverter().configure(json);
		new dsl_json.java.util.DateDslJsonConverter().configure(json);
		new dsl_json.java.sql.ResultSetDslJsonConverter().configure(json);
		new ByteDslJsonConverter().configure(json);
		new OptionalDoubleDslJsonConverter().configure(json);
		new OptionalIntDslJsonConverter().configure(json);
		new OptionalLongDslJsonConverter().configure(json);
		new BigIntegerDslJsonConverter().configure(json);
		new OptionalDslJsonConverter().configure(json);
	}
}
