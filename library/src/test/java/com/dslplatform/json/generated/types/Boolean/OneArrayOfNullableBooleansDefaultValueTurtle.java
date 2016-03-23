package com.dslplatform.json.generated.types.Boolean;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.BooleanAsserts;

import java.io.IOException;

public class OneArrayOfNullableBooleansDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final Boolean[] defaultValue = new Boolean[0];
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final Boolean[] defaultValueJsonDeserialized = jsonSerialization.deserialize(Boolean[].class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		BooleanAsserts.assertOneArrayOfNullableEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final Boolean[] borderValue1 = new Boolean[] { null };
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final Boolean[] borderValue1JsonDeserialized = jsonSerialization.deserialize(Boolean[].class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		BooleanAsserts.assertOneArrayOfNullableEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final Boolean[] borderValue2 = new Boolean[] { false };
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final Boolean[] borderValue2JsonDeserialized = jsonSerialization.deserialize(Boolean[].class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		BooleanAsserts.assertOneArrayOfNullableEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final Boolean[] borderValue3 = new Boolean[] { true };
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final Boolean[] borderValue3JsonDeserialized = jsonSerialization.deserialize(Boolean[].class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		BooleanAsserts.assertOneArrayOfNullableEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final Boolean[] borderValue4 = new Boolean[] { false, true };
		final StaticJson.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final Boolean[] borderValue4JsonDeserialized = jsonSerialization.deserialize(Boolean[].class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		BooleanAsserts.assertOneArrayOfNullableEquals(borderValue4, borderValue4JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue5Equality() throws IOException {
		final Boolean[] borderValue5 = new Boolean[] { null, false, true };
		final StaticJson.Bytes borderValue5JsonSerialized = jsonSerialization.serialize(borderValue5);
		final Boolean[] borderValue5JsonDeserialized = jsonSerialization.deserialize(Boolean[].class, borderValue5JsonSerialized.content, borderValue5JsonSerialized.length);
		BooleanAsserts.assertOneArrayOfNullableEquals(borderValue5, borderValue5JsonDeserialized);
	}
}
