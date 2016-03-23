package com.dslplatform.json.generated.types.Integer;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.IntegerAsserts;

import java.io.IOException;

public class OneIntegerDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final int defaultValue = 0;
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final int defaultValueJsonDeserialized = jsonSerialization.deserialize(int.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		IntegerAsserts.assertOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final int borderValue1 = Integer.MIN_VALUE;
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final int borderValue1JsonDeserialized = jsonSerialization.deserialize(int.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		IntegerAsserts.assertOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final int borderValue2 = Integer.MAX_VALUE;
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final int borderValue2JsonDeserialized = jsonSerialization.deserialize(int.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		IntegerAsserts.assertOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final int borderValue3 = -1000000000;
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final int borderValue3JsonDeserialized = jsonSerialization.deserialize(int.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		IntegerAsserts.assertOneEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final int borderValue4 = 1000000000;
		final StaticJson.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final int borderValue4JsonDeserialized = jsonSerialization.deserialize(int.class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		IntegerAsserts.assertOneEquals(borderValue4, borderValue4JsonDeserialized);
	}
}
