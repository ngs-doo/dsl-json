package com.dslplatform.json.generated.types.Guid;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.GuidAsserts;

import java.io.IOException;

public class OneGuidDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.util.UUID defaultValue = java.util.UUID.randomUUID();
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.util.UUID defaultValueJsonDeserialized = jsonSerialization.deserialize(java.util.UUID.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		GuidAsserts.assertOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.util.UUID borderValue1 = java.util.UUID.fromString("1-2-3-4-5");
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.util.UUID borderValue1JsonDeserialized = jsonSerialization.deserialize(java.util.UUID.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		GuidAsserts.assertOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.util.UUID borderValue2 = new java.util.UUID(0L, 0L);
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.util.UUID borderValue2JsonDeserialized = jsonSerialization.deserialize(java.util.UUID.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		GuidAsserts.assertOneEquals(borderValue2, borderValue2JsonDeserialized);
	}
}
