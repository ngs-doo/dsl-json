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
		//TODO: not supported by default yet
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

	static class ArrayReader implements DslJson.ConverterFactory<JsonReader.ReadObject> {
		@Override
		public JsonReader.ReadObject tryCreate(Type manifest, DslJson dslJson) {
			if (!int[][].class.equals(manifest)) return null;
			return new JsonReader.ReadObject<int[][]>() {
				@Override
				public int[][] read(JsonReader reader) throws IOException {
					if (reader.last() != '[') throw new IOException("Expecting '['");
					if (reader.getNextToken() == ']') return new int[0][];
					ArrayList<int[]> result = new ArrayList<int[]>();
					if (reader.wasNull()) {
						result.add(null);
					} else if (reader.last() == '[') {
						reader.getNextToken();
						result.add(NumberConverter.deserializeIntArray(reader));
					} else throw new IOException("Expecting '[' or null");
					while (reader.getNextToken() == ',') {
						reader.getNextToken();
						if (reader.wasNull()) {
							result.add(null);
						} else if (reader.last() == '[') {
							reader.getNextToken();
							result.add(NumberConverter.deserializeIntArray(reader));
						} else throw new IOException("Expecting '[' or null");
					}
					if (reader.last() != ']') throw new IOException("Expecting ']'");
					return result.toArray(new int[0][]);
				}
			};
		}
	}

	static class ArrayWriter implements DslJson.ConverterFactory<JsonWriter.WriteObject> {
		@Override
		public JsonWriter.WriteObject tryCreate(Type manifest, DslJson dslJson) {
			if (!int[][].class.equals(manifest)) return null;
			return new JsonWriter.WriteObject<int[][]>() {
				@Override
				public void write(JsonWriter writer, int[][] value) {
					if (value == null) {
						writer.writeNull();
					} else {
						writer.writeByte(JsonWriter.ARRAY_START);
						for (int i = 0; i < value.length; i++) {
							if (i != 0) writer.writeByte(JsonWriter.COMMA);
							NumberConverter.serialize(value[i], writer);
						}
						writer.writeByte(JsonWriter.ARRAY_END);
					}
				}
			};
		}
	}

	@Test
	public void testNestedArray() throws IOException {
		DslJson.Settings<Object> settings = new DslJson.Settings<Object>()
				.resolveReader(new ArrayReader())
				.resolveWriter(new ArrayWriter());
		DslJson<Object> json = new DslJson<Object>(settings);
		JsonWriter writer = json.newWriter();
		int[][] items = new int[2][];
		items[0] = new int[]{1, 2};
		items[1] = new int[]{3, 4, 5};
		json.serialize(writer, items);
		String result = writer.toString();
		Assert.assertEquals("[[1,2],[3,4,5]]", result);
		int[][] deserialized = json.deserialize(int[][].class, writer.getByteBuffer(), writer.size());
		Assert.assertArrayEquals(items, deserialized);
		Assert.assertTrue(json.canSerialize(int[][].class));
		Assert.assertTrue(json.canDeserialize(int[][].class));
	}
}
