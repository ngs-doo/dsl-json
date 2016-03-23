package com.dslplatform.json.generated.types.Money;



import com.dslplatform.json.generated.types.StaticJson;
import com.dslplatform.json.generated.ocd.javaasserts.MoneyAsserts;

import java.io.IOException;

public class OneArrayOfOneMoniesDefaultValueTurtle {
	private static StaticJson.JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final java.math.BigDecimal[] defaultValue = new java.math.BigDecimal[0];
		final StaticJson.Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final java.math.BigDecimal[] defaultValueJsonDeserialized = jsonSerialization.deserialize(java.math.BigDecimal[].class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		MoneyAsserts.assertOneArrayOfOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final java.math.BigDecimal[] borderValue1 = new java.math.BigDecimal[] { java.math.BigDecimal.ZERO.setScale(2) };
		final StaticJson.Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final java.math.BigDecimal[] borderValue1JsonDeserialized = jsonSerialization.deserialize(java.math.BigDecimal[].class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		MoneyAsserts.assertOneArrayOfOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final java.math.BigDecimal[] borderValue2 = new java.math.BigDecimal[] { new java.math.BigDecimal("1E19") };
		final StaticJson.Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final java.math.BigDecimal[] borderValue2JsonDeserialized = jsonSerialization.deserialize(java.math.BigDecimal[].class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		MoneyAsserts.assertOneArrayOfOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final java.math.BigDecimal[] borderValue3 = new java.math.BigDecimal[] { java.math.BigDecimal.ZERO.setScale(2), java.math.BigDecimal.ONE, new java.math.BigDecimal("3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679").setScale(2, java.math.BigDecimal.ROUND_HALF_UP), new java.math.BigDecimal("-1E-2"), new java.math.BigDecimal("1E19") };
		final StaticJson.Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final java.math.BigDecimal[] borderValue3JsonDeserialized = jsonSerialization.deserialize(java.math.BigDecimal[].class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		MoneyAsserts.assertOneArrayOfOneEquals(borderValue3, borderValue3JsonDeserialized);
	}
}
