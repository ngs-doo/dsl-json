package dsl_json.java.time;

import com.dslplatform.json.Configuration;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.JavaTimeConverter;

import java.time.LocalTime;

public class LocalTimeDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerReader(LocalTime.class, JavaTimeConverter.LOCAL_TIME_READER);
		json.registerWriter(LocalTime.class, JavaTimeConverter.LOCAL_TIME_WRITER);
	}
}