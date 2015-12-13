package com.dslplatform.json.generated.types.Location;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.LocationAsserts;

import java.io.IOException;

public class NullableArrayOfOneLocationsDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.awt.geom.Point2D[] defaultValue = null;
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.awt.geom.Point2D[] defaultValueJsonDeserialized = jsonSerialization.deserialize(java.awt.geom.Point2D[].class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		LocationAsserts.assertNullableArrayOfOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.awt.geom.Point2D[] borderValue1 = new java.awt.geom.Point2D[] { new java.awt.geom.Point2D.Float() };
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.awt.geom.Point2D[] borderValue1JsonDeserialized = jsonSerialization.deserialize(java.awt.geom.Point2D[].class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		LocationAsserts.assertNullableArrayOfOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.awt.geom.Point2D[] borderValue2 = new java.awt.geom.Point2D[] { new java.awt.geom.Point2D.Double(-1.000000000000001, 1.000000000000001) };
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.awt.geom.Point2D[] borderValue2JsonDeserialized = jsonSerialization.deserialize(java.awt.geom.Point2D[].class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		LocationAsserts.assertNullableArrayOfOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.awt.geom.Point2D[] borderValue3 = new java.awt.geom.Point2D[] { new java.awt.geom.Point2D.Float(), new java.awt.Point(Integer.MIN_VALUE, Integer.MAX_VALUE), new java.awt.Point(-1000000000, 1000000000), new java.awt.geom.Point2D.Float(Float.MIN_VALUE, Float.MAX_VALUE), new java.awt.geom.Point2D.Float(-1.0000001f, 1.0000001f), new java.awt.geom.Point2D.Double(Double.MIN_VALUE, Double.MAX_VALUE), new java.awt.geom.Point2D.Double(-1.000000000000001, 1.000000000000001) };
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.awt.geom.Point2D[] borderValue3JsonDeserialized = jsonSerialization.deserialize(java.awt.geom.Point2D[].class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		LocationAsserts.assertNullableArrayOfOneEquals(borderValue3, borderValue3JsonDeserialized);
	}
}
