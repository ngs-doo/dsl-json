package com.dslplatform.json.generated.types.Date;


import com.dslplatform.json.generated.ocd.javaasserts.DateAsserts;
import com.dslplatform.json.generated.types.StaticJsonJoda;

import java.io.IOException;

public class NullableSetOfNullableDatesDefaultValueTurtle {
	private static StaticJsonJoda.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJsonJoda.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.util.Set<org.joda.time.LocalDate> defaultValue = null;
		final StaticJsonJoda.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.util.List<org.joda.time.LocalDate> deserializedTmpList = jsonSerialization.deserializeList(org.joda.time.LocalDate.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		final java.util.Set<org.joda.time.LocalDate> defaultValueJsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<org.joda.time.LocalDate>(deserializedTmpList);
		DateAsserts.assertNullableSetOfNullableEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.util.Set<org.joda.time.LocalDate> borderValue1 = new java.util.HashSet<org.joda.time.LocalDate>(java.util.Arrays.asList((org.joda.time.LocalDate) null));
		final StaticJsonJoda.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.util.List<org.joda.time.LocalDate> deserializedTmpList = jsonSerialization.deserializeList(org.joda.time.LocalDate.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		final java.util.Set<org.joda.time.LocalDate> borderValue1JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<org.joda.time.LocalDate>(deserializedTmpList);
		DateAsserts.assertNullableSetOfNullableEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.util.Set<org.joda.time.LocalDate> borderValue2 = new java.util.HashSet<org.joda.time.LocalDate>(java.util.Arrays.asList(org.joda.time.LocalDate.now()));
		final StaticJsonJoda.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.util.List<org.joda.time.LocalDate> deserializedTmpList = jsonSerialization.deserializeList(org.joda.time.LocalDate.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		final java.util.Set<org.joda.time.LocalDate> borderValue2JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<org.joda.time.LocalDate>(deserializedTmpList);
		DateAsserts.assertNullableSetOfNullableEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.util.Set<org.joda.time.LocalDate> borderValue3 = new java.util.HashSet<org.joda.time.LocalDate>(java.util.Arrays.asList(new org.joda.time.LocalDate(9999, 12, 31)));
		final StaticJsonJoda.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.util.List<org.joda.time.LocalDate> deserializedTmpList = jsonSerialization.deserializeList(org.joda.time.LocalDate.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		final java.util.Set<org.joda.time.LocalDate> borderValue3JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<org.joda.time.LocalDate>(deserializedTmpList);
		DateAsserts.assertNullableSetOfNullableEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final java.util.Set<org.joda.time.LocalDate> borderValue4 = new java.util.HashSet<org.joda.time.LocalDate>(java.util.Arrays.asList(org.joda.time.LocalDate.now(), new org.joda.time.LocalDate(1, 2, 3), new org.joda.time.LocalDate(1, 1, 1), new org.joda.time.LocalDate(0), new org.joda.time.LocalDate(Integer.MAX_VALUE * 1001L), new org.joda.time.LocalDate(9999, 12, 31)));
		final StaticJsonJoda.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final java.util.List<org.joda.time.LocalDate> deserializedTmpList = jsonSerialization.deserializeList(org.joda.time.LocalDate.class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		final java.util.Set<org.joda.time.LocalDate> borderValue4JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<org.joda.time.LocalDate>(deserializedTmpList);
		DateAsserts.assertNullableSetOfNullableEquals(borderValue4, borderValue4JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue5Equality() throws IOException {
		final java.util.Set<org.joda.time.LocalDate> borderValue5 = new java.util.HashSet<org.joda.time.LocalDate>(java.util.Arrays.asList((org.joda.time.LocalDate) null, org.joda.time.LocalDate.now(), new org.joda.time.LocalDate(1, 2, 3), new org.joda.time.LocalDate(1, 1, 1), new org.joda.time.LocalDate(0), new org.joda.time.LocalDate(Integer.MAX_VALUE * 1001L), new org.joda.time.LocalDate(9999, 12, 31)));
		final StaticJsonJoda.Bytes borderValue5JsonSerialized = jsonSerialization.serialize(borderValue5);
		final java.util.List<org.joda.time.LocalDate> deserializedTmpList = jsonSerialization.deserializeList(org.joda.time.LocalDate.class, borderValue5JsonSerialized.content, borderValue5JsonSerialized.length);
		final java.util.Set<org.joda.time.LocalDate> borderValue5JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<org.joda.time.LocalDate>(deserializedTmpList);
		DateAsserts.assertNullableSetOfNullableEquals(borderValue5, borderValue5JsonDeserialized);
	}
}
