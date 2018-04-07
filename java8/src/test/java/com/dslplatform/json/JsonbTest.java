package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

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
}
