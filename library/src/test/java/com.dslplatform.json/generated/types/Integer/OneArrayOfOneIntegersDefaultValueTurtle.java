package com.dslplatform.json.generated.types.Integer;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.IntegerAsserts;

import java.io.IOException;

public class OneArrayOfOneIntegersDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final int[] defaultValue = new int[0];
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final int[] defaultValueJsonDeserialized = jsonSerialization.deserialize(int[].class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		IntegerAsserts.assertOneArrayOfOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final int[] borderValue1 = new int[] { 0 };
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final int[] borderValue1JsonDeserialized = jsonSerialization.deserialize(int[].class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		IntegerAsserts.assertOneArrayOfOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final int[] borderValue2 = new int[] { 1000000000 };
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final int[] borderValue2JsonDeserialized = jsonSerialization.deserialize(int[].class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		IntegerAsserts.assertOneArrayOfOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final int[] borderValue3 = new int[] { 0, Integer.MIN_VALUE, Integer.MAX_VALUE, -1000000000, 1000000000 };
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final int[] borderValue3JsonDeserialized = jsonSerialization.deserialize(int[].class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		IntegerAsserts.assertOneArrayOfOneEquals(borderValue3, borderValue3JsonDeserialized);
	}
}
