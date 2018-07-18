package dsl_json.org.joda.time;

import com.dslplatform.json.Configuration;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.JodaTimeConverter;

public class DateTimeDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerReader(org.joda.time.DateTime.class, JodaTimeConverter.DATE_TIME_READER);
		json.registerWriter(org.joda.time.DateTime.class, JodaTimeConverter.DATE_TIME_WRITER);
	}
}