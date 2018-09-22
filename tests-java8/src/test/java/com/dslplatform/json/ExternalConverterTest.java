package com.dslplatform.json;

import com.dslplatform.json.test.GenericPojo;
import com.dslplatform.json.test.SimplePojo;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ExternalConverterTest {

	@CompiledJson
	static class Model1 {
		public SimplePojo value;
	}

	@CompiledJson
	static class Model2 {
		public GenericPojo<String> value;
	}

	@Test
	public void doNotIgnoreExternalConverterWhenSerializeSimpleClass() {
		Model1 model = new Model1();
		model.value = new SimplePojo();
		model.value.key = "simple";

		DslJson<Object> dslJson = new DslJson<>();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		assertThatThrownBy(() -> dslJson.serialize(model, out))
				.hasMessage("This is fake writer for class com.dslplatform.json.test.SimplePojo");
	}

	@Test
	public void doNotIgnoreExternalConverterWhenDeserializeSimpleClass() {
		Model1 model = new Model1();
		model.value = new SimplePojo();
		model.value.key = "simple";

		DslJson<Object> dslJson = new DslJson<>();
		byte[] json = "{\"value\":{\"key\":\"simple\"}}".getBytes(StandardCharsets.UTF_8);

		assertThatThrownBy(() -> dslJson.deserialize(Model1.class, json, json.length))
				.hasMessage("This is fake reader for class com.dslplatform.json.test.SimplePojo");
	}

	@Test
	public void doNotIgnoreExternalConverterWhenSerializeGenericClass() {
		Model2 model = new Model2();
		model.value = new GenericPojo<>();
		model.value.key = "generic";

		DslJson<Object> dslJson = new DslJson<>();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		assertThatThrownBy(() -> dslJson.serialize(model, out))
				.hasMessage("This is fake writer for class com.dslplatform.json.test.GenericPojo");
	}

	@Test
	public void doNotIgnoreExternalConverterWhenDeserializeGenericClass() {
		Model2 model = new Model2();
		model.value = new GenericPojo<>();
		model.value.key = "generic";

		DslJson<Object> dslJson = new DslJson<>();
		byte[] json = "{\"value\":{\"key\":\"generic\"}}".getBytes(StandardCharsets.UTF_8);

		assertThatThrownBy(() -> dslJson.deserialize(Model2.class, json, json.length))
				.hasMessage("This is fake reader for class com.dslplatform.json.test.GenericPojo");
	}
}
