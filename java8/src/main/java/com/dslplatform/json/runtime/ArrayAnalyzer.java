package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class ArrayAnalyzer {

	private static final JsonWriter.WriteObject tmpWriter = (writer, value) -> {
	};
	private static final JsonReader.ReadObject tmpReader = reader -> null;

	public static final DslJson.ConverterFactory<ArrayDecoder> READER = (manifest, dslJson) -> {
		if (manifest instanceof Class<?>) {
			final Class<?> array = (Class<?>)manifest;
			if (array.isArray()) {
				return analyzeDecoder(manifest, array.getComponentType(), dslJson);
			}
		}
		if (manifest instanceof GenericArrayType) {
			final GenericArrayType gat = (GenericArrayType) manifest;
			return analyzeDecoder(manifest, gat.getGenericComponentType(), dslJson);
		}
		return null;
	};

	public static final DslJson.ConverterFactory<ArrayEncoder> WRITER = (manifest, dslJson) -> {
		if (manifest instanceof Class<?>) {
			final Class<?> array = (Class<?>)manifest;
			if (array.isArray()) {
				return analyzeEncoder(manifest, array.getComponentType(), dslJson);
			}
		}
		if (manifest instanceof GenericArrayType) {
			final GenericArrayType gat = (GenericArrayType) manifest;
			return analyzeEncoder(manifest, gat.getGenericComponentType(), dslJson);
		}
		return null;
	};

	private static Class<?> checkSignature(final Type element) {
		final Class<?> raw;
		if (element instanceof Class<?>) {
			raw = (Class<?>)element;
		} else if (element instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) element;
			if (pt.getRawType() instanceof Class<?>) {
				raw = (Class<?>) pt.getRawType();
			} else {
				return null;
			}
		} else {
			return null;
		}
		if (raw.isPrimitive()) return null;
		return raw;
	}

	private static <T> ArrayDecoder<T> analyzeDecoder(final Type manifest, final Type element, final DslJson json) {
		final Class<?> raw = checkSignature(element);
		if (raw == null) return null;
		final JsonReader.ReadObject oldReader = json.registerReader(manifest, tmpReader);
		final JsonReader.ReadObject<?> reader = json.tryFindReader(element);
		if (reader == null) {
			json.registerReader(manifest, oldReader);
			return null;
		}
		final ArrayDecoder<T> converter = new ArrayDecoder((T[])Array.newInstance(raw, 0), reader);
		json.registerReader(manifest, converter);
		return converter;
	}

	private static <T> ArrayEncoder<T> analyzeEncoder(final Type manifest, final Type element, final DslJson json) {
		final Class<?> raw = checkSignature(element);
		if (raw == null) return null;
		final JsonWriter.WriteObject oldWriter = json.registerWriter(manifest, tmpWriter);
		final JsonWriter.WriteObject<?> writer = json.tryFindWriter(element);
		if (Object.class != element && writer == null) {
			json.registerWriter(manifest, oldWriter);
			return null;
		}
		final ArrayEncoder<T> converter = new ArrayEncoder(json, Object.class == element ? null : writer);
		json.registerWriter(manifest, converter);
		return converter;
	}
}
