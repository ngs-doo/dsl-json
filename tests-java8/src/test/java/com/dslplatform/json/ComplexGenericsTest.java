package com.dslplatform.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ComplexGenericsTest {

    @CompiledJson(discriminator = "type")
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

    @CompiledJson
    public static abstract class Parent2<T> {

        private T prop;

        public abstract String getType();

        public T getProp() {
            return prop;
        }

        public void setProp(T prop) {
            this.prop = prop;
        }
    }

    @CompiledJson(discriminator = "$type", name = "first")
    public static class FirstChild2 extends Parent2<Long> {

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

    @CompiledJson(deserializeName = "second")
    public static class SecondChild2 extends Parent2<String> {

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

    @CompiledJson
    public static abstract class Parent3<T> {

        private T prop;

        public T getProp() {
            return prop;
        }

        public void setProp(T prop) {
            this.prop = prop;
        }
    }

    @CompiledJson(discriminator = "$type")
    public static class FirstChild3 extends Parent3<Long> {

        private Boolean boolValue;

        public Boolean getBoolValue() {
            return boolValue;
        }

        public void setBoolValue(Boolean boolValue) {
            this.boolValue = boolValue;
        }
    }

    @CompiledJson
    public static class SecondChild3 extends Parent3<String> {

        private Integer intValue;

        public Integer getIntValue() {
            return intValue;
        }

        public void setIntValue(Integer intValue) {
            this.intValue = intValue;
        }
    }

    private final DslJson<Object> dslJson = new DslJson<>();

    @Test
    public void inheritanceWithGenericsTestAndCustomDiscriminator() throws IOException {

        byte[] bytes = "{\"type\":\"first\",\"prop\":1,\"boolValue\":true}".getBytes();
        Parent parent = dslJson.deserialize(Parent.class, bytes, bytes.length);

        assertNotNull(parent);
        assertEquals("first", parent.getType());
        assertTrue(parent instanceof FirstChild);
        FirstChild child = (FirstChild) parent;
        assertEquals(1L, child.getProp().longValue());
        assertTrue(child.getBoolValue());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        dslJson.serialize(child, outputStream);

        assertEquals("{\"type\":\"first\",\"boolValue\":true,\"prop\":1}", outputStream.toString());
    }

    @Test
    public void inheritanceWithGenericsTest() throws IOException {

        byte[] bytes = "{\"$type\":\"first\",\"prop\":1,\"boolValue\":true}".getBytes();
        Parent2 parent = dslJson.deserialize(Parent2.class, bytes, bytes.length);

        assertNotNull(parent);
        assertEquals("first", parent.getType());
        assertTrue(parent instanceof FirstChild2);
        FirstChild2 child = (FirstChild2) parent;
        assertEquals(1L, child.getProp().longValue());
        assertTrue(child.getBoolValue());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        dslJson.serialize(child, outputStream);

        assertEquals("{\"$type\":\"first\",\"boolValue\":true,\"prop\":1}", outputStream.toString());
    }

    @Test
    public void inheritance() throws IOException {

        byte[] bytes = "{\"$type\":\"com.dslplatform.json.ComplexGenericsTest.FirstChild3\",\"prop\":1,\"boolValue\":true}".getBytes();
        Parent3 parent = dslJson.deserialize(Parent3.class, bytes, bytes.length);

        assertNotNull(parent);
        assertTrue(parent instanceof FirstChild3);
        FirstChild3 child = (FirstChild3) parent;
        assertEquals(1L, child.getProp().longValue());
        assertTrue(child.getBoolValue());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        dslJson.serialize(child, outputStream);

        assertEquals("{\"$type\":\"com.dslplatform.json.ComplexGenericsTest.FirstChild3\",\"boolValue\":true,\"prop\":1}",
            outputStream.toString());
    }
}
