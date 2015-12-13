package com.dslplatform.json.generated.types.StringWithMaxLengthOf9;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.StringWithMaxLengthOf9Asserts;

import java.io.IOException;

public class OneArrayOfOneStringsWithMaxLengthOf9DefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final String[] defaultValue = new String[0];
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final String[] defaultValueJsonDeserialized = jsonSerialization.deserialize(String[].class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		StringWithMaxLengthOf9Asserts.assertOneArrayOfOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final String[] borderValue1 = new String[] { "" };
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final String[] borderValue1JsonDeserialized = jsonSerialization.deserialize(String[].class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		StringWithMaxLengthOf9Asserts.assertOneArrayOfOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final String[] borderValue2 = new String[] { "xxxxxxxxx" };
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final String[] borderValue2JsonDeserialized = jsonSerialization.deserialize(String[].class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		StringWithMaxLengthOf9Asserts.assertOneArrayOfOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final String[] borderValue3 = new String[] { "", "\"", "'/\\[](){}", "\u0001\u0002\u0003\u0004\u0005\u0006\u0007\b\t", "xxxxxxxxx" };
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final String[] borderValue3JsonDeserialized = jsonSerialization.deserialize(String[].class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		StringWithMaxLengthOf9Asserts.assertOneArrayOfOneEquals(borderValue3, borderValue3JsonDeserialized);
	}
}
