package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Arrays;

public class DateTest {

	@Test
	public void dateTimeOffsetConversion() throws IOException {
		OffsetDateTime now = OffsetDateTime.now();
		JsonWriter jw = new JsonWriter(null);
		JavaTimeConverter.serialize(now, jw);
		JsonReader jr = new JsonReader<>(jw.toString().getBytes(StandardCharsets.UTF_8), null);
		jr.read();
		OffsetDateTime value = JavaTimeConverter.deserializeDateTime(jr);
		Assert.assertEquals(now, value);
	}

	@Test
	public void dateTimeOffsetUtcConversion() throws IOException {
		OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		JsonWriter jw = new JsonWriter(null);
		JavaTimeConverter.serialize(now, jw);
		JsonReader jr = new JsonReader<>(jw.toString().getBytes(StandardCharsets.UTF_8), null);
		jr.read();
		OffsetDateTime value = JavaTimeConverter.deserializeDateTime(jr);
		Assert.assertEquals(now, value);
	}

	@Test
	public void timeOffsetConversion() throws IOException {
		OffsetTime now = OffsetTime.now();
		JsonWriter jw = new JsonWriter(null);
		JavaTimeConverter.serialize(now, jw);
		JsonReader jr = new JsonReader<>(jw.toString().getBytes(StandardCharsets.UTF_8), null);
		jr.read();
		OffsetTime value = JavaTimeConverter.deserializeOffsetTime(jr);
		Assert.assertEquals(now, value);
	}

	@Test
	public void negativeTimeOffsetConversion() throws IOException {
		OffsetTime now = OffsetTime.parse("12:13:14.123456789-12:15");
		JsonWriter jw = new JsonWriter(null);
		JavaTimeConverter.serialize(now, jw);
		JsonReader jr = new JsonReader<>(jw.toString().getBytes(StandardCharsets.UTF_8), null);
		jr.read();
		OffsetTime value = JavaTimeConverter.deserializeOffsetTime(jr);
		Assert.assertEquals(now, value);
	}

	@Test
	public void timeOffsetUtcConversion() throws IOException {
		OffsetTime now = OffsetTime.now(ZoneOffset.UTC);
		JsonWriter jw = new JsonWriter(null);
		JavaTimeConverter.serialize(now, jw);
		JsonReader jr = new JsonReader<>(jw.toString().getBytes(StandardCharsets.UTF_8), null);
		jr.read();
		OffsetTime value = JavaTimeConverter.deserializeOffsetTime(jr);
		Assert.assertEquals(now, value);
	}

	@Test
	public void localDateTimeConversion() throws IOException {
		LocalDateTime now = LocalDateTime.now();
		JsonWriter jw = new JsonWriter(null);
		JavaTimeConverter.serialize(now, jw);
		JsonReader jr = new JsonReader<>(jw.toString().getBytes(StandardCharsets.UTF_8), null);
		jr.read();
		LocalDateTime value = JavaTimeConverter.deserializeLocalDateTime(jr);
		Assert.assertEquals(now, value);
	}

	@Test
	public void localTimeConversion() throws IOException {
		LocalTime now = LocalTime.now();
		JsonWriter jw = new JsonWriter(null);
		JavaTimeConverter.serialize(now, jw);
		JsonReader jr = new JsonReader<>(jw.toString().getBytes(StandardCharsets.UTF_8), null);
		jr.read();
		LocalTime value = JavaTimeConverter.deserializeLocalTime(jr);
		Assert.assertEquals(now, value);
	}

