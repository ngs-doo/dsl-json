package com.dslplatform.json.generated.types.Rectangle;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.RectangleAsserts;

import java.io.IOException;

public class OneArrayOfNullableRectanglesDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.awt.geom.Rectangle2D[] defaultValue = new java.awt.geom.Rectangle2D[0];
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.awt.geom.Rectangle2D[] defaultValueJsonDeserialized = jsonSerialization.deserialize(java.awt.geom.Rectangle2D[].class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		RectangleAsserts.assertOneArrayOfNullableEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.awt.geom.Rectangle2D[] borderValue1 = new java.awt.geom.Rectangle2D[] { null };
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.awt.geom.Rectangle2D[] borderValue1JsonDeserialized = jsonSerialization.deserialize(java.awt.geom.Rectangle2D[].class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		RectangleAsserts.assertOneArrayOfNullableEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.awt.geom.Rectangle2D[] borderValue2 = new java.awt.geom.Rectangle2D[] { new java.awt.geom.Rectangle2D.Float() };
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.awt.geom.Rectangle2D[] borderValue2JsonDeserialized = jsonSerialization.deserialize(java.awt.geom.Rectangle2D[].class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		RectangleAsserts.assertOneArrayOfNullableEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.awt.geom.Rectangle2D[] borderValue3 = new java.awt.geom.Rectangle2D[] { new java.awt.geom.Rectangle2D.Double(-1.000000000000001, -1.000000000000001, 1.000000000000001, 1.000000000000001) };
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.awt.geom.Rectangle2D[] borderValue3JsonDeserialized = jsonSerialization.deserialize(java.awt.geom.Rectangle2D[].class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		RectangleAsserts.assertOneArrayOfNullableEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final java.awt.geom.Rectangle2D[] borderValue4 = new java.awt.geom.Rectangle2D[] { new java.awt.geom.Rectangle2D.Float(), new java.awt.Rectangle(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE), new java.awt.Rectangle(-1000000000, -1000000000, 1000000000, 1000000000), new java.awt.geom.Rectangle2D.Float(Float.MIN_VALUE, Float.MIN_VALUE, Float.MAX_VALUE, Float.MAX_VALUE), new java.awt.geom.Rectangle2D.Float(-1.0000001f, -1.0000001f, 1.0000001f, 1.0000001f), new java.awt.geom.Rectangle2D.Double(Double.MIN_VALUE, Double.MIN_VALUE, Double.MAX_VALUE, Double.MAX_VALUE), new java.awt.geom.Rectangle2D.Double(-1.000000000000001, -1.000000000000001, 1.000000000000001, 1.000000000000001) };
		final StaticJson.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final java.awt.geom.Rectangle2D[] borderValue4JsonDeserialized = jsonSerialization.deserialize(java.awt.geom.Rectangle2D[].class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		RectangleAsserts.assertOneArrayOfNullableEquals(borderValue4, borderValue4JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue5Equality() throws IOException {
		final java.awt.geom.Rectangle2D[] borderValue5 = new java.awt.geom.Rectangle2D[] { null, new java.awt.geom.Rectangle2D.Float(), new java.awt.Rectangle(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE), new java.awt.Rectangle(-1000000000, -1000000000, 1000000000, 1000000000), new java.awt.geom.Rectangle2D.Float(Float.MIN_VALUE, Float.MIN_VALUE, Float.MAX_VALUE, Float.MAX_VALUE), new java.awt.geom.Rectangle2D.Float(-1.0000001f, -1.0000001f, 1.0000001f, 1.0000001f), new java.awt.geom.Rectangle2D.Double(Double.MIN_VALUE, Double.MIN_VALUE, Double.MAX_VALUE, Double.MAX_VALUE), new java.awt.geom.Rectangle2D.Double(-1.000000000000001, -1.000000000000001, 1.000000000000001, 1.000000000000001) };
		final StaticJson.Bytes borderValue5JsonSerialized = jsonSerialization.serialize(borderValue5);
		final java.awt.geom.Rectangle2D[] borderValue5JsonDeserialized = jsonSerialization.deserialize(java.awt.geom.Rectangle2D[].class, borderValue5JsonSerialized.content, borderValue5JsonSerialized.length);
		RectangleAsserts.assertOneArrayOfNullableEquals(borderValue5, borderValue5JsonDeserialized);
	}
}
