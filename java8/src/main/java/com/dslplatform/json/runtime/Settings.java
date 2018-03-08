package com.dslplatform.json.runtime;

import com.dslplatform.json.*;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class Settings {
	private static final DslJson.ConverterFactory<JsonReader.ReadObject> UNKNOWN_READER =
			(manifest, dslJson) -> Object.class == manifest ? ObjectConverter::deserializeObject : null;

	static boolean isKnownType(final Type type) {
		if (type == Object.class) return false;
		if (type instanceof Class<?>) {
			Class<?> manifest = (Class<?>)type;
			if (manifest.isInterface()) return false;
			return (manifest.getModifiers() & Modifier.ABSTRACT) == 0;
		}
		return true;
	}

	public static <T, R> JsonWriter.WriteObject<T> createEncoder(
			final Function<T, R> read,
			final String name,
			final DslJson json,
			final Type type) {
		if (read == null) throw new IllegalArgumentException("read can't be null");
		if (name == null) throw new IllegalArgumentException("name can't be null");
		if (json == null) throw new IllegalArgumentException("json can't be null");
		final JsonWriter.WriteObject<R> encoder = type != null ? json.tryFindWriter(type) : null;
		if (encoder == null || Object.class.equals(type)) return new LazyAttributeEncoder<>(read, name, json, type);
		return new AttributeEncoder<>(read, name, !json.omitDefaults, encoder);
	}

	public static <T, R> DecodePropertyInfo<JsonReader.BindObject<T>> createDecoder(
			final BiConsumer<T, R> write,
			final String name,
			final DslJson json,
			final boolean exactNameMatch,
			final boolean isMandatory,
			final int index,
			final Type type) {
		if (write == null) throw new IllegalArgumentException("write can't be null");
		if (name == null) throw new IllegalArgumentException("name can't be null");
		if (json == null) throw new IllegalArgumentException("json can't be null");
		final JsonReader.ReadObject<R> decoder = type != null ? json.tryFindReader(type) : null;
		if (decoder == null || Object.class.equals(type)) return new DecodePropertyInfo<>(name, exactNameMatch, isMandatory, index, new LazyAttributeDecoder<>(write, name, json, type));
		return new DecodePropertyInfo<>(name, exactNameMatch, isMandatory, index, new AttributeDecoder<>(write, decoder));
	}

	public static <T> DslJson.Settings<T> withRuntime() {
		return new DslJson.Settings()
				.resolveReader(UNKNOWN_READER)
				.resolveReader(CollectionAnalyzer.READER)
				.resolveWriter(CollectionAnalyzer.WRITER)
				.resolveReader(ArrayAnalyzer.READER)
				.resolveWriter(ArrayAnalyzer.WRITER)
				.resolveReader(MapAnalyzer.READER)
				.resolveWriter(MapAnalyzer.WRITER)
				.resolveWriter(EnumAnalyzer.CONVERTER)
				.resolveReader(EnumAnalyzer.CONVERTER)
				.resolveWriter(BeanAnalyzer.CONVERTER)
				.resolveBinder(BeanAnalyzer.CONVERTER)
				.resolveReader(BeanAnalyzer.CONVERTER)
				.resolveWriter(ImmutableAnalyzer.CONVERTER)
				.resolveReader(ImmutableAnalyzer.CONVERTER)
				.with(new ConfigureJava8());
	}
}
