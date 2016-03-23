package com.dslplatform.json.generated.types.Ip;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.IpAsserts;
import com.dslplatform.json.generated.ocd.test.TypeFactory;

import java.io.IOException;

public class NullableIpDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.net.InetAddress defaultValue = null;
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.net.InetAddress defaultValueJsonDeserialized = jsonSerialization.deserialize(java.net.InetAddress.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		IpAsserts.assertNullableEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.net.InetAddress borderValue1 = TypeFactory.buildIP("127.0.0.1");
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.net.InetAddress borderValue1JsonDeserialized = jsonSerialization.deserialize(java.net.InetAddress.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		IpAsserts.assertNullableEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.net.InetAddress borderValue2 = TypeFactory.buildIP("0");
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.net.InetAddress borderValue2JsonDeserialized = jsonSerialization.deserialize(java.net.InetAddress.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		IpAsserts.assertNullableEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.net.InetAddress borderValue3 = TypeFactory.buildIP("255.255.255.255");
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.net.InetAddress borderValue3JsonDeserialized = jsonSerialization.deserialize(java.net.InetAddress.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		IpAsserts.assertNullableEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final java.net.InetAddress borderValue4 = TypeFactory.buildIP("::1");
		final StaticJson.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final java.net.InetAddress borderValue4JsonDeserialized = jsonSerialization.deserialize(java.net.InetAddress.class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		IpAsserts.assertNullableEquals(borderValue4, borderValue4JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue5Equality() throws IOException {
		final java.net.InetAddress borderValue5 = TypeFactory.buildIP("ffff::ffff");
		final StaticJson.Bytes borderValue5JsonSerialized = jsonSerialization.serialize(borderValue5);
		final java.net.InetAddress borderValue5JsonDeserialized = jsonSerialization.deserialize(java.net.InetAddress.class, borderValue5JsonSerialized.content, borderValue5JsonSerialized.length);
		IpAsserts.assertNullableEquals(borderValue5, borderValue5JsonDeserialized);
	}
}