	@Test
	public void localTimeBoundary() throws IOException {
		DslJson<Object> dslJson = new DslJson<>();
		LocalTime now = LocalTime.parse("12:13:14.123456789");
		JsonWriter jw = dslJson.newWriter(20);
		LocalTime[] lts = new LocalTime[2];
		Arrays.fill(lts, now);
		dslJson.serialize(jw, lts);
		LocalTime[] values = dslJson.deserialize(LocalTime[].class, jw.getByteBuffer(), jw.size());
		Assert.assertArrayEquals(lts, values);
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
	public void zonedDateTimeBoundary() throws IOException {
		DslJson<Object> dslJson = new DslJson<>();
		ZonedDateTime now = ZonedDateTime.parse("2020-01-12T12:13:14.123456789-03:30");
		JsonWriter jw = dslJson.newWriter(34);
		ZonedDateTime[] lts = new ZonedDateTime[3];
		Arrays.fill(lts, now);
		dslJson.serialize(jw, lts);
		ZonedDateTime[] values = dslJson.deserialize(ZonedDateTime[].class, jw.getByteBuffer(), jw.size());
		Assert.assertArrayEquals(lts, values);
	}

	@Test
	public void utcDateOffsetSpecificValues() throws IOException {
		String n = "";
		String[] values = new String[10];
		values[0] = "1919-03-05T04:51:49Z";
		for (int i = 1; i <= 9; i++) {
			n += (char) (48 + i);
			values[i] = "1919-03-05T04:51:49." + n + "Z";
		}
		for (String v : values) {
			OffsetDateTime value = OffsetDateTime.parse(v);
			JsonWriter jw = new JsonWriter(null);
			JavaTimeConverter.serialize(value, jw);
			JsonReader jr = new JsonReader<>(jw.toString().getBytes(StandardCharsets.UTF_8), null);
			jr.read();
			OffsetDateTime deser = JavaTimeConverter.deserializeDateTime(jr);
			Assert.assertEquals(value, deser);
		}
	}

	@Test
	public void timezoneDateOffsetSpecificValues() throws IOException {
		String n = "";
		String[] values = new String[10];
		values[0] = "1919-03-05T04:51:49+01:00";
		for(int i = 1; i <= 9; i++) {
			n += (char) (48 + i);
			values[i] = "1919-03-05T04:51:49." + n + "+01:00";
		}
		for (String v : values) {
			OffsetDateTime value = OffsetDateTime.parse(v);
			JsonWriter jw = new JsonWriter(null);
			JavaTimeConverter.serialize(value, jw);
			JsonReader jr = new JsonReader<>(jw.toString().getBytes(StandardCharsets.UTF_8), null);
			jr.read();
			OffsetDateTime deser = JavaTimeConverter.deserializeDateTime(jr);
			Assert.assertEquals(value, deser);
		}
	}

	@Test
	public void utcTimeOffsetSpecificValues() throws IOException {
		String n = "";
		String[] values = new String[10];
		values[0] = "04:51:49Z";
		for(int i = 1; i <= 9; i++) {
			n += (char) (48 + i);
			values[i] = "04:51:49." + n + "Z";
		}
		for (String v : values) {
			OffsetTime value = OffsetTime.parse(v);
			JsonWriter jw = new JsonWriter(null);
			JavaTimeConverter.serialize(value, jw);
			JsonReader jr = new JsonReader<>(jw.toString().getBytes(StandardCharsets.UTF_8), null);
			jr.read();
			OffsetTime deser = JavaTimeConverter.deserializeOffsetTime(jr);
			Assert.assertEquals(value, deser);
		}
	}

	@Test
	public void timezoneTimeOffsetSpecificValues() throws IOException {
		String n = "";
		String[] values = new String[10];
		values[0] = "04:51:49+01:00";
		for(int i = 1; i <= 9; i++) {
			n += (char)(48 + i);
			values[i] = "04:51:49." + n + "+01:00";
		}
		for (String v : values) {
			OffsetTime value = OffsetTime.parse(v);
			JsonWriter jw = new JsonWriter(null);
			JavaTimeConverter.serialize(value, jw);
			JsonReader jr = new JsonReader<>(jw.toString().getBytes(StandardCharsets.UTF_8), null);
			jr.read();
			OffsetTime deser = JavaTimeConverter.deserializeOffsetTime(jr);
			Assert.assertEquals(value, deser);
		}
	}

	@Test
	public void localDateTimeSpecificValues() throws IOException {
		String n = "";
		String[] values = new String[10];
		values[0] = "1919-03-05T04:51:49";
		for (int i = 1; i <= 9; i++) {
			n += (char) (48 + i);
			values[i] = "1919-03-05T04:51:49." + n;
		}
		for (String v : values) {
			LocalDateTime value = LocalDateTime.parse(v);
			JsonWriter jw = new JsonWriter(null);
			JavaTimeConverter.serialize(value, jw);
			JsonReader jr = new JsonReader<>(jw.toString().getBytes(StandardCharsets.UTF_8), null);
			jr.read();
			LocalDateTime deser = JavaTimeConverter.deserializeLocalDateTime(jr);
			Assert.assertEquals(value, deser);
		}
	}

	@Test
	public void localTimeSpecificValues() throws IOException {
		String n = "";
		String[] values = new String[10];
		values[0] = "04:51:49";
		for (int i = 1; i <= 9; i++) {
			n += (char) (48 + i);
			values[i] = "04:51:49." + n;
		}
		for (String v : values) {
			LocalTime value = LocalTime.parse(v);
			JsonWriter jw = new JsonWriter(null);
			JavaTimeConverter.serialize(value, jw);
			JsonReader jr = new JsonReader<>(jw.toString().getBytes(StandardCharsets.UTF_8), null);
			jr.read();
			LocalTime deser = JavaTimeConverter.deserializeLocalTime(jr);
			Assert.assertEquals(value, deser);
		}
	}

	@Test
	public void nineDigitsODT() {
		OffsetDateTime dt = OffsetDateTime.parse("1930-09-04T00:03:48.750431006Z");
		JsonWriter jw = new JsonWriter(null);
		JavaTimeConverter.serialize(dt, jw);
		Assert.assertEquals("\"" + dt.toString() + "\"", jw.toString());
	}

	@Test
	public void nineDigitsOT() {
		OffsetTime dt = OffsetTime.parse("00:03:48.750431006Z");
		JsonWriter jw = new JsonWriter(null);
		JavaTimeConverter.serialize(dt, jw);
		Assert.assertEquals("\"" + dt.toString() + "\"", jw.toString());
	}

	@Test
	public void nineDigitsLT() {
		LocalTime dt = LocalTime.parse("00:03:48.750431006");
		JsonWriter jw = new JsonWriter(null);
		JavaTimeConverter.serialize(dt, jw);
		Assert.assertEquals("\"" + dt.toString() + "\"", jw.toString());
	}

	public static class NineODT {
		public OffsetDateTime at;
	}

	@Test
	public void nineDigitsInAODTClass() throws IOException {
		NineODT n = new NineODT();
		n.at = OffsetDateTime.parse("1930-09-04T00:03:48.750431006Z");
		DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime());
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(n, os);;
		Assert.assertEquals("{\"at\":\"1930-09-04T00:03:48.750431006Z\"}", os.toString());
	}

