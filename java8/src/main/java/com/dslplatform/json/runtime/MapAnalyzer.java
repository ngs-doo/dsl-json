package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

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

	public static final DslJson.ConverterFactory<MapDecoder> READER = (manifest, dslJson) -> {
		if (manifest instanceof Class<?>) {
			return analyzeDecoder(manifest, Object.class, Object.class, (Class<?>)manifest, dslJson);
		}
		if (manifest instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) manifest;
			if (pt.getActualTypeArguments().length == 2 && pt.getRawType() instanceof Class<?>) {
				return analyzeDecoder(manifest, pt.getActualTypeArguments()[0], pt.getActualTypeArguments()[1], (Class<?>) pt.getRawType(), dslJson);
			}
		}
		return null;
	};

	public static final DslJson.ConverterFactory<MapEncoder> WRITER = (manifest, dslJson) -> {
		if (manifest instanceof Class<?>) {
			return analyzeEncoder(manifest, Object.class, Object.class, (Class<?>)manifest, dslJson);
		}
		if (manifest instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) manifest;
			if (pt.getActualTypeArguments().length == 2 && pt.getRawType() instanceof Class<?>) {
				return analyzeEncoder(manifest, pt.getActualTypeArguments()[0], pt.getActualTypeArguments()[1], (Class<?>) pt.getRawType(), dslJson);
			}
		}
		return null;
	};

	private static boolean canNew(final Class<?> map) {
		try {
			map.newInstance();
			return true;
		} catch (Exception ignore) {
			return false;
		}
	}

	private static MapDecoder analyzeDecoder(final Type manifest, final Type key, final Type value, final Class<?> map, final DslJson json) {
		if (!Map.class.isAssignableFrom(map)) return null;
		final Callable newInstance;
		if (!map.isInterface() && canNew(map)) {
			newInstance = map::newInstance;
		} else if (map.isAssignableFrom(LinkedHashMap.class)) {
			newInstance = () -> new LinkedHashMap<>(4);
		} else {
			return null;
		}
		final JsonReader.ReadObject oldReader = json.registerReader(manifest, tmpReader);
		final JsonReader.ReadObject<?> keyReader = json.tryFindReader(key);
		final JsonReader.ReadObject<?> valueReader = json.tryFindReader(value);
		if (keyReader == null || valueReader == null) {
			json.registerReader(manifest, oldReader);
			return null;
		}
		final MapDecoder decoder =
				new MapDecoder(
						manifest,
						newInstance,
						Object.class == key ? stringReader : keyReader,
						valueReader);
		json.registerReader(manifest, decoder);
		return decoder;
	}

	private static MapEncoder analyzeEncoder(final Type manifest, final Type key, final Type value, final Class<?> map, final DslJson json) {
		if (!Map.class.isAssignableFrom(map)) return null;
		final JsonWriter.WriteObject oldWriter = json.registerWriter(manifest, tmpWriter);
		final JsonWriter.WriteObject<?> keyWriter = json.tryFindWriter(key);
		final JsonWriter.WriteObject<?> valueWriter = json.tryFindWriter(value);
		if (Object.class != key && keyWriter == null
				|| Object.class != value && valueWriter == null) {
			json.registerWriter(manifest, oldWriter);
			return null;
		}
		final MapEncoder encoder =
				new MapEncoder(
						json,
						Object.class == key ? null : keyWriter,
						Object.class == value ? null : valueWriter);
		json.registerWriter(manifest, encoder);
		return encoder;
	}
}
