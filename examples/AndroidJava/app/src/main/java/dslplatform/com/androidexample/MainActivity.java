package dslplatform.com.androidexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonAttribute;
import com.dslplatform.json.JsonConverter;
import com.dslplatform.json.JsonObject;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.NumberConverter;
import com.dslplatform.json.StringConverter;
import com.dslplatform.json.runtime.MapAnalyzer;

import org.threeten.bp.LocalTime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    @CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE) //ignore unknown properties (default for objects). to disallow unknown properties in JSON set it to FAIL which will result in exception instead
    static class Model { //package private visibility is supported in Java8 version
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
        @JsonAttribute(typeSignature = CompiledJson.TypeSignature.EXCLUDE) //$type attribute can be excluded from resulting JSON
        public Abstract abs;//abstract classes or interfaces can be used which will also include $type attribute in JSON by default
        public List<Abstract> absList;
        public Interface iface;//interfaces without deserializedAs will also include $type attribute in JSON by default
        public ParentClass inheritance;
        @JsonAttribute(mandatory = true)// mandatory adds check if property exist in JSON and will serialize it even in omit-defaults mode
        public List<State> states;
        public JsonObjectReference jsonObject; //object implementing JsonObject manage their own conversion. They must start with '{'
        public List<JsonObjectReference> jsonObjects;
        @JsonAttribute(ignore = true)
        public GregorianCalendar ignored;
        public LocalTime time; //LocalTime is not supported, but with the use of converter it will work
        public List<LocalTime> times; //even containers with unsupported type will be resolved
        @JsonAttribute(converter = FormatDecimal2.class)
        public BigDecimal decimal2; //custom formatting can be implemented with per property converters
        public ArrayList<Integer> intList; //most collections are supported through runtime converters
        //since this signature has an unknown part (Object), it must be whitelisted
        //This can be done via appropriate converter, by registering @JsonConverter for the specified type
        //or by enabling support for unknown types in the annotation processor
        @JsonAttribute(converter = MapAnalyzer.Runtime.class)
        public Map<String, Object> map;
        public ImmutablePerson person; //immutable objects are supported via several patterns (in this case ctor with arguments)
        public List<ViaFactory> factories; //objects without accessible constructor can be created through factory methods
        public PersonBuilder builder; //builder pattern is supported
        public List<SpecialNumber> numbers;//enum with specific values

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

        @CompiledJson(name = "custom-name")//by default class name will be used for $type attribute
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
            public static BigDecimal read(JsonReader reader) throws IOException {
                if (reader.wasNull()) return null;
                return NumberConverter.deserializeDecimal(reader).setScale(2);
            }
            public static void write(JsonWriter writer, BigDecimal value) {
                if (value == null) {
                    writer.writeNull();
                } else {
                    NumberConverter.serializeNullable(value.setScale(2), writer);
                }
            }
        }
    }

    @JsonConverter(target = LocalTime.class)
    public static abstract class LocalTimeConverter {
        public static LocalTime read(JsonReader reader) throws IOException {
            if (reader.wasNull()) return null;
            return LocalTime.parse(reader.readSimpleString());
        }
        public static void write(JsonWriter writer, LocalTime value) {
            if (value == null) {
                writer.writeNull();
            } else {
                writer.writeString(value.toString());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DslJson<Object> dslJson = DSL.JSON();

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
        instance.person = new ImmutablePerson("first name", "last name", 35);
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
        instance.numbers = Arrays.asList(SpecialNumber.E, SpecialNumber.PI, SpecialNumber.ZERO);

        TextView tv = (TextView)findViewById(R.id.tvHello);
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            //serialize into stream
            dslJson.serialize(instance, os);

            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
            //deserialized using stream API
            Model deserialized = dslJson.deserialize(Model.class, is);

            tv.setText(deserialized.string);
        } catch (IOException ex) {
            tv.setText(ex.getMessage());
        }
    }
}
