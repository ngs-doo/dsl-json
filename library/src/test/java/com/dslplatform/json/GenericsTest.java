package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class GenericsTest {
	public abstract static class Generic<T> {
		public final Type type;

		protected Generic() {
			Type superclass = getClass().getGenericSuperclass();
			this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
		}
	}

	public static class JsonPartial implements JsonObject {
		@Override
		public void serialize(JsonWriter writer, boolean minimal) {
		}
	}

	public static class JsonFull implements JsonObject {
		@Override
		public void serialize(JsonWriter writer, boolean minimal) {
		}

		public static final JsonReader.ReadJsonObject<JsonFull> JSON_READER = new JsonReader.ReadJsonObject<JsonFull>() {
			@Override
			public JsonFull deserialize(JsonReader reader) throws IOException {
				return new JsonFull();
			}
		};
	}

	@Test
	public void checkType() throws IOException {
		List<Type> types = Arrays.asList(
				int.class,
				int[].class,
				JsonFull.class,
				JsonFull[].class,
				new Generic<List<Integer>>() {
				}.type,
				new Generic<Integer[]>() {
				}.type,
				new Generic<List<JsonFull>>() {
				}.type,
				new Generic<JsonFull[]>() {
				}.type
		);
		DslJson<Object> json = new DslJson<Object>();
		for (Type t : types) {
			Assert.assertTrue(json.canSerialize(t));
			Assert.assertTrue(json.canDeserialize(t));
		}
		Assert.assertTrue(json.canSerialize(JsonPartial.class));
		Assert.assertTrue(json.canSerialize(new Generic<List<JsonPartial>>() {
		}.type));
		Assert.assertFalse(json.canDeserialize(JsonPartial.class));
		Assert.assertFalse(json.canDeserialize(new Generic<List<JsonPartial>>() {
		}.type));
		//TODO: not supported yet
		Assert.assertFalse(json.canSerialize(int[][].class));
		Assert.assertFalse(json.canDeserialize(int[][].class));
	}

	@Test
	public void testListGenerics() throws IOException {
		DslJson<Object> json = new DslJson<Object>();
		JsonWriter writer = json.newWriter();
		List<Integer> items = Arrays.asList(1, 2);
		json.serialize(writer, items);
		String result = writer.toString();
		Assert.assertEquals("[1,2]", result);
		List<Integer> deserialized = (List) json.deserialize(new Generic<List<Integer>>() {
		}.type, writer.getByteBuffer(), writer.size());
		Assert.assertEquals(items, deserialized);
	}

	@Test
	public void testPrimitiveArray() throws IOException {
		DslJson<Object> json = new DslJson<Object>();
		JsonWriter writer = json.newWriter();
		int[] items = new int[]{1, 2};
		json.serialize(writer, items);
		String result = writer.toString();
		Assert.assertEquals("[1,2]", result);
		int[] deserialized = json.deserialize(int[].class, writer.getByteBuffer(), writer.size());
		Assert.assertArrayEquals(items, deserialized);
	}

	@Ignore("not supported yet")
	@Test
	public void testNestedArray() throws IOException {
		DslJson<Object> json = new DslJson<Object>();
		JsonWriter writer = json.newWriter();
		int[][] items = new int[2][];
		items[0] = new int[]{1, 2};
		items[1] = new int[]{3, 4, 5};
		json.serialize(writer, items);
		String result = writer.toString();
		Assert.assertEquals("[[1,2],[3,4,5]]", result);
		int[][] deserialized = json.deserialize(int[][].class, writer.getByteBuffer(), writer.size());
		Assert.assertArrayEquals(items, deserialized);
	}
}
