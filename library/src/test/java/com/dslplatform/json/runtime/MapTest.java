package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
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
		wo.mapMapLong = new HashMap<String, Map<String, Long>>();
		wo.mapMapLong.put("abc", new HashMap<String, Long>());
		wo.mapMapLong.put("def", null);
		HashMap<String, Long> sl = new HashMap<String, Long>();
		sl.put("x", 1L);
		sl.put("y", null);
		sl.put("z", -22L);
		wo.mapMapLong.put("sl", sl);
		wo.mapMapUnknown = new HashMap<String, Map>();
		wo.mapMapUnknown.put("abc", null);
		wo.mapMapUnknown.put("def", new HashMap<Object, Object>());
		wo.mapMapUnknown.put("sl", sl);
		wo.mapSelf = new HashMap<String, Example>();
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
		Generic<String, Long> wo = new Generic<String, Long>();
		wo.mapKV = new HashMap<String, Long>();
		wo.mapKV.put("x", 1L);
		wo.mapKV.put("y", null);
		wo.mapKV.put("z", -22L);
		wo.mapMapKV = new HashMap<String, Map<String, Long>>();
		wo.mapMapKV.put("abc", new HashMap<String, Long>());
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
		Generic<Long, Long> wo = new Generic<Long, Long>();
		wo.mapKV = new HashMap<Long, Long>();
		wo.mapKV.put(2L, 1L);
		wo.mapKV.put(5L, null);
		wo.mapKV.put(-5L, -22L);
		wo.mapMapKV = new HashMap<Long, Map<Long, Long>>();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(wo, baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Generic<Long, Long> wo2 = (Generic<Long, Long>) json.deserialize(new TypeDefinition<Generic<Long, Long>>() {}.type, bais);
		Assert.assertEquals(wo.mapKV, wo2.mapKV);
		Assert.assertEquals(wo.mapMapKV, wo2.mapMapKV);
	}

	public static class GenericMap<T> {
		public Map<String, T> map = new LinkedHashMap<String, T>();
	}

	@Test
	public void nonPrimitiveTypes() throws IOException {
		GenericMap<String> wo = new GenericMap<String>();
		wo.map.put("abc", "today");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(wo, baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		GenericMap<String> wo2 = (GenericMap<String>) json.deserialize(new TypeDefinition<GenericMap<String>>() {}.type, bais);
		Assert.assertEquals(wo.map, wo2.map);
	}
}
