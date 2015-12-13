package com.dslplatform.json.generated.types.Float;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.FloatAsserts;

import java.io.IOException;

public class OneListOfOneFloatsDefaultValueTurtle {
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
		FloatAsserts.assertOneListOfOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.util.List<Float> borderValue1 = new java.util.ArrayList<Float>(java.util.Arrays.asList(0.0f));
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.util.List<Float> borderValue1JsonDeserialized = jsonSerialization.deserializeList(Float.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		FloatAsserts.assertOneListOfOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.util.List<Float> borderValue2 = new java.util.ArrayList<Float>(java.util.Arrays.asList(Float.POSITIVE_INFINITY));
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.util.List<Float> borderValue2JsonDeserialized = jsonSerialization.deserializeList(Float.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		FloatAsserts.assertOneListOfOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.util.List<Float> borderValue3 = new java.util.ArrayList<Float>(java.util.Arrays.asList(0.0f, -1.2345E-10f, 1.2345E20f, -1E-5f, Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY));
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.util.List<Float> borderValue3JsonDeserialized = jsonSerialization.deserializeList(Float.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		FloatAsserts.assertOneListOfOneEquals(borderValue3, borderValue3JsonDeserialized);
	}
}
