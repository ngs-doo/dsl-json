package dsl_json.org.threeten.bp;

import com.dslplatform.json.Configuration;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.ThreetenbpConverter;
import org.threeten.bp.OffsetDateTime;

public class OffsetDateTimeDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerReader(OffsetDateTime.class, ThreetenbpConverter.DATE_TIME_READER);
		json.registerWriter(OffsetDateTime.class, ThreetenbpConverter.DATE_TIME_WRITER);
	}
}