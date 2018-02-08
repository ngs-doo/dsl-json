package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Callable;

public abstract class ArrayAnalyzer {

	private static final JsonWriter.WriteObject tmpWriter = (writer, value) -> {
	};
	private static final JsonReader.ReadObject tmpReader = reader -> null;

	public static final DslJson.ConverterFactory<ArrayDescription> CONVERTER = (manifest, dslJson) -> {
		if (manifest instanceof Class<?>) {
			final Class<?> array = (Class<?>)manifest;
			if (array.isArray()) {
				return analyze(manifest, array.getComponentType(), dslJson);
			}
		}
		if (manifest instanceof GenericArrayType) {
			final GenericArrayType gat = (GenericArrayType) manifest;
			return analyze(manifest, gat.getGenericComponentType(), dslJson);
		}
		return null;
	};

	private static <T> ArrayDescription<T> analyze(final Type manifest, final Type element, final DslJson json) {
		final Class<?> raw;
		if (element instanceof Class<?>) {
			raw = (Class<?>)element;
		} else if (element instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) manifest;
			if (pt.getRawType() instanceof Class<?>) {
				raw = (Class<?>) pt.getRawType();
			} else {
				return null;
			}
		} else {
			return null;
		}
		if (raw.isPrimitive()) return null;
		json.registerWriter(manifest, tmpWriter);
		json.registerReader(manifest, tmpReader);
		final JsonWriter.WriteObject<?> writer = json.tryFindWriter(element);
		final JsonReader.ReadObject<?> reader = json.tryFindReader(element);
		if (Object.class != element && writer == null || reader == null) {
			json.registerWriter(manifest, null);
			json.registerReader(manifest, null);
			return null;
		}
		final ArrayDescription<T> converter =
				new ArrayDescription(
						(T[])Array.newInstance(raw, 0),
						json,
						Object.class == element ? null : writer,
						reader);
		json.registerWriter(manifest, converter);
		json.registerReader(manifest, converter);
		return converter;
	}
}
