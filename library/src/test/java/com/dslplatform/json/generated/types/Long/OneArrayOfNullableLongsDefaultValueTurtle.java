package com.dslplatform.json.generated.types.Long;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.LongAsserts;

import java.io.IOException;

public class OneArrayOfNullableLongsDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final Long[] defaultValue = new Long[0];
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final Long[] defaultValueJsonDeserialized = jsonSerialization.deserialize(Long[].class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		LongAsserts.assertOneArrayOfNullableEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final Long[] borderValue1 = new Long[] { null };
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final Long[] borderValue1JsonDeserialized = jsonSerialization.deserialize(Long[].class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		LongAsserts.assertOneArrayOfNullableEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final Long[] borderValue2 = new Long[] { 0L };
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final Long[] borderValue2JsonDeserialized = jsonSerialization.deserialize(Long[].class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		LongAsserts.assertOneArrayOfNullableEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final Long[] borderValue3 = new Long[] { Long.MAX_VALUE };
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final Long[] borderValue3JsonDeserialized = jsonSerialization.deserialize(Long[].class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		LongAsserts.assertOneArrayOfNullableEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final Long[] borderValue4 = new Long[] { 0L, 1L, 1000000000000000000L, -1000000000000000000L, Long.MIN_VALUE, Long.MAX_VALUE };
		final StaticJson.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final Long[] borderValue4JsonDeserialized = jsonSerialization.deserialize(Long[].class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		LongAsserts.assertOneArrayOfNullableEquals(borderValue4, borderValue4JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue5Equality() throws IOException {
		final Long[] borderValue5 = new Long[] { null, 0L, 1L, 1000000000000000000L, -1000000000000000000L, Long.MIN_VALUE, Long.MAX_VALUE };
		final StaticJson.Bytes borderValue5JsonSerialized = jsonSerialization.serialize(borderValue5);
		final Long[] borderValue5JsonDeserialized = jsonSerialization.deserialize(Long[].class, borderValue5JsonSerialized.content, borderValue5JsonSerialized.length);
		LongAsserts.assertOneArrayOfNullableEquals(borderValue5, borderValue5JsonDeserialized);
	}
}
