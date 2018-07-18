package dsl_json.org.joda.time;

import com.dslplatform.json.Configuration;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.JodaTimeConverter;

public class LocalDateDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerReader(org.joda.time.LocalDate.class, JodaTimeConverter.LOCAL_DATE_READER);
		json.registerWriter(org.joda.time.LocalDate.class, JodaTimeConverter.LOCAL_DATE_WRITER);
	}
}