package com.dslplatform.array;

import com.dslplatform.json.*;
import com.dslplatform.json.runtime.MapAnalyzer;
import com.dslplatform.json.runtime.Settings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.*;

public class Example {

	//this object will be serialized in array format even when allowArrayFormat(false) is specified since it does not support the OBJECT format
	@CompiledJson(formats = CompiledJson.Format.ARRAY)
	static class Model { //package private visibility is supported in Java8 version
		@JsonAttribute(nullable = false, index = 1) //indicate that field can't be null
		public String string;
		@JsonAttribute(nullable = false, index = 2)
		public List<Integer> integers;
		@JsonAttribute(index = 3)
		public UUID[] uuids;
		@JsonAttribute(index = 4)
		public Set<BigDecimal> decimals;
		@JsonAttribute(index = 5)
		public Vector<Long> longs;
		@JsonAttribute(hashMatch = false, index = 6) // exact name match can be forced, otherwise hash value will be used for matching
		public int number;
		@JsonAttribute(index = 10)//only the sort order is relevant. indexes can be skipped (they do not map into index in output format)
		public List<Nested> nested;
		@JsonAttribute(typeSignature = CompiledJson.TypeSignature.EXCLUDE, index = 11) //$type attribute can be excluded from resulting JSON
		public Abstract abs;//abstract classes or interfaces can be used which will also include $type attribute in JSON by default
		@JsonAttribute(index = 20)
		public List<Abstract> absList;
		@JsonAttribute(index = 15)
		public Interface iface;//interfaces without deserializedAs will also include $type attribute in JSON by default
		@JsonAttribute(index = 21)
		public ParentClass inheritance;
		@JsonAttribute(index = 22)
		public List<State> states;
		@JsonAttribute(index = 23)
		public JsonObjectReference jsonObject; //object implementing JsonObject manage their own conversion. They must start with '{'
		@JsonAttribute(index = 24)
		public List<JsonObjectReference> jsonObjects;
		@JsonAttribute(index = 25)
		public LocalTime time; //LocalTime is not supported, but with the use of converter it will work
		@JsonAttribute(index = 26)
		public List<LocalTime> times; //even containers with unsupported type will be resolved
		@JsonAttribute(converter = FormatDecimal2.class, index = 27)
		public BigDecimal decimal2; //custom formatting can be implemented with per property converters
		@JsonAttribute(index = 28)
		public ArrayList<Integer> intList; //most collections are supported through runtime converters, but some are even supported during compilation
		//since this signature has an unknown part (Object), it must be whitelisted
		//This can be done via appropriate converter, by registering @JsonConverter for the specified type
		//or by enabling support for unknown types in the annotation processor
		@JsonAttribute(converter = MapAnalyzer.Runtime.class, index = 30)
		public Map<String, Object> map;
		@JsonAttribute(index = 31)
		public ImmutablePerson person; //immutable objects are supported via several patterns (in this case ctor with arguments)
		@JsonAttribute(index = 32)
		public List<ViaFactory> factories; //objects without accessible constructor can be created through factory methods
		@JsonAttribute(index = 33)
		public PersonBuilder builder; //builder pattern is supported
		@JsonAttribute(index = 35)
		public NestedListUnoptimized listUnoptimized;
		@JsonAttribute(index = 36)
		public NestedListOptimized listOptimized;

		//explicitly referenced classes don't require @CompiledJson annotation
		public static class Nested {
			public long x;
			public double y;
			public float z;
		}

		@CompiledJson(deserializeAs = Concrete.class)//without deserializeAs deserializing Abstract would fails since it doesn't contain a $type due to it's exclusion in the above configuration
		public static abstract class Abstract {
			public int x;
		}

		//since this class is not explicitly referenced, but it's an extension of the abstract class used as a property
		//it needs to be decorated with annotation
		@CompiledJson
		public static class Concrete extends Abstract {
			public long y;
		}

