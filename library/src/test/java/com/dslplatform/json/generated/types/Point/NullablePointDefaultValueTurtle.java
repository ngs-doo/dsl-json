package com.dslplatform.json.generated.types.Point;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.PointAsserts;

import java.io.IOException;

public class NullablePointDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.awt.Point defaultValue = null;
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.awt.Point defaultValueJsonDeserialized = jsonSerialization.deserialize(java.awt.Point.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		PointAsserts.assertNullableEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.awt.Point borderValue1 = new java.awt.Point();
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.awt.Point borderValue1JsonDeserialized = jsonSerialization.deserialize(java.awt.Point.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		PointAsserts.assertNullableEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.awt.Point borderValue2 = new java.awt.Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.awt.Point borderValue2JsonDeserialized = jsonSerialization.deserialize(java.awt.Point.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		PointAsserts.assertNullableEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.awt.Point borderValue3 = new java.awt.Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.awt.Point borderValue3JsonDeserialized = jsonSerialization.deserialize(java.awt.Point.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		PointAsserts.assertNullableEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final java.awt.Point borderValue4 = new java.awt.Point(0, -1000000000);
		final StaticJson.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final java.awt.Point borderValue4JsonDeserialized = jsonSerialization.deserialize(java.awt.Point.class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		PointAsserts.assertNullableEquals(borderValue4, borderValue4JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue5Equality() throws IOException {
		final java.awt.Point borderValue5 = new java.awt.Point(0, 1000000000);
		final StaticJson.Bytes borderValue5JsonSerialized = jsonSerialization.serialize(borderValue5);
		final java.awt.Point borderValue5JsonDeserialized = jsonSerialization.deserialize(java.awt.Point.class, borderValue5JsonSerialized.content, borderValue5JsonSerialized.length);
		PointAsserts.assertNullableEquals(borderValue5, borderValue5JsonDeserialized);
	}
}
