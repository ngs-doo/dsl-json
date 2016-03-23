package com.dslplatform.json.generated.types.Double;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.DoubleAsserts;

import java.io.IOException;

public class NullableDoubleDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final Double defaultValue = null;
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final Double defaultValueJsonDeserialized = jsonSerialization.deserialize(Double.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		DoubleAsserts.assertNullableEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final Double borderValue1 = 0.0;
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final Double borderValue1JsonDeserialized = jsonSerialization.deserialize(Double.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		DoubleAsserts.assertNullableEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final Double borderValue2 = 1E-307;
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final Double borderValue2JsonDeserialized = jsonSerialization.deserialize(Double.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		DoubleAsserts.assertNullableEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final Double borderValue3 = 9E307;
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final Double borderValue3JsonDeserialized = jsonSerialization.deserialize(Double.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		DoubleAsserts.assertNullableEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final Double borderValue4 = -1.23456789012345E-10;
		final StaticJson.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final Double borderValue4JsonDeserialized = jsonSerialization.deserialize(Double.class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		DoubleAsserts.assertNullableEquals(borderValue4, borderValue4JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue5Equality() throws IOException {
		final Double borderValue5 = 1.23456789012345E20;
		final StaticJson.Bytes borderValue5JsonSerialized = jsonSerialization.serialize(borderValue5);
		final Double borderValue5JsonDeserialized = jsonSerialization.deserialize(Double.class, borderValue5JsonSerialized.content, borderValue5JsonSerialized.length);
		DoubleAsserts.assertNullableEquals(borderValue5, borderValue5JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue6Equality() throws IOException {
		final Double borderValue6 = Double.NEGATIVE_INFINITY;
		final StaticJson.Bytes borderValue6JsonSerialized = jsonSerialization.serialize(borderValue6);
		final Double borderValue6JsonDeserialized = jsonSerialization.deserialize(Double.class, borderValue6JsonSerialized.content, borderValue6JsonSerialized.length);
		DoubleAsserts.assertNullableEquals(borderValue6, borderValue6JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue7Equality() throws IOException {
		final Double borderValue7 = Double.POSITIVE_INFINITY;
		final StaticJson.Bytes borderValue7JsonSerialized = jsonSerialization.serialize(borderValue7);
		final Double borderValue7JsonDeserialized = jsonSerialization.deserialize(Double.class, borderValue7JsonSerialized.content, borderValue7JsonSerialized.length);
		DoubleAsserts.assertNullableEquals(borderValue7, borderValue7JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue8Equality() throws IOException {
		final Double borderValue8 = Double.NaN;
		final StaticJson.Bytes borderValue8JsonSerialized = jsonSerialization.serialize(borderValue8);
		final Double borderValue8JsonDeserialized = jsonSerialization.deserialize(Double.class, borderValue8JsonSerialized.content, borderValue8JsonSerialized.length);
		DoubleAsserts.assertNullableEquals(borderValue8, borderValue8JsonDeserialized);
	}
}
