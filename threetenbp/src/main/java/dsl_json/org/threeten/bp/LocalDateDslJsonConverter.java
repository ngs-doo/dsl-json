package dsl_json.org.threeten.bp;

import com.dslplatform.json.Configuration;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.ThreetenbpConverter;
import org.threeten.bp.LocalDate;

public class LocalDateDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerReader(LocalDate.class, ThreetenbpConverter.LOCAL_DATE_READER);
		json.registerWriter(LocalDate.class, ThreetenbpConverter.LOCAL_DATE_WRITER);
	}
}