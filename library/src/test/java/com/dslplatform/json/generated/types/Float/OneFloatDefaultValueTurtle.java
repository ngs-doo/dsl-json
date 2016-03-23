package com.dslplatform.json.generated.types.Float;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.FloatAsserts;

import java.io.IOException;

public class OneFloatDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final float defaultValue = 0.0f;
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final float defaultValueJsonDeserialized = jsonSerialization.deserialize(float.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		FloatAsserts.assertOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final float borderValue1 = -1.2345E-10f;
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final float borderValue1JsonDeserialized = jsonSerialization.deserialize(float.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		FloatAsserts.assertOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final float borderValue2 = 1.2345E20f;
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final float borderValue2JsonDeserialized = jsonSerialization.deserialize(float.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		FloatAsserts.assertOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final float borderValue3 = -1E-5f;
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final float borderValue3JsonDeserialized = jsonSerialization.deserialize(float.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		FloatAsserts.assertOneEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final float borderValue4 = Float.NaN;
		final StaticJson.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final float borderValue4JsonDeserialized = jsonSerialization.deserialize(float.class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		FloatAsserts.assertOneEquals(borderValue4, borderValue4JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue5Equality() throws IOException {
		final float borderValue5 = Float.NEGATIVE_INFINITY;
		final StaticJson.Bytes borderValue5JsonSerialized = jsonSerialization.serialize(borderValue5);
		final float borderValue5JsonDeserialized = jsonSerialization.deserialize(float.class, borderValue5JsonSerialized.content, borderValue5JsonSerialized.length);
		FloatAsserts.assertOneEquals(borderValue5, borderValue5JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue6Equality() throws IOException {
		final float borderValue6 = Float.POSITIVE_INFINITY;
		final StaticJson.Bytes borderValue6JsonSerialized = jsonSerialization.serialize(borderValue6);
		final float borderValue6JsonDeserialized = jsonSerialization.deserialize(float.class, borderValue6JsonSerialized.content, borderValue6JsonSerialized.length);
		FloatAsserts.assertOneEquals(borderValue6, borderValue6JsonDeserialized);
	}
}
