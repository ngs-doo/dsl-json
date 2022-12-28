package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import com.dslplatform.json.runtime.TypeDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class GenericTest {

	private final DslJson<Object> dslJson = new DslJson<>(
			new DslJson.Settings<>().allowArrayFormat(true).includeServiceLoader());

	private final DslJson<Object> dslJsonRuntime = new DslJson<>(
			Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());

	@CompiledJson
	static class GenericModel<T, V> {
		public T value1;
		public V[] value2;
		public Entry<T, V>[] value3;
		public GenericModel<Entry<T, Integer>, V> nested;
		//TODO: support this without runtime
		/*public Map<T, V> map1;
		public Map<Integer, T> map2;
		public Set<V> set;
		public List<T> list;
		public GenericModel<V, T> self;
		public List<GenericModel<V, V>> selfList;*/
	}

	@CompiledJson
	static class GenericCollections {
		public Map<String, Double> map1;
		public Map<Integer, String> map2;
		public Map<Double, String> map3;
		public Set<Double> set;
		public List<String> list;
	}

	@CompiledJson
	static class UnboundedCollections {
		public Map<String, ? extends BigDecimal> map;
		public Set<? extends Double> set;
		public List<? extends String> list;
	}

	@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
	static class Entry<K, V> {
		@JsonAttribute(index = 0)
		public K key;
		@JsonAttribute(index = 1)
		public V value;
	}

	@CompiledJson
	public static class ClassExtendingClassWithBoundTypeParameter extends ClassWithBoundTypeParameter<String> {
	}

	@CompiledJson
	public static class ClassWithBoundTypeParameter<T extends String> {

		private T[] property;

		public T[] getProperty() {
			return property;
		}

		public void setProperty(T[] property) {
			this.property = property;
		}
	}

	@Test
	public void checkBoundTypeParameter() throws IOException {
		byte[] bytes = "{\"property\":[\"abc\"]}".getBytes("UTF-8");
		ClassExtendingClassWithBoundTypeParameter deser = dslJson.deserialize(ClassExtendingClassWithBoundTypeParameter.class, bytes, bytes.length);
		Assert.assertArrayEquals(new String[]{"abc"}, deser.getProperty());
	}

	@Test
	public void testSerializeAndDeserializeGeneric() throws IOException {
		GenericModel<String, Double> model = generateModel();
		Type type = new TypeDefinition<GenericModel<String, Double>>() {}.type;

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JsonWriter writer = dslJson.newWriter();
		writer.reset(os);

		dslJson.serialize(writer, type, model);
		writer.flush();

		GenericModel<String, Double> result = (GenericModel<String, Double>) dslJson.deserialize(type, os.toByteArray(), os.size());

		assertThat(result).isEqualToComparingFieldByFieldRecursively(model);
	}

	@Test
	public void testBindGeneric() throws IOException {
		GenericModel<String, Double> model = generateModel();
		Type type = new TypeDefinition<GenericModel<String, Double>>() {}.type;

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JsonWriter writer = dslJson.newWriter();
		writer.reset(os);

		dslJson.serialize(writer, type, model);
		writer.flush();

		JsonReader.BindObject<GenericModel<String, Double>> bindObject = (JsonReader.BindObject<GenericModel<String, Double>>) dslJson.tryFindBinder(type);
		GenericModel<String, Double> result = new GenericModel<>();
		JsonReader<Object> reader = dslJson.newReader(os.toByteArray(), os.size());
		reader.getNextToken();
		bindObject.bind(reader, result);

		assertThat(result).isEqualToComparingFieldByFieldRecursively(model);
	}

	@Test
	public void testRawSignature() throws IOException {
		DslJson.Settings<Object> settings = new DslJson.Settings<>()
				.resolveReader(Settings.UNKNOWN_READER)
				.resolveWriter(Settings.UNKNOWN_WRITER)
				.allowArrayFormat(true)
				.includeServiceLoader();
		DslJson<Object> dslJsonUnknown = new DslJson<>(settings);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		GenericModel model = generateModel();
		try {
			dslJson.serialize(model, os);
			Assert.fail("Expecting exception");
		} catch (ConfigurationException ex) {
			Assert.assertTrue(ex.getMessage().contains("Unable to serialize provided object. Failed to find serializer"));
		}
		os.reset();
		dslJsonUnknown.serialize(model, os);

		Type type = new TypeDefinition<GenericModel<String, Double>>() {}.type;
		GenericModel<String, Double> result = (GenericModel<String, Double>) dslJsonUnknown.deserialize(type, os.toByteArray(), os.size());

		assertThat(result).isEqualToComparingFieldByFieldRecursively(model);
	}

	@Test
	public void testRawSignatureWithRuntime() throws IOException {
		DslJson<Object> dslJsonUnknown = new DslJson<>(Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		GenericModel model = generateModel();
		try {
			dslJson.serialize(model, os);
			Assert.fail("Expecting exception");
		} catch (ConfigurationException ex) {
			Assert.assertTrue(ex.getMessage().contains("Unable to serialize provided object. Failed to find serializer"));
		}
		os.reset();
		dslJsonUnknown.serialize(model, os);

		Type type = new TypeDefinition<GenericModel<String, Double>>() {}.type;
		GenericModel<String, Double> result = (GenericModel<String, Double>) dslJsonUnknown.deserialize(type, os.toByteArray(), os.size());

		assertThat(result).isEqualToComparingFieldByFieldRecursively(model);
	}

	@Test
	public void rountripWithoutRuntime() throws IOException {
		GenericCollections model = new GenericCollections();
		model.map1 = new HashMap<>();
		model.map1.put("abc", Double.NEGATIVE_INFINITY);
		model.map1.put("x", 2.2);
		model.map2 = new HashMap<>();
		model.map2.put(2, "abc");
		model.map2.put(-3, "def");
		model.map3 = new HashMap<>();
		model.map3.put(Double.NaN, "A");
		model.map3.put(1.0, "B");
		model.map3.put(Double.POSITIVE_INFINITY, "");
		model.set = new HashSet<>();
		model.set.add(Double.POSITIVE_INFINITY);
		model.set.add(2.2);
		model.list = Arrays.asList("xXx", null, "xyz");

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(model, os);

		GenericCollections result = dslJson.deserialize(GenericCollections.class, os.toByteArray(), os.size());

		assertThat(result).isEqualToComparingFieldByFieldRecursively(model);
	}

	@Test
	public void unboundedCollections() throws IOException {
		DslJson<Object> dslJsonUnknown = new DslJson<>(Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());
		UnboundedCollections model = new UnboundedCollections();
		Map map = new HashMap<>();
		map.put("x", BigDecimal.valueOf(505, 1));
		model.map = map;
		Set set = new HashSet<>();
		set.add(Double.POSITIVE_INFINITY);
		set.add(2.2);
		model.set = set;
		model.list = Arrays.asList("xXx", null, "xyz");

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJsonUnknown.serialize(model, os);

		UnboundedCollections result = dslJsonUnknown.deserialize(UnboundedCollections.class, os.toByteArray(), os.size());

		assertThat(result).isEqualToComparingFieldByFieldRecursively(model);
	}

	@CompiledJson
	public static class GenericFromAnnotation<T> {
		@JsonAttribute(name = "VALUE")
		public T value;
	}

	@Test
	public void willUseAnnotationProcessorVersion() throws IOException {

		DslJson<Object> customJson = new DslJson<>(Settings.basicSetup());

		byte[] bytes = "{\"VALUE\":\"ABC\"}".getBytes("UTF-8");

		Type type = new TypeDefinition<GenericFromAnnotation<String>>() {}.type;
		GenericFromAnnotation<String> result = (GenericFromAnnotation<String>)customJson.deserialize(type, bytes, bytes.length);

		Assert.assertEquals("ABC", result.value);
	}

	private GenericModel<String, Double> generateModel() {
		GenericModel<String, Double> model = new GenericModel<>();
		model.value1 = "a";
		model.value2 = new Double[]{1.0, 1.1};
		model.value3 = new Entry[]{createEntry("a", 0.1)};
		GenericModel<Entry<String, Integer>, Double> nestedModel = new GenericModel<>();
		nestedModel.value1 = createEntry("b", 2);
		nestedModel.value2 = new Double[]{2.0, 2.1};
		model.nested = nestedModel;
		/*model.map1 = new HashMap<>();
		model.map1.put("abc", Double.NEGATIVE_INFINITY);
		model.map1.put("x", 2.2);
		model.map2 = new HashMap<>();
		model.map2.put(2, "abc");
		model.map2.put(-3, "def");
		model.set = new HashSet<>();
		model.set.add(Double.POSITIVE_INFINITY);
		model.set.add(2.2);
		model.list = Arrays.asList("xXx", null, "xyz");*/
		return model;
	}

	private <K, V> Entry<K, V> createEntry(K key, V value) {
		Entry<K, V> entry = new Entry<>();
		entry.key = key;
		entry.value = value;
		return entry;
	}

	interface Inner {};
	static class InnerA implements Inner {};
	static class InnerB implements Inner {};

	@CompiledJson
	static abstract class X<T extends Inner> {
	}

	@CompiledJson
	public static class Y extends X<InnerA> {
	}

	@CompiledJson
	public static class Z extends X<InnerB> {
	}

	@Test
	public void willCreateClassesWithoutProperties() throws IOException {

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(new Y(), os);

		Y result = dslJson.deserialize(Y.class, os.toByteArray(), os.size());
		Assert.assertNotNull(result);
	}

	@CompiledJson
	public static class GenericArrays<T1, T2> {
		@JsonAttribute(nullable = false)
		public T1[] i1;
		@JsonAttribute(nullable = false)
		public T2[][] i2;
		@JsonAttribute(nullable = false)
		public List<T1>[] i3;
		@JsonAttribute(nullable = false)
		public List<T2>[][] i4;
		@JsonAttribute(nullable = false)
		public Map<T1,List<T2>>[][] i5;
	}

	@Test
	public void emptyGenericArrays() throws IOException {
		GenericArrays<String, Integer> z = (GenericArrays)dslJsonRuntime.deserialize(new TypeDefinition<GenericArrays<String, Integer>>(){}.type, new byte[]{'{', '}'}, 2);
		Assert.assertEquals(0, z.i1.length);
		Assert.assertEquals(0, z.i2.length);
		Assert.assertEquals(0, z.i3.length);
		Assert.assertEquals(0, z.i4.length);
		Assert.assertEquals(0, z.i5.length);
	}

	@CompiledJson
	public static class GenericArraysWithCtor<T1, T2> {
		@JsonAttribute(nullable = false)
		public T1[] i1;
		@JsonAttribute(nullable = false)
		public T2[][] i2;
		@JsonAttribute(nullable = false)
		public List<T1>[] i3;
		@JsonAttribute(nullable = false)
		public List<T2>[][] i4;
		@JsonAttribute(nullable = false)
		public Map<T1,List<T2>>[][] i5;

		public GenericArraysWithCtor(T1[] i1, T2[][] i2, List<T1>[] i3, List<T2>[][] i4, Map<T1,List<T2>>[][] i5) {
			this.i1 = i1;
			this.i2 = i2;
			this.i3 = i3;
			this.i4 = i4;
			this.i5 = i5;
		}
	}

	@Test
	public void emptyGenericArraysWithCtor() throws IOException {
		GenericArraysWithCtor<String, Integer> z = (GenericArraysWithCtor)dslJsonRuntime.deserialize(new TypeDefinition<GenericArraysWithCtor<String, Integer>>(){}.type, new byte[]{'{', '}'}, 2);
		Assert.assertEquals(0, z.i1.length);
		Assert.assertEquals(0, z.i2.length);
		Assert.assertEquals(0, z.i3.length);
		Assert.assertEquals(0, z.i4.length);
		Assert.assertEquals(0, z.i5.length);
	}

	@CompiledJson
	public static class GenericSelfReference<T> implements Comparable<GenericSelfReference<T>> {

		final T genericField;

		public GenericSelfReference(T genericField) {
			this.genericField = genericField;
		}

		public final T getGenericField() {
			return genericField;
		}

		@Override
		public int compareTo(GenericSelfReference<T> other) {
			return 1;
		}
	}

	@Test
	public void canCopeWithSelfReferences() throws IOException {

		byte[] bytes = "{\"genericField\":\"ABC\"}".getBytes("UTF-8");

		Type type = new TypeDefinition<GenericSelfReference<String>>() {}.type;
		GenericSelfReference<String> result = (GenericSelfReference<String>)dslJson.deserialize(type, bytes, bytes.length);

		Assert.assertEquals("ABC", result.getGenericField());

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJsonRuntime.serialize(new GenericSelfReference<>("XYZ"), os);

		Assert.assertEquals("{\"genericField\":\"XYZ\"}", os.toString("UTF-8"));

		os.reset();
		JsonWriter.WriteObject wo = dslJson.tryFindWriter(type);
		JsonWriter w = dslJson.newWriter();
		w.reset(os);
		wo.write(w, new GenericSelfReference<>("XYZ"));
		w.flush();

		Assert.assertEquals("{\"genericField\":\"XYZ\"}", os.toString("UTF-8"));
	}

	@CompiledJson
	public static class GenericSelfReferenceString extends GenericSelfReference<String> {

		public GenericSelfReferenceString(String stringField) {
			super(stringField);
		}
	}

	@Test
	public void canCopeWithBoundSelfReferences() throws IOException {

		byte[] bytes = "{\"genericField\":\"ABC\"}".getBytes("UTF-8");

		GenericSelfReferenceString result = dslJson.deserialize(GenericSelfReferenceString.class, bytes, bytes.length);

		Assert.assertEquals("ABC", result.getGenericField());

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(new GenericSelfReferenceString("XYZ"), os);

		Assert.assertEquals("{\"genericField\":\"XYZ\"}", os.toString("UTF-8"));
	}
}
