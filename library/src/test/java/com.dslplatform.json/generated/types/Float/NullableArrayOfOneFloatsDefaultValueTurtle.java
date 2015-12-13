package com.dslplatform.json.generated.types.Float;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.FloatAsserts;

import java.io.IOException;

public class NullableArrayOfOneFloatsDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final float[] defaultValue = null;
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final float[] defaultValueJsonDeserialized = jsonSerialization.deserialize(float[].class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		FloatAsserts.assertNullableArrayOfOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final float[] borderValue1 = new float[] { 0.0f };
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final float[] borderValue1JsonDeserialized = jsonSerialization.deserialize(float[].class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		FloatAsserts.assertNullableArrayOfOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final float[] borderValue2 = new float[] { Float.POSITIVE_INFINITY };
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final float[] borderValue2JsonDeserialized = jsonSerialization.deserialize(float[].class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		FloatAsserts.assertNullableArrayOfOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final float[] borderValue3 = new float[] { 0.0f, -1.2345E-10f, 1.2345E20f, -1E-5f, Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY };
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final float[] borderValue3JsonDeserialized = jsonSerialization.deserialize(float[].class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		FloatAsserts.assertNullableArrayOfOneEquals(borderValue3, borderValue3JsonDeserialized);
	}
}
