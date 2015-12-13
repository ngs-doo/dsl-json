package com.dslplatform.json.generated.types.Binary;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.BinaryAsserts;

import java.io.IOException;

public class NullableArrayOfNullableBinariesDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final byte[][] defaultValue = null;
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final byte[][] defaultValueJsonDeserialized = jsonSerialization.deserialize(byte[][].class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		BinaryAsserts.assertNullableArrayOfNullableEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final byte[][] borderValue1 = new byte[][] { null };
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final byte[][] borderValue1JsonDeserialized = jsonSerialization.deserialize(byte[][].class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		BinaryAsserts.assertNullableArrayOfNullableEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final byte[][] borderValue2 = new byte[][] { new byte[0] };
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final byte[][] borderValue2JsonDeserialized = jsonSerialization.deserialize(byte[][].class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		BinaryAsserts.assertNullableArrayOfNullableEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final byte[][] borderValue3 = new byte[][] { new byte[] { Byte.MIN_VALUE, 0, Byte.MAX_VALUE } };
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final byte[][] borderValue3JsonDeserialized = jsonSerialization.deserialize(byte[][].class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		BinaryAsserts.assertNullableArrayOfNullableEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final byte[][] borderValue4 = new byte[][] { new byte[0], new byte[] { Byte.MIN_VALUE }, new byte[] { Byte.MIN_VALUE, 0 }, new byte[] { Byte.MIN_VALUE, 0, Byte.MAX_VALUE } };
		final StaticJson.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final byte[][] borderValue4JsonDeserialized = jsonSerialization.deserialize(byte[][].class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		BinaryAsserts.assertNullableArrayOfNullableEquals(borderValue4, borderValue4JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue5Equality() throws IOException {
		final byte[][] borderValue5 = new byte[][] { null, new byte[0], new byte[] { Byte.MIN_VALUE }, new byte[] { Byte.MIN_VALUE, 0 }, new byte[] { Byte.MIN_VALUE, 0, Byte.MAX_VALUE } };
		final StaticJson.Bytes borderValue5JsonSerialized = jsonSerialization.serialize(borderValue5);
		final byte[][] borderValue5JsonDeserialized = jsonSerialization.deserialize(byte[][].class, borderValue5JsonSerialized.content, borderValue5JsonSerialized.length);
		BinaryAsserts.assertNullableArrayOfNullableEquals(borderValue5, borderValue5JsonDeserialized);
	}
}
