package com.dslplatform.json.generated.types.Xml;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.XmlAsserts;
import com.dslplatform.json.generated.ocd.test.Utils;

import java.io.IOException;

public class NullableSetOfNullableXmlsDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.util.Set<org.w3c.dom.Element> defaultValue = null;
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.util.List<org.w3c.dom.Element> deserializedTmpList = jsonSerialization.deserializeList(org.w3c.dom.Element.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		final java.util.Set<org.w3c.dom.Element> defaultValueJsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<org.w3c.dom.Element>(deserializedTmpList);
		XmlAsserts.assertNullableSetOfNullableEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.util.Set<org.w3c.dom.Element> borderValue1 = new java.util.HashSet<org.w3c.dom.Element>(java.util.Arrays.asList((org.w3c.dom.Element) null));
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.util.List<org.w3c.dom.Element> deserializedTmpList = jsonSerialization.deserializeList(org.w3c.dom.Element.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		final java.util.Set<org.w3c.dom.Element> borderValue1JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<org.w3c.dom.Element>(deserializedTmpList);
		XmlAsserts.assertNullableSetOfNullableEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.util.Set<org.w3c.dom.Element> borderValue2 = new java.util.HashSet<org.w3c.dom.Element>(java.util.Arrays.asList(Utils.stringToElement("<ns3000:NamespacedElement/>")));
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.util.List<org.w3c.dom.Element> deserializedTmpList = jsonSerialization.deserializeList(org.w3c.dom.Element.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		final java.util.Set<org.w3c.dom.Element> borderValue2JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<org.w3c.dom.Element>(deserializedTmpList);
		XmlAsserts.assertNullableSetOfNullableEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.util.Set<org.w3c.dom.Element> borderValue3 = new java.util.HashSet<org.w3c.dom.Element>(java.util.Arrays.asList(Utils.stringToElement("<document/>"), Utils.stringToElement("<TextElement>some text &amp; &lt;stuff&gt;</TextElement>"), Utils.stringToElement("<ElementWithCData>&lt;?xml?&gt;&lt;xml&gt;&lt;!xml!&gt;</ElementWithCData>"), Utils.stringToElement("<AtributedElement foo=\"bar\" qwe=\"poi\"/>"), Utils.stringToElement("<NestedTextElement><FirstNest><SecondNest>bird</SecondNest></FirstNest></NestedTextElement>"), Utils.stringToElement("<ns3000:NamespacedElement/>")));
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.util.List<org.w3c.dom.Element> deserializedTmpList = jsonSerialization.deserializeList(org.w3c.dom.Element.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		final java.util.Set<org.w3c.dom.Element> borderValue3JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<org.w3c.dom.Element>(deserializedTmpList);
		XmlAsserts.assertNullableSetOfNullableEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final java.util.Set<org.w3c.dom.Element> borderValue4 = new java.util.HashSet<org.w3c.dom.Element>(java.util.Arrays.asList((org.w3c.dom.Element) null, Utils.stringToElement("<document/>"), Utils.stringToElement("<TextElement>some text &amp; &lt;stuff&gt;</TextElement>"), Utils.stringToElement("<ElementWithCData>&lt;?xml?&gt;&lt;xml&gt;&lt;!xml!&gt;</ElementWithCData>"), Utils.stringToElement("<AtributedElement foo=\"bar\" qwe=\"poi\"/>"), Utils.stringToElement("<NestedTextElement><FirstNest><SecondNest>bird</SecondNest></FirstNest></NestedTextElement>"), Utils.stringToElement("<ns3000:NamespacedElement/>")));
		final StaticJson.Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final java.util.List<org.w3c.dom.Element> deserializedTmpList = jsonSerialization.deserializeList(org.w3c.dom.Element.class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		final java.util.Set<org.w3c.dom.Element> borderValue4JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<org.w3c.dom.Element>(deserializedTmpList);
		XmlAsserts.assertNullableSetOfNullableEquals(borderValue4, borderValue4JsonDeserialized);
	}
}
