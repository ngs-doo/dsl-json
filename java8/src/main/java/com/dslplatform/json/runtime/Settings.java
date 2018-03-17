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
		return type != null;
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
		if (encoder == null || Object.class.equals(type)) {
			return new LazyAttributeObjectEncoder<>(read, name, json, type);
		}
		if (json.omitDefaults) {
			//TODO: better default value look
			return new AttributeObjectNonDefaultEncoder<>(read, name, encoder, null);
		}
		return new AttributeObjectAlwaysEncoder<>(read, name, encoder);
	}

	public static <T, R> JsonWriter.WriteObject<T> createEncoder(
			final Function<T, R> read,
			final String name,
			final DslJson json,
			final JsonWriter.WriteObject<R> encoder) {
		if (read == null) throw new IllegalArgumentException("read can't be null");
		if (name == null) throw new IllegalArgumentException("name can't be null");
		if (json == null) throw new IllegalArgumentException("json can't be null");
		if (encoder == null) throw new IllegalArgumentException("encoder can't be null");
		return json.omitDefaults
				? new AttributeObjectNonDefaultEncoder<>(read, name, encoder, null)
				: new AttributeObjectAlwaysEncoder<>(read, name, encoder);
	}

	public static <T, R> JsonWriter.WriteObject<T> createArrayEncoder(
			final Function<T, R> read,
			final DslJson json,
			final Type type) {
		if (read == null) throw new IllegalArgumentException("read can't be null");
		if (json == null) throw new IllegalArgumentException("json can't be null");
		final JsonWriter.WriteObject<R> encoder = type != null ? json.tryFindWriter(type) : null;
		if (encoder == null || Object.class.equals(type)) return new LazyAttributeArrayEncoder<>(read, json, type);
		return new AttributeArrayEncoder<>(read, encoder);
	}

	public static <T, R> JsonWriter.WriteObject<T> createArrayEncoder(
			final Function<T, R> read,
			final JsonWriter.WriteObject<R> encoder) {
		if (read == null) throw new IllegalArgumentException("read can't be null");
		if (encoder == null) throw new IllegalArgumentException("encoder can't be null");
		return new AttributeArrayEncoder<>(read, encoder);
	}

	public static <T, R> DecodePropertyInfo<JsonReader.BindObject<T>> createDecoder(
			final BiConsumer<T, R> write,
			final String name,
			final DslJson json,
			final Class<R> manifest) {
		return createDecoder(write, name, json, false, false, -1, false, manifest);
	}

	public static <T, R> DecodePropertyInfo<JsonReader.BindObject<T>> createDecoder(
			final BiConsumer<T, R> write,
			final String name,
			final DslJson json,
			final boolean exactNameMatch,
			final boolean isMandatory,
			final int index,
			final boolean nonNull,
			final Type type) {
		if (write == null) throw new IllegalArgumentException("write can't be null");
		if (name == null) throw new IllegalArgumentException("name can't be null");
		if (json == null) throw new IllegalArgumentException("json can't be null");
		final JsonReader.ReadObject<R> decoder = type != null ? json.tryFindReader(type) : null;
		if (decoder == null || !isKnownType(type)) return new DecodePropertyInfo<>(name, exactNameMatch, isMandatory, index, nonNull, new LazyAttributeDecoder<>(write, json, type));
		return new DecodePropertyInfo<>(name, exactNameMatch, isMandatory, index, nonNull, new AttributeDecoder<>(write, decoder));
	}

	public static <T, R> DecodePropertyInfo<JsonReader.BindObject<T>> createDecoder(
			final BiConsumer<T, R> write,
			final String name,
			final DslJson json,
			final boolean exactNameMatch,
			final boolean isMandatory,
			final int index,
			final boolean nonNull,
			final JsonReader.ReadObject<R> decoder) {
		if (write == null) throw new IllegalArgumentException("write can't be null");
		if (name == null) throw new IllegalArgumentException("name can't be null");
		if (json == null) throw new IllegalArgumentException("json can't be null");
		if (decoder == null) throw new IllegalArgumentException("decoder can't be null");
		return new DecodePropertyInfo<>(name, exactNameMatch, isMandatory, index, nonNull, new AttributeDecoder<>(write, decoder));
	}

	public static <T, R> JsonReader.BindObject<T> createArrayDecoder(
			final BiConsumer<T, R> write,
			final DslJson json,
			final Type type) {
		if (write == null) throw new IllegalArgumentException("write can't be null");
		if (json == null) throw new IllegalArgumentException("json can't be null");
		final JsonReader.ReadObject<R> decoder = type != null ? json.tryFindReader(type) : null;
		if (decoder == null || !isKnownType(type)) return new LazyAttributeDecoder<>(write, json, type);
		return new AttributeDecoder<>(write, decoder);
	}

	public static <T, R> JsonReader.BindObject<T> createArrayDecoder(
			final BiConsumer<T, R> write,
			final JsonReader.ReadObject<R> decoder) {
		if (write == null) throw new IllegalArgumentException("write can't be null");
		if (decoder == null) throw new IllegalArgumentException("decoder can't be null");
		return new AttributeDecoder<>(write, decoder);
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
				.resolveWriter(ObjectAnalyzer.CONVERTER)
				.resolveBinder(ObjectAnalyzer.CONVERTER)
				.resolveReader(ObjectAnalyzer.CONVERTER)
				.resolveWriter(ImmutableAnalyzer.CONVERTER)
				.resolveReader(ImmutableAnalyzer.CONVERTER)
				.resolveWriter(MixinAnalyzer.WRITER)
				.with(new ConfigureJava8());
	}
}
