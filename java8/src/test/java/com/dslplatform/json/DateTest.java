package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
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
	public void zonedDateTimeOffsetConversion() throws IOException {
		DslJson<Object> dslJson = new DslJson<>();
		ZonedDateTime now = ZonedDateTime.now();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		dslJson.serialize(now, baos);
		ZonedDateTime value = dslJson.deserialize(ZonedDateTime.class, baos.toByteArray(), baos.size());
		Assert.assertTrue(now.isEqual(value));
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

	@Test
	public void nineDigits() throws IOException {
		OffsetDateTime dt = OffsetDateTime.parse("1930-09-04T00:03:48.750431006Z");
		JsonWriter jw = new JsonWriter(null);
		JavaTimeConverter.serialize(dt, jw);
		Assert.assertEquals("\"" + dt.toString() + "\"", jw.toString());
	}

	public static class Nine {
		public OffsetDateTime at;
	}

	@Test
	public void nineDigitsInAClass() throws IOException {
		Nine n = new Nine();
		n.at = OffsetDateTime.parse("1930-09-04T00:03:48.750431006Z");
		DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime().with(new ConfigureJava8()));
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(n, os);;
		Assert.assertEquals("{\"at\":\"1930-09-04T00:03:48.750431006Z\"}", os.toString());
	}
}
