package com.dslplatform.json;

import com.dslplatform.json.runtime.Generics;
import com.dslplatform.json.runtime.TypeDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class GenericsTest {
	@Test
	public void testMakeGenericArrayType() {
		Type expected = new TypeDefinition<List<String>[]>(){}.type;
		Type actual = Generics.makeArrayType(new TypeDefinition<List<String>>(){}.type);

		assertTypeEquals(expected, actual);
	}

	@Test
	public void testArrayType() {
		Type expected = new TypeDefinition<String[]>(){}.type;
		Type actual = Generics.makeArrayType(String.class);

		assertTypeEquals(expected, actual);
	}

	@Test
	public void testPrimitiveArrayType() {
		Type expected = new TypeDefinition<int[]>(){}.type;
		Type actual = Generics.makeArrayType(int.class);

		assertTypeEquals(expected, actual);
	}

	@Test
	public void testMakeParameterizedType() {
		Type expected = new TypeDefinition<Map<String, Object>>(){}.type;
		Type actual = Generics.makeParameterizedType(Map.class, String.class, Object.class);

		assertTypeEquals(expected, actual);
	}

	@Test
	public void testMakeParameterizedTypeWithArrayComponent() {
		Type expected = new TypeDefinition<Map<String[], Object[][]>>(){}.type;
		Type actual = Generics.makeParameterizedType(Map.class, String[].class, Object[][].class);

		assertTypeEquals(expected, actual);
	}

	private void assertTypeEquals(Type expected, Type actual) {
		Assert.assertEquals(expected, actual);
		Assert.assertEquals(actual, expected);
		Assert.assertEquals(expected.hashCode(), actual.hashCode());
		Assert.assertEquals(expected.toString(), actual.toString());
	}
}
