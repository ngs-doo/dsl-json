package com.dslplatform.json.generated.types.Date;


import com.dslplatform.json.generated.ocd.javaasserts.DateAsserts;
import com.dslplatform.json.generated.types.StaticJsonJava;

import java.io.IOException;

public class NullableDateDefaultValueTurtle {
	private static StaticJsonJava.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJsonJava.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.time.LocalDate defaultValue = null;
		final StaticJsonJava.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.time.LocalDate defaultValueJsonDeserialized = jsonSerialization.deserialize(java.time.LocalDate.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		DateAsserts.assertNullableEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.time.LocalDate borderValue1 = java.time.LocalDate.now();
		final StaticJsonJava.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.time.LocalDate borderValue1JsonDeserialized = jsonSerialization.deserialize(java.time.LocalDate.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		DateAsserts.assertNullableEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.time.LocalDate borderValue2 = java.time.LocalDate.of(1, 2, 3);
		final StaticJsonJava.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.time.LocalDate borderValue2JsonDeserialized = jsonSerialization.deserialize(java.time.LocalDate.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		DateAsserts.assertNullableEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.time.LocalDate borderValue3 = java.time.LocalDate.of(1, 1, 1);
		final StaticJsonJava.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.time.LocalDate borderValue3JsonDeserialized = jsonSerialization.deserialize(java.time.LocalDate.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		DateAsserts.assertNullableEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final java.time.LocalDate borderValue4 = java.time.LocalDate.of(1970, 1, 1);
		final StaticJsonJava.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final java.time.LocalDate borderValue4JsonDeserialized = jsonSerialization.deserialize(java.time.LocalDate.class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		DateAsserts.assertNullableEquals(borderValue4, borderValue4JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue5Equality() throws IOException {
		final java.time.LocalDate borderValue5 = java.time.LocalDate.of(2038, 2, 13);
		final StaticJsonJava.Bytes borderValue5JsonSerialized = jsonSerialization.serialize(borderValue5);
		final java.time.LocalDate borderValue5JsonDeserialized = jsonSerialization.deserialize(java.time.LocalDate.class, borderValue5JsonSerialized.content, borderValue5JsonSerialized.length);
		DateAsserts.assertNullableEquals(borderValue5, borderValue5JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue6Equality() throws IOException {
		final java.time.LocalDate borderValue6 = java.time.LocalDate.of(9999, 12, 31);
		final StaticJsonJava.Bytes borderValue6JsonSerialized = jsonSerialization.serialize(borderValue6);
		final java.time.LocalDate borderValue6JsonDeserialized = jsonSerialization.deserialize(java.time.LocalDate.class, borderValue6JsonSerialized.content, borderValue6JsonSerialized.length);
		DateAsserts.assertNullableEquals(borderValue6, borderValue6JsonDeserialized);
	}
}
