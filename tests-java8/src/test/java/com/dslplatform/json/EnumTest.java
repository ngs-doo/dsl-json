package com.dslplatform.json;

import com.dslplatform.json.runtime.EnumAnalyzer;
import com.dslplatform.json.runtime.EnumDescription;
import com.dslplatform.json.runtime.Settings;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class EnumTest {

	@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
	public enum MyEnum1 {
		ABC,
		DEF,
		GHI;
	}

	public enum MyEnum2 {
		XX1,
		YY,
		ZZ2;
	}

	@CompiledJson
	public enum DuplicateHash {
		n3307663,
		n519524;
	}

	@CompiledJson
	public static class SingleNonImmutable {
		public MyEnum1 e1;
		private MyEnum2 e2;
		@JsonAttribute(nullable = false)
		@NotNull
		public MyEnum1 e3;

		public MyEnum2 e2() {
			return e2;
		}

		public void e2(MyEnum2 v) {
			this.e2 = v;
		}

		public Map<MyEnum1, Integer> map1;
		public List<MyEnum2> list2;
	}

	@CompiledJson
	public static class SingleImmutable {
		public final MyEnum1 e1;
		public final MyEnum2 e2;
		@JsonAttribute(nullable = false)
		public MyEnum1 e3;
		public final Map<MyEnum1, Integer> map1;
		private List<MyEnum2> list2;

		public List<MyEnum2> list2() {
			return list2;
		}

		public SingleImmutable(MyEnum1 e1, MyEnum2 e2, MyEnum1 e3, Map<MyEnum1, Integer> map1, List<MyEnum2> list2) {
			this.e1 = e1;
			this.e2 = e2;
			this.e3 = e3;
			this.map1 = map1;
			this.list2 = list2;
		}
	}

	// Test @JsonValue annotation on field
	@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
	public enum EnumWithCustomNames1 {
		TEST_A1("a1"),
		TEST_A2("a2"),
		TEST_A3("a3");

		@JsonValue
		public final String value;

		EnumWithCustomNames1(String value) {
			this.value = value;
		}
	}

	// Test @JsonValue annotation on method
	@CompiledJson
	public enum EnumWithCustomNames2 {
		TEST_B1("b1"),
		TEST_B2("b2"),
		TEST_B3("b3");

		private final String value;

		EnumWithCustomNames2(String value) {
			this.value = value;
		}

		@JsonValue
		public String getValue() {
			return value;
		}
	}

	public enum EnumWithCustomNamesPrimitive {
		TEST_C1,
		TEST_C2,
		TEST_C3;

		@JsonValue
		public int getValue() {
			switch (this) {
				case TEST_C1: return 10;
				case TEST_C2: return 20;
				case TEST_C3: return 30;
			}
			throw new IllegalStateException();
		}
	}

	public enum EnumWithCustomNamesDecimal {
		ONE(BigDecimal.ONE),
		PI(BigDecimal.valueOf(3.14159)),
		E(BigDecimal.valueOf(2.71828)),
		ZERO(BigDecimal.ZERO);

		private final BigDecimal value;

		EnumWithCustomNamesDecimal(BigDecimal value) {
			this.value = value;
		}

		@JsonValue
		public BigDecimal getValue() {
			return value;
		}
	}

	public enum EnumWithCustomNamesObject {
		STRING(""),
		LONG(0L),
		DOUBLE(0.0);

		private final Object value;

		EnumWithCustomNamesObject(Object value) {
			this.value = value;
		}

		@JsonValue
		public Object getValue() {
			return value;
		}
	}

	@CompiledJson
	public static class EnumHolder {
		public EnumWithCustomNames1 enum1;
		public EnumWithCustomNames2 enum2;
		public EnumWithCustomNamesPrimitive enum3;
		public EnumWithCustomNamesDecimal enum4;
		public List<EnumWithCustomNames1> enumList1;
		public List<EnumWithCustomNames2> enumList2;
		public List<EnumWithCustomNamesPrimitive> enumList3;
		public List<EnumWithCustomNamesDecimal> enumList4;
	}

	@CompiledJson
	public static class EnumHolderUnknown {
		public EnumWithCustomNames1 enum1;
		public EnumWithCustomNamesObject enum2;
		public List<EnumWithCustomNames1> enumList1;
		public List<EnumWithCustomNamesObject> enumList2;
	}

	private final DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime().includeServiceLoader());

	@Test
	public void objectRoundtrip() throws IOException {
		SingleNonImmutable sni = new SingleNonImmutable();
		sni.e1 = MyEnum1.DEF;
		sni.e2(MyEnum2.ZZ2);
		sni.e3 = MyEnum1.GHI;
		sni.map1 = new LinkedHashMap<>();
		sni.map1.put(MyEnum1.ABC, 2);
		sni.map1.put(MyEnum1.GHI, 5);
		sni.list2 = Arrays.asList(MyEnum2.ZZ2, MyEnum2.YY);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(sni, os);
		SingleNonImmutable res = dslJson.deserialize(SingleNonImmutable.class, os.toByteArray(), os.size());
		Assert.assertEquals(sni.e1, res.e1);
		Assert.assertEquals(sni.e2, res.e2);
		Assert.assertEquals(sni.e3, res.e3);
		Assert.assertEquals(sni.map1, res.map1);
		Assert.assertEquals(sni.list2, res.list2);
	}

	@Test
	public void immutableRoundtrip() throws IOException {
		Map map1 = new LinkedHashMap<>();
		map1.put(MyEnum1.ABC, 2);
		map1.put(MyEnum1.GHI, 5);
		SingleImmutable si = new SingleImmutable(
				MyEnum1.DEF,
				MyEnum2.ZZ2,
				MyEnum1.GHI,
				map1,
				Arrays.asList(MyEnum2.ZZ2, MyEnum2.YY));
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(si, os);
		SingleImmutable res = dslJson.deserialize(SingleImmutable.class, os.toByteArray(), os.size());
		Assert.assertEquals(si.e1, res.e1);
		Assert.assertEquals(si.e2, res.e2);
		Assert.assertEquals(si.e3, res.e3);
		Assert.assertEquals(si.map1, res.map1);
		Assert.assertEquals(si.list2, res.list2);
	}

	@Test
	public void errorOnUnknown() throws IOException {
		byte[] json = "{\"e2\":\"A\"}".getBytes("UTF-8");
		try {
			dslJson.deserialize(SingleNonImmutable.class, json, json.length);
			Assert.fail("Exception expected");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e.getMessage().contains("No enum constant com.dslplatform.json.EnumTest.MyEnum2.A"));
		}
	}

	@Test
	public void defaultOnUnknown() throws IOException {
		byte[] json = "{\"e1\":\"A\"}".getBytes("UTF-8");
		SingleNonImmutable v = dslJson.deserialize(SingleNonImmutable.class, json, json.length);
		Assert.assertEquals(MyEnum1.ABC, v.e1);
	}

	@Test
	public void testCustomNames() throws IOException {
		EnumHolder model = new EnumHolder();
		model.enum1 = EnumWithCustomNames1.TEST_A1;
		model.enum2 = EnumWithCustomNames2.TEST_B2;
		model.enum3 = EnumWithCustomNamesPrimitive.TEST_C3;
		model.enum4 = EnumWithCustomNamesDecimal.E;
		model.enumList1 = Arrays.asList(EnumWithCustomNames1.values());
		model.enumList2 = Arrays.asList(EnumWithCustomNames2.values());
		model.enumList3 = Arrays.asList(EnumWithCustomNamesPrimitive.values());
		model.enumList4 = Arrays.asList(EnumWithCustomNamesDecimal.values());

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(model, os);
		byte[] json = os.toByteArray();

		Assertions.assertThat(new String(json))
				.isEqualTo("{\"enumList4\":[1,3.14159,2.71828,0],\"enumList3\":[10,20,30],\"enumList2\":[\"b1\",\"b2\",\"b3\"],\"enumList1\":[\"a1\",\"a2\",\"a3\"]," +
						"\"enum1\":\"a1\",\"enum2\":\"b2\",\"enum3\":30,\"enum4\":2.71828}");

		EnumHolder result = dslJson.deserialize(EnumHolder.class, json, json.length);
		Assertions.assertThat(result).isEqualToComparingFieldByFieldRecursively(model);
	}

	@Test
	public void testCustomNamesWithUnknown() throws IOException {
		DslJson<Object> dslJsonUnknown = new DslJson<>(
				Settings.withAnalyzers(true, true)
						.includeServiceLoader()
						.unknownNumbers(JsonReader.UnknownNumberParsing.LONG_AND_DOUBLE));
		EnumHolderUnknown model = new EnumHolderUnknown();
		model.enum1 = EnumWithCustomNames1.TEST_A1;
		model.enum2 = EnumWithCustomNamesObject.DOUBLE;
		model.enumList1 = Arrays.asList(EnumWithCustomNames1.values());
		model.enumList2 = Arrays.asList(EnumWithCustomNamesObject.values());

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJsonUnknown.serialize(model, os);
		byte[] json = os.toByteArray();

		Assertions.assertThat(new String(json))
				.isEqualTo("{\"enumList2\":[\"\",0,0.0],\"enumList1\":[\"a1\",\"a2\",\"a3\"]," +
						"\"enum1\":\"a1\",\"enum2\":0.0}");

		EnumHolderUnknown result = dslJsonUnknown.deserialize(EnumHolderUnknown.class, json, json.length);
		Assertions.assertThat(result).isEqualToComparingFieldByFieldRecursively(model);
	}

	@Test
	public void defaultOnUnknown_customNames() throws IOException {
		byte[] json = "[\"Z\"]".getBytes(StandardCharsets.UTF_8);
		List<EnumWithCustomNames1> result = dslJson.deserializeList(EnumWithCustomNames1.class, json, json.length);
		Assertions.assertThat(result).containsExactly(EnumWithCustomNames1.TEST_A1);
	}

	@Test
	public void errorOnUnknown_customNames() {
		byte[] json = "[\"Z\"]".getBytes(StandardCharsets.UTF_8);

		Assertions.assertThatThrownBy(() ->
				dslJson.deserializeList(EnumWithCustomNames2.class, json, json.length)
		).hasMessage("No enum constant com.dslplatform.json.EnumTest.EnumWithCustomNames2 associated with value 'Z'");
	}

	@CompiledJson
	public static class EnumWithCustomConverter {
		@JsonAttribute(converter = MyEnum1Converter.class)
		public MyEnum1 enum1 = MyEnum1.GHI;
	}

	public static class MyEnum1Converter {
		public static JsonReader.ReadObject<MyEnum1> JSON_READER = r -> {
			throw new IllegalArgumentException("Custom reader exception");
		};
		public static JsonWriter.WriteObject<MyEnum1> JSON_WRITER = (w, v) -> {
			throw new IllegalArgumentException("Custom writer exception");
		};
	}

	@Test
	public void cantChangeReferencedConverter() throws IOException {
		DslJson<Object> customJson = new DslJson<>();
		EnumDescription description = EnumAnalyzer.CONVERTER.tryCreate(MyEnum1.class, customJson);
		Assert.assertEquals(description, customJson.tryFindWriter(MyEnum1.class));
		Assert.assertEquals(description, customJson.tryFindReader(MyEnum1.class));
		try {
			customJson.serialize(new EnumWithCustomConverter(), new ByteArrayOutputStream());
			Assert.fail("Expecting exception");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e.getMessage().contains("Custom writer exception"));
		}
		try {
			byte[] bytes = "{\"enum1\":\"\"}".getBytes(StandardCharsets.UTF_8);
			customJson.deserialize(EnumWithCustomConverter.class, bytes, bytes.length);
			Assert.fail("Expecting exception");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e.getMessage().contains("Custom reader exception"));
		}
	}

	@CompiledJson
	public enum EnumWithCustomValueConverter {
		ONE(BigDecimal.ONE),
		PI(BigDecimal.valueOf(3.14159)),
		E(BigDecimal.valueOf(2.71828)),
		ZERO(BigDecimal.ZERO);

		@JsonValue
		public final NumberWrapper value;

		EnumWithCustomValueConverter(BigDecimal value) {
			this.value = new NumberWrapper(value);
		}
	}

	public static class NumberWrapper {
		public final BigDecimal value;
		private NumberWrapper(BigDecimal value) {
			this.value = value;
		}

		@Override
		public int hashCode() { return value.hashCode(); }

		@Override
		public boolean equals(Object obj) {
			return obj instanceof NumberWrapper && ((NumberWrapper)obj).value.equals(value);
		}

		@JsonConverter(target = NumberWrapper.class)
		public static class NumberWrapperConverter {
			public static final JsonReader.ReadObject<NumberWrapper> JSON_READER = r -> {
				BigDecimal value = NumberConverter.deserializeDecimal(r);
				return new NumberWrapper(value);
			};
			public static final JsonWriter.WriteObject<NumberWrapper> JSON_WRITER = (w, v) -> {
				if (v != null) NumberConverter.serialize(v.value, w);
				else w.writeNull();
			};
		}
	}

	@Test
	public void wrapperClassWillUseSpecifiedConverter() throws IOException {
		DslJson<Object> customJson = new DslJson<>();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		List<EnumWithCustomValueConverter> input = Arrays.asList(EnumWithCustomValueConverter.E, EnumWithCustomValueConverter.PI);
		customJson.serialize(input, os);
		Assert.assertEquals("[2.71828,3.14159]", os.toString("UTF-8"));
		List<EnumWithCustomValueConverter> output = customJson.deserializeList(EnumWithCustomValueConverter.class, os.toByteArray(), os.size());
		Assert.assertEquals(input, output);
	}

	@Test
	public void objectRoundtripWithNullValue() throws IOException {
		SingleNonImmutable sni = new SingleNonImmutable();
		sni.e1 = MyEnum1.DEF;
		sni.e3 = MyEnum1.GHI;
		sni.map1 = new LinkedHashMap<>();
		sni.map1.put(MyEnum1.ABC, 2);
		sni.map1.put(MyEnum1.GHI, 5);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(sni, os);
		SingleNonImmutable res = dslJson.deserialize(SingleNonImmutable.class, os.toByteArray(), os.size());
		Assert.assertEquals(sni.e1, res.e1);
		Assert.assertEquals(sni.e2, res.e2);
		Assert.assertEquals(sni.e3, res.e3);
		Assert.assertEquals(sni.map1, res.map1);
		Assert.assertEquals(sni.list2, res.list2);
	}

	@Test
	public void emptyMap() throws IOException {
		Map emptyMap = new LinkedHashMap<>();
		SingleImmutable si = new SingleImmutable(
				MyEnum1.DEF,
				MyEnum2.ZZ2,
				MyEnum1.GHI,
				emptyMap,
				Arrays.asList(MyEnum2.ZZ2, MyEnum2.YY));
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(si, os);
		SingleImmutable res = dslJson.deserialize(SingleImmutable.class, os.toByteArray(), os.size());
		Assert.assertEquals(si.e1, res.e1);
		Assert.assertEquals(si.e2, res.e2);
		Assert.assertEquals(si.e3, res.e3);
		Assert.assertEquals(si.map1, res.map1);
		Assert.assertEquals(si.list2, res.list2);
	}

}