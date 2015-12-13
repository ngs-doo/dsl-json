package com.dslplatform.json.generated.types.Float;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.FloatAsserts;

import java.io.IOException;

public class OneListOfNullableFloatsDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.util.List<Float> defaultValue = new java.util.ArrayList<Float>(0);
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.util.List<Float> defaultValueJsonDeserialized = jsonSerialization.deserializeList(Float.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		FloatAsserts.assertOneListOfNullableEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.util.List<Float> borderValue1 = new java.util.ArrayList<Float>(java.util.Arrays.asList((Float) null));
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.util.List<Float> borderValue1JsonDeserialized = jsonSerialization.deserializeList(Float.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		FloatAsserts.assertOneListOfNullableEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.util.List<Float> borderValue2 = new java.util.ArrayList<Float>(java.util.Arrays.asList(0.0f));
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.util.List<Float> borderValue2JsonDeserialized = jsonSerialization.deserializeList(Float.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		FloatAsserts.assertOneListOfNullableEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.util.List<Float> borderValue3 = new java.util.ArrayList<Float>(java.util.Arrays.asList(Float.POSITIVE_INFINITY));
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.util.List<Float> borderValue3JsonDeserialized = jsonSerialization.deserializeList(Float.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		FloatAsserts.assertOneListOfNullableEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final java.util.List<Float> borderValue4 = new java.util.ArrayList<Float>(java.util.Arrays.asList(0.0f, -1.2345E-10f, 1.2345E20f, -1E-5f, Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY));
		final StaticJson.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final java.util.List<Float> borderValue4JsonDeserialized = jsonSerialization.deserializeList(Float.class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		FloatAsserts.assertOneListOfNullableEquals(borderValue4, borderValue4JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue5Equality() throws IOException {
		final java.util.List<Float> borderValue5 = new java.util.ArrayList<Float>(java.util.Arrays.asList((Float) null, 0.0f, -1.2345E-10f, 1.2345E20f, -1E-5f, Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY));
		final StaticJson.Bytes borderValue5JsonSerialized = jsonSerialization.serialize(borderValue5);
		final java.util.List<Float> borderValue5JsonDeserialized = jsonSerialization.deserializeList(Float.class, borderValue5JsonSerialized.content, borderValue5JsonSerialized.length);
		FloatAsserts.assertOneListOfNullableEquals(borderValue5, borderValue5JsonDeserialized);
	}
}
