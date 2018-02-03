package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.SerializationException;

import java.io.IOException;
import java.lang.reflect.*;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

public abstract class OptionalAnalyzer {

	private static final JsonWriter.WriteObject tmpWriter = (writer, value) -> {
	};
	private static final JsonReader.ReadObject tmpReader = reader -> null;

	public static final DslJson.ConverterFactory<OptionalDescription> CONVERTER = (manifest, dslJson) -> {
		if (manifest instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) manifest;
			if (pt.getActualTypeArguments().length == 1 && pt.getRawType() instanceof Class<?>) {
				return analyze(manifest, pt.getActualTypeArguments()[0], (Class<?>) pt.getRawType(), dslJson);
			}
		}
		return null;
	};

	private static <T> OptionalDescription<T> analyze(final Type manifest, final Type content, final Class<T> raw, final DslJson json) {
		if (raw != Optional.class) {
			return null;
		}
		json.registerWriter(manifest, tmpWriter);
		json.registerReader(manifest, tmpReader);
		final JsonWriter.WriteObject<T> writer = json.tryFindWriter(content);
		final JsonReader.ReadObject<T> reader = json.tryFindReader(content);
		if (writer == null || reader == null) {
			json.registerWriter(manifest, null);
			json.registerReader(manifest, null);
			return null;
		}
		final OptionalDescription<T> converter = new OptionalDescription<>(content == Object.class ? json : null, writer, reader);
		json.registerWriter(manifest, converter);
		json.registerReader(manifest, converter);
		return converter;
	}
}
