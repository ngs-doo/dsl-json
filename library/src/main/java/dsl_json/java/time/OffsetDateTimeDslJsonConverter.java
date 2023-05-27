package dsl_json.java.time;

import com.dslplatform.json.Configuration;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.JavaTimeConverter;

import java.time.OffsetDateTime;

public class OffsetDateTimeDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerReader(OffsetDateTime.class, JavaTimeConverter.DATE_TIME_READER);
		json.registerWriter(OffsetDateTime.class, JavaTimeConverter.DATE_TIME_WRITER);
	}
}