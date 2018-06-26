package com.dslplatform.json;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZonedDateTime;

public class ConfigureThreetenbp implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerReader(LocalDate.class, ThreetenbpConverter.LOCAL_DATE_READER);
		json.registerWriter(LocalDate.class, ThreetenbpConverter.LOCAL_DATE_WRITER);
		json.registerReader(LocalDateTime.class, ThreetenbpConverter.LocalDateTimeReader);
		json.registerWriter(LocalDateTime.class, ThreetenbpConverter.LocalDateTimeWriter);
		json.registerReader(OffsetDateTime.class, ThreetenbpConverter.DATE_TIME_READER);
		json.registerWriter(OffsetDateTime.class, ThreetenbpConverter.DATE_TIME_WRITER);
		json.registerReader(ZonedDateTime.class, ThreetenbpConverter.ZonedDateTimeReader);
		json.registerWriter(ZonedDateTime.class, ThreetenbpConverter.ZonedDateTimeWriter);
	}
}
