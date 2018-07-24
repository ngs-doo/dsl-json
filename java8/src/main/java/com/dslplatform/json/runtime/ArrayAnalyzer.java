package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class ArrayAnalyzer {

	public static final DslJson.ConverterFactory<ArrayDecoder> READER = new DslJson.ConverterFactory<ArrayDecoder>() {
		@Nullable
		@Override
		public ArrayDecoder tryCreate(Type manifest, DslJson dslJson) {
			if (manifest instanceof Class<?>) {
				final Class<?> array = (Class<?>) manifest;
				if (array.isArray()) {
					return analyzeDecoder(manifest, array.getComponentType(), dslJson);
				}
			}
			if (manifest instanceof GenericArrayType) {
				final GenericArrayType gat = (GenericArrayType) manifest;
				return analyzeDecoder(manifest, gat.getGenericComponentType(), dslJson);
			}
			return null;
		}
	};

	public static final DslJson.ConverterFactory<ArrayEncoder> WRITER = new DslJson.ConverterFactory<ArrayEncoder>() {
		@Nullable
		@Override
		public ArrayEncoder tryCreate(Type manifest, DslJson dslJson) {
			if (manifest instanceof Class<?>) {
				final Class<?> array = (Class<?>) manifest;
				if (array.isArray()) {
					return analyzeEncoder(manifest, array.getComponentType(), dslJson);
				}
			}
			if (manifest instanceof GenericArrayType) {
				final GenericArrayType gat = (GenericArrayType) manifest;
				return analyzeEncoder(manifest, gat.getGenericComponentType(), dslJson);
			}
			return null;
		}
	};

	@Nullable
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

	@Nullable
	private static <T> ArrayDecoder<T> analyzeDecoder(final Type manifest, final Type element, final DslJson json) {
		final Class<?> raw = checkSignature(element);
		if (raw == null) return null;
		final JsonReader.ReadObject<?> reader = json.tryFindReader(element);
		if (reader == null) {
			return null;
		}
		final ArrayDecoder<T> decoder = new ArrayDecoder((T[])Array.newInstance(raw, 0), reader);
		json.registerReader(manifest, decoder);
		return decoder;
	}

	@Nullable
	private static <T> ArrayEncoder<T> analyzeEncoder(final Type manifest, final Type element, final DslJson json) {
		final Class<?> raw = checkSignature(element);
		if (raw == null) return null;
		final JsonWriter.WriteObject<?> writer = Object.class == element ? null : json.tryFindWriter(element);
		if (Object.class != element && writer == null) {
			return null;
		}
		final ArrayEncoder<T> encoder = new ArrayEncoder(json, Settings.isKnownType(element) ? writer : null);
		json.registerWriter(manifest, encoder);
		return encoder;
	}
}
