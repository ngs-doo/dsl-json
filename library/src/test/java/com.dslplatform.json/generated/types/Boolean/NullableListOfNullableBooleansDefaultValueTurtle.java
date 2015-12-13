package com.dslplatform.json.generated.types.Boolean;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.BooleanAsserts;

import java.io.IOException;

public class NullableListOfNullableBooleansDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.util.List<Boolean> defaultValue = null;
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.util.List<Boolean> defaultValueJsonDeserialized = jsonSerialization.deserializeList(Boolean.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		BooleanAsserts.assertNullableListOfNullableEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.util.List<Boolean> borderValue1 = new java.util.ArrayList<Boolean>(java.util.Arrays.asList((Boolean) null));
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.util.List<Boolean> borderValue1JsonDeserialized = jsonSerialization.deserializeList(Boolean.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		BooleanAsserts.assertNullableListOfNullableEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.util.List<Boolean> borderValue2 = new java.util.ArrayList<Boolean>(java.util.Arrays.asList(false));
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.util.List<Boolean> borderValue2JsonDeserialized = jsonSerialization.deserializeList(Boolean.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		BooleanAsserts.assertNullableListOfNullableEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.util.List<Boolean> borderValue3 = new java.util.ArrayList<Boolean>(java.util.Arrays.asList(true));
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.util.List<Boolean> borderValue3JsonDeserialized = jsonSerialization.deserializeList(Boolean.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		BooleanAsserts.assertNullableListOfNullableEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final java.util.List<Boolean> borderValue4 = new java.util.ArrayList<Boolean>(java.util.Arrays.asList(false, true));
		final StaticJson.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final java.util.List<Boolean> borderValue4JsonDeserialized = jsonSerialization.deserializeList(Boolean.class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		BooleanAsserts.assertNullableListOfNullableEquals(borderValue4, borderValue4JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue5Equality() throws IOException {
		final java.util.List<Boolean> borderValue5 = new java.util.ArrayList<Boolean>(java.util.Arrays.asList((Boolean) null, false, true));
		final StaticJson.Bytes borderValue5JsonSerialized = jsonSerialization.serialize(borderValue5);
		final java.util.List<Boolean> borderValue5JsonDeserialized = jsonSerialization.deserializeList(Boolean.class, borderValue5JsonSerialized.content, borderValue5JsonSerialized.length);
		BooleanAsserts.assertNullableListOfNullableEquals(borderValue5, borderValue5JsonDeserialized);
	}
}
