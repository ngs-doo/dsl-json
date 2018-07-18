package dsl_json.org.threeten.bp;

import com.dslplatform.json.Configuration;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.ThreetenbpConverter;
import org.threeten.bp.ZonedDateTime;

public class ZonedDateTimeDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerReader(ZonedDateTime.class, ThreetenbpConverter.ZONED_DATE_TIME_READER);
		json.registerWriter(ZonedDateTime.class, ThreetenbpConverter.ZONED_DATE_TIME_WRITER);
	}
}