package com.dslplatform.json;

import com.dslplatform.json.runtime.TypeDefinition;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

import static org.assertj.core.api.Assertions.assertThat;

public class GenericTest {

	private final DslJson<Object> dslJson = new DslJson<>(
			new DslJson.Settings<>().allowArrayFormat(true).includeServiceLoader());

	@CompiledJson
	static class GenericModel<T, V> {
		public T value1;
		public V[] value2;
		public Entry<T, V>[] value3;
		public GenericModel<Entry<T, Integer>, V> nested;
	}

	@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
	static class Entry<K, V> {
		@JsonAttribute(index = 0)
		public K key;
		@JsonAttribute(index = 1)
		public V value;
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

	private GenericModel<String, Double> generateModel() {
		GenericModel<String, Double> model = new GenericModel<>();
		model.value1 = "a";
		model.value2 = new Double[]{1.0, 1.1};
		model.value3 = new Entry[]{createEntry("a", 0.1)};
		GenericModel<Entry<String, Integer>, Double> nestedModel = new GenericModel<>();
		nestedModel.value1 = createEntry("b", 2);
		nestedModel.value2 = new Double[]{2.0, 2.1};
		model.nested = nestedModel;
		return model;
	}

	private <K, V> Entry<K, V> createEntry(K key, V value) {
		Entry<K, V> entry = new Entry<>();
		entry.key = key;
		entry.value = value;
		return entry;
	}
}
