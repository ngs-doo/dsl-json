package com.dslplatform.json.generated.types.Float;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.FloatAsserts;

import java.io.IOException;

public class OneArrayOfNullableFloatsDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final Float[] defaultValue = new Float[0];
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final Float[] defaultValueJsonDeserialized = jsonSerialization.deserialize(Float[].class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		FloatAsserts.assertOneArrayOfNullableEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final Float[] borderValue1 = new Float[] { null };
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final Float[] borderValue1JsonDeserialized = jsonSerialization.deserialize(Float[].class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		FloatAsserts.assertOneArrayOfNullableEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final Float[] borderValue2 = new Float[] { 0.0f };
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final Float[] borderValue2JsonDeserialized = jsonSerialization.deserialize(Float[].class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		FloatAsserts.assertOneArrayOfNullableEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final Float[] borderValue3 = new Float[] { Float.POSITIVE_INFINITY };
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final Float[] borderValue3JsonDeserialized = jsonSerialization.deserialize(Float[].class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		FloatAsserts.assertOneArrayOfNullableEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final Float[] borderValue4 = new Float[] { 0.0f, -1.2345E-10f, 1.2345E20f, -1E-5f, Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY };
		final StaticJson.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final Float[] borderValue4JsonDeserialized = jsonSerialization.deserialize(Float[].class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		FloatAsserts.assertOneArrayOfNullableEquals(borderValue4, borderValue4JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue5Equality() throws IOException {
		final Float[] borderValue5 = new Float[] { null, 0.0f, -1.2345E-10f, 1.2345E20f, -1E-5f, Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY };
		final StaticJson.Bytes borderValue5JsonSerialized = jsonSerialization.serialize(borderValue5);
		final Float[] borderValue5JsonDeserialized = jsonSerialization.deserialize(Float[].class, borderValue5JsonSerialized.content, borderValue5JsonSerialized.length);
		FloatAsserts.assertOneArrayOfNullableEquals(borderValue5, borderValue5JsonDeserialized);
	}
}