		public interface Interface {
			void x(int v);
			int x();
		}

		@CompiledJson(deserializeName = "custom-name")//by default class name will be used for $type attribute
		public static class WithCustomCtor implements Interface {
			private int x;
			private int y;

			public WithCustomCtor(int x) {
				this.x = x;
				this.y = x;
			}

			@CompiledJson
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
			//compiled json can also be used on factory methods when ctor is not available
			@CompiledJson
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

			JsonObjectReference(int x, String s) {
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
		public static abstract class FormatDecimal2 {
			public static final JsonReader.ReadObject<BigDecimal> JSON_READER = reader -> {
				if (reader.wasNull()) return null;
				return NumberConverter.deserializeDecimal(reader).setScale(2);
			};
			public static final JsonWriter.WriteObject<BigDecimal> JSON_WRITER = (writer, value) -> {
				if (value == null) {
					writer.writeNull();
				} else {
					NumberConverter.serializeNullable(value.setScale(2), writer);
				}
			};
		}
	}

	//moved outside of private package class since it reference object in java namespace
	@JsonConverter(target = LocalTime.class)
	public static abstract class LocalTimeConverter {
		public static final JsonReader.ReadObject<LocalTime> JSON_READER = reader -> {
			if (reader.wasNull()) return null;
			return LocalTime.parse(reader.readSimpleString());
		};
		public static final JsonWriter.WriteObject<LocalTime> JSON_WRITER = (writer, value) -> {
			if (value == null) {
				writer.writeNull();
			} else {
				writer.writeString(value.toString());
			}
		};
	}

	public static void main(String[] args) throws IOException {

		//Model converters will be loaded based on naming convention.
		//Previously it would be loaded through ServiceLoader.load,
		//which is still an option if dsljson.configuration name is specified.
		//DSL-JSON loads all services registered into META-INF/services
		//and falls back to naming based convention of package._NAME_DslJsonConfiguration if not found
		//withRuntime is enabled to support runtime analysis for stuff which is not registered by default
		//Annotation processor will run by default and generate descriptions for JSON encoding/decoding
		DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());

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
		instance.iface = new Model.WithCustomCtor(5, 6);
		instance.person = new ImmutablePerson("first name", "last name", 35, Arrays.asList("DSL", "JSON"));
		instance.states = Arrays.asList(Model.State.HI, Model.State.LOW);
		instance.jsonObject = new Model.JsonObjectReference(43, "abcd");
		instance.jsonObjects = Collections.singletonList(new Model.JsonObjectReference(34, "dcba"));
		instance.time = LocalTime.of(12, 15);
		instance.times = Arrays.asList(null, LocalTime.of(8, 16));
		Model.Concrete concrete = new Model.Concrete();
		concrete.x = 11;
		concrete.y = 23;
		instance.abs = concrete;
		instance.absList = Arrays.<Model.Abstract>asList(concrete, null, concrete);
		instance.decimal2 = BigDecimal.TEN;
		instance.intList = new ArrayList<>(Arrays.asList(123, 456));
		instance.map = new HashMap<>();
		instance.map.put("abc", 678);
		instance.map.put("array", new int[] { 2, 4, 8});
		instance.factories = Arrays.asList(null, Model.ViaFactory.create("me", 2), Model.ViaFactory.create("you", 3), null);
		instance.builder = PersonBuilder.builder().firstName("first").lastName("last").age(42).build();
		instance.listUnoptimized = new NestedListUnoptimized(1, 2, 3);
		instance.listOptimized = new NestedListOptimized(4, 5, 6);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		//To get a pretty JSON output PrettifyOutputStream wrapper can be used
		dslJson.serialize(instance, new PrettifyOutputStream(os));

		byte[] bytes = os.toByteArray();
		System.out.println(os);

		//deserialization using Stream API
		Model deser = dslJson.deserialize(Model.class, new ByteArrayInputStream(bytes));

		System.out.println(deser.string);
	}
}
