package com.dslplatform.json.runtime;

import com.dslplatform.json.*;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

public abstract class Settings {
	public static final DslJson.ConverterFactory<JsonReader.ReadObject> UNKNOWN_READER = new DslJson.ConverterFactory<JsonReader.ReadObject>() {
		private final JsonReader.ReadObject READER = new JsonReader.ReadObject() {
			@Override
			public Object read(JsonReader reader) throws IOException {
				return ObjectConverter.deserializeObject(reader);
			}
		};
		@Nullable
		@Override
		public JsonReader.ReadObject tryCreate(Type manifest, DslJson dslJson) {
			return Object.class == manifest ? READER : null;
		}
	};
	public static final DslJson.ConverterFactory<JsonWriter.WriteObject> UNKNOWN_WRITER = new DslJson.ConverterFactory<JsonWriter.WriteObject>() {
		class ObjectWriter implements JsonWriter.WriteObject {
			private final DslJson dslJson;
			public ObjectWriter(DslJson dslJson) {
				this.dslJson = dslJson;
			}

			@Override
			public void write(JsonWriter writer, Object value) {
				if (value == null) writer.writeNull();
				else {
					Class<?> signature = value.getClass();
					JsonWriter.WriteObject instanceWriter = dslJson.tryFindWriter(signature);
					if (instanceWriter instanceof ObjectWriter || instanceWriter == null) {
						throw new SerializationException("Unable to serialize provided instance");
					}
					instanceWriter.write(writer, value);
				}
			}
		}
		@Nullable
		@Override
		public JsonWriter.WriteObject tryCreate(Type manifest, final DslJson dslJson) {
			return Object.class == manifest ? new ObjectWriter(dslJson) : null;
		}
	};

	public interface Function<TIn, TOut> {
		@Nullable
		TOut apply(@Nullable TIn arguments);
	}
	public interface BiConsumer<T, U> {
		void accept(T t, @Nullable U u);
	}

	static boolean isKnownType(@Nullable final Type type) {
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
			@Nullable final Type type) {
		return createEncoder(read, name, json, type, null);
	}

	public static <T, R> JsonWriter.WriteObject<T> createEncoder(
			final Function<T, R> read,
			final String name,
			final DslJson json,
			@Nullable final Type type,
			@Nullable final JsonWriter.WriteObject<R> customEncoder) {
		if (read == null) throw new IllegalArgumentException("read can't be null");
		if (name == null) throw new IllegalArgumentException("name can't be null");
		if (json == null) throw new IllegalArgumentException("json can't be null");

		final JsonWriter.WriteObject<R> encoder = customEncoder != null ? customEncoder :
				(type != null ? json.tryFindWriter(type) : null);
		if (encoder == null || Object.class.equals(type)) {
			return new LazyAttributeObjectEncoder<>(read, name, json, type);
		}
		if (json.omitDefaults) {
			return new AttributeObjectNonDefaultEncoder<>(read, name, encoder, (R)json.getDefault(type));
		}
		return new AttributeObjectAlwaysEncoder<>(read, name, encoder);
	}

	public static <T, R> JsonWriter.WriteObject<T> createArrayEncoder(
			final Function<T, R> read,
			final DslJson json,
			@Nullable final Type type) {
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
			@Nullable final Type type) {
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
			@Nullable final Type type) {
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
		DslJson.Settings<T> settings = withAnalyzers();
		return settings.with(new ConfigureJava8());
	}

	public static <T> DslJson.Settings<T> basicSetup() {
		DslJson.Settings<T> settings = withAnalyzers();
		return settings.includeServiceLoader();
	}

	public static <T> DslJson.Settings<T> withAnalyzers() {
		return withAnalyzers(true, true);
	}
	public static <T> DslJson.Settings<T> withAnalyzers(boolean unknownReader, boolean unknownWriter) {
		DslJson.Settings<T> settings = new DslJson.Settings<>();
		if (unknownReader) settings.resolveReader(UNKNOWN_READER);
		settings.resolveReader(CollectionAnalyzer.READER)
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
				.resolveWriter(MixinAnalyzer.WRITER);
		if (unknownWriter) settings.resolveWriter(UNKNOWN_WRITER);
		return settings;
	}
}
