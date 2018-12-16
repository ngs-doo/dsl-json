package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import static com.dslplatform.json.CompiledJson.ObjectFormatPolicy.MINIMAL;
import static com.dslplatform.json.CompiledJson.ObjectFormatPolicy.DEFAULT;
import static com.dslplatform.json.CompiledJson.ObjectFormatPolicy.FULL;
import static com.dslplatform.json.JsonAttribute.IncludePolicy.ALWAYS;
import static com.dslplatform.json.JsonAttribute.IncludePolicy.NON_DEFAULT;

public class ObjectFormatPolicyTest {

	private DslJson<Object> dslJsonMinimal = new DslJson<>(new DslJson.Settings<>().includeServiceLoader().skipDefaultValues(true));
	private DslJson<Object> dslJsonFull = new DslJson<>(new DslJson.Settings<>().includeServiceLoader().skipDefaultValues(false));

	@CompiledJson
	static class User1 {
		public UUID id;

		@JsonAttribute
		public int age;

		@JsonAttribute(includeToMinimal = NON_DEFAULT)
		public String firstName;

		@JsonAttribute(includeToMinimal = ALWAYS)
		public String lastName;
	}

	@CompiledJson(objectFormatPolicy = DEFAULT)
	static class User2 {
		public UUID id;

		@JsonAttribute
		public int age;

		@JsonAttribute(includeToMinimal = NON_DEFAULT)
		public String firstName;

		@JsonAttribute(includeToMinimal = ALWAYS)
		public String lastName;
	}

	@CompiledJson(objectFormatPolicy = MINIMAL)
	static class User3 {
		public UUID id;

		@JsonAttribute
		public int age;

		@JsonAttribute(includeToMinimal = NON_DEFAULT)
		public String firstName;

		@JsonAttribute(includeToMinimal = ALWAYS)
		public String lastName;
	}

	@CompiledJson(objectFormatPolicy = FULL)
	static class User4 {
		public UUID id;

		@JsonAttribute
		public int age;

		@JsonAttribute(includeToMinimal = NON_DEFAULT)
		public String firstName;

		@JsonAttribute(includeToMinimal = ALWAYS)
		public String lastName;
	}

	@Test
	public void testDefaultObjectFormatPolicy() throws IOException {
		User1 user = new User1();

		Assert.assertEquals("{\"lastName\":null}", serialize(dslJsonMinimal, user));
		Assert.assertEquals("{\"age\":0,\"id\":null,\"lastName\":null,\"firstName\":null}", serialize(dslJsonFull, user));
	}

	@Test
	public void testDefaultObjectFormatPolicySpecifiedExplicitly() throws IOException {
		User2 user = new User2();

		Assert.assertEquals("{\"lastName\":null}", serialize(dslJsonMinimal, user));
		Assert.assertEquals("{\"age\":0,\"id\":null,\"lastName\":null,\"firstName\":null}", serialize(dslJsonFull, user));
	}

	@Test
	public void testMinimalObjectFormatPolicy() throws IOException {
		User3 user = new User3();

		Assert.assertEquals("{\"lastName\":null}", serialize(dslJsonMinimal, user));
		Assert.assertEquals("{\"lastName\":null}", serialize(dslJsonFull, user));
	}

	@Test
	public void testFullObjectFormatPolicy() throws IOException {
		User4 user = new User4();

		Assert.assertEquals("{\"age\":0,\"id\":null,\"lastName\":null,\"firstName\":null}", serialize(dslJsonMinimal, user));
		Assert.assertEquals("{\"age\":0,\"id\":null,\"lastName\":null,\"firstName\":null}", serialize(dslJsonFull, user));
	}

	private static String serialize(DslJson<?> dslJson, Object instance) throws IOException {
		JsonWriter jsonWriter = dslJson.newWriter();
		dslJson.serialize(jsonWriter, instance);
		return jsonWriter.toString();
	}
}
