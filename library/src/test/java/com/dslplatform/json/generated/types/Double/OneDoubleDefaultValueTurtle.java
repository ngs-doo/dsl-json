package com.dslplatform.json.generated.types.Double;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.DoubleAsserts;

import java.io.IOException;

public class OneDoubleDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final double defaultValue = 0.0;
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final double defaultValueJsonDeserialized = jsonSerialization.deserialize(double.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		DoubleAsserts.assertOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final double borderValue1 = 1E-307;
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final double borderValue1JsonDeserialized = jsonSerialization.deserialize(double.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		DoubleAsserts.assertOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final double borderValue2 = 9E307;
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final double borderValue2JsonDeserialized = jsonSerialization.deserialize(double.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		DoubleAsserts.assertOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final double borderValue3 = -1.23456789012345E-10;
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final double borderValue3JsonDeserialized = jsonSerialization.deserialize(double.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		DoubleAsserts.assertOneEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final double borderValue4 = 1.23456789012345E20;
		final StaticJson.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final double borderValue4JsonDeserialized = jsonSerialization.deserialize(double.class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		DoubleAsserts.assertOneEquals(borderValue4, borderValue4JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue5Equality() throws IOException {
		final double borderValue5 = Double.NEGATIVE_INFINITY;
		final StaticJson.Bytes borderValue5JsonSerialized = jsonSerialization.serialize(borderValue5);
		final double borderValue5JsonDeserialized = jsonSerialization.deserialize(double.class, borderValue5JsonSerialized.content, borderValue5JsonSerialized.length);
		DoubleAsserts.assertOneEquals(borderValue5, borderValue5JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue6Equality() throws IOException {
		final double borderValue6 = Double.POSITIVE_INFINITY;
		final StaticJson.Bytes borderValue6JsonSerialized = jsonSerialization.serialize(borderValue6);
		final double borderValue6JsonDeserialized = jsonSerialization.deserialize(double.class, borderValue6JsonSerialized.content, borderValue6JsonSerialized.length);
		DoubleAsserts.assertOneEquals(borderValue6, borderValue6JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue7Equality() throws IOException {
		final double borderValue7 = Double.NaN;
		final StaticJson.Bytes borderValue7JsonSerialized = jsonSerialization.serialize(borderValue7);
		final double borderValue7JsonDeserialized = jsonSerialization.deserialize(double.class, borderValue7JsonSerialized.content, borderValue7JsonSerialized.length);
		DoubleAsserts.assertOneEquals(borderValue7, borderValue7JsonDeserialized);
	}
}
