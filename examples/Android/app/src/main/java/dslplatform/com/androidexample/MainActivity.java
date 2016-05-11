package dslplatform.com.androidexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    @CompiledJson
    public static class Model {
        public String string;
        public List<Integer> integers;
        public UUID[] uuids;
        public Set<BigDecimal> decimals;
        public Vector<Long> longs;
        public int number;
        public List<Nested> nested;
        public Abstract abs;//abstract classes or interfaces can be used
        public ParentClass inheritance;
        public List<State> states;

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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //during initialization ServiceLoader.load should pick up services registered into META-INF/services
        //this doesn't really work on Android so DslJson will fallback to default generated class name
        //"dsl_json.json.ExternalSerialization" and try to initialize it manually
        DslJson<Object> dslJson = new DslJson<>();
        //it's best to reuse writer if possible
        JsonWriter writer = new JsonWriter();

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        Model example = new Model();
        example.number = 42;
        example.string = "Hello World!";
        example.integers = Arrays.asList(1, 2, 3);
        example.decimals = new HashSet<BigDecimal>(Arrays.asList(BigDecimal.ONE, BigDecimal.ZERO));
        example.uuids = new UUID[]{new UUID(1L, 2L), new UUID(3L, 4L)};
        example.longs = new Vector<Long>(Arrays.asList(1L, 2L));
        example.nested = Arrays.asList(new Model.Nested(), null);
        example.inheritance = new Model.ParentClass();
        example.inheritance.a = 5;
        example.inheritance.b = 6;
        example.states = Arrays.asList(Model.State.HI, Model.State.LOW);
        Model.Concrete concrete = new Model.Concrete();
        concrete.x = 11;
        concrete.y = 23;
        example.abs = concrete;
        try {
            //serialize into writer
            dslJson.serialize(writer, example);
            //after serialization is done we can copy it into output stream
            writer.toStream(os);

            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());

            //deserialized using stream API. temporary buffer is passes which should be reused if possible
            Model deserialized = dslJson.deserialize(Model.class, is, new byte[1024]);
            Toast.makeText(this, deserialized.string, Toast.LENGTH_LONG);
        } catch (IOException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG);
        }
    }
}
