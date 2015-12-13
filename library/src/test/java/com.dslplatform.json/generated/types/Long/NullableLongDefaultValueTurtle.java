package com.dslplatform.json.generated.types.Long;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.LongAsserts;

import java.io.IOException;

public class NullableLongDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final Long defaultValue = null;
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final Long defaultValueJsonDeserialized = jsonSerialization.deserialize(Long.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		LongAsserts.assertNullableEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final Long borderValue1 = 0L;
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final Long borderValue1JsonDeserialized = jsonSerialization.deserialize(Long.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		LongAsserts.assertNullableEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final Long borderValue2 = 1L;
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final Long borderValue2JsonDeserialized = jsonSerialization.deserialize(Long.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		LongAsserts.assertNullableEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final Long borderValue3 = 1000000000000000000L;
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final Long borderValue3JsonDeserialized = jsonSerialization.deserialize(Long.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		LongAsserts.assertNullableEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final Long borderValue4 = -1000000000000000000L;
		final StaticJson.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final Long borderValue4JsonDeserialized = jsonSerialization.deserialize(Long.class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		LongAsserts.assertNullableEquals(borderValue4, borderValue4JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue5Equality() throws IOException {
		final Long borderValue5 = Long.MIN_VALUE;
		final StaticJson.Bytes borderValue5JsonSerialized = jsonSerialization.serialize(borderValue5);
		final Long borderValue5JsonDeserialized = jsonSerialization.deserialize(Long.class, borderValue5JsonSerialized.content, borderValue5JsonSerialized.length);
		LongAsserts.assertNullableEquals(borderValue5, borderValue5JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue6Equality() throws IOException {
		final Long borderValue6 = Long.MAX_VALUE;
		final StaticJson.Bytes borderValue6JsonSerialized = jsonSerialization.serialize(borderValue6);
		final Long borderValue6JsonDeserialized = jsonSerialization.deserialize(Long.class, borderValue6JsonSerialized.content, borderValue6JsonSerialized.length);
		LongAsserts.assertNullableEquals(borderValue6, borderValue6JsonDeserialized);
	}
}
