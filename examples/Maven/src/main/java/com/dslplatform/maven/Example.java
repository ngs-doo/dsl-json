package com.dslplatform.maven;

import com.dslplatform.json.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class Example {

	@CompiledJson
	public static class Model {
		@JsonAttribute(nullable = false) //indicate that field can't be null
		public String string;
		public List<Integer> integers;
		@JsonAttribute(name = "guids") //use alternative name in JSON
		public UUID[] uuids;
		public Set<BigDecimal> decimals;
		public Vector<Long> longs;
		@JsonAttribute(hashMatch = false) // exact name match can be forced, otherwise hash value will be used for matching
		public int number;
		@JsonAttribute(alternativeNames = {"old_nested", "old_nested2"}) //several JSON attribute names can be deserialized into this field
		public List<Nested> nested;
		public Abstract abs;//abstract classes or interfaces can be used
		public ParentClass inheritance;
		public List<State> states;
		public JsonObjectReference jsonObject; //object implementing JsonObject manage their own conversion. They must start with '{'
		public List<JsonObjectReference> jsonObjects;
		@JsonAttribute(ignore = true)
		public char ignored;
		public Date date; //date is not supported, but with the use of converter it can work
		public List<Date> dates;

		//explicitly referenced classes don't require @CompiledJson annotation
		public static class Nested {
			public long x;
			public double y;
			public float z;
		}

		public static abstract class Abstract {
			public int x;
		}

		//since this class is not explicitly referenced, but it's an extension of the abstract class used as a property
		//it needs to be decorated with annotation
		@CompiledJson
		public static class Concrete extends Abstract {
			public long y;
		}

		public static class BaseClass {
			public int a;
		}

		public static class ParentClass extends BaseClass {
			public long b;
		}

		public enum State {
			LOW(0),
			MID(1),
			HI(2);

			private final int value;

			State(int value) {
				this.value = value;
			}
		}

		public static class JsonObjectReference implements JsonObject {

			public final int x;
			public final String s;

			public JsonObjectReference(int x, String s) {
				this.x = x;
				this.s = s;
			}

			public void serialize(JsonWriter writer, boolean minimal) {
				writer.writeAscii("{\"x\":");
				NumberConverter.serialize(x, writer);
				writer.writeAscii(",\"s\":");
				StringConverter.serialize(s, writer);
				writer.writeAscii("}");
			}

			public static final JsonReader.ReadJsonObject<JsonObjectReference> JSON_READER = new JsonReader.ReadJsonObject<JsonObjectReference>() {
				public JsonObjectReference deserialize(JsonReader reader) throws IOException {
					reader.fillName();//"x"
					reader.getNextToken();//start number
					int x = NumberConverter.deserializeInt(reader);
					reader.getNextToken();//,
					reader.getNextToken();//start name
					reader.fillName();//"s"
					reader.getNextToken();//start string
					String s = StringConverter.deserialize(reader);
					reader.getNextToken();//}
					return new JsonObjectReference(x, s);
				}
			};
		}
		@JsonConverter(target = Date.class)
		public static abstract class DateConverter {
			public static final JsonReader.ReadObject<Date> JSON_READER = new JsonReader.ReadObject<Date>() {
				public Date read(JsonReader reader) throws IOException {
					long time = NumberConverter.deserializeLong(reader);
					return new Date(time);
				}
			};
			public static final JsonWriter.WriteObject<Date> JSON_WRITER = new JsonWriter.WriteObject<Date>() {
				public void write(JsonWriter writer, Date value) {
					if (value == null) {
						writer.writeNull();
					} else {
						NumberConverter.serialize(value.getTime(), writer);
					}
				}
			};
		}
	}

	public static void main(String[] args) throws IOException {

		//ServiceLoader.load will load Model since it will be registered into META-INF/services during annotation processing
		DslJson<Object> dslJson = new DslJson<Object>();
		//writer should be reused. For per thread reuse use ThreadLocal pattern
		JsonWriter writer = new JsonWriter();

		Model instance = new Model();
		instance.string = "Hello World!";
		instance.number = 42;
		instance.integers = Arrays.asList(1, 2, 3);
		instance.decimals = new HashSet<BigDecimal>(Arrays.asList(BigDecimal.ONE, BigDecimal.ZERO));
		instance.uuids = new UUID[]{new UUID(1L, 2L), new UUID(3L, 4L)};
		instance.longs = new Vector<Long>(Arrays.asList(1L, 2L));
		instance.nested = Arrays.asList(new Model.Nested(), null);
		instance.inheritance = new Model.ParentClass();
		instance.inheritance.a = 5;
		instance.inheritance.b = 6;
		instance.states = Arrays.asList(Model.State.HI, Model.State.LOW);
		instance.jsonObject = new Model.JsonObjectReference(43, "abcd");
		instance.jsonObjects = Collections.singletonList(new Model.JsonObjectReference(34, "dcba"));
		instance.date = new Date();
		instance.dates = Arrays.asList(null, new Date(0));
		Model.Concrete concrete = new Model.Concrete();
		concrete.x = 11;
		concrete.y = 23;
		instance.abs = concrete;

		dslJson.serialize(writer, instance);

		//resulting buffer with JSON
		byte[] buffer = writer.getByteBuffer();
		//end of buffer
		int size = writer.size();

		//deserialization using byte[] API
		Model deser = dslJson.deserialize(Model.class, buffer, size);

		System.out.println(deser.string);
	}
}
