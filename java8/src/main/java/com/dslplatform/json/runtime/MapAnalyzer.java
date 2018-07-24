package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.Nullable;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Callable;

public abstract class MapAnalyzer {

	private static final JsonReader.ReadObject<String> stringReader = new JsonReader.ReadObject<String>() {
		@Nullable
		@Override
		public String read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : reader.readString();
		}
	};

	public static final DslJson.ConverterFactory<MapDecoder> READER = new DslJson.ConverterFactory<MapDecoder>() {
		@Nullable
		@Override
		public MapDecoder tryCreate(Type manifest, DslJson dslJson) {
			if (manifest instanceof Class<?>) {
				return analyzeDecoder(manifest, Object.class, Object.class, (Class<?>) manifest, dslJson);
			}
			if (manifest instanceof ParameterizedType) {
				final ParameterizedType pt = (ParameterizedType) manifest;
				if (pt.getActualTypeArguments().length == 2 && pt.getRawType() instanceof Class<?>) {
					return analyzeDecoder(manifest, pt.getActualTypeArguments()[0], pt.getActualTypeArguments()[1], (Class<?>) pt.getRawType(), dslJson);
				}
			}
			return null;
		}
	};

	public static final DslJson.ConverterFactory<MapEncoder> WRITER = new DslJson.ConverterFactory<MapEncoder>() {
		@Nullable
		@Override
		public MapEncoder tryCreate(Type manifest, DslJson dslJson) {
			if (manifest instanceof Class<?>) {
				return analyzeEncoder(manifest, Object.class, Object.class, (Class<?>) manifest, dslJson);
			}
			if (manifest instanceof ParameterizedType) {
				final ParameterizedType pt = (ParameterizedType) manifest;
				if (pt.getActualTypeArguments().length == 2 && pt.getRawType() instanceof Class<?>) {
					return analyzeEncoder(manifest, pt.getActualTypeArguments()[0], pt.getActualTypeArguments()[1], (Class<?>) pt.getRawType(), dslJson);
				}
			}
			return null;
		}
	};

	private static boolean canNew(final Class<?> map) {
		try {
			map.newInstance();
			return true;
		} catch (Exception ignore) {
			return false;
		}
	}

	@Nullable
	private static MapDecoder analyzeDecoder(final Type manifest, final Type key, final Type value, final Class<?> map, final DslJson json) {
		if (!Map.class.isAssignableFrom(map)) return null;
		final Callable newInstance;
		if (!map.isInterface() && canNew(map)) {
			newInstance = new Callable() {
				@Override
				public Object call() throws Exception {
					return map.newInstance();
				}
			};
		} else if (map.isAssignableFrom(LinkedHashMap.class)) {
			newInstance = new Callable() {
				@Override
				public Object call() {
					return new LinkedHashMap<>(4);
				}
			};
		} else {
			return null;
		}
		final JsonReader.ReadObject<?> keyReader = json.tryFindReader(key);
		final JsonReader.ReadObject<?> valueReader = json.tryFindReader(value);
		if (keyReader == null || valueReader == null) {
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

	@Nullable
	private static MapEncoder analyzeEncoder(final Type manifest, final Type key, final Type value, final Class<?> map, final DslJson json) {
		if (!Map.class.isAssignableFrom(map)) return null;
		final JsonWriter.WriteObject<?> keyWriter = Object.class == key ? null : json.tryFindWriter(key);
		final JsonWriter.WriteObject<?> valueWriter = Object.class == value ? null : json.tryFindWriter(value);
		if (Object.class != key && keyWriter == null || Object.class != value && valueWriter == null) {
			return null;
		}
		//TODO: temp hack to encode some keys as strings even if they are numbers
		final boolean checkForConversionToString = key instanceof Class<?> && Number.class.isAssignableFrom((Class<?>) key);
		final MapEncoder encoder =
				new MapEncoder(
						json,
						checkForConversionToString,
						Object.class == key ? null : keyWriter,
						Object.class == value ? null : valueWriter);
		json.registerWriter(manifest, encoder);
		return encoder;
	}
}
