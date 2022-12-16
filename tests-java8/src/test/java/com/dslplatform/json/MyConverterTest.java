package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class MyConverterTest {
    @JsonConverter(target = BindingClass.class)
    public static class BindingClassConverter {
        // todo make separate interface?
        public static final JsonReader.ReadObject<BindingClass> JSON_READER = new JsonReader.ReadObject<BindingClass>() {
            @Override
            public BindingClass read(JsonReader reader) throws IOException {
                BindingClass inst = new BindingClass();
                inst.value = reader.readString();
                return inst;
            }
        };
        public static JsonWriter.WriteObject<BindingClass> JSON_WRITER() {
            return new JsonWriter.WriteObject<BindingClass>() {
                @Override
                public void write(JsonWriter writer, @Nullable BindingClass value) {
                    writer.writeString(value.value);
                }
            };
        }
        // todo ideally should be configured per usage probably?
        public static final JsonReader.BindObject<BindingClass> JSON_BINDER = new JsonReader.BindObject<BindingClass>() {
            @Override
            public BindingClass bind(JsonReader reader, BindingClass inst) throws IOException {
                inst.value = reader.readString();
                return inst;
            }
        };
    }

    @CompiledJson
    public static class WrapperClass {
        public String a;
        @JsonAttribute(converter = BindingClassConverter.class, nullable = false, mandatory = true)
        public BindingClass b = new BindingClass(); // todo default is null
    }

    public static class BindingClass {
        public String value;
    }

    private final DslJson<Object> dslJson = new DslJson<>(new DslJson.Settings<>().allowArrayFormat(true).includeServiceLoader());

    @Test
    public void bindWorks() throws IOException {
        WrapperClass wc = new WrapperClass();
        BindingClass internal = wc.b;
        {
            byte[] input = "{\"a\":\"abc\", \"b\":\"value\"}".getBytes("UTF-8");
            JsonReader<Object> reader = dslJson.newReader(input);
            reader.getNextToken();
            dslJson.tryFindBinder(WrapperClass.class).bind(reader, wc);
            Assert.assertEquals("abc", wc.a);
            Assert.assertEquals("value", wc.b.value);
            Assert.assertSame(internal, wc.b);
        }
        {
            byte[] input = "{\"a\":\"abc2\", \"b\":\"value2\"}".getBytes("UTF-8");
            JsonReader<Object> reader = dslJson.newReader(input);
            reader.getNextToken();
            dslJson.tryFindBinder(WrapperClass.class).bind(reader, wc);
            Assert.assertEquals("abc2", wc.a);
            Assert.assertEquals("value2", wc.b.value);
            Assert.assertSame(internal, wc.b);
        }
    }

    @Test
    public void readWorks() throws IOException {
        byte[] input = "{\"a\":\"abc\", \"b\":\"value\"}".getBytes("UTF-8");
        WrapperClass wc = dslJson.deserialize(WrapperClass.class, input, input.length);
        Assert.assertEquals("abc", wc.a);
        Assert.assertEquals("value", wc.b.value);
    }

}
