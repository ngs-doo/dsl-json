package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.time.*;

public class DateTest {

	@Test
	public void dateTimeOffsetConversion() throws IOException {
		OffsetDateTime now = OffsetDateTime.now();
		JsonWriter jw = new JsonWriter(null);
		JavaTimeConverter.serialize(now, jw);
		JsonReader jr = new JsonReader<>(jw.toString().getBytes("UTF-8"), null);
		jr.read();
		OffsetDateTime value = JavaTimeConverter.deserializeDateTime(jr);
		Assert.assertEquals(now, value);
	}

	@Test
	public void dateTimeOffsetUtcConversion() throws IOException {
		OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		JsonWriter jw = new JsonWriter(null);
		JavaTimeConverter.serialize(now, jw);
		JsonReader jr = new JsonReader<>(jw.toString().getBytes("UTF-8"), null);
		jr.read();
		OffsetDateTime value = JavaTimeConverter.deserializeDateTime(jr);
		Assert.assertEquals(now, value);
	}

	@Test
	public void localDateTimeConversion() throws IOException {
		LocalDateTime now = LocalDateTime.now();
		JsonWriter jw = new JsonWriter(null);
		JavaTimeConverter.serialize(now, jw);
		JsonReader jr = new JsonReader<>(jw.toString().getBytes("UTF-8"), null);
		jr.read();
		LocalDateTime value = JavaTimeConverter.deserializeLocalDateTime(jr);
		Assert.assertEquals(now, value);
	}

	@Test
	public void utcOffsetSpecificValues() throws IOException {
		String n = "";
		for(int i = 1; i <= 9; i++) {
			n += (char)(48 + i);
			OffsetDateTime value = OffsetDateTime.parse("1919-03-05T04:51:49." + n + "Z");
			JsonWriter jw = new JsonWriter(null);
			JavaTimeConverter.serialize(value, jw);
			JsonReader jr = new JsonReader<>(jw.toString().getBytes("UTF-8"), null);
			jr.read();
			OffsetDateTime deser = JavaTimeConverter.deserializeDateTime(jr);
			Assert.assertEquals(value, deser);
		}
	}

	@Test
	public void timezoneOffsetSpecificValues() throws IOException {
		String n = "";
		for(int i = 1; i <= 9; i++) {
			n += (char)(48 + i);
			OffsetDateTime value = OffsetDateTime.parse("1919-03-05T04:51:49." + n + "+01:00");
			JsonWriter jw = new JsonWriter(null);
			JavaTimeConverter.serialize(value, jw);
			JsonReader jr = new JsonReader<>(jw.toString().getBytes("UTF-8"), null);
			jr.read();
			OffsetDateTime deser = JavaTimeConverter.deserializeDateTime(jr);
			Assert.assertEquals(value, deser);
		}
	}

	@Test
	public void localtimeSpecificValues() throws IOException {
		String n = "";
		for(int i = 1; i <= 9; i++) {
			n += (char)(48 + i);
			LocalDateTime value = LocalDateTime.parse("1919-03-05T04:51:49." + n);
			JsonWriter jw = new JsonWriter(null);
			JavaTimeConverter.serialize(value, jw);
			JsonReader jr = new JsonReader<>(jw.toString().getBytes("UTF-8"), null);
			jr.read();
			LocalDateTime deser = JavaTimeConverter.deserializeLocalDateTime(jr);
			Assert.assertEquals(value, deser);
		}
	}
}
