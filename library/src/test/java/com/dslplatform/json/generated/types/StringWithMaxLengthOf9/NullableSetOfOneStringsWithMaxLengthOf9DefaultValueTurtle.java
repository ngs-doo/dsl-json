package com.dslplatform.json.generated.types.StringWithMaxLengthOf9;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.StringWithMaxLengthOf9Asserts;

import java.io.IOException;

public class NullableSetOfOneStringsWithMaxLengthOf9DefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.util.Set<String> defaultValue = null;
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.util.List<String> deserializedTmpList = jsonSerialization.deserializeList(String.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		final java.util.Set<String> defaultValueJsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<String>(deserializedTmpList);
		StringWithMaxLengthOf9Asserts.assertNullableSetOfOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.util.Set<String> borderValue1 = new java.util.HashSet<String>(java.util.Arrays.asList(""));
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.util.List<String> deserializedTmpList = jsonSerialization.deserializeList(String.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		final java.util.Set<String> borderValue1JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<String>(deserializedTmpList);
		StringWithMaxLengthOf9Asserts.assertNullableSetOfOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.util.Set<String> borderValue2 = new java.util.HashSet<String>(java.util.Arrays.asList("xxxxxxxxx"));
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.util.List<String> deserializedTmpList = jsonSerialization.deserializeList(String.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		final java.util.Set<String> borderValue2JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<String>(deserializedTmpList);
		StringWithMaxLengthOf9Asserts.assertNullableSetOfOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.util.Set<String> borderValue3 = new java.util.HashSet<String>(java.util.Arrays.asList("", "\"", "'/\\[](){}", "\u0001\u0002\u0003\u0004\u0005\u0006\u0007\b\t", "xxxxxxxxx"));
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.util.List<String> deserializedTmpList = jsonSerialization.deserializeList(String.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		final java.util.Set<String> borderValue3JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<String>(deserializedTmpList);
		StringWithMaxLengthOf9Asserts.assertNullableSetOfOneEquals(borderValue3, borderValue3JsonDeserialized);
	}
}
