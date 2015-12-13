package com.dslplatform.json.generated.types.Boolean;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.BooleanAsserts;

import java.io.IOException;

public class NullableBooleanDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final Boolean defaultValue = null;
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final Boolean defaultValueJsonDeserialized = jsonSerialization.deserialize(Boolean.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		BooleanAsserts.assertNullableEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final Boolean borderValue1 = false;
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final Boolean borderValue1JsonDeserialized = jsonSerialization.deserialize(Boolean.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		BooleanAsserts.assertNullableEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final Boolean borderValue2 = true;
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final Boolean borderValue2JsonDeserialized = jsonSerialization.deserialize(Boolean.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		BooleanAsserts.assertNullableEquals(borderValue2, borderValue2JsonDeserialized);
	}
}
