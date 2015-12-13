package com.dslplatform.json.generated.types.Binary;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.BinaryAsserts;

import java.io.IOException;

public class OneListOfOneBinariesDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.util.List<byte[]> defaultValue = new java.util.ArrayList<byte[]>(0);
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.util.List<byte[]> defaultValueJsonDeserialized = jsonSerialization.deserializeList(byte[].class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		BinaryAsserts.assertOneListOfOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.util.List<byte[]> borderValue1 = new java.util.ArrayList<byte[]>(java.util.Arrays.asList(new byte[0]));
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.util.List<byte[]> borderValue1JsonDeserialized = jsonSerialization.deserializeList(byte[].class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		BinaryAsserts.assertOneListOfOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.util.List<byte[]> borderValue2 = new java.util.ArrayList<byte[]>(java.util.Arrays.asList(new byte[] { Byte.MIN_VALUE, 0, Byte.MAX_VALUE }));
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.util.List<byte[]> borderValue2JsonDeserialized = jsonSerialization.deserializeList(byte[].class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		BinaryAsserts.assertOneListOfOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.util.List<byte[]> borderValue3 = new java.util.ArrayList<byte[]>(java.util.Arrays.asList(new byte[0], new byte[] { Byte.MIN_VALUE }, new byte[] { Byte.MIN_VALUE, 0 }, new byte[] { Byte.MIN_VALUE, 0, Byte.MAX_VALUE }));
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.util.List<byte[]> borderValue3JsonDeserialized = jsonSerialization.deserializeList(byte[].class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		BinaryAsserts.assertOneListOfOneEquals(borderValue3, borderValue3JsonDeserialized);
	}
}
