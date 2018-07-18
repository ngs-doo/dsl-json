package dsl_json.java.time;

import com.dslplatform.json.Configuration;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.JavaTimeConverter;

import java.time.LocalDateTime;

public class LocalDateTimeDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerReader(LocalDateTime.class, JavaTimeConverter.LOCAL_DATE_TIME_READER);
		json.registerWriter(LocalDateTime.class, JavaTimeConverter.LOCAL_DATE_TIME_WRITER);
	}
}