package com.dslplatform.json.generated.types.Integer;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.IntegerAsserts;

import java.io.IOException;

public class NullableSetOfNullableIntegersDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.util.Set<Integer> defaultValue = null;
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.util.List<Integer> deserializedTmpList = jsonSerialization.deserializeList(Integer.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		final java.util.Set<Integer> defaultValueJsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<Integer>(deserializedTmpList);
		IntegerAsserts.assertNullableSetOfNullableEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.util.Set<Integer> borderValue1 = new java.util.HashSet<Integer>(java.util.Arrays.asList((Integer) null));
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.util.List<Integer> deserializedTmpList = jsonSerialization.deserializeList(Integer.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		final java.util.Set<Integer> borderValue1JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<Integer>(deserializedTmpList);
		IntegerAsserts.assertNullableSetOfNullableEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.util.Set<Integer> borderValue2 = new java.util.HashSet<Integer>(java.util.Arrays.asList(0));
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.util.List<Integer> deserializedTmpList = jsonSerialization.deserializeList(Integer.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		final java.util.Set<Integer> borderValue2JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<Integer>(deserializedTmpList);
		IntegerAsserts.assertNullableSetOfNullableEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.util.Set<Integer> borderValue3 = new java.util.HashSet<Integer>(java.util.Arrays.asList(1000000000));
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.util.List<Integer> deserializedTmpList = jsonSerialization.deserializeList(Integer.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		final java.util.Set<Integer> borderValue3JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<Integer>(deserializedTmpList);
		IntegerAsserts.assertNullableSetOfNullableEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final java.util.Set<Integer> borderValue4 = new java.util.HashSet<Integer>(java.util.Arrays.asList(0, Integer.MIN_VALUE, Integer.MAX_VALUE, -1000000000, 1000000000));
		final StaticJson.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final java.util.List<Integer> deserializedTmpList = jsonSerialization.deserializeList(Integer.class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		final java.util.Set<Integer> borderValue4JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<Integer>(deserializedTmpList);
		IntegerAsserts.assertNullableSetOfNullableEquals(borderValue4, borderValue4JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue5Equality() throws IOException {
		final java.util.Set<Integer> borderValue5 = new java.util.HashSet<Integer>(java.util.Arrays.asList((Integer) null, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, -1000000000, 1000000000));
		final StaticJson.Bytes borderValue5JsonSerialized = jsonSerialization.serialize(borderValue5);
		final java.util.List<Integer> deserializedTmpList = jsonSerialization.deserializeList(Integer.class, borderValue5JsonSerialized.content, borderValue5JsonSerialized.length);
		final java.util.Set<Integer> borderValue5JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<Integer>(deserializedTmpList);
		IntegerAsserts.assertNullableSetOfNullableEquals(borderValue5, borderValue5JsonDeserialized);
	}
}
