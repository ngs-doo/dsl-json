package com.dslplatform.json.generated.types.Date;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.DateAsserts;

import java.io.IOException;

public class NullableListOfOneDatesDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.util.List<org.joda.time.LocalDate> defaultValue = null;
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.util.List<org.joda.time.LocalDate> defaultValueJsonDeserialized = jsonSerialization.deserializeList(org.joda.time.LocalDate.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		DateAsserts.assertNullableListOfOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.util.List<org.joda.time.LocalDate> borderValue1 = new java.util.ArrayList<org.joda.time.LocalDate>(java.util.Arrays.asList(org.joda.time.LocalDate.now()));
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.util.List<org.joda.time.LocalDate> borderValue1JsonDeserialized = jsonSerialization.deserializeList(org.joda.time.LocalDate.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		DateAsserts.assertNullableListOfOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.util.List<org.joda.time.LocalDate> borderValue2 = new java.util.ArrayList<org.joda.time.LocalDate>(java.util.Arrays.asList(new org.joda.time.LocalDate(9999, 12, 31)));
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.util.List<org.joda.time.LocalDate> borderValue2JsonDeserialized = jsonSerialization.deserializeList(org.joda.time.LocalDate.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		DateAsserts.assertNullableListOfOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.util.List<org.joda.time.LocalDate> borderValue3 = new java.util.ArrayList<org.joda.time.LocalDate>(java.util.Arrays.asList(org.joda.time.LocalDate.now(), new org.joda.time.LocalDate(1, 2, 3), new org.joda.time.LocalDate(1, 1, 1), new org.joda.time.LocalDate(0), new org.joda.time.LocalDate(Integer.MAX_VALUE * 1001L), new org.joda.time.LocalDate(9999, 12, 31)));
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.util.List<org.joda.time.LocalDate> borderValue3JsonDeserialized = jsonSerialization.deserializeList(org.joda.time.LocalDate.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		DateAsserts.assertNullableListOfOneEquals(borderValue3, borderValue3JsonDeserialized);
	}
}
