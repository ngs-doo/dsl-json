package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EnumTest {

    @Test
    public void testDeserializeEnumWithUnknownValue() throws IOException {
        final DslJson dslJson = new DslJson(Settings.withRuntime().includeServiceLoader());

        byte[] json = "{\"enumField\":\"UNKNOWN\",\"enumFieldNoHash\":\"UNKNOWN\"}".getBytes(StandardCharsets.UTF_8);
        EnumHolder deserialized = (EnumHolder) dslJson.deserialize(EnumHolder.class, json, json.length);

        Assert.assertNotNull(deserialized);
        Assert.assertEquals(deserialized.getEnumField(), EnumClass.SECOND);
        Assert.assertEquals(deserialized.getEnumFieldNoHash(), EnumClass.SECOND);
    }
    @Test
    public void testDeserializeEnumWithKnownValue() throws IOException {
        final DslJson dslJson = new DslJson(Settings.withRuntime().includeServiceLoader());

        byte[] json = "{\"enumField\":\"FIRST\",\"enumFieldNoHash\":\"FIRST\"}".getBytes(StandardCharsets.UTF_8);
        EnumHolder deserialized = (EnumHolder) dslJson.deserialize(EnumHolder.class, json, json.length);

        Assert.assertNotNull(deserialized);
        Assert.assertEquals(deserialized.getEnumField(), EnumClass.FIRST);
        Assert.assertEquals(deserialized.getEnumFieldNoHash(), EnumClass.FIRST);
    }
}
