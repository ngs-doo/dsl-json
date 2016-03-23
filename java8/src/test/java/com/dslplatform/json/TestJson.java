package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.time.*;

public class TestJson {

	@Test
	public void dateTimeOffsetConversion() throws IOException {
		OffsetDateTime now = OffsetDateTime.now();
		JsonWriter jw = new JsonWriter();
		JavaTimeConverter.serialize(now, jw);
		JsonReader jr = new JsonReader<>(jw.toString().getBytes("UTF-8"), null);
		jr.read();
		OffsetDateTime value = JavaTimeConverter.deserializeDateTime(jr);
		Assert.assertEquals(now, value);
	}

	@Test
	public void dateTimeOffsetUtcConversion() throws IOException {
		OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		JsonWriter jw = new JsonWriter();
		JavaTimeConverter.serialize(now, jw);
		JsonReader jr = new JsonReader<>(jw.toString().getBytes("UTF-8"), null);
		jr.read();
		OffsetDateTime value = JavaTimeConverter.deserializeDateTime(jr);
		Assert.assertEquals(now, value);
	}

	@Test
	public void localDateTimeConversion() throws IOException {
		LocalDateTime now = LocalDateTime.now();
		JsonWriter jw = new JsonWriter();
		JavaTimeConverter.serialize(now, jw);
		JsonReader jr = new JsonReader<>(jw.toString().getBytes("UTF-8"), null);
		jr.read();
		LocalDateTime value = JavaTimeConverter.deserializeLocalDateTime(jr);
		Assert.assertEquals(now, value);
	}
}
