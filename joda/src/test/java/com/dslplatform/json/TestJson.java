package com.dslplatform.json;

import org.joda.time.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class TestJson {

	@Test
	public void dateTimeOffsetConversion() throws IOException {
		DateTime now = DateTime.now();
		JsonWriter jw = new JsonWriter(null);
		JodaTimeConverter.serialize(now, jw);
		JsonReader<Object> jr = new JsonReader<Object>(jw.toString().getBytes("UTF-8"), null);
		jr.read();
		DateTime value = JodaTimeConverter.deserializeDateTime(jr);
		Assert.assertEquals(0, now.compareTo(value));
	}

	@Test
	public void dateTimeOffsetUtcConversion() throws IOException {
		DateTime now = DateTime.now(DateTimeZone.UTC);
		JsonWriter jw = new JsonWriter(null);
		JodaTimeConverter.serialize(now, jw);
		JsonReader<Object> jr = new JsonReader<Object>(jw.toString().getBytes("UTF-8"), null);
		jr.read();
		DateTime value = JodaTimeConverter.deserializeDateTime(jr);
		Assert.assertEquals(now, value);
	}

	@Test
	public void localDateConversion() throws IOException {
		LocalDate today = LocalDate.now();
		JsonWriter jw = new JsonWriter(null);
		JodaTimeConverter.serialize(today, jw);
		JsonReader<Object> jr = new JsonReader<Object>(jw.toString().getBytes("UTF-8"), null);
		jr.read();
		LocalDate value = JodaTimeConverter.deserializeLocalDate(jr);
		Assert.assertEquals(today, value);
	}
}
