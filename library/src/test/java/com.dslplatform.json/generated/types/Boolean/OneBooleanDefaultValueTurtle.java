package com.dslplatform.json.generated.types.Boolean;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.BooleanAsserts;

import java.io.IOException;

public class OneBooleanDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final boolean defaultValue = false;
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final boolean defaultValueJsonDeserialized = jsonSerialization.deserialize(boolean.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		BooleanAsserts.assertOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final boolean borderValue1 = true;
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final boolean borderValue1JsonDeserialized = jsonSerialization.deserialize(boolean.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		BooleanAsserts.assertOneEquals(borderValue1, borderValue1JsonDeserialized);
	}
}
