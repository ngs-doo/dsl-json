package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.StringConverter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Callable;

public abstract class MapAnalyzer {

	private static final JsonWriter.WriteObject tmpWriter = (writer, value) -> {
	};
	private static final JsonReader.ReadObject tmpReader = reader -> null;
	private static final JsonReader.ReadObject<String> stringReader =
			reader -> reader.wasNull() ? null : reader.readString();

	public static final DslJson.ConverterFactory<MapDescription> CONVERTER = (manifest, dslJson) -> {
		if (manifest instanceof Class<?>) {
			return analyze(manifest, Object.class, Object.class, (Class<?>)manifest, dslJson);
		}
		if (manifest instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) manifest;
			if (pt.getActualTypeArguments().length == 2 && pt.getRawType() instanceof Class<?>) {
				return analyze(manifest, pt.getActualTypeArguments()[0], pt.getActualTypeArguments()[1], (Class<?>) pt.getRawType(), dslJson);
			}
		}
		return null;
	};

	private static MapDescription analyze(final Type manifest, final Type key, final Type value, final Class<?> map, final DslJson json) {
		if (!Map.class.isAssignableFrom(map)) return null;
		final Callable newInstance;
		if (!map.isInterface()) {
			try {
				map.newInstance();
			} catch (Exception ex) {
				return null;
			}
			newInstance = map::newInstance;
		} else {
			newInstance = () -> new LinkedHashMap<>(4);
		}
		json.registerWriter(manifest, tmpWriter);
		json.registerReader(manifest, tmpReader);
		final JsonWriter.WriteObject<?> keyWriter = json.tryFindWriter(key);
		final JsonReader.ReadObject<?> keyReader = json.tryFindReader(key);
		final JsonWriter.WriteObject<?> valueWriter = json.tryFindWriter(value);
		final JsonReader.ReadObject<?> valueReader = json.tryFindReader(value);
		if (Object.class != key && (keyWriter == null || keyReader == null)
				|| Object.class != value && (valueWriter == null || valueReader == null)) {
			json.registerWriter(manifest, null);
			json.registerReader(manifest, null);
			return null;
		}
		final MapDescription converter =
				new MapDescription(
						manifest,
						newInstance,
						json,
						Object.class == key ? null : keyWriter,
						Object.class == key ? stringReader : keyReader,
						Object.class == value ? null : valueWriter,
						valueReader);
		json.registerWriter(manifest, converter);
		json.registerReader(manifest, converter);
		return converter;
	}
}
