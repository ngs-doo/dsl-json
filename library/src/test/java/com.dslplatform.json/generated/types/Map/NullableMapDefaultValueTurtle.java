package com.dslplatform.json.generated.types.Map;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.MapAsserts;

import java.io.IOException;

public class NullableMapDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.util.Map<String, String> defaultValue = null;
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.util.Map<String, String> defaultValueJsonDeserialized = (java.util.Map<String, String>) jsonSerialization.deserialize(java.util.Map.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		MapAsserts.assertNullableEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.util.Map<String, String> borderValue1 = new java.util.HashMap<String, String>(0);
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.util.Map<String, String> borderValue1JsonDeserialized = (java.util.Map<String, String>) jsonSerialization.deserialize(java.util.Map.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		MapAsserts.assertNullableEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.util.Map<String, String> borderValue2 = new java.util.HashMap<String, String>() {{ put("a", "b"); }};
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.util.Map<String, String> borderValue2JsonDeserialized = (java.util.Map<String, String>) jsonSerialization.deserialize(java.util.Map.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		MapAsserts.assertNullableEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.util.Map<String, String> borderValue3 = new java.util.HashMap<String, String>() {{ put("Quote: \", Solidus /", "Backslash: \\, Aphos: ', Brackets: [] () {}"); }};
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.util.Map<String, String> borderValue3JsonDeserialized = (java.util.Map<String, String>) jsonSerialization.deserialize(java.util.Map.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		MapAsserts.assertNullableEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final java.util.Map<String, String> borderValue4 = new java.util.HashMap<String, String>() {{ put("", "empty"); put("a", "1"); put("b", "2"); put("c", "3"); }};
		final StaticJson.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final java.util.Map<String, String> borderValue4JsonDeserialized = (java.util.Map<String, String>) jsonSerialization.deserialize(java.util.Map.class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		MapAsserts.assertNullableEquals(borderValue4, borderValue4JsonDeserialized);
	}
}
