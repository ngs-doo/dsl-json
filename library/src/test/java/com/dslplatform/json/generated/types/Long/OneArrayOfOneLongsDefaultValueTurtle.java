package com.dslplatform.json.generated.types.Long;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.LongAsserts;

import java.io.IOException;

public class OneArrayOfOneLongsDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final long[] defaultValue = new long[0];
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final long[] defaultValueJsonDeserialized = jsonSerialization.deserialize(long[].class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		LongAsserts.assertOneArrayOfOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final long[] borderValue1 = new long[] { 0L };
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final long[] borderValue1JsonDeserialized = jsonSerialization.deserialize(long[].class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		LongAsserts.assertOneArrayOfOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final long[] borderValue2 = new long[] { Long.MAX_VALUE };
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final long[] borderValue2JsonDeserialized = jsonSerialization.deserialize(long[].class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		LongAsserts.assertOneArrayOfOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final long[] borderValue3 = new long[] { 0L, 1L, 1000000000000000000L, -1000000000000000000L, Long.MIN_VALUE, Long.MAX_VALUE };
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final long[] borderValue3JsonDeserialized = jsonSerialization.deserialize(long[].class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		LongAsserts.assertOneArrayOfOneEquals(borderValue3, borderValue3JsonDeserialized);
	}
}
