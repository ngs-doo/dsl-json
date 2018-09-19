package com.dslplatform.jackson;

import com.dslplatform.json.*;
import com.dslplatform.json.runtime.MapAnalyzer;
import com.dslplatform.json.runtime.Settings;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class Example {

	//package private visibility is supported
	static class Model {
		//Compile time databinding will use Jackson annotations for analysis (when enabled)
		@JsonCreator
		Model() {}

		public String string;
		public List<Integer> integers;
		@JsonProperty(value = "guids") //use alternative name in JSON
		public UUID[] uuids;
		public Set<BigDecimal> decimals;
		public Vector<Long> longs;
		public Interface iface;//interfaces without deserializedAs will also include $type attribute in JSON by default
		public ParentClass inheritance;
		@JsonProperty(required = true) // currently up to 64 mandatory properties can be used per bean
		public List<State> states;
		public JsonObjectReference jsonObject; //object implementing JsonObject manage their own conversion. They must start with '{'
		public List<JsonObjectReference> jsonObjects;
		@JsonIgnore
		public GregorianCalendar ignored;
		public ArrayList<Integer> intList; //most collections are supported through runtime converters
		//since this signature has an unknown part (Object), it must be whitelisted
		//This can be done via appropriate converter, by registering @JsonConverter for the specified type
		//or by enabling support for unknown types in the annotation processor
		//@JsonAttribute(converter = MapAnalyzer.Runtime.class)
		public Map<String, Object> map;
		public ImmutablePerson person; //immutable objects are supported via several patterns (in this case ctor with arguments)
		public List<ViaFactory> factories; //objects without accessible constructor can be created through factory methods
		public PersonBuilder builder; //builder pattern is supported

		//explicitly referenced classes don't require @CompiledJson annotation
		public static class Nested {
			public long x;
			public double y;
			public float z;
		}

		public interface Interface {
			void x(int v);
			int x();
		}

		public static class WithCustomCtor implements Interface {
			private int x;
			private int y;

			public WithCustomCtor(int x) {
				this.x = x;
				this.y = x;
			}

			//JsonCreator can be used for selecting the appropriate constructor when there are multiple ones
			@JsonCreator
			public WithCustomCtor(int x, int y) {
				this.x = x;
				this.y = y;
			}

			public void x(int v) { x = v; }
			public int x() { return x; }
			public void setY(int v) { y = v; }
			public int getY() { return y; }
		}

		public static class ViaFactory {
			public final String name;
			public final int num;
			private ViaFactory(String name, int num) {
				this.name = name;
				this.num = num;
			}
			@JsonCreator
			public static ViaFactory create(String name, int num) {
				return new ViaFactory(name, num);
			}
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

		//Model converters will be loaded based on naming convention.
		//Previously it would be loaded through ServiceLoader.load,
		//which is still an option if dsljson.configuration name is specified.
		//DSL-JSON loads all services registered into META-INF/services
		//and falls back to naming based convention of package._NAME_DslJsonConfiguration if not found
		//Annotation processor will run by default and generate descriptions for JSON encoding/decoding
		//To include Jackson annotations dsljson.jackson=true must be passed to annotation processor
		//When conversion is not fully supported by compiler Settings.basicSetup() can be enabled to support runtime analysis
		//for features not registered by annotation processor. Currently it is enabled due to use of Set and Vector
		DslJson<Object> dslJson = new DslJson<>(Settings.basicSetup());

		Model instance = new Model();
		instance.string = "Hello World!";
		instance.integers = Arrays.asList(1, 2, 3);
		instance.decimals = new HashSet<>(Arrays.asList(BigDecimal.ONE, BigDecimal.ZERO));
		instance.uuids = new UUID[]{new UUID(1L, 2L), new UUID(3L, 4L)};
		instance.longs = new Vector<>(Arrays.asList(1L, 2L));
		instance.inheritance = new Model.ParentClass();
		instance.inheritance.a = 5;
		instance.inheritance.b = 6;
		instance.iface = new Model.WithCustomCtor(5, 6);
		instance.person = new ImmutablePerson("first name", "last name", 35);
		instance.states = Arrays.asList(Model.State.HI, Model.State.LOW);
		instance.jsonObject = new Model.JsonObjectReference(43, "abcd");
		instance.jsonObjects = Collections.singletonList(new Model.JsonObjectReference(34, "dcba"));
		instance.intList = new ArrayList<>(Arrays.asList(123, 456));
		instance.map = new HashMap<>();
		instance.map.put("abc", 678);
		instance.map.put("array", new int[] { 2, 4, 8});
		instance.factories = Arrays.asList(null, Model.ViaFactory.create("me", 2), Model.ViaFactory.create("you", 3), null);
		instance.builder = PersonBuilder.builder().firstName("first").lastName("last").age(42).build();

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(instance, os);

		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		//deserialization using Stream API
		Model deser = dslJson.deserialize(Model.class, is);

		System.out.println(deser.string);
	}
}
