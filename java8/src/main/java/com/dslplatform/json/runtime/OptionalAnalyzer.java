package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.lang.reflect.*;
import java.util.Optional;

public abstract class OptionalAnalyzer {

	public static final DslJson.ConverterFactory<OptionalDecoder> READER = (manifest, dslJson) -> {
		if (manifest instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) manifest;
			if (pt.getActualTypeArguments().length == 1 && pt.getRawType() instanceof Class<?>) {
				return analyzeDecoding(manifest, pt.getActualTypeArguments()[0], (Class<?>) pt.getRawType(), dslJson);
			}
		}
		if (manifest == Optional.class) {
			return analyzeDecoding(manifest, Object.class, Optional.class, dslJson);
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
		if (manifest == Optional.class) {
			return analyzeEncoding(manifest, Object.class, Optional.class, dslJson);
		}
		return null;
	};

	private static OptionalDecoder analyzeDecoding(final Type manifest, final Type content, final Class<?> raw, final DslJson json) {
		if (raw != Optional.class) {
			return null;
		} else if (content == Optional.class) {
			final OptionalDecoder nested = analyzeDecoding(content, Object.class, Optional.class, json);
			final OptionalDecoder outer = new OptionalDecoder<>(nested);
			json.registerReader(manifest, outer);
			return outer;
		}
		final JsonReader.ReadObject<?> reader = json.tryFindReader(content);
		if (reader == null) {
			return null;
		}
		final OptionalDecoder decoder = new OptionalDecoder<>(reader);
		json.registerReader(manifest, decoder);
		return decoder;
	}

	private static OptionalEncoder analyzeEncoding(final Type manifest, final Type content, final Class<?> raw, final DslJson json) {
		if (raw != Optional.class) {
			return null;
		} else if (content == Optional.class) {
			final OptionalEncoder nested = analyzeEncoding(content, Object.class, Optional.class, json);
			json.registerWriter(manifest, nested);
			return nested;
		}
		final JsonWriter.WriteObject<?> writer = Object.class == content ? null : json.tryFindWriter(content);
		if (Object.class != content && writer == null) {
			return null;
		}
		final OptionalEncoder encoder = new OptionalEncoder<>(json, Object.class == content ? null : writer);
		json.registerWriter(manifest, encoder);
		return encoder;
	}
}
