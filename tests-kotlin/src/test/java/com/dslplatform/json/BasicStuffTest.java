package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class BasicStuffTest {

    @Test
    public void willPickUpPropertyFromBaseClass() throws IOException {
        final DslJson dslJson = new DslJson(Settings.withRuntime().includeServiceLoader());

        DataClass dc = new DataClass(
                "Kotlin",
                Arrays.asList(170, 171, 172),
                "DSL-JSON",
                new CustomObject("abc"),
                ObjectFactory.Companion.create("xyz"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        dslJson.serialize(dc, output);

        Assert.assertEquals(
                "{\"lang\":\"Kotlin\",\"versions\":[170,171,172],\"library\":\"DSL-JSON\",\"custom\":{\"text\":\"abc\"},\"factory\":{\"text\":\"xyz\"}}",
                output.toString("UTF-8"));


        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());

        DataClass deser = (DataClass) dslJson.deserialize(DataClass.class, input);
        Assert.assertEquals(dc.getLanguage(), deser.getLanguage());
        Assert.assertEquals(dc.getVersions(), deser.getVersions());
        Assert.assertEquals(dc.getCustom(), deser.getCustom());
        Assert.assertEquals(dc.getFactory().getText(), deser.getFactory().getText());
    }

    @Test
    public void classShouldNotBeEmpty() throws IOException {
        final DslJson dslJson = new DslJson(Settings.basicSetup().allowArrayFormat(true).skipDefaultValues(false));

        ClassWithNullDefault nd = new ClassWithNullDefault("Value");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        dslJson.serialize(nd, output);

        Assert.assertEquals("{\"field\":\"Value\"}", output.toString("UTF-8"));

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        ClassWithNullDefault deser = (ClassWithNullDefault) dslJson.deserialize(ClassWithNullDefault.class, input);
        Assert.assertEquals(nd.getField(), deser.getField());
    }

    @Test
    public void classCanBeEmpty() throws IOException {
        final DslJson dslJson = new DslJson(Settings.basicSetup().allowArrayFormat(true).skipDefaultValues(false));

        EmptyClass ec = new EmptyClass();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        dslJson.serialize(ec, output);

        Assert.assertEquals("{}", output.toString("UTF-8"));

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        EmptyClass deser = (EmptyClass) dslJson.deserialize(EmptyClass.class, input);
        Assert.assertNotNull(deser);
    }

    @Test
    public void isNamingWithoutDefault() throws IOException {
        final DslJson dslJson = new DslJson(Settings.basicSetup());

        WithIs wi = new WithIs(true, null, null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        dslJson.serialize(wi, output);

        Assert.assertEquals("{\"isLocked\":true,\"id\":null,\"token\":null}", output.toString("UTF-8"));

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        WithIs deser = (WithIs) dslJson.deserialize(WithIs.class, input);
        Assert.assertNotNull(deser);
        Assert.assertTrue(deser.isLocked());
    }
}