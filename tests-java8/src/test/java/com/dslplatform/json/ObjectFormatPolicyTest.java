package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

	@CompiledJson
	static class EmptyCollections {
		@JsonAttribute(includeToMinimal = NON_DEFAULT, nullable = false, index = 1)
		public List<String> col1 = new ArrayList<>();
		@JsonAttribute(includeToMinimal = NON_DEFAULT, nullable = true, index = 2)
		public List<String> col2;
		@JsonAttribute(includeToMinimal = NON_DEFAULT, nullable = true, index = 3)
		public List<String> col3 = new ArrayList<>();
		@JsonAttribute(includeToMinimal = ALWAYS, nullable = false, index = 4)
		public List<String> col4 = new ArrayList<>();
		@JsonAttribute(includeToMinimal = ALWAYS, nullable = true, index = 5)
		public List<String> col5;
		@JsonAttribute(includeToMinimal = ALWAYS, nullable = true, index = 6)
		public List<String> col6 = new ArrayList<>();
		@JsonAttribute(includeToMinimal = NON_DEFAULT, nullable = false, index = 7)
		public String[] arr1 = new String[0];
		@JsonAttribute(includeToMinimal = NON_DEFAULT, nullable = true, index = 8)
		public String[] arr2;
		@JsonAttribute(includeToMinimal = NON_DEFAULT, nullable = true, index = 9)
		public String[] arr3 = new String[0];
		@JsonAttribute(includeToMinimal = ALWAYS, nullable = false, index = 10)
		public String[] arr4 = new String[0];
		@JsonAttribute(includeToMinimal = ALWAYS, nullable = true, index = 11)
		public String[] arr5;
		@JsonAttribute(includeToMinimal = ALWAYS, nullable = true, index = 12)
		public String[] arr6 = new String[0];
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

	@Test
	public void testEmptyCollections() throws IOException {
		EmptyCollections col = new EmptyCollections();

		Assert.assertEquals("{\"col3\":[],\"col4\":[],\"col5\":null,\"col6\":[],\"arr3\":[],\"arr4\":[],\"arr5\":null,\"arr6\":[]}", serialize(dslJsonMinimal, col));
		Assert.assertEquals("{\"col1\":[],\"col2\":null,\"col3\":[],\"col4\":[],\"col5\":null,\"col6\":[],\"arr1\":[],\"arr2\":null,\"arr3\":[],\"arr4\":[],\"arr5\":null,\"arr6\":[]}", serialize(dslJsonFull, col));
	}

	private static String serialize(DslJson<?> dslJson, Object instance) throws IOException {
		JsonWriter jsonWriter = dslJson.newWriter();
		dslJson.serialize(jsonWriter, instance);
		return jsonWriter.toString();
	}
}
