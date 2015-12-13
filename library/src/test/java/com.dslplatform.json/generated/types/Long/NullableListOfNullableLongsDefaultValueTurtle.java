package com.dslplatform.json.generated.types.Long;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.LongAsserts;

import java.io.IOException;

public class NullableListOfNullableLongsDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.util.List<Long> defaultValue = null;
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.util.List<Long> defaultValueJsonDeserialized = jsonSerialization.deserializeList(Long.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		LongAsserts.assertNullableListOfNullableEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.util.List<Long> borderValue1 = new java.util.ArrayList<Long>(java.util.Arrays.asList((Long) null));
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.util.List<Long> borderValue1JsonDeserialized = jsonSerialization.deserializeList(Long.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		LongAsserts.assertNullableListOfNullableEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.util.List<Long> borderValue2 = new java.util.ArrayList<Long>(java.util.Arrays.asList(0L));
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.util.List<Long> borderValue2JsonDeserialized = jsonSerialization.deserializeList(Long.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		LongAsserts.assertNullableListOfNullableEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.util.List<Long> borderValue3 = new java.util.ArrayList<Long>(java.util.Arrays.asList(Long.MAX_VALUE));
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.util.List<Long> borderValue3JsonDeserialized = jsonSerialization.deserializeList(Long.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		LongAsserts.assertNullableListOfNullableEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final java.util.List<Long> borderValue4 = new java.util.ArrayList<Long>(java.util.Arrays.asList(0L, 1L, 1000000000000000000L, -1000000000000000000L, Long.MIN_VALUE, Long.MAX_VALUE));
		final StaticJson.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final java.util.List<Long> borderValue4JsonDeserialized = jsonSerialization.deserializeList(Long.class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		LongAsserts.assertNullableListOfNullableEquals(borderValue4, borderValue4JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue5Equality() throws IOException {
		final java.util.List<Long> borderValue5 = new java.util.ArrayList<Long>(java.util.Arrays.asList((Long) null, 0L, 1L, 1000000000000000000L, -1000000000000000000L, Long.MIN_VALUE, Long.MAX_VALUE));
		final StaticJson.Bytes borderValue5JsonSerialized = jsonSerialization.serialize(borderValue5);
		final java.util.List<Long> borderValue5JsonDeserialized = jsonSerialization.deserializeList(Long.class, borderValue5JsonSerialized.content, borderValue5JsonSerialized.length);
		LongAsserts.assertNullableListOfNullableEquals(borderValue5, borderValue5JsonDeserialized);
	}
}
