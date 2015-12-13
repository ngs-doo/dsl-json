package com.dslplatform.json.generated.types.Long;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.LongAsserts;

import java.io.IOException;

public class OneLongDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final long defaultValue = 0L;
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final long defaultValueJsonDeserialized = jsonSerialization.deserialize(long.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		LongAsserts.assertOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final long borderValue1 = 1L;
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final long borderValue1JsonDeserialized = jsonSerialization.deserialize(long.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		LongAsserts.assertOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final long borderValue2 = 1000000000000000000L;
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final long borderValue2JsonDeserialized = jsonSerialization.deserialize(long.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		LongAsserts.assertOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final long borderValue3 = -1000000000000000000L;
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final long borderValue3JsonDeserialized = jsonSerialization.deserialize(long.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		LongAsserts.assertOneEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final long borderValue4 = Long.MIN_VALUE;
		final StaticJson.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final long borderValue4JsonDeserialized = jsonSerialization.deserialize(long.class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		LongAsserts.assertOneEquals(borderValue4, borderValue4JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue5Equality() throws IOException {
		final long borderValue5 = Long.MAX_VALUE;
		final StaticJson.Bytes borderValue5JsonSerialized = jsonSerialization.serialize(borderValue5);
		final long borderValue5JsonDeserialized = jsonSerialization.deserialize(long.class, borderValue5JsonSerialized.content, borderValue5JsonSerialized.length);
		LongAsserts.assertOneEquals(borderValue5, borderValue5JsonDeserialized);
	}
}
