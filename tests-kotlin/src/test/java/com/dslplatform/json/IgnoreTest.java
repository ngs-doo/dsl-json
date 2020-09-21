package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class IgnoreTest {
    private DslJson dslJson = new DslJson();
    public IgnoreTest(){}
    @Test
    public void ignoresAreNotInOutput() throws IOException, InterruptedException {
        HasIgnoreWithDefault model = new HasIgnoreWithDefault();
        model.stringFieldWithIgnoreFlag = "this string is ignored";
        model.stringWhichShouldBeInPlace = "this string should be in json";
        OutputStream os = new ByteArrayOutputStream();
        dslJson.serialize(model, os);
        Assert.assertEquals(
                "{\"someString\":\"some-string\",\"stringWhichShouldBeInPlace\":\"this string should be in json\"}",
                os.toString()
        );
        Thread.sleep(5000);
        System.out.println("Hello from test");
    }
}