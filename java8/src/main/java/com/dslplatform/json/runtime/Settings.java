package com.dslplatform.json.runtime;

import com.dslplatform.json.*;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class Settings {
	private static final DslJson.ConverterFactory<JsonReader.ReadObject> UNKNOWN_READER =
			(manifest, dslJson) -> Object.class == manifest ? ObjectConverter::deserializeObject : null;

	public static <T, R> JsonWriter.WriteObject<T> createEncoder(
			final Function<T, R> read,
			final String name,
			final DslJson json,
			final Type type) {
		JsonWriter.WriteObject<R> encoder = json.tryFindWriter(type);
		if (encoder == null) return new LazyAttributeEncoder<>(read, name, json, type);
		return new KnownAttributeEncoder<>(read, name, !json.omitDefaults, encoder);
	}

	public static <T, R> DecodePropertyInfo<JsonReader.BindObject<T>> createDecoder(
			final BiConsumer<T, R> write,
			final String name,
			final DslJson json,
			final Type type) {
		JsonReader.ReadObject<R> decoder = json.tryFindReader(type);
		if (decoder == null) return new DecodePropertyInfo<>(name, false, new LazyAttributeDecoder<>(write, name, json, type));
		return new DecodePropertyInfo<>(name, false, new KnownAttributeDecoder<>(write, decoder));
	}

	public static <T> DslJson.Settings<T> withRuntime() {
		return new DslJson.Settings()
				.resolveReader(UNKNOWN_READER)
				.resolveReader(OptionalAnalyzer.READER)
				.resolveWriter(OptionalAnalyzer.WRITER)
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
