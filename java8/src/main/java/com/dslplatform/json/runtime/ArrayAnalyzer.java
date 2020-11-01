package com.dslplatform.json.runtime;

import com.dslplatform.json.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class ArrayAnalyzer {

	public static class Runtime {
		public static final JsonReader.ReadObject<Object[]> JSON_READER = new JsonReader.ReadObject<Object[]>() {
			@Override
			public Object[] read(JsonReader reader) throws IOException {
				if (reader.wasNull()) return null;
				return ObjectConverter.deserializeList(reader).toArray();
			}
		};
		public static final JsonWriter.WriteObject<Object[]> JSON_WRITER = new JsonWriter.WriteObject<Object[]>() {
			@Override
			public void write(JsonWriter writer, @Nullable Object[] value) {
				if (value == null) {
					writer.writeNull();
				} else if (value.length == 0) {
					writer.writeAscii("[]");
				} else {
					writer.writeByte(JsonWriter.ARRAY_START);
					writer.serializeObject(value[0]);
					for(int i = 1; i < value.length; i++) {
						writer.writeByte(JsonWriter.COMMA);
						writer.serializeObject(value[i]);
					}
					writer.writeByte(JsonWriter.ARRAY_END);
				}
			}
		};
	}

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
			raw = (Class<?>) pt.getRawType();
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
		final JsonWriter.WriteObject<?> elementWriter = Settings.isKnownType(element) || writer instanceof MixinDescription
				? writer
				: null;
		final ArrayEncoder<T> encoder = new ArrayEncoder(json, elementWriter);
		json.registerWriter(manifest, encoder);
		return encoder;
	}
}
