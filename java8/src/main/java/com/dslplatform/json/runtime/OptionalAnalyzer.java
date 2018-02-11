package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.lang.reflect.*;
import java.util.Optional;

public abstract class OptionalAnalyzer {

	private static final JsonWriter.WriteObject tmpWriter = (writer, value) -> {
	};
	private static final JsonReader.ReadObject tmpReader = reader -> null;

	public static final DslJson.ConverterFactory<OptionalDecoder> READER = (manifest, dslJson) -> {
		if (manifest instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) manifest;
			if (pt.getActualTypeArguments().length == 1 && pt.getRawType() instanceof Class<?>) {
				return analyzeDecoding(manifest, pt.getActualTypeArguments()[0], (Class<?>) pt.getRawType(), dslJson);
			}
		}
		return null;
	};

	public static final DslJson.ConverterFactory<OptionalEncoder> WRITER = (manifest, dslJson) -> {
		if (manifest instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) manifest;
			if (pt.getActualTypeArguments().length == 1 && pt.getRawType() instanceof Class<?>) {
				return analyzeEncoding(manifest, pt.getActualTypeArguments()[0], (Class<?>) pt.getRawType(), dslJson);
			}
		}
		return null;
	};

	private static <T> OptionalDecoder<T> analyzeDecoding(final Type manifest, final Type content, final Class<T> raw, final DslJson json) {
		if (raw != Optional.class) {
			return null;
		}
		final JsonReader.ReadObject oldReader = json.registerReader(manifest, tmpReader);
		final JsonReader.ReadObject<T> reader = json.tryFindReader(content);
		if (reader == null) {
			json.registerReader(manifest, oldReader);
			return null;
		}
		final OptionalDecoder<T> converter = new OptionalDecoder<>(reader);
		json.registerReader(manifest, converter);
		return converter;
	}

	private static <T> OptionalEncoder<T> analyzeEncoding(final Type manifest, final Type content, final Class<T> raw, final DslJson json) {
		if (raw != Optional.class) {
			return null;
		}
		final JsonWriter.WriteObject oldWriter = json.registerWriter(manifest, tmpWriter);
		final JsonWriter.WriteObject<T> writer = json.tryFindWriter(content);
		if (Object.class != content && writer == null) {
			json.registerWriter(manifest, oldWriter);
			return null;
		}
		final OptionalEncoder<T> converter = new OptionalEncoder<>(json, Object.class == content ? null : writer);
		json.registerWriter(manifest, converter);
		return converter;
	}
}
