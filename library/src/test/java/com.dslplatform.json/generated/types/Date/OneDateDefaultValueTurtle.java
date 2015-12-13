package com.dslplatform.json.generated.types.Date;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.DateAsserts;

import java.io.IOException;

public class OneDateDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final org.joda.time.LocalDate defaultValue = org.joda.time.LocalDate.now();
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final org.joda.time.LocalDate defaultValueJsonDeserialized = jsonSerialization.deserialize(org.joda.time.LocalDate.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		DateAsserts.assertOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final org.joda.time.LocalDate borderValue1 = new org.joda.time.LocalDate(1, 2, 3);
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final org.joda.time.LocalDate borderValue1JsonDeserialized = jsonSerialization.deserialize(org.joda.time.LocalDate.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		DateAsserts.assertOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final org.joda.time.LocalDate borderValue2 = new org.joda.time.LocalDate(1, 1, 1);
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final org.joda.time.LocalDate borderValue2JsonDeserialized = jsonSerialization.deserialize(org.joda.time.LocalDate.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		DateAsserts.assertOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final org.joda.time.LocalDate borderValue3 = new org.joda.time.LocalDate(0);
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final org.joda.time.LocalDate borderValue3JsonDeserialized = jsonSerialization.deserialize(org.joda.time.LocalDate.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		DateAsserts.assertOneEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final org.joda.time.LocalDate borderValue4 = new org.joda.time.LocalDate(Integer.MAX_VALUE * 1001L);
		final StaticJson.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final org.joda.time.LocalDate borderValue4JsonDeserialized = jsonSerialization.deserialize(org.joda.time.LocalDate.class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		DateAsserts.assertOneEquals(borderValue4, borderValue4JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue5Equality() throws IOException {
		final org.joda.time.LocalDate borderValue5 = new org.joda.time.LocalDate(9999, 12, 31);
		final StaticJson.Bytes borderValue5JsonSerialized = jsonSerialization.serialize(borderValue5);
		final org.joda.time.LocalDate borderValue5JsonDeserialized = jsonSerialization.deserialize(org.joda.time.LocalDate.class, borderValue5JsonSerialized.content, borderValue5JsonSerialized.length);
		DateAsserts.assertOneEquals(borderValue5, borderValue5JsonDeserialized);
	}
}
