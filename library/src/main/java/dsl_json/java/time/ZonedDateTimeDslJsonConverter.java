package dsl_json.java.time;

import com.dslplatform.json.Configuration;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.JavaTimeConverter;

import java.time.ZonedDateTime;

public class ZonedDateTimeDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerReader(ZonedDateTime.class, JavaTimeConverter.ZONED_DATE_TIME_READER);
		json.registerWriter(ZonedDateTime.class, JavaTimeConverter.ZONED_DATE_TIME_WRITER);
	}
}