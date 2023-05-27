package com.dslplatform.json.generated.types.Date;


import com.dslplatform.json.generated.ocd.javaasserts.DateAsserts;
import com.dslplatform.json.generated.types.StaticJsonJava;

import java.io.IOException;

public class OneArrayOfNullableDatesDefaultValueTurtle {
	private static StaticJsonJava.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJsonJava.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.time.LocalDate[] defaultValue = new java.time.LocalDate[0];
		final StaticJsonJava.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.time.LocalDate[] defaultValueJsonDeserialized = jsonSerialization.deserialize(java.time.LocalDate[].class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		DateAsserts.assertOneArrayOfNullableEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.time.LocalDate[] borderValue1 = new java.time.LocalDate[] { null };
		final StaticJsonJava.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.time.LocalDate[] borderValue1JsonDeserialized = jsonSerialization.deserialize(java.time.LocalDate[].class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		DateAsserts.assertOneArrayOfNullableEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.time.LocalDate[] borderValue2 = new java.time.LocalDate[] { java.time.LocalDate.now() };
		final StaticJsonJava.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.time.LocalDate[] borderValue2JsonDeserialized = jsonSerialization.deserialize(java.time.LocalDate[].class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		DateAsserts.assertOneArrayOfNullableEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.time.LocalDate[] borderValue3 = new java.time.LocalDate[] { java.time.LocalDate.of(9999, 12, 31) };
		final StaticJsonJava.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.time.LocalDate[] borderValue3JsonDeserialized = jsonSerialization.deserialize(java.time.LocalDate[].class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		DateAsserts.assertOneArrayOfNullableEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final java.time.LocalDate[] borderValue4 = new java.time.LocalDate[] { java.time.LocalDate.now(), java.time.LocalDate.of(1, 2, 3), java.time.LocalDate.of(1, 1, 1), java.time.LocalDate.of(1970, 1, 1), java.time.LocalDate.of(2038, 2, 13), java.time.LocalDate.of(9999, 12, 31) };
		final StaticJsonJava.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final java.time.LocalDate[] borderValue4JsonDeserialized = jsonSerialization.deserialize(java.time.LocalDate[].class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		DateAsserts.assertOneArrayOfNullableEquals(borderValue4, borderValue4JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue5Equality() throws IOException {
		final java.time.LocalDate[] borderValue5 = new java.time.LocalDate[] { null, java.time.LocalDate.now(), java.time.LocalDate.of(1, 2, 3), java.time.LocalDate.of(1, 1, 1), java.time.LocalDate.of(1970, 1, 1), java.time.LocalDate.of(2038, 2, 13), java.time.LocalDate.of(9999, 12, 31) };
		final StaticJsonJava.Bytes borderValue5JsonSerialized = jsonSerialization.serialize(borderValue5);
		final java.time.LocalDate[] borderValue5JsonDeserialized = jsonSerialization.deserialize(java.time.LocalDate[].class, borderValue5JsonSerialized.content, borderValue5JsonSerialized.length);
		DateAsserts.assertOneArrayOfNullableEquals(borderValue5, borderValue5JsonDeserialized);
	}
}
