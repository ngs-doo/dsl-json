package com.dslplatform.json.generated.types.Guid;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.GuidAsserts;

import java.io.IOException;

public class OneArrayOfNullableGuidsDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.util.UUID[] defaultValue = new java.util.UUID[0];
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.util.UUID[] defaultValueJsonDeserialized = jsonSerialization.deserialize(java.util.UUID[].class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		GuidAsserts.assertOneArrayOfNullableEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.util.UUID[] borderValue1 = new java.util.UUID[] { null };
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.util.UUID[] borderValue1JsonDeserialized = jsonSerialization.deserialize(java.util.UUID[].class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		GuidAsserts.assertOneArrayOfNullableEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.util.UUID[] borderValue2 = new java.util.UUID[] { java.util.UUID.randomUUID() };
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.util.UUID[] borderValue2JsonDeserialized = jsonSerialization.deserialize(java.util.UUID[].class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		GuidAsserts.assertOneArrayOfNullableEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.util.UUID[] borderValue3 = new java.util.UUID[] { new java.util.UUID(0L, 0L) };
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.util.UUID[] borderValue3JsonDeserialized = jsonSerialization.deserialize(java.util.UUID[].class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		GuidAsserts.assertOneArrayOfNullableEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final java.util.UUID[] borderValue4 = new java.util.UUID[] { java.util.UUID.randomUUID(), java.util.UUID.fromString("1-2-3-4-5"), new java.util.UUID(0L, 0L) };
		final StaticJson.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final java.util.UUID[] borderValue4JsonDeserialized = jsonSerialization.deserialize(java.util.UUID[].class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		GuidAsserts.assertOneArrayOfNullableEquals(borderValue4, borderValue4JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue5Equality() throws IOException {
		final java.util.UUID[] borderValue5 = new java.util.UUID[] { null, java.util.UUID.randomUUID(), java.util.UUID.fromString("1-2-3-4-5"), new java.util.UUID(0L, 0L) };
		final StaticJson.Bytes borderValue5JsonSerialized = jsonSerialization.serialize(borderValue5);
		final java.util.UUID[] borderValue5JsonDeserialized = jsonSerialization.deserialize(java.util.UUID[].class, borderValue5JsonSerialized.content, borderValue5JsonSerialized.length);
		GuidAsserts.assertOneArrayOfNullableEquals(borderValue5, borderValue5JsonDeserialized);
	}
}
