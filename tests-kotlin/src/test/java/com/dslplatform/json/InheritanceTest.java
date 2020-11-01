package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import com.dslplatform.json.runtime.TypeDefinition;
import org.junit.Assert;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

public class InheritanceTest {

    private final DslJson dslJson = new DslJson();

    @Test
    public void willPickUpPropertyFromBaseClass() throws IOException {
        TopLevelB model = new TopLevelB("x");
        OutputStream os = new ByteArrayOutputStream();
        dslJson.serialize(model, os);
        Assert.assertEquals("{\"r\":\"x\",\"t\":\"hello\"}", os.toString());
    }

	@Test
	public void discriminatorOnList() throws IOException {
		DslJson dslJson = new DslJson(Settings.basicSetup());
		List<BaseDiscriminator> list = Arrays.asList(new One("one"), new Two(2));
		OutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(list, os);
		Assert.assertEquals("[{\"value\":\"one\"},{\"value\":2}]", os.toString());
	}

	@Test
	public void discriminatorOnListWithExplicitSignature() {
		DslJson dslJson = new DslJson(Settings.basicSetup());
		List<BaseDiscriminator> list = Arrays.asList(new One("one"), new Two(2));
		JsonWriter writer = dslJson.newWriter();
		dslJson.serialize(writer, new TypeDefinition<List<BaseDiscriminator>>(){}.type, list);
		Assert.assertEquals("[{\"@type\":\"one\",\"value\":\"one\"},{\"@type\":\"two\",\"value\":2}]", writer.toString());
	}
}