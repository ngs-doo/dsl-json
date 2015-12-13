package com.dslplatform.json.generated.types.Url;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.UrlAsserts;
import com.dslplatform.json.generated.ocd.test.TypeFactory;

import java.io.IOException;

public class OneUrlDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.net.URI defaultValue = TypeFactory.buildURI("http://127.0.0.1/");
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.net.URI defaultValueJsonDeserialized = jsonSerialization.deserialize(java.net.URI.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		UrlAsserts.assertOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.net.URI borderValue1 = TypeFactory.buildURI("http://www.xyz.com/");
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.net.URI borderValue1JsonDeserialized = jsonSerialization.deserialize(java.net.URI.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		UrlAsserts.assertOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.net.URI borderValue2 = TypeFactory.buildURI("https://www.abc.com/");
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.net.URI borderValue2JsonDeserialized = jsonSerialization.deserialize(java.net.URI.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		UrlAsserts.assertOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.net.URI borderValue3 = TypeFactory.buildURI("ftp://www.pqr.com/");
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.net.URI borderValue3JsonDeserialized = jsonSerialization.deserialize(java.net.URI.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		UrlAsserts.assertOneEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final java.net.URI borderValue4 = TypeFactory.buildURI("https://localhost:8080/");
		final StaticJson.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final java.net.URI borderValue4JsonDeserialized = jsonSerialization.deserialize(java.net.URI.class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		UrlAsserts.assertOneEquals(borderValue4, borderValue4JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue5Equality() throws IOException {
		final java.net.URI borderValue5 = TypeFactory.buildURI("mailto:snail@mail.hu");
		final StaticJson.Bytes borderValue5JsonSerialized = jsonSerialization.serialize(borderValue5);
		final java.net.URI borderValue5JsonDeserialized = jsonSerialization.deserialize(java.net.URI.class, borderValue5JsonSerialized.content, borderValue5JsonSerialized.length);
		UrlAsserts.assertOneEquals(borderValue5, borderValue5JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue6Equality() throws IOException {
		final java.net.URI borderValue6 = TypeFactory.buildURI("file:///~/opt/somefile.md");
		final StaticJson.Bytes borderValue6JsonSerialized = jsonSerialization.serialize(borderValue6);
		final java.net.URI borderValue6JsonDeserialized = jsonSerialization.deserialize(java.net.URI.class, borderValue6JsonSerialized.content, borderValue6JsonSerialized.length);
		UrlAsserts.assertOneEquals(borderValue6, borderValue6JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue7Equality() throws IOException {
		final java.net.URI borderValue7 = TypeFactory.buildURI("tcp://localhost:8181/");
		final StaticJson.Bytes borderValue7JsonSerialized = jsonSerialization.serialize(borderValue7);
		final java.net.URI borderValue7JsonDeserialized = jsonSerialization.deserialize(java.net.URI.class, borderValue7JsonSerialized.content, borderValue7JsonSerialized.length);
		UrlAsserts.assertOneEquals(borderValue7, borderValue7JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue8Equality() throws IOException {
		final java.net.URI borderValue8 = TypeFactory.buildURI("failover:(tcp://localhost:8181,tcp://localhost:8080/)");
		final StaticJson.Bytes borderValue8JsonSerialized = jsonSerialization.serialize(borderValue8);
		final java.net.URI borderValue8JsonDeserialized = jsonSerialization.deserialize(java.net.URI.class, borderValue8JsonSerialized.content, borderValue8JsonSerialized.length);
		UrlAsserts.assertOneEquals(borderValue8, borderValue8JsonDeserialized);
	}
}
