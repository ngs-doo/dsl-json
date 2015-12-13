package com.dslplatform.json.generated.types.Location;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.LocationAsserts;

import java.io.IOException;

public class NullableSetOfOneLocationsDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.util.Set<java.awt.geom.Point2D> defaultValue = null;
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.util.List<java.awt.geom.Point2D> deserializedTmpList = jsonSerialization.deserializeList(java.awt.geom.Point2D.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		final java.util.Set<java.awt.geom.Point2D> defaultValueJsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<java.awt.geom.Point2D>(deserializedTmpList);
		LocationAsserts.assertNullableSetOfOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.util.Set<java.awt.geom.Point2D> borderValue1 = new java.util.HashSet<java.awt.geom.Point2D>(java.util.Arrays.asList(new java.awt.geom.Point2D.Float()));
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.util.List<java.awt.geom.Point2D> deserializedTmpList = jsonSerialization.deserializeList(java.awt.geom.Point2D.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		final java.util.Set<java.awt.geom.Point2D> borderValue1JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<java.awt.geom.Point2D>(deserializedTmpList);
		LocationAsserts.assertNullableSetOfOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.util.Set<java.awt.geom.Point2D> borderValue2 = new java.util.HashSet<java.awt.geom.Point2D>(java.util.Arrays.asList(new java.awt.geom.Point2D.Double(-1.000000000000001, 1.000000000000001)));
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.util.List<java.awt.geom.Point2D> deserializedTmpList = jsonSerialization.deserializeList(java.awt.geom.Point2D.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		final java.util.Set<java.awt.geom.Point2D> borderValue2JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<java.awt.geom.Point2D>(deserializedTmpList);
		LocationAsserts.assertNullableSetOfOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.util.Set<java.awt.geom.Point2D> borderValue3 = new java.util.HashSet<java.awt.geom.Point2D>(java.util.Arrays.asList(new java.awt.geom.Point2D.Float(), new java.awt.Point(Integer.MIN_VALUE, Integer.MAX_VALUE), new java.awt.Point(-1000000000, 1000000000), new java.awt.geom.Point2D.Float(Float.MIN_VALUE, Float.MAX_VALUE), new java.awt.geom.Point2D.Float(-1.0000001f, 1.0000001f), new java.awt.geom.Point2D.Double(Double.MIN_VALUE, Double.MAX_VALUE), new java.awt.geom.Point2D.Double(-1.000000000000001, 1.000000000000001)));
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.util.List<java.awt.geom.Point2D> deserializedTmpList = jsonSerialization.deserializeList(java.awt.geom.Point2D.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		final java.util.Set<java.awt.geom.Point2D> borderValue3JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<java.awt.geom.Point2D>(deserializedTmpList);
		LocationAsserts.assertNullableSetOfOneEquals(borderValue3, borderValue3JsonDeserialized);
	}
}
