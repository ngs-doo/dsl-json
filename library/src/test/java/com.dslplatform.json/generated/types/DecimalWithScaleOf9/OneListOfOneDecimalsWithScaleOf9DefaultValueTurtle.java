package com.dslplatform.json.generated.types.DecimalWithScaleOf9;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.DecimalWithScaleOf9Asserts;

import java.io.IOException;

public class OneListOfOneDecimalsWithScaleOf9DefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.util.List<java.math.BigDecimal> defaultValue = new java.util.ArrayList<java.math.BigDecimal>(0);
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.util.List<java.math.BigDecimal> defaultValueJsonDeserialized = jsonSerialization.deserializeList(java.math.BigDecimal.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		DecimalWithScaleOf9Asserts.assertOneListOfOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.util.List<java.math.BigDecimal> borderValue1 = new java.util.ArrayList<java.math.BigDecimal>(java.util.Arrays.asList(java.math.BigDecimal.ZERO.setScale(9)));
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.util.List<java.math.BigDecimal> borderValue1JsonDeserialized = jsonSerialization.deserializeList(java.math.BigDecimal.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		DecimalWithScaleOf9Asserts.assertOneListOfOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.util.List<java.math.BigDecimal> borderValue2 = new java.util.ArrayList<java.math.BigDecimal>(java.util.Arrays.asList(new java.math.BigDecimal("1E19")));
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.util.List<java.math.BigDecimal> borderValue2JsonDeserialized = jsonSerialization.deserializeList(java.math.BigDecimal.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		DecimalWithScaleOf9Asserts.assertOneListOfOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.util.List<java.math.BigDecimal> borderValue3 = new java.util.ArrayList<java.math.BigDecimal>(java.util.Arrays.asList(java.math.BigDecimal.ZERO.setScale(9), java.math.BigDecimal.ONE, new java.math.BigDecimal("3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679").setScale(9, java.math.BigDecimal.ROUND_HALF_UP), new java.math.BigDecimal("-1E-9"), new java.math.BigDecimal("1E19")));
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.util.List<java.math.BigDecimal> borderValue3JsonDeserialized = jsonSerialization.deserializeList(java.math.BigDecimal.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		DecimalWithScaleOf9Asserts.assertOneListOfOneEquals(borderValue3, borderValue3JsonDeserialized);
	}
}