	public static class NineOT {
		public OffsetTime at;
	}

	@Test
	public void nineDigitsInAOTClass() throws IOException {
		NineOT n = new NineOT();
		n.at = OffsetTime.parse("01:33:08.750431006Z");
		DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime());
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(n, os);;
		Assert.assertEquals("{\"at\":\"01:33:08.750431006Z\"}", os.toString());
	}

	public static class ModelLDT {

		public LocalDateTime now;

		public LocalDateTime date;
	}

	@Test
	public void twoDateTimes() throws IOException {
		ModelLDT model = new ModelLDT();
		model.date = LocalDateTime.of(2018, 12, 25, 1, 0);
		model.now = LocalDateTime.now();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime().skipDefaultValues(true));
		dslJson.serialize(model, os);
		byte[] bytes = os.toByteArray();
		System.out.println(new String(bytes));
		for (int i = 0; i < 1000; i++) {
			ModelLDT result = dslJson.deserialize(ModelLDT.class, bytes, bytes.length);
			Assert.assertEquals(model.date, result.date);
			Assert.assertEquals(model.now, result.now);
		}
	}

	public static class ModelODT {

		public OffsetDateTime now;

		public OffsetDateTime date;
	}

	@Test
	public void twoOffsets() throws IOException {
		ModelODT model = new ModelODT();
		model.date = OffsetDateTime.of(2018, 12, 25, 1, 0, 0, 0, ZoneOffset.UTC);
		model.now = OffsetDateTime.now();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime().skipDefaultValues(true));
		dslJson.serialize(model, os);
		byte[] bytes = os.toByteArray();
		System.out.println(new String(bytes));
		for (int i = 0; i < 1000; i++) {
			ModelODT result = dslJson.deserialize(ModelODT.class, bytes, bytes.length);
			Assert.assertEquals(model.date, result.date);
			Assert.assertEquals(model.now, result.now);
		}
	}
}
