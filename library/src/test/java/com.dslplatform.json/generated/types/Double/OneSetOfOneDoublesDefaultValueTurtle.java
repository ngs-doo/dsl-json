package com.dslplatform.json.generated.types.Double;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.DoubleAsserts;

import java.io.IOException;

public class OneSetOfOneDoublesDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.util.Set<Double> defaultValue = new java.util.HashSet<Double>(0);
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.util.List<Double> deserializedTmpList = jsonSerialization.deserializeList(Double.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		final java.util.Set<Double> defaultValueJsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<Double>(deserializedTmpList);
		DoubleAsserts.assertOneSetOfOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.util.Set<Double> borderValue1 = new java.util.HashSet<Double>(java.util.Arrays.asList(0.0));
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.util.List<Double> deserializedTmpList = jsonSerialization.deserializeList(Double.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		final java.util.Set<Double> borderValue1JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<Double>(deserializedTmpList);
		DoubleAsserts.assertOneSetOfOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.util.Set<Double> borderValue2 = new java.util.HashSet<Double>(java.util.Arrays.asList(Double.NaN));
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.util.List<Double> deserializedTmpList = jsonSerialization.deserializeList(Double.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		final java.util.Set<Double> borderValue2JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<Double>(deserializedTmpList);
		DoubleAsserts.assertOneSetOfOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.util.Set<Double> borderValue3 = new java.util.HashSet<Double>(java.util.Arrays.asList(0.0, 1E-307, 9E307, -1.23456789012345E-10, 1.23456789012345E20, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NaN));
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.util.List<Double> deserializedTmpList = jsonSerialization.deserializeList(Double.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		final java.util.Set<Double> borderValue3JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<Double>(deserializedTmpList);
		DoubleAsserts.assertOneSetOfOneEquals(borderValue3, borderValue3JsonDeserialized);
	}
}
