package com.dslplatform.json.generated.types.Date;


import com.dslplatform.json.generated.ocd.javaasserts.DateAsserts;
import com.dslplatform.json.generated.types.StaticJsonJava;

import java.io.IOException;

public class NullableSetOfOneDatesDefaultValueTurtle {
	private static StaticJsonJava.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJsonJava.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.util.Set<java.time.LocalDate> defaultValue = null;
		final StaticJsonJava.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.util.List<java.time.LocalDate> deserializedTmpList = jsonSerialization.deserializeList(java.time.LocalDate.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		final java.util.Set<java.time.LocalDate> defaultValueJsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<java.time.LocalDate>(deserializedTmpList);
		DateAsserts.assertNullableSetOfOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.util.Set<java.time.LocalDate> borderValue1 = new java.util.HashSet<java.time.LocalDate>(java.util.Arrays.asList(java.time.LocalDate.now()));
		final StaticJsonJava.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.util.List<java.time.LocalDate> deserializedTmpList = jsonSerialization.deserializeList(java.time.LocalDate.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		final java.util.Set<java.time.LocalDate> borderValue1JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<java.time.LocalDate>(deserializedTmpList);
		DateAsserts.assertNullableSetOfOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.util.Set<java.time.LocalDate> borderValue2 = new java.util.HashSet<java.time.LocalDate>(java.util.Arrays.asList(java.time.LocalDate.of(9999, 12, 31)));
		final StaticJsonJava.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.util.List<java.time.LocalDate> deserializedTmpList = jsonSerialization.deserializeList(java.time.LocalDate.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		final java.util.Set<java.time.LocalDate> borderValue2JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<java.time.LocalDate>(deserializedTmpList);
		DateAsserts.assertNullableSetOfOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.util.Set<java.time.LocalDate> borderValue3 = new java.util.HashSet<java.time.LocalDate>(java.util.Arrays.asList(java.time.LocalDate.now(), java.time.LocalDate.of(1, 2, 3), java.time.LocalDate.of(1, 1, 1), java.time.LocalDate.of(1970, 1, 1), java.time.LocalDate.of(2038, 2, 13), java.time.LocalDate.of(9999, 12, 31)));
		final StaticJsonJava.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.util.List<java.time.LocalDate> deserializedTmpList = jsonSerialization.deserializeList(java.time.LocalDate.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		final java.util.Set<java.time.LocalDate> borderValue3JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<java.time.LocalDate>(deserializedTmpList);
		DateAsserts.assertNullableSetOfOneEquals(borderValue3, borderValue3JsonDeserialized);
	}
}
