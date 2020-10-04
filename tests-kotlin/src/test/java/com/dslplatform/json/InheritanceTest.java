package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class InheritanceTest {

    private final DslJson dslJson = new DslJson();

    @Test
    public void willPickUpPropertyFromBaseClass() throws IOException {
        TopLevelB model = new TopLevelB("x");
        OutputStream os = new ByteArrayOutputStream();
        dslJson.serialize(model, os);
        Assert.assertEquals("{\"r\":\"x\",\"t\":\"hello\"}", os.toString());
    }
}