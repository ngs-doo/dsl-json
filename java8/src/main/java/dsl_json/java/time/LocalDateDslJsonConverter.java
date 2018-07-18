package dsl_json.java.time;

import com.dslplatform.json.*;

import java.time.LocalDate;

public class LocalDateDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerReader(LocalDate.class, JavaTimeConverter.LOCAL_DATE_READER);
		json.registerWriter(LocalDate.class, JavaTimeConverter.LOCAL_DATE_WRITER);
	}
}