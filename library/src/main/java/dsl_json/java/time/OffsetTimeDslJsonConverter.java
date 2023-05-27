package dsl_json.java.time;

import com.dslplatform.json.Configuration;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.JavaTimeConverter;

import java.time.OffsetTime;

public class OffsetTimeDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerReader(OffsetTime.class, JavaTimeConverter.OFFSET_TIME_READER);
		json.registerWriter(OffsetTime.class, JavaTimeConverter.OFFSET_TIME_WRITER);
	}
}