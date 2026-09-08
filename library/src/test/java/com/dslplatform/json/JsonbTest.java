package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

public class JsonbTest {

	public static class SimpleClass {
		public int x;
		private String y1;
		public String getY() {
			return y1;
		}
		public void setY(String v) {
			y1 = v;
		}
	}

	public static class DefaultsAndNulls {
		public int count;
		public boolean active;
		public String name;
		public SimpleClass nested;
	}

	@Test
	public void checkSimple() {
		SimpleClass sc = new SimpleClass();
		sc.x = 12;
		sc.setY("abc");
		Jsonb jsonb = JsonbBuilder.create();
		String json = jsonb.toJson(sc);
		SimpleClass sc2 = jsonb.fromJson(json, SimpleClass.class);
		Assert.assertEquals(sc.x, sc2.x);
		Assert.assertEquals(sc.getY(), sc2.getY());
	}

	@Test
	public void omitNullsKeepsPrimitiveDefaults() {
		DefaultsAndNulls value = new DefaultsAndNulls();
		Jsonb jsonb = JsonbBuilder.create();
		String json = jsonb.toJson(value);
		Assert.assertTrue(json.contains("\"count\":0"));
		Assert.assertTrue(json.contains("\"active\":false"));
		Assert.assertFalse(json.contains("name"));
		Assert.assertFalse(json.contains("nested"));
		Assert.assertFalse(json.contains("null"));
	}

	@Test
	public void includeNullsWhenConfigured() {
		DefaultsAndNulls value = new DefaultsAndNulls();
		Jsonb jsonb = JsonbBuilder.newBuilder()
				.withConfig(new JsonbConfig().withNullValues(true))
				.build();
		String json = jsonb.toJson(value);
		Assert.assertTrue(json.contains("\"count\":0"));
		Assert.assertTrue(json.contains("\"active\":false"));
		Assert.assertTrue(json.contains("\"name\":null"));
		Assert.assertTrue(json.contains("\"nested\":null"));
	}
}
