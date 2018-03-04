package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import com.dslplatform.json.runtime.TypeDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class MapTest {

	private final DslJson<Object> json = new DslJson<Object>(Settings.withRuntime());

	public static class Example {
		public Map<String, Map<String, Long>> mapMapLong;
		public Map<String, Map> mapMapUnknown;
		public Map<String, Example> mapSelf;
	}

	public static class Generic<K, V> {
		public Map<K, V> mapKV;
		public Map<K, Map<K, V>> mapMapKV;
	}

	@Test
	public void testResolution() throws IOException {
		Example wo = new Example();
		wo.mapMapLong = new HashMap<>();
		wo.mapMapLong.put("abc", new HashMap<>());
		wo.mapMapLong.put("def", null);
		HashMap<String, Long> sl = new HashMap<>();
		sl.put("x", 1L);
		sl.put("y", null);
		sl.put("z", -22L);
		wo.mapMapLong.put("sl", sl);
		wo.mapMapUnknown = new HashMap<>();
		wo.mapMapUnknown.put("abc", null);
		wo.mapMapUnknown.put("def", new HashMap<>());
		wo.mapMapUnknown.put("sl", sl);
		wo.mapSelf = new HashMap<>();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(wo, baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Example wo2 = json.deserialize(Example.class, bais);
		Assert.assertEquals(wo.mapMapLong, wo2.mapMapLong);
		Assert.assertEquals(wo.mapMapUnknown, wo2.mapMapUnknown);
		Assert.assertEquals(wo.mapSelf, wo2.mapSelf);
	}

	@Test
	public void testGeneric() throws IOException {
		Generic<String, Long> wo = new Generic<>();
		wo.mapKV = new HashMap<>();
		wo.mapKV.put("x", 1L);
		wo.mapKV.put("y", null);
		wo.mapKV.put("z", -22L);
		wo.mapMapKV = new HashMap<>();
		wo.mapMapKV.put("abc", new HashMap<>());
		wo.mapMapKV.put("def", null);
		wo.mapMapKV.put("g", wo.mapKV);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(wo, baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Generic<String, Long> wo2 = (Generic<String, Long>) json.deserialize(new TypeDefinition<Generic<String, Long>>() {}.type, bais);
		Assert.assertEquals(wo.mapKV, wo2.mapKV);
		Assert.assertEquals(wo.mapMapKV, wo2.mapMapKV);
	}

	@Test
	public void keysMustBeQuoted() throws IOException {
		Generic<Long, Long> wo = new Generic<>();
		wo.mapKV = new HashMap<>();
		wo.mapKV.put(2L, 1L);
		wo.mapKV.put(5L, null);
		wo.mapKV.put(-5L, -22L);
		wo.mapMapKV = new HashMap<>();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(wo, baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Generic<Long, Long> wo2 = (Generic<Long, Long>) json.deserialize(new TypeDefinition<Generic<Long, Long>>() {}.type, bais);
		Assert.assertEquals(wo.mapKV, wo2.mapKV);
		Assert.assertEquals(wo.mapMapKV, wo2.mapMapKV);
	}
}
