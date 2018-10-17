package com.dslplatform.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ComplexGenericsTest {

    @CompiledJson(deserializeDiscriminator = "type")
    public static abstract class Parent<T> {

        private T prop;

        public abstract String getType();

        public T getProp() {
            return prop;
        }

        public void setProp(T prop) {
            this.prop = prop;
        }
    }

    @CompiledJson(deserializeDiscriminator = "type", deserializeName = "first")
    public static class FirstChild extends Parent<Long> {

        private Boolean boolValue;

        public Boolean getBoolValue() {
            return boolValue;
        }

        public void setBoolValue(Boolean boolValue) {
            this.boolValue = boolValue;
        }

        @Override
        public String getType() {
            return "first";
        }
    }

    @CompiledJson(deserializeDiscriminator = "type", deserializeName = "second")
    public static class SecondChild extends Parent<String> {

        private Integer intValue;

        public Integer getIntValue() {
            return intValue;
        }

        public void setIntValue(Integer intValue) {
            this.intValue = intValue;
        }

        @Override
        public String getType() {
            return "second";
        }
    }

    private final DslJson<Object> dslJson = new DslJson<>();

    @Test
    public void inheritanceWithGenericsTest() throws IOException {

        byte[] bytes = "{\"type\":\"first\",\"prop\":1,\"boolValue\":true}".getBytes();
        Parent parent = dslJson.deserialize(Parent.class, bytes, bytes.length);

        assertNotNull(parent);
        assertEquals("first", parent.getType());
        assertTrue(parent instanceof FirstChild);
        FirstChild child = (FirstChild) parent;
        assertEquals(1L, child.getProp().longValue());
        assertTrue(child.getBoolValue());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        dslJson.serialize(child, Parent.class, outputStream);

        assertEquals("{\"type\":\"first\",\"boolValue\":true,\"prop\":1}", outputStream.toString());
    }
}
