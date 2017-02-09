package com.dslplatform.maven;

import com.dslplatform.json.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class Example {

	@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE) //ignore unknown properties (default for objects). to disallow unknown properties in JSON set it to FAIL which will result in exception instead
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
		@JsonAttribute(mandatory = true) // mandatory adds check if property exist in JSON and will serialize it even in omit-defaults mode
		public List<State> states;
		public JsonObjectReference jsonObject; //object implementing JsonObject manage their own conversion. They must start with '{'
		public List<JsonObjectReference> jsonObjects;
		@JsonAttribute(ignore = true)
		public char ignored;
		public Date date; //date is not supported, but with the use of converter it can work
		public List<Date> dates;
		@JsonAttribute(converter = FormatDecimal2.class)
		public BigDecimal decimal2; //custom formatting can be implemented with per property converters
		@JsonAttribute(converter = SupportArrayList.class)
		public ArrayList<Integer> intList; //unsupported collections can be supported through property converters
		public Map<String, Object> map; //even unknown stuff can be used. If it fails it will throw SerializationException
		public ImmutablePerson person; //immutable objects can be supported with helper DTO implementations

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
		public static abstract class FormatDecimal2 {
			public static final JsonReader.ReadObject<BigDecimal> JSON_READER = new JsonReader.ReadObject<BigDecimal>() {
				public BigDecimal read(JsonReader reader) throws IOException {
					return NumberConverter.deserializeDecimal(reader).setScale(2);
				}
			};
			public static final JsonWriter.WriteObject<BigDecimal> JSON_WRITER = new JsonWriter.WriteObject<BigDecimal>() {
				public void write(JsonWriter writer, BigDecimal value) {
					if (value == null) {
						writer.writeNull();
					} else {
						NumberConverter.serializeNullable(value.setScale(2), writer);
					}
				}
			};
		}
		public static abstract class SupportArrayList {
			public static final JsonReader.ReadObject<ArrayList<Integer>> JSON_READER = new JsonReader.ReadObject<ArrayList<Integer>>() {
				public ArrayList<Integer> read(JsonReader reader) throws IOException {
					reader.getNextToken();
					return NumberConverter.deserializeIntNullableCollection(reader);
				}
			};
			public static final JsonWriter.WriteObject<Integer> INT_WRITER = new JsonWriter.WriteObject<Integer>() {
				public void write(JsonWriter writer, Integer value) {
					NumberConverter.serializeNullable(value, writer);
				}
			};
			public static final JsonWriter.WriteObject<ArrayList<Integer>> JSON_WRITER = new JsonWriter.WriteObject<ArrayList<Integer>>() {
				public void write(JsonWriter writer, ArrayList<Integer> value) {
					writer.serialize(value, INT_WRITER);
				}
			};
		}
	}

	public static void main(String[] args) throws IOException {

		//ServiceLoader.load will load Model since it will be registered into META-INF/services during annotation processing
		DslJson<Object> dslJson = new DslJson<Object>();
		//writer should be reused. For per thread reuse use ThreadLocal pattern
		JsonWriter writer = dslJson.newWriter();

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
		instance.person = new ImmutablePerson("first name", "last name", 35);
		instance.states = Arrays.asList(Model.State.HI, Model.State.LOW);
		instance.jsonObject = new Model.JsonObjectReference(43, "abcd");
		instance.jsonObjects = Collections.singletonList(new Model.JsonObjectReference(34, "dcba"));
		instance.date = new Date();
		instance.dates = Arrays.asList(null, new Date(0));
		Model.Concrete concrete = new Model.Concrete();
		concrete.x = 11;
		concrete.y = 23;
		instance.abs = concrete;
		instance.decimal2 = BigDecimal.TEN;
		instance.intList = new ArrayList<Integer>(Arrays.asList(123, 456));
		instance.map = new HashMap<String, Object>();
		instance.map.put("abc", 678);
		instance.map.put("array", new int[] { 2, 4, 8});

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
