package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import com.dslplatform.json.runtime.TypeDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class MandatoryTest {

	private DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime().includeServiceLoader());

	//@Test
	public void willPickUpPropertyFromBaseClass() {
		byte[] input = "{\"b\":\"\"}".getBytes(StandardCharsets.UTF_8);
		try {
			dslJson.deserialize(MandatoryClass.class, input, input.length);
			Assert.fail("Expecting mandatory property error");
		} catch (IOException ex) {
			Assert.assertTrue(ex.getMessage(), ex.getMessage().contains("Mandatory property (a) not found"));
		}
	}
}