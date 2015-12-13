package com.dslplatform.json.generated.types.Point;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.PointAsserts;

import java.io.IOException;

public class OneListOfOnePointsDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.util.List<java.awt.Point> defaultValue = new java.util.ArrayList<java.awt.Point>(0);
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.util.List<java.awt.Point> defaultValueJsonDeserialized = jsonSerialization.deserializeList(java.awt.Point.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		PointAsserts.assertOneListOfOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.util.List<java.awt.Point> borderValue1 = new java.util.ArrayList<java.awt.Point>(java.util.Arrays.asList(new java.awt.Point()));
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.util.List<java.awt.Point> borderValue1JsonDeserialized = jsonSerialization.deserializeList(java.awt.Point.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		PointAsserts.assertOneListOfOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.util.List<java.awt.Point> borderValue2 = new java.util.ArrayList<java.awt.Point>(java.util.Arrays.asList(new java.awt.Point(0, 1000000000)));
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.util.List<java.awt.Point> borderValue2JsonDeserialized = jsonSerialization.deserializeList(java.awt.Point.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		PointAsserts.assertOneListOfOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.util.List<java.awt.Point> borderValue3 = new java.util.ArrayList<java.awt.Point>(java.util.Arrays.asList(new java.awt.Point(), new java.awt.Point(Integer.MIN_VALUE, Integer.MIN_VALUE), new java.awt.Point(Integer.MAX_VALUE, Integer.MAX_VALUE), new java.awt.Point(0, -1000000000), new java.awt.Point(0, 1000000000)));
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.util.List<java.awt.Point> borderValue3JsonDeserialized = jsonSerialization.deserializeList(java.awt.Point.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		PointAsserts.assertOneListOfOneEquals(borderValue3, borderValue3JsonDeserialized);
	}
}
