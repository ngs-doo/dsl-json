package com.dslplatform.json.generated.types.Boolean;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.BooleanAsserts;

import java.io.IOException;

public class OneListOfOneBooleansDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.util.List<Boolean> defaultValue = new java.util.ArrayList<Boolean>(0);
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.util.List<Boolean> defaultValueJsonDeserialized = jsonSerialization.deserializeList(Boolean.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		BooleanAsserts.assertOneListOfOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.util.List<Boolean> borderValue1 = new java.util.ArrayList<Boolean>(java.util.Arrays.asList(false));
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.util.List<Boolean> borderValue1JsonDeserialized = jsonSerialization.deserializeList(Boolean.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		BooleanAsserts.assertOneListOfOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.util.List<Boolean> borderValue2 = new java.util.ArrayList<Boolean>(java.util.Arrays.asList(true));
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.util.List<Boolean> borderValue2JsonDeserialized = jsonSerialization.deserializeList(Boolean.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		BooleanAsserts.assertOneListOfOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.util.List<Boolean> borderValue3 = new java.util.ArrayList<Boolean>(java.util.Arrays.asList(false, true));
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.util.List<Boolean> borderValue3JsonDeserialized = jsonSerialization.deserializeList(Boolean.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		BooleanAsserts.assertOneListOfOneEquals(borderValue3, borderValue3JsonDeserialized);
	}
}
