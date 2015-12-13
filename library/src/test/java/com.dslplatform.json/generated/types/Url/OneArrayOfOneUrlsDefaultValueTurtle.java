package com.dslplatform.json.generated.types.Url;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.UrlAsserts;
import com.dslplatform.json.generated.ocd.test.TypeFactory;

import java.io.IOException;

public class OneArrayOfOneUrlsDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.net.URI[] defaultValue = new java.net.URI[0];
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.net.URI[] defaultValueJsonDeserialized = jsonSerialization.deserialize(java.net.URI[].class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		UrlAsserts.assertOneArrayOfOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.net.URI[] borderValue1 = new java.net.URI[] { TypeFactory.buildURI("failover:(tcp://localhost:8181,tcp://localhost:8080/)") };
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.net.URI[] borderValue1JsonDeserialized = jsonSerialization.deserialize(java.net.URI[].class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		UrlAsserts.assertOneArrayOfOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.net.URI[] borderValue2 = new java.net.URI[] { TypeFactory.buildURI("http://127.0.0.1/"), TypeFactory.buildURI("http://www.xyz.com/"), TypeFactory.buildURI("https://www.abc.com/"), TypeFactory.buildURI("ftp://www.pqr.com/"), TypeFactory.buildURI("https://localhost:8080/"), TypeFactory.buildURI("mailto:snail@mail.hu"), TypeFactory.buildURI("file:///~/opt/somefile.md"), TypeFactory.buildURI("tcp://localhost:8181/"), TypeFactory.buildURI("failover:(tcp://localhost:8181,tcp://localhost:8080/)") };
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.net.URI[] borderValue2JsonDeserialized = jsonSerialization.deserialize(java.net.URI[].class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		UrlAsserts.assertOneArrayOfOneEquals(borderValue2, borderValue2JsonDeserialized);
	}
}
