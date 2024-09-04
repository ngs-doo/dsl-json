package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static com.dslplatform.json.CompiledJson.ObjectFormatPolicy.*;
import static com.dslplatform.json.JsonAttribute.IncludePolicy.ALWAYS;
import static com.dslplatform.json.JsonAttribute.IncludePolicy.NON_DEFAULT;
import static com.dslplatform.json.TestJsonWriters.*;
public class ObjectFormatPolicyTest {

	private DslJson<Object> dslJsonMinimal = new DslJson<>(new DslJson.Settings<>().includeServiceLoader().skipDefaultValues(true));
	private DslJson<Object> dslJsonFull = new DslJson<>(new DslJson.Settings<>().includeServiceLoader().skipDefaultValues(false));
	private DslJson<Object> dslJsonFilteredAll = new DslJson<>(new DslJson.Settings<>().includeServiceLoader().writerFactory(new AllWriterFactory()).filterOutputs(true));
	private DslJson<Object> dslJsonFilteredNone = new DslJson<>(new DslJson.Settings<>().includeServiceLoader().writerFactory(new NoneWriterFactory()).filterOutputs(true));
	private DslJson<Object> dslJsonFilteredSecret(String fieldName)  {
		return new DslJson<>(new DslJson.Settings<>().includeServiceLoader().writerFactory(new SecretWriterFactory(fieldName)).filterOutputs(true));
	}


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

	@CompiledJson(objectFormatPolicy = EXPLICIT)
	static class User5 {
		public UUID id;

		@JsonAttribute
		public int age;

		public String firstName;

		@JsonAttribute(ignore = true)
		public String lastName;
	}

	@CompiledJson(objectFormatPolicy = EXPLICIT, formats = CompiledJson.Format.ARRAY)
	static class User6 {
		public UUID id;

		@JsonAttribute
		public int age;

		public String firstName;

		@JsonAttribute(ignore = true)
		public String lastName;
	}

	static abstract class BaseUser {

		protected BaseUser() {}

		public UUID id;

		@JsonAttribute
		public int age;

		public String firstName;

		@JsonAttribute(ignore = true)
		public String lastName;
	}

	@CompiledJson(objectFormatPolicy = EXPLICIT)
	static class User7 extends BaseUser {
		public UUID identity;

		@JsonAttribute
		public int level;

		public String description;
	}
	@CompiledJson(objectFormatPolicy = CONTROLLED)
	static class User8 {
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
		Assert.assertEquals("{}", serialize(dslJsonFilteredNone, user));
		Assert.assertEquals("{\"age\":0,\"id\":null,\"lastName\":null,\"firstName\":null}", serialize(dslJsonFilteredAll, user));
		Assert.assertEquals("{\"age\":0,\"id\":null,\"lastName\":null,\"firstName\":\"That's a secret!\"}", serialize(dslJsonFilteredSecret("firstName"), user));
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

	@Test
	public void testExplicitObjectFormatPolicy() throws IOException {
		User5 user = new User5();
		user.id = UUID.randomUUID();
		user.age = 42;
		user.firstName = "John";
		user.lastName = "Doe";

		Assert.assertEquals("{\"age\":42}", serialize(dslJsonMinimal, user));
		Assert.assertEquals("{\"age\":42}", serialize(dslJsonFull, user));

		byte[] bytes = "{\"age\":42}".getBytes("UTF-8");
		User5 res = dslJsonFull.deserialize(User5.class, bytes, bytes.length);
		Assert.assertEquals(42, res.age);
		Assert.assertNull(res.id);
		Assert.assertNull(res.firstName);
		Assert.assertNull(res.lastName);
	}

	@Test
	public void testExplicitObjectFormatPolicyWithArray() throws IOException {
		User6 user = new User6();
		user.id = UUID.randomUUID();
		user.age = 42;
		user.firstName = "John";
		user.lastName = "Doe";

		Assert.assertEquals("[42]", serialize(dslJsonMinimal, user));
		Assert.assertEquals("[42]", serialize(dslJsonFull, user));

		byte[] bytes = "[42]".getBytes("UTF-8");
		User6 res = dslJsonFull.deserialize(User6.class, bytes, bytes.length);
		Assert.assertEquals(42, res.age);
		Assert.assertNull(res.id);
		Assert.assertNull(res.firstName);
		Assert.assertNull(res.lastName);
	}

	@Test
	public void testExplicitObjectFormatPolicyWithInheritance() throws IOException {
		User7 user = new User7();
		user.id = user.identity = UUID.randomUUID();
		user.age = 42;
		user.firstName = "John";
		user.lastName = "Doe";
		user.level = 9;
		user.description = "description";

		Assert.assertEquals("{\"age\":42,\"level\":9}", serialize(dslJsonMinimal, user));
		Assert.assertEquals("{\"age\":42,\"level\":9}", serialize(dslJsonFull, user));

		byte[] bytes = "{\"age\":42,\"level\":9}".getBytes("UTF-8");
		User7 res = dslJsonFull.deserialize(User7.class, bytes, bytes.length);
		Assert.assertEquals(42, res.age);
		Assert.assertEquals(9, res.level);
		Assert.assertNull(res.id);
		Assert.assertNull(res.identity);
		Assert.assertNull(res.firstName);
		Assert.assertNull(res.lastName);
		Assert.assertNull(res.description);
	}

	private static String serialize(DslJson<?> dslJson, Object instance) throws IOException {
		JsonWriter jsonWriter = dslJson.newWriter();
		dslJson.serialize(jsonWriter, instance);
		return jsonWriter.toString();
	}
}
