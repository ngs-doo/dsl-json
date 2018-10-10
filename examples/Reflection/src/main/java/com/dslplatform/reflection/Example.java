package com.dslplatform.reflection;

import com.dslplatform.json.*;
import com.dslplatform.json.runtime.Settings;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class Example {

	// supported via ObjectAnalyzer
	// when in reflection DSL-JSON does not uses annotation for customization
	public static class Model {
		public String string;
		public List<Integer> integers;
		public UUID[] uuids;
		public Set<BigDecimal> decimals;
		public Vector<Long> longs;
		public int number;
		public List<Nested> nested;
		public ParentClass inheritance;
		public List<State> states; // supported via EnumAnalyzer
		public JsonObjectReference jsonObject; //object implementing JsonObject manage their own conversion. They must start with '{'
		public List<JsonObjectReference> jsonObjects;
		public Map<String, Object> map;
		public ImmutablePerson person; // supported via ImmutableAnalyzer

		public static class Nested {
			public long x;
			public double y;
			public float z;
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
	}

	public static void main(String[] args) throws IOException {

		//since annotation processor is disabled in pom.xml only reflection will be used.
		//even if annotation processor was not disabled, it would still run in reflection mode unless
		//.includeServiceLoader() is called on Settings
		DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime()); //Runtime configuration needs to be explicitly enabled
		//writer should be reused. For per thread reuse use ThreadLocal pattern
		JsonWriter writer = dslJson.newWriter();

		Model instance = new Model();
		instance.string = "Hello World!";
		instance.number = 42;
		instance.integers = Arrays.asList(1, 2, 3);
		instance.decimals = new HashSet<>(Arrays.asList(BigDecimal.ONE, BigDecimal.ZERO));
		instance.uuids = new UUID[]{new UUID(1L, 2L), new UUID(3L, 4L)};
		instance.longs = new Vector<>(Arrays.asList(1L, 2L));
		instance.nested = Arrays.asList(new Model.Nested(), null);
		instance.inheritance = new Model.ParentClass();
		instance.inheritance.a = 5;
		instance.inheritance.b = 6;
		instance.person = new ImmutablePerson("first name", "last name", 35);
		instance.states = Arrays.asList(Model.State.HI, Model.State.LOW);
		instance.jsonObject = new Model.JsonObjectReference(43, "abcd");
		instance.jsonObjects = Collections.singletonList(new Model.JsonObjectReference(34, "dcba"));
		instance.map = new HashMap<>();
		instance.map.put("abc", 678);
		instance.map.put("array", new int[] { 2, 4, 8});

		dslJson.serialize(writer, instance);

		//resulting buffer with JSON
		byte[] buffer = writer.getByteBuffer();
		//end of buffer
		int size = writer.size();
		System.out.println(writer);

		//deserialization using byte[] API
		Model deser = dslJson.deserialize(Model.class, buffer, size);

		System.out.println(deser.string);
	}
}
