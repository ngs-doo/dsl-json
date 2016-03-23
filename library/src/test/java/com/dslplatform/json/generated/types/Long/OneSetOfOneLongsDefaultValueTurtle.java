package com.dslplatform.json.generated.types.Long;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.LongAsserts;

import java.io.IOException;

public class OneSetOfOneLongsDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.util.Set<Long> defaultValue = new java.util.HashSet<Long>(0);
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.util.List<Long> deserializedTmpList = jsonSerialization.deserializeList(Long.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		final java.util.Set<Long> defaultValueJsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<Long>(deserializedTmpList);
		LongAsserts.assertOneSetOfOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.util.Set<Long> borderValue1 = new java.util.HashSet<Long>(java.util.Arrays.asList(0L));
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.util.List<Long> deserializedTmpList = jsonSerialization.deserializeList(Long.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		final java.util.Set<Long> borderValue1JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<Long>(deserializedTmpList);
		LongAsserts.assertOneSetOfOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.util.Set<Long> borderValue2 = new java.util.HashSet<Long>(java.util.Arrays.asList(Long.MAX_VALUE));
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.util.List<Long> deserializedTmpList = jsonSerialization.deserializeList(Long.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		final java.util.Set<Long> borderValue2JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<Long>(deserializedTmpList);
		LongAsserts.assertOneSetOfOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.util.Set<Long> borderValue3 = new java.util.HashSet<Long>(java.util.Arrays.asList(0L, 1L, 1000000000000000000L, -1000000000000000000L, Long.MIN_VALUE, Long.MAX_VALUE));
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.util.List<Long> deserializedTmpList = jsonSerialization.deserializeList(Long.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		final java.util.Set<Long> borderValue3JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<Long>(deserializedTmpList);
		LongAsserts.assertOneSetOfOneEquals(borderValue3, borderValue3JsonDeserialized);
	}
}
