package dsl_json.org.threeten.bp;

import com.dslplatform.json.Configuration;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.ThreetenbpConverter;
import org.threeten.bp.LocalDateTime;

public class LocalDateTimeDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerReader(LocalDateTime.class, ThreetenbpConverter.LOCAL_DATE_TIME_READER);
		json.registerWriter(LocalDateTime.class, ThreetenbpConverter.LOCAL_DATE_TIME_WRITER);
	}
}