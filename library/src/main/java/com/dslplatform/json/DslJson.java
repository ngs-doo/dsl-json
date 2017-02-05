package com.dslplatform.json;

import org.w3c.dom.Element;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Main DSL-JSON class.
 * Easiest way to use the library is to create an DslJson&lt;Object&gt; instance and reuse it within application.
 * DslJson has optional constructor for specifying default readers/writers.
 * <p>
 * During initialization DslJson will use ServiceLoader API to load registered services.
 * This is done through `META-INF/services/com.dslplatform.json.CompiledJson` file.
 * <p>
 * DslJson can fallback to another serializer in case when it doesn't know how to handle specific type.
 * This can be specified by Fallback interface during initialization.
 * <p>
 * If you wish to use compile time databinding @CompiledJson annotation must be specified on the classes
 * and annotation processor must be configured to register those classes into services file.
 * <p>
 * Usage example:
 * <pre>
 *     DslJson&lt;Object&gt; dsl = new DslJson&lt;&lt;();
 *     dsl.serialize(instance, OutputStream);
 *     POJO pojo = dsl.deserialize(POJO.class, InputStream, new byte[1024]);
 * </pre>
 * <p>
 * For best performance use serialization API with JsonWriter which is reused.
 * For best deserialization performance prefer byte[] API instead of InputStream API.
 * If InputStream API is used, reuse the buffer instance or reuse the whole JsonStreamReader by calling reset(InputStream)
 * <p>
 * During deserialization TContext can be used to pass data into deserialized classes.
 * This is useful when deserializing domain objects which require state or service provider.
 * For example DSL Platform entities require service locator to be able to perform lazy load.
 * <p>
 * DslJson doesn't have a String or Reader API since it's optimized for processing bytes.
 * If you wish to process String, use String.getBytes("UTF-8") as argument for DslJson
 * <pre>
 *     DslJson&lt;Object&gt; dsl = new DslJson&lt;&gt;();
 *     JsonWriter writer = dsl.newWriter();
 *     dsl.serialize(writer, instance);
 *     String json = writer.toString(); //JSON as string
 *     byte[] input = json.getBytes("UTF-8");
 *     POJO pojo = dsl.deserialize(POJO.class, input, input.length);
 * </pre>
 *
 * @param <TContext> used for library specialization. If unsure, use Object
 */
public class DslJson<TContext> implements UnknownSerializer {

	private static final Charset UTF8 = Charset.forName("UTF-8");

	/**
	 * The context of this instance.
	 * Can be used for library specialization
	 */
	public final TContext context;
	protected final Fallback<TContext> fallback;
	/**
	 * Should properties with default values be omitted from the resulting JSON?
	 * This will leave out nulls, empty collections, zeros and other attributes with default values
	 * which can be reconstructed from schema information
	 */
	public final boolean omitDefaults;
	protected final StringCache keyCache;
	protected final StringCache valuesCache;
	protected final List<ConverterFactory<JsonWriter.WriteObject>> writerFactories = new ArrayList<ConverterFactory<JsonWriter.WriteObject>>();
	protected final List<ConverterFactory<JsonReader.ReadObject>> readerFactories = new ArrayList<ConverterFactory<JsonReader.ReadObject>>();

	public interface Fallback<TContext> {
		void serialize(Object instance, OutputStream stream) throws IOException;

		Object deserialize(TContext context, Type manifest, byte[] body, int size) throws IOException;

		Object deserialize(TContext context, Type manifest, InputStream stream) throws IOException;
	}

	public interface ConverterFactory<T> {
		T tryCreate(Type manifest, DslJson dslJson);
	}

	/**
	 * Configuration for DslJson options.
	 * By default key cache is enabled. Everything else is not configured.
	 * To load `META-INF/services` call `includeServiceLoader()`
	 *
	 * @param <TContext> DslJson context
	 */
	public static class Settings<TContext> {
		private TContext context;
		private boolean javaSpecifics;
		private Fallback<TContext> fallback;
		private boolean omitDefaults;
		private StringCache keyCache = new SimpleStringCache();
		private StringCache valuesCache;
		private boolean withServiceLoader;
		private final List<Configuration> configurations = new ArrayList<Configuration>();
		private final List<ConverterFactory<JsonWriter.WriteObject>> writerFactories = new ArrayList<ConverterFactory<JsonWriter.WriteObject>>();
		private final List<ConverterFactory<JsonReader.ReadObject>> readerFactories = new ArrayList<ConverterFactory<JsonReader.ReadObject>>();

		/**
		 * Pass in context for DslJson.
		 * Context will be available in JsonReader for objects which needs it.
		 *
		 * @param context context propagated to JsonReaders
		 * @return itself
		 */
		public Settings<TContext> withContext(TContext context) {
			this.context = context;
			return this;
		}

		/**
		 * Enable converters for Java specific types (Graphics API) not available on Android.
		 *
		 * @param javaSpecifics should register Java specific converters
		 * @return itself
		 */
		public Settings<TContext> withJavaConverters(boolean javaSpecifics) {
			this.javaSpecifics = javaSpecifics;
			return this;
		}

		/**
		 * Will be eventually replaced with writer/reader factories.
		 * Used by DslJson to call into when trying to serialize/deserialize object which is not supported.
		 *
		 * @param fallback how to handle unsupported type
		 * @return which fallback to use in case of unsupported type
		 */
		@Deprecated
		public Settings<TContext> fallbackTo(Fallback<TContext> fallback) {
			this.fallback = fallback;
			return this;
		}

		/**
		 * DslJson can exclude some properties from resulting JSON which it can reconstruct fully from schema information.
		 * Eg. int with value 0 can be omitted since that is default value for the type.
		 * Null values can be excluded since they are handled the same way as missing property.
		 *
		 * @param omitDefaults should exclude default values from resulting JSON
		 * @return itself
		 */
		public Settings<TContext> skipDefaultValues(boolean omitDefaults) {
			this.omitDefaults = omitDefaults;
			return this;
		}

		/**
		 * Use specific key cache implementation.
		 * Key cache is enabled by default and it's used when deserializing unstructured objects such as Map&lt;String, Object&gt;
		 * to avoid allocating new String key instance. Instead StringCache will provide a new or an old instance.
		 * This improves memory usage and performance since there is usually small number of keys.
		 * It does have some performance overhead, but this is dependant on the implementation.
		 * <p>
		 * To disable key cache, provide null for it.
		 *
		 * @param keyCache which key cache to use
		 * @return itself
		 */
		public Settings<TContext> useKeyCache(StringCache keyCache) {
			this.keyCache = keyCache;
			return this;
		}

		/**
		 * Use specific string values cache implementation.
		 * By default string values cache is disabled.
		 * <p>
		 * To support memory restricted scenarios where there is limited number of string values,
		 * values cache can be used.
		 * <p>
		 * Not every "JSON string" will use this cache... eg UUID, LocalDate don't create an instance of string
		 * and therefore don't use this cache.
		 *
		 * @param valuesCache which values cache to use
		 * @return itself
		 */
		public Settings<TContext> useStringValuesCache(StringCache valuesCache) {
			this.valuesCache = valuesCache;
			return this;
		}

		/**
		 * DslJson will iterate over converter factories when requested type is unknown.
		 * Registering writer converter factory allows for constructing JSON converter lazily.
		 *
		 * @param writer registered writer factory
		 * @return itself
		 */
		public Settings<TContext> resolveWriter(ConverterFactory<JsonWriter.WriteObject> writer) {
			if (writer == null) throw new IllegalArgumentException("writer can't be null");
			writerFactories.add(writer);
			return this;
		}

		/**
		 * DslJson will iterate over converter factories when requested type is unknown.
		 * Registering reader converter factory allows for constructing JSON converter lazily.
		 *
		 * @param reader registered reader factory
		 * @return itself
		 */
		public Settings<TContext> resolveReader(ConverterFactory<JsonReader.ReadObject> reader) {
			if (reader == null) throw new IllegalArgumentException("reader can't be null");
			readerFactories.add(reader);
			return this;
		}

		/**
		 * Load converters using `ServiceLoader.load(Configuration.class)`
		 * Will scan through `META-INF/services/com.dslplatform.json.Configuration` file and register implementation during startup.
		 * This will pick up compile time databindings if they are available in specific folder.
		 * <p>
		 * Note that gradle on Android has issues with preserving that file, in which case it can be provided manually.
		 * DslJson will fall back to "expected" class name if it doesn't find anything during scanning.
		 *
		 * @return itself
		 */
		public Settings<TContext> includeServiceLoader() {
			withServiceLoader = true;
			for (Configuration c : ServiceLoader.load(Configuration.class)) {
				configurations.add(c);
			}
			return this;
		}

		/**
		 * Configure DslJson with custom Configuration during startup.
		 * Configurations are extension points for setting up readers/writers during DslJson initialization.
		 *
		 * @param conf custom extensibility point
		 * @return itself
		 */
		public Settings<TContext> with(Configuration conf) {
			if (conf == null) throw new IllegalArgumentException("conf can't be null");
			configurations.add(conf);
			return this;
		}

		private Settings<TContext> with(Iterable<Configuration> confs) {
			if (confs != null) {
				withServiceLoader = true;
				for (Configuration c : confs)
					configurations.add(c);
			}
			return this;
		}
	}

	/**
	 * Simple initialization entry point.
	 * Will provide null for TContext
	 * Java graphics readers/writers will not be registered.
	 * Fallback will not be configured.
	 * Key cache will be enables, values cache will be disabled.
	 * Default ServiceLoader.load method will be used to setup services from META-INF
	 */
	public DslJson() {
		this(new Settings<TContext>().includeServiceLoader());
	}

	/**
	 * Will be removed. Use DslJson(Settings) instead.
	 * Fully configurable entry point.
	 *
	 * @param context       context instance which can be provided to deserialized objects. Use null if not sure
	 * @param javaSpecifics register Java graphics specific classes such as java.awt.Point, Image, ...
	 * @param fallback      in case of unsupported type, try serialization/deserialization through external API
	 * @param omitDefaults  should serialization produce minified JSON (omit nulls and default values)
	 * @param keyCache      parsed keys can be cached (this is only used in small subset of parsing)
	 * @param serializers   additional serializers/deserializers which will be immediately registered into readers/writers
	 */
	@Deprecated
	public DslJson(
			final TContext context,
			final boolean javaSpecifics,
			final Fallback<TContext> fallback,
			final boolean omitDefaults,
			final StringCache keyCache,
			final Iterable<Configuration> serializers) {
		this(new Settings<TContext>()
				.withContext(context)
				.withJavaConverters(javaSpecifics)
				.fallbackTo(fallback)
				.skipDefaultValues(omitDefaults)
				.useKeyCache(keyCache)
				.with(serializers)
		);
	}

	/**
	 * Fully configurable entry point.
	 * Provide settings for DSL-JSON initialization.
	 *
	 * @param settings DSL-JSON configuration
	 */
	public DslJson(Settings<TContext> settings) {
		if (settings == null) throw new IllegalArgumentException("settings can't be null");
		this.context = settings.context;
		this.fallback = settings.fallback;
		this.omitDefaults = settings.omitDefaults;
		this.keyCache = settings.keyCache;
		this.valuesCache = settings.valuesCache;
		this.writerFactories.addAll(settings.writerFactories);
		this.readerFactories.addAll(settings.readerFactories);
		registerReader(byte[].class, BinaryConverter.Base64Reader);
		registerWriter(byte[].class, BinaryConverter.Base64Writer);
		registerReader(boolean.class, BoolConverter.BooleanReader);
		registerReader(Boolean.class, BoolConverter.BooleanReader);
		registerWriter(boolean.class, BoolConverter.BooleanWriter);
		registerWriter(Boolean.class, BoolConverter.BooleanWriter);
		if (settings.javaSpecifics) {
			registerJavaSpecifics(this);
		}
		registerReader(LinkedHashMap.class, ObjectConverter.MapReader);
		registerReader(HashMap.class, ObjectConverter.MapReader);
		registerReader(Map.class, ObjectConverter.MapReader);
		registerWriter(Map.class, new JsonWriter.WriteObject<Map>() {
			@Override
			public void write(JsonWriter writer, Map value) {
				if (value == null) {
					writer.writeNull();
				} else {
					try {
						serializeMap(value, writer);
					} catch (IOException ex) {
						throw new SerializationException(ex);
					}
				}
			}
		});
		registerReader(URI.class, NetConverter.UriReader);
		registerWriter(URI.class, NetConverter.UriWriter);
		registerReader(InetAddress.class, NetConverter.AddressReader);
		registerWriter(InetAddress.class, NetConverter.AddressWriter);
		registerReader(double.class, NumberConverter.DoubleReader);
		registerWriter(double.class, NumberConverter.DoubleWriter);
		registerReader(Double.class, NumberConverter.DoubleReader);
		registerWriter(Double.class, NumberConverter.DoubleWriter);
		registerReader(float.class, NumberConverter.FloatReader);
		registerWriter(float.class, NumberConverter.FloatWriter);
		registerReader(Float.class, NumberConverter.FloatReader);
		registerWriter(Float.class, NumberConverter.FloatWriter);
		registerReader(int.class, NumberConverter.IntReader);
		registerWriter(int.class, NumberConverter.IntWriter);
		registerReader(Integer.class, NumberConverter.IntReader);
		registerWriter(Integer.class, NumberConverter.IntWriter);
		registerReader(long.class, NumberConverter.LongReader);
		registerWriter(long.class, NumberConverter.LongWriter);
		registerReader(Long.class, NumberConverter.LongReader);
		registerWriter(Long.class, NumberConverter.LongWriter);
		registerReader(BigDecimal.class, NumberConverter.DecimalReader);
		registerWriter(BigDecimal.class, NumberConverter.DecimalWriter);
		registerReader(String.class, StringConverter.Reader);
		registerWriter(String.class, StringConverter.Writer);
		registerReader(UUID.class, UUIDConverter.Reader);
		registerWriter(UUID.class, UUIDConverter.Writer);
		registerReader(Number.class, NumberConverter.NumberReader);

		for (Configuration serializer : settings.configurations) {
			serializer.configure(this);
		}
		if (settings.withServiceLoader && settings.configurations.isEmpty()) {
			//TODO: workaround common issue with failed services registration. try to load common external name if exists
			loadDefaultConverters(this, "dsl_json.json.ExternalSerialization");
			loadDefaultConverters(this, "dsl_json_ExternalSerialization");
		}
	}

	/**
	 * Simplistic string cache implementation.
	 * It uses a fixed String[] structure in which it caches string value based on it's hash.
	 * Eg, hash &amp; mask provide index into the structure. Different string with same hash will overwrite the previous one.
	 */
	public static class SimpleStringCache implements StringCache {

		private final int mask;
		private final String[] cache;

		/**
		 * Will use String[] with 1024 elements.
		 */
		public SimpleStringCache() {
			this(10);
		}

		public SimpleStringCache(int log2Size) {
			int size = 2;
			for (int i = 1; i < log2Size; i++) {
				size *= 2;
			}
			mask = size - 1;
			cache = new String[size];
		}

		/**
		 * Calculates hash of the provided "string" and looks it up from the String[]
		 * It it doesn't exists of a different string is already there a new String instance is created
		 * and saved into the String[]
		 *
		 * @param chars buffer into which string was parsed
		 * @param len the string length inside the buffer
		 * @return String instance matching the char[]/int pair
		 */
		@Override
		public String get(char[] chars, int len) {
			long hash = 0x811c9dc5;
			for (int i = 0; i < len; i++) {
				hash ^= (byte) chars[i];
				hash *= 0x1000193;
			}
			final int index = (int) hash & mask;
			String value = cache[index];
			if (value == null) return createAndPut(index, chars, len);
			if (value.length() != len) return createAndPut(index, chars, len);
			for (int i = 0; i < value.length(); i++) {
				if (value.charAt(i) != chars[i]) return createAndPut(index, chars, len);
			}
			return value;
		}

		private String createAndPut(int index, char[] chars, int len) {
			String value = new String(chars, 0, len);
			cache[index] = value;
			return value;
		}
	}

	/**
	 * Create a writer bound to this DSL-JSON.
	 * Ideally it should be reused.
	 * Bound writer can use lookups to find custom writers.
	 * This can be used to serialize unknown types such as Object.class
	 *
	 * @return bound writer
	 */
	public JsonWriter newWriter() {
		return new JsonWriter(this);
	}

	/**
	 * Create a writer bound to this DSL-JSON.
	 * Ideally it should be reused.
	 * Bound writer can use lookups to find custom writers.
	 * This can be used to serialize unknown types such as Object.class
	 *
	 * @param size initial buffer size
	 * @return bound writer
	 */
	public JsonWriter newWriter(int size) {
		return new JsonWriter(size, this);
	}

	/**
	 * Create a writer bound to this DSL-JSON.
	 * Ideally it should be reused.
	 * Bound writer can use lookups to find custom writers.
	 * This can be used to serialize unknown types such as Object.class
	 *
	 * @param buffer initial buffer
	 * @return bound writer
	 */
	public JsonWriter newWriter(byte[] buffer) {
		if (buffer == null) throw new IllegalArgumentException("null value provided for buffer");
		return new JsonWriter(buffer, this);
	}

	/**
	 * Create a reader bound to this DSL-JSON.
	 * Bound reader can reuse key cache (which is used during Map deserialization)
	 *
	 * @param bytes input bytes
	 * @return bound reader
	 */
	public JsonReader<TContext> newReader(byte[] bytes) {
		return new JsonReader<TContext>(bytes, context, keyCache, valuesCache);
	}

	/**
	 * Create a reader bound to this DSL-JSON.
	 * Bound reader can reuse key cache (which is used during Map deserialization)
	 *
	 * @param bytes  input bytes
	 * @param length use input bytes up to specified length
	 * @return bound reader
	 */
	public JsonReader<TContext> newReader(byte[] bytes, int length) {
		return new JsonReader<TContext>(bytes, length, context, new char[64], keyCache, valuesCache);
	}


	/**
	 * Create a reader bound to this DSL-JSON.
	 * Bound reader can reuse key cache (which is used during Map deserialization)
	 * Pass in initial string buffer.
	 *
	 * @param bytes  input bytes
	 * @param length use input bytes up to specified length
	 * @param tmp string parsing buffer
	 * @return bound reader
	 */
	public JsonReader<TContext> newReader(byte[] bytes, int length, char[] tmp) {
		return new JsonReader<TContext>(bytes, length, context, tmp, keyCache, valuesCache);
	}

	/**
	 * Create a reader bound to this DSL-JSON.
	 * Bound reader can reuse key cache (which is used during Map deserialization)
	 * Stream readers can be reused (using reset method).
	 *
	 * @param stream input stream
	 * @param buffer temporary buffer
	 * @return bound reader
	 * @throws java.io.IOException unable to read from stream
	 */
	public JsonStreamReader<TContext> newReader(InputStream stream, byte[] buffer) throws IOException {
		return new JsonStreamReader<TContext>(stream, buffer, context, keyCache, valuesCache);
	}

	/**
	 * Create a reader bound to this DSL-JSON.
	 * Bound reader can reuse key cache (which is used during Map deserialization)
	 * This method id Deprecated since it should be avoided.
	 * It's better to use byte[] or InputStream based readers
	 *
	 * @param input JSON string
	 * @return bound reader
	 */
	@Deprecated
	public JsonReader<TContext> newReader(String input) {
		final byte[] bytes = input.getBytes(UTF8);
		return new JsonReader<TContext>(bytes, context, keyCache, valuesCache);
	}

	private static void loadDefaultConverters(final DslJson json, final String name) {
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			Class<?> external = loader.loadClass(name);
			Configuration instance = (Configuration) external.newInstance();
			instance.configure(json);
		} catch (NoClassDefFoundError ignore) {
		} catch (Exception ignore) {
		}
	}

	static void registerJavaSpecifics(final DslJson json) {
		json.registerReader(java.awt.geom.Point2D.Double.class, JavaGeomConverter.LocationReader);
		json.registerReader(java.awt.geom.Point2D.class, JavaGeomConverter.LocationReader);
		json.registerWriter(java.awt.geom.Point2D.class, JavaGeomConverter.LocationWriter);
		json.registerReader(java.awt.Point.class, JavaGeomConverter.PointReader);
		json.registerWriter(java.awt.Point.class, JavaGeomConverter.PointWriter);
		json.registerReader(java.awt.geom.Rectangle2D.Double.class, JavaGeomConverter.RectangleReader);
		json.registerReader(java.awt.geom.Rectangle2D.class, JavaGeomConverter.RectangleReader);
		json.registerWriter(java.awt.geom.Rectangle2D.class, JavaGeomConverter.RectangleWriter);
		json.registerReader(java.awt.image.BufferedImage.class, JavaGeomConverter.ImageReader);
		json.registerReader(java.awt.Image.class, JavaGeomConverter.ImageReader);
		json.registerWriter(java.awt.Image.class, JavaGeomConverter.ImageWriter);
		json.registerReader(Element.class, XmlConverter.Reader);
		json.registerWriter(Element.class, XmlConverter.Writer);
	}

	protected static boolean isNull(final int size, final byte[] body) {
		return size == 4
				&& body[0] == 'n'
				&& body[1] == 'u'
				&& body[2] == 'l'
				&& body[3] == 'l';
	}

	private final ConcurrentHashMap<Class<?>, JsonReader.ReadJsonObject<JsonObject>> jsonObjectReaders =
			new ConcurrentHashMap<Class<?>, JsonReader.ReadJsonObject<JsonObject>>();

	private final HashMap<Type, JsonReader.ReadObject<?>> jsonReaders = new HashMap<Type, JsonReader.ReadObject<?>>();

	/**
	 * Register custom reader for specific type (JSON -&gt; instance conversion).
	 * Reader is used for conversion from input byte[] -&gt; target object instance
	 * <p>
	 * Types registered through @CompiledJson annotation should be registered automatically through
	 * ServiceLoader.load method and you should not be registering them manually.
	 * <p>
	 * If null is registered for a reader this will disable deserialization of specified type
	 *
	 * @param manifest specified type
	 * @param reader   provide custom implementation for reading JSON into an object instance
	 * @param <T>      type
	 * @param <S>      type or subtype
	 */
	public <T, S extends T> void registerReader(final Class<T> manifest, final JsonReader.ReadObject<S> reader) {
		jsonReaders.put(manifest, reader);
	}

	/**
	 * Register custom reader for specific type (JSON -&gt; instance conversion).
	 * Reader is used for conversion from input byte[] -&gt; target object instance
	 * <p>
	 * Types registered through @CompiledJson annotation should be registered automatically through
	 * ServiceLoader.load method and you should not be registering them manually.
	 * <p>
	 * If null is registered for a reader this will disable deserialization of specified type
	 *
	 * @param manifest specified type
	 * @param reader   provide custom implementation for reading JSON into an object instance
	 */
	public void registerReader(final Type manifest, final JsonReader.ReadObject<?> reader) {
		jsonReaders.put(manifest, reader);
	}

	private final HashMap<Type, JsonWriter.WriteObject<?>> jsonWriters = new HashMap<Type, JsonWriter.WriteObject<?>>();

	/**
	 * Register custom writer for specific type (instance -&gt; JSON conversion).
	 * Writer is used for conversion from object instance -&gt; output byte[]
	 * <p>
	 * Types registered through @CompiledJson annotation should be registered automatically through
	 * ServiceLoader.load method and you should not be registering them manually.
	 * <p>
	 * If null is registered for a writer this will disable serialization of specified type
	 *
	 * @param manifest specified type
	 * @param writer   provide custom implementation for writing JSON from object instance
	 * @param <T>      type
	 */
	public <T> void registerWriter(final Class<T> manifest, final JsonWriter.WriteObject<T> writer) {
		writerMap.put(manifest, manifest);
		jsonWriters.put(manifest, writer);
	}

	/**
	 * Register custom writer for specific type (instance -&gt; JSON conversion).
	 * Writer is used for conversion from object instance -&gt; output byte[]
	 * <p>
	 * Types registered through @CompiledJson annotation should be registered automatically through
	 * ServiceLoader.load method and you should not be registering them manually.
	 * <p>
	 * If null is registered for a writer this will disable serialization of specified type
	 *
	 * @param manifest specified type
	 * @param writer   provide custom implementation for writing JSON from object instance
	 */
	public void registerWriter(final Type manifest, final JsonWriter.WriteObject<?> writer) {
		jsonWriters.put(manifest, writer);
	}

	private final ConcurrentMap<Class<?>, Class<?>> writerMap = new ConcurrentHashMap<Class<?>, Class<?>>();

	/**
	 * Try to find registered writer for provided type.
	 * If writer is not found, null will be returned.
	 * If writer for exact type is not found, type hierarchy will be scanned for base writer.
	 * <p>
	 * Writer is used for conversion from object instance into JSON representation.
	 *
	 * @param manifest specified type
	 * @return writer for specified type if found
	 */
	public JsonWriter.WriteObject<?> tryFindWriter(final Type manifest) {
		JsonWriter.WriteObject writer = jsonWriters.get(manifest);
		if (writer != null) return writer;
		for (ConverterFactory<JsonWriter.WriteObject> wrt : writerFactories) {
			writer = wrt.tryCreate(manifest, this);
			if (writer != null) {
				jsonWriters.put(manifest, writer);
				return writer;
			}
		}
		Class<?> found = writerMap.get(manifest);
		if (found != null) {
			return jsonWriters.get(found);
		}
		if (manifest instanceof Class<?> == false) {
			return null;
		}
		Class<?> container = (Class<?>) manifest;
		final ArrayList<Class<?>> signatures = new ArrayList<Class<?>>();
		findAllSignatures(container, signatures);
		for (final Class<?> sig : signatures) {
			writer = jsonWriters.get(sig);
			if (writer != null) {
				writerMap.putIfAbsent(container, sig);
				return writer;
			}
		}
		return null;
	}

	/**
	 * Try to find registered reader for provided type.
	 * If reader is not found, null will be returned.
	 * Exact match must be found, type hierarchy will not be scanned for alternative writer.
	 * <p>
	 * If you wish to use alternative writer for specific type, register it manually with something along the lines of
	 * <pre>
	 *     DslJson dslJson = ...
	 *     dslJson.registerReader(Interface.class, dslJson.tryFindWriter(Implementation.class));
	 * </pre>
	 *
	 * @param manifest specified type
	 * @return found reader for specified type
	 */
	public JsonReader.ReadObject<?> tryFindReader(final Type manifest) {
		JsonReader.ReadObject found = jsonReaders.get(manifest);
		if (found == null) {
			for (ConverterFactory<JsonReader.ReadObject> rdr : readerFactories) {
				found = rdr.tryCreate(manifest, this);
				if (found != null) {
					jsonReaders.put(manifest, found);
					break;
				}
			}
		}
		return found;
	}

	/**
	 * Try to find registered reader for provided type.
	 * If reader is not found, null will be returned.
	 * Exact match must be found, type hierarchy will not be scanned for alternative writer.
	 * <p>
	 * If you wish to use alternative writer for specific type, register it manually with something along the lines of
	 * <pre>
	 *     DslJson dslJson = ...
	 *     dslJson.registerReader(Interface.class, dslJson.tryFindWriter(Implementation.class));
	 * </pre>
	 *
	 * @param manifest specified class
	 * @param <T> specified type
	 * @return found reader for specified class
	 */
	@SuppressWarnings("unchecked")
	public <T> JsonReader.ReadObject<T> tryFindReader(final Class<T> manifest) {
		return (JsonReader.ReadObject<T>) tryFindReader((Type) manifest);
	}

	private static void findAllSignatures(final Class<?> manifest, final ArrayList<Class<?>> found) {
		if (found.contains(manifest)) {
			return;
		}
		found.add(manifest);
		final Class<?> superClass = manifest.getSuperclass();
		if (superClass != null && superClass != Object.class) {
			findAllSignatures(superClass, found);
		}
		for (final Class<?> iface : manifest.getInterfaces()) {
			findAllSignatures(iface, found);
		}
	}

	@SuppressWarnings("unchecked")
	protected final JsonReader.ReadJsonObject<JsonObject> getObjectReader(final Class<?> manifest) {
		try {
			JsonReader.ReadJsonObject<JsonObject> reader = jsonObjectReaders.get(manifest);
			if (reader == null) {
				try {
					reader = (JsonReader.ReadJsonObject<JsonObject>) manifest.getField("JSON_READER").get(null);
				} catch (Exception ignore) {
					//log error!?
					return null;
				}
				jsonObjectReaders.putIfAbsent(manifest, reader);
			}
			return reader;
		} catch (final Exception ignore) {
			return null;
		}
	}

	public void serializeMap(final Map<String, Object> value, final JsonWriter sw) throws IOException {
		sw.writeByte(JsonWriter.OBJECT_START);
		final int size = value.size();
		if (size > 0) {
			final Iterator<Map.Entry<String, Object>> iterator = value.entrySet().iterator();
			Map.Entry<String, Object> kv = iterator.next();
			sw.writeString(kv.getKey());
			sw.writeByte(JsonWriter.SEMI);
			serialize(sw, kv.getValue());
			for (int i = 1; i < size; i++) {
				sw.writeByte(JsonWriter.COMMA);
				kv = iterator.next();
				sw.writeString(kv.getKey());
				sw.writeByte(JsonWriter.SEMI);
				serialize(sw, kv.getValue());
			}
		}
		sw.writeByte(JsonWriter.OBJECT_END);
	}

	@Deprecated
	public static Object deserializeObject(final JsonReader reader) throws IOException {
		return ObjectConverter.deserializeObject(reader);
	}

	/**
	 * Will be removed
	 * @param reader JSON reader
	 * @return deseralized list
	 * @throws IOException error during parsing
	 */
	@Deprecated
	public static ArrayList<Object> deserializeList(final JsonReader reader) throws IOException {
		return ObjectConverter.deserializeList(reader);
	}

	/**
	 * Will be removed
	 * @param reader JSON reader
	 * @return deserialized map
	 * @throws IOException error during parsing
	 */
	@Deprecated
	public static LinkedHashMap<String, Object> deserializeMap(final JsonReader reader) throws IOException {
		return ObjectConverter.deserializeMap(reader);
	}

	private static Object convertResultToArray(Class<?> elementType, List<?> result) {
		if (elementType.isPrimitive()) {
			if (boolean.class.equals(elementType)) {
				boolean[] array = new boolean[result.size()];
				for (int i = 0; i < result.size(); i++) {
					array[i] = (Boolean) result.get(i);
				}
				return array;
			} else if (int.class.equals(elementType)) {
				int[] array = new int[result.size()];
				for (int i = 0; i < result.size(); i++) {
					array[i] = (Integer) result.get(i);
				}
				return array;
			} else if (long.class.equals(elementType)) {
				long[] array = new long[result.size()];
				for (int i = 0; i < result.size(); i++) {
					array[i] = (Long) result.get(i);
				}
				return array;
			} else if (short.class.equals(elementType)) {
				short[] array = new short[result.size()];
				for (int i = 0; i < result.size(); i++) {
					array[i] = (Short) result.get(i);
				}
				return array;
			} else if (byte.class.equals(elementType)) {
				byte[] array = new byte[result.size()];
				for (int i = 0; i < result.size(); i++) {
					array[i] = (Byte) result.get(i);
				}
				return array;
			} else if (float.class.equals(elementType)) {
				float[] array = new float[result.size()];
				for (int i = 0; i < result.size(); i++) {
					array[i] = (Float) result.get(i);
				}
				return array;
			} else if (double.class.equals(elementType)) {
				double[] array = new double[result.size()];
				for (int i = 0; i < result.size(); i++) {
					array[i] = (Double) result.get(i);
				}
				return array;
			} else if (char.class.equals(elementType)) {
				char[] array = new char[result.size()];
				for (int i = 0; i < result.size(); i++) {
					array[i] = (Character) result.get(i);
				}
				return array;
			}
		}
		return result.toArray((Object[]) Array.newInstance(elementType, 0));
	}

	/**
	 * Check if DslJson knows how to serialize a type.
	 * It will check if a writer for such type exists or can be used.
	 *
	 * @param manifest type to check
	 * @return can serialize this type into JSON
	 */
	public final boolean canSerialize(final Type manifest) {
		JsonWriter.WriteObject writer = jsonWriters.get(manifest);
		if (writer != null) return true;
		if (manifest instanceof Class<?>) {
			final Class<?> content = (Class<?>) manifest;
			if (JsonObject.class.isAssignableFrom(content)) {
				return true;
			}
			if (JsonObject[].class.isAssignableFrom(content)) {
				return true;
			}
			if (tryFindWriter(manifest) != null) {
				return true;
			}
			if (content.isArray()) {
				return !content.getComponentType().isArray()
						&& !Collection.class.isAssignableFrom(content.getComponentType())
						&& canSerialize(content.getComponentType());
			}
		}
		if (manifest instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) manifest;
			if (pt.getActualTypeArguments().length == 1 && pt.getRawType() instanceof Class<?>) {
				final Class<?> container = (Class<?>) pt.getRawType();
				if (container.isArray() || Collection.class.isAssignableFrom(container)) {
					final Type content = pt.getActualTypeArguments()[0];
					return content instanceof Class<?> && JsonObject.class.isAssignableFrom((Class<?>) content)
							|| tryFindWriter(content) != null;
				}
			}
		} else if (manifest instanceof GenericArrayType) {
			final GenericArrayType gat = (GenericArrayType) manifest;
			return gat.getGenericComponentType() instanceof Class<?>
					&& JsonObject.class.isAssignableFrom((Class<?>) gat.getGenericComponentType())
					|| tryFindWriter(gat.getGenericComponentType()) != null;
		}
		for (ConverterFactory<JsonWriter.WriteObject> wrt : writerFactories) {
			if (wrt.tryCreate(manifest, this) != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if DslJson knows how to deserialize a type.
	 * It will check if a reader for such type exists or can be used.
	 *
	 * @param manifest type to check
	 * @return can read this type from JSON
	 */
	public final boolean canDeserialize(final Type manifest) {
		if (tryFindReader(manifest) != null) {
			return true;
		}
		if (manifest instanceof Class<?>) {
			final Class<?> objectType = (Class<?>) manifest;
			if (JsonObject.class.isAssignableFrom(objectType)) {
				return getObjectReader(objectType) != null;
			}
			if (objectType.isArray()) {
				return !objectType.getComponentType().isArray()
						&& !Collection.class.isAssignableFrom(objectType.getComponentType())
						&& canDeserialize(objectType.getComponentType());
			}
		}
		if (manifest instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) manifest;
			if (pt.getActualTypeArguments().length == 1 && pt.getRawType() instanceof Class<?>) {
				final Class<?> container = (Class<?>) pt.getRawType();
				if (container.isArray() || Collection.class.isAssignableFrom(container)) {
					final Type content = pt.getActualTypeArguments()[0];
					if (tryFindReader(content) != null) {
						return true;
					} else if (content instanceof Class<?>) {
						final Class<?> objectType = (Class<?>) content;
						return JsonObject.class.isAssignableFrom(objectType) && getObjectReader(objectType) != null;
					}
				}
			}
		} else if (manifest instanceof GenericArrayType) {
			final Type content = ((GenericArrayType) manifest).getGenericComponentType();
			if (tryFindReader(content) != null) {
				return true;
			} else if (content instanceof Class<?>) {
				final Class<?> objectType = (Class<?>) content;
				return JsonObject.class.isAssignableFrom(objectType) && getObjectReader(objectType) != null;
			}
		}
		return false;
	}

	/**
	 * Reusable deserialize API.
	 * For maximum performance `JsonReader` should be reused (otherwise small buffer will be allocated for processing)
	 * and `JsonReader.ReadObject` should be prepared (otherwise a lookup will be required).
	 * <p>
	 * This is mostly convenience API since it starts the processing of the JSON by calling getNextToken on JsonReader,
	 * checks for null and calls converter.read(input).
	 *
	 * @param <T> specified type
	 * @param converter target reader
	 * @param input     input JSON
	 * @return deserialized instance
	 * @throws IOException error during deserialization
	 */
	public <T> T deserialize(
			final JsonReader.ReadObject<T> converter,
			final JsonReader<TContext> input) throws IOException {
		if (converter == null) {
			throw new IllegalArgumentException("converter can't be null");
		}
		if (input == null) {
			throw new IllegalArgumentException("input can't be null");
		}
		input.getNextToken();
		if (input.wasNull()) {
			return null;
		}
		return converter.read(input);
	}

	/**
	 * Convenient deserialize API for working with bytes.
	 * Deserialize provided byte input into target object.
	 * <p>
	 * Since JSON is often though of as a series of char,
	 * most libraries will convert inputs into a sequence of chars and do processing on them.
	 * DslJson will treat input as a sequence of bytes which allows for various optimizations.
	 *
	 * @param manifest  target type
	 * @param body      input JSON
	 * @param size      length
	 * @param <TResult> target type
	 * @return deserialized instance
	 * @throws IOException error during deserialization
	 */
	@SuppressWarnings("unchecked")
	public <TResult> TResult deserialize(
			final Class<TResult> manifest,
			final byte[] body,
			final int size) throws IOException {
		if (manifest == null) {
			throw new IllegalArgumentException("manifest can't be null");
		}
		if (body == null) {
			throw new IllegalArgumentException("body can't be null");
		}
		if (isNull(size, body)) {
			return null;
		}
		if (size == 2 && body[0] == '{' && body[1] == '}' && !manifest.isInterface()) {
			try {
				return manifest.newInstance();
			} catch (InstantiationException ignore) {
			} catch (IllegalAccessException e) {
				throw new IOException(e);
			}
		}
		final JsonReader json = newReader(body, size);
		json.getNextToken();
		if (json.wasNull()) {
			return null;
		}
		if (JsonObject.class.isAssignableFrom(manifest)) {
			final JsonReader.ReadJsonObject<JsonObject> objectReader = getObjectReader(manifest);
			if (objectReader != null) {
				if (json.last() == '{') {
					json.getNextToken();
					return (TResult) objectReader.deserialize(json);
				} else throw json.expecting("{");
			}
		}
		final JsonReader.ReadObject<?> simpleReader = tryFindReader(manifest);
		if (simpleReader != null) {
			return (TResult) simpleReader.read(json);
		}
		if (manifest.isArray()) {
			if (json.last() != '[') {
				throw json.expecting("[");
			}
			final Class<?> elementManifest = manifest.getComponentType();
			final List<?> list = deserializeList(elementManifest, body, size);
			if (list == null) {
				return null;
			}
			return (TResult) convertResultToArray(elementManifest, list);
		}
		if (fallback != null) {
			return (TResult) fallback.deserialize(context, manifest, body, size);
		}
		throw createErrorMessage(manifest);
	}

	/**
	 * Deserialize API for working with bytes.
	 * Deserialize provided byte input into target object.
	 * <p>
	 * Since JSON is often though of as a series of char,
	 * most libraries will convert inputs into a sequence of chars and do processing on them.
	 * DslJson will treat input as a sequence of bytes which allows for various optimizations.
	 *
	 * @param manifest target type
	 * @param body     input JSON
	 * @param size     length
	 * @return deserialized instance
	 * @throws IOException error during deserialization
	 */
	public Object deserialize(
			final Type manifest,
			final byte[] body,
			final int size) throws IOException {
		if (manifest instanceof Class<?>) {
			return deserialize((Class<?>) manifest, body, size);
		}
		if (manifest == null) {
			throw new IllegalArgumentException("manifest can't be null");
		}
		if (body == null) {
			throw new IllegalArgumentException("body can't be null");
		}
		if (isNull(size, body)) {
			return null;
		}
		final JsonReader json = newReader(body, size);
		json.getNextToken();
		if (json.wasNull()) {
			return null;
		}
		final Object result = deserializeWith(manifest, json);
		if (result != null) return result;
		if (fallback != null) {
			return fallback.deserialize(context, manifest, body, size);
		}
		throw new IOException("Unable to find reader for provided type: " + manifest + " and fallback serialization is not registered.\n" +
				"Try initializing DslJson with custom fallback in case of unsupported objects or register specified type using registerReader into " + getClass());
	}

	@SuppressWarnings("unchecked")
	private Object deserializeWith(Type manifest, JsonReader json) throws IOException {
		final JsonReader.ReadObject<?> simpleReader = tryFindReader(manifest);
		if (simpleReader != null) {
			return simpleReader.read(json);
		}
		if (manifest instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) manifest;
			if (pt.getActualTypeArguments().length == 1 && pt.getRawType() instanceof Class<?>) {
				final Type content = pt.getActualTypeArguments()[0];
				final Class<?> container = (Class<?>) pt.getRawType();
				if (container.isArray() || Collection.class.isAssignableFrom(container)) {
					if (json.last() != '[') {
						throw json.expecting("[");
					}
					if (json.getNextToken() == ']') {
						if (container.isArray()) {
							returnEmptyArray(content);
						}
						return new ArrayList<Object>(0);
					}
					final JsonReader.ReadObject<?> contentReader = tryFindReader(content);
					if (contentReader != null) {
						final ArrayList<?> result = json.deserializeNullableCollection(contentReader);
						if (container.isArray()) {
							return returnAsArray(content, result);
						}
						return result;
					} else if (content instanceof Class<?>) {
						final Class<?> contentType = (Class<?>) content;
						if (JsonObject.class.isAssignableFrom(contentType)) {
							final JsonReader.ReadJsonObject<JsonObject> objectReader = getObjectReader(contentType);
							if (objectReader != null) {
								final ArrayList<JsonObject> result = json.deserializeNullableCollection(objectReader);
								if (container.isArray()) {
									return result.toArray((Object[]) Array.newInstance(contentType, 0));
								}
								return result;
							}
						}
					}
				}
			}
		} else if (manifest instanceof GenericArrayType) {
			if (json.last() != '[') {
				throw json.expecting("[");
			}
			final Type content = ((GenericArrayType) manifest).getGenericComponentType();
			if (json.getNextToken() == ']') {
				return returnEmptyArray(content);
			}
			final JsonReader.ReadObject<?> contentReader = tryFindReader(content);
			if (contentReader != null) {
				final ArrayList<?> result = json.deserializeNullableCollection(contentReader);
				return returnAsArray(content, result);
			} else if (content instanceof Class<?>) {
				final Class<?> contentType = (Class<?>) content;
				if (JsonObject.class.isAssignableFrom(contentType)) {
					final JsonReader.ReadJsonObject<JsonObject> objectReader = getObjectReader(contentType);
					if (objectReader != null) {
						final ArrayList<JsonObject> result = json.deserializeNullableCollection(objectReader);
						return result.toArray((Object[]) Array.newInstance(contentType, 0));
					}
				}
			}
		}
		return null;
	}

	private static Object returnAsArray(final Type content, final ArrayList<?> result) {
		if (content instanceof Class<?>) {
			return convertResultToArray((Class<?>) content, result);
		}
		if (content instanceof ParameterizedType) {
			final ParameterizedType cpt = (ParameterizedType) content;
			if (cpt.getRawType() instanceof Class<?>) {
				return result.toArray((Object[]) Array.newInstance((Class<?>) cpt.getRawType(), 0));
			}
		}
		return result.toArray();
	}

	private static Object returnEmptyArray(Type content) {
		if (content instanceof Class<?>) {
			return Array.newInstance((Class<?>) content, 0);
		}
		if (content instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) content;
			if (pt.getRawType() instanceof Class<?>) {
				return Array.newInstance((Class<?>) pt.getRawType(), 0);
			}
		}
		return new Object[0];
	}

	private IOException createErrorMessage(final Class<?> manifest) throws IOException {
		final ArrayList<Class<?>> signatures = new ArrayList<Class<?>>();
		findAllSignatures(manifest, signatures);
		for (final Class<?> sig : signatures) {
			if (jsonReaders.containsKey(sig)) {
				if (sig.equals(manifest)) {
					return new IOException("Reader for provided type: " + manifest + " is disabled and fallback serialization is not registered (converter is registered as null).\n" +
							"Try initializing system with custom fallback or don't register null for " + manifest);
				}
				return new IOException("Unable to find reader for provided type: " + manifest + " and fallback serialization is not registered.\n" +
						"Found reader for: " + sig + " so try deserializing into that instead?\n" +
						"Alternatively, try initializing system with custom fallback or register specified type using registerReader into " + getClass());
			}
		}
		return new IOException("Unable to find reader for provided type: " + manifest + " and fallback serialization is not registered.\n" +
				"Try initializing DslJson with custom fallback in case of unsupported objects or register specified type using registerReader into " + getClass());
	}

	/**
	 * Convenient deserialize list API for working with bytes.
	 * Deserialize provided byte input into target object.
	 * <p>
	 * Since JSON is often though of as a series of char,
	 * most libraries will convert inputs into a sequence of chars and do processing on them.
	 * DslJson will treat input as a sequence of bytes which allows for various optimizations.
	 *
	 * @param manifest  target type
	 * @param body      input JSON
	 * @param size      length
	 * @param <TResult> target element type
	 * @return deserialized list instance
	 * @throws IOException error during deserialization
	 */
	@SuppressWarnings("unchecked")
	public <TResult> List<TResult> deserializeList(
			final Class<TResult> manifest,
			final byte[] body,
			final int size) throws IOException {
		if (manifest == null) {
			throw new IllegalArgumentException("manifest can't be null");
		}
		if (body == null) {
			throw new IllegalArgumentException("body can't be null");
		}
		if (isNull(size, body)) {
			return null;
		}
		if (size == 2 && body[0] == '[' && body[1] == ']') {
			return new ArrayList<TResult>(0);
		}
		final JsonReader json = newReader(body, size);
		if (json.getNextToken() != '[') {
			if (json.wasNull()) {
				return null;
			}
			throw json.expecting("[");
		}
		if (json.getNextToken() == ']') {
			return new ArrayList<TResult>(0);
		}
		if (JsonObject.class.isAssignableFrom(manifest)) {
			final JsonReader.ReadJsonObject<JsonObject> reader = getObjectReader(manifest);
			if (reader != null) {
				return (List<TResult>) json.deserializeNullableCollection(reader);
			}
		}
		final JsonReader.ReadObject<?> simpleReader = tryFindReader(manifest);
		if (simpleReader != null) {
			return json.deserializeNullableCollection(simpleReader);
		}
		if (fallback != null) {
			final Object array = Array.newInstance(manifest, 0);
			final TResult[] result = (TResult[]) fallback.deserialize(context, array.getClass(), body, size);
			if (result == null) {
				return null;
			}
			final ArrayList<TResult> list = new ArrayList<TResult>(result.length);
			for (TResult aResult : result) {
				list.add(aResult);
			}
			return list;
		}
		throw createErrorMessage(manifest);
	}

	/**
	 * Convenient deserialize list API for working with streams.
	 * Deserialize provided stream input into target object.
	 * Use buffer for internal conversion from stream into byte[] for partial processing.
	 * <p>
	 * Since JSON is often though of as a series of char,
	 * most libraries will convert inputs into a sequence of chars and do processing on them.
	 * DslJson will treat input as a sequence of bytes which allows for various optimizations.
	 * <p>
	 * When working on InputStream DslJson will process JSON in chunks of byte[] inputs.
	 * Provided buffer will be used as input for partial processing.
	 * <p>
	 * For best performance buffer should be reused.
	 *
	 * @param manifest  target type
	 * @param stream    input JSON
	 * @param buffer    buffer used for InputStream -&gt; byte[] conversion
	 * @param <TResult> target element type
	 * @return deserialized list
	 * @throws IOException error during deserialization
	 */
	@SuppressWarnings("unchecked")
	public <TResult> List<TResult> deserializeList(
			final Class<TResult> manifest,
			final InputStream stream,
			final byte[] buffer) throws IOException {
		if (manifest == null) {
			throw new IllegalArgumentException("manifest can't be null");
		}
		if (stream == null) {
			throw new IllegalArgumentException("stream can't be null");
		}
		if (buffer == null) {
			throw new IllegalArgumentException("buffer can't be null");
		}
		final JsonStreamReader json = newReader(stream, buffer);
		if (json.getNextToken() != '[') {
			if (json.wasNull()) {
				return null;
			}
			throw json.expecting("[");
		}
		if (json.getNextToken() == ']') {
			return new ArrayList<TResult>(0);
		}
		if (JsonObject.class.isAssignableFrom(manifest)) {
			final JsonReader.ReadJsonObject<JsonObject> reader = getObjectReader(manifest);
			if (reader != null) {
				return (List<TResult>) json.deserializeNullableCollection(reader);
			}
		}
		final JsonReader.ReadObject<?> simpleReader = tryFindReader(manifest);
		if (simpleReader != null) {
			return json.deserializeNullableCollection(simpleReader);
		}
		if (fallback != null) {
			final Object array = Array.newInstance(manifest, 0);
			final TResult[] result = (TResult[]) fallback.deserialize(context, array.getClass(), json.streamFromStart());
			if (result == null) {
				return null;
			}
			final ArrayList<TResult> list = new ArrayList<TResult>(result.length);
			for (TResult aResult : result) {
				list.add(aResult);
			}
			return list;
		}
		throw createErrorMessage(manifest);
	}

	/**
	 * Convenient deserialize API for working with streams.
	 * Deserialize provided stream input into target object.
	 * Use buffer for internal conversion from stream into byte[] for partial processing.
	 * <p>
	 * Since JSON is often though of as a series of char,
	 * most libraries will convert inputs into a sequence of chars and do processing on them.
	 * DslJson will treat input as a sequence of bytes which allows for various optimizations.
	 * <p>
	 * When working on InputStream DslJson will process JSON in chunks of byte[] inputs.
	 * Provided buffer will be used as input for partial processing.
	 * <p>
	 * For best performance buffer should be reused.
	 *
	 * @param manifest  target type
	 * @param stream    input JSON
	 * @param buffer    buffer used for InputStream -&gt; byte[] conversion
	 * @param <TResult> target type
	 * @return deserialized instance
	 * @throws IOException error during deserialization
	 */
	@SuppressWarnings("unchecked")
	public <TResult> TResult deserialize(
			final Class<TResult> manifest,
			final InputStream stream,
			final byte[] buffer) throws IOException {
		if (manifest == null) {
			throw new IllegalArgumentException("manifest can't be null");
		}
		if (stream == null) {
			throw new IllegalArgumentException("stream can't be null");
		}
		if (buffer == null) {
			throw new IllegalArgumentException("buffer can't be null");
		}
		final JsonStreamReader json = newReader(stream, buffer);
		json.getNextToken();
		if (json.wasNull()) {
			return null;
		}
		if (JsonObject.class.isAssignableFrom(manifest)) {
			final JsonReader.ReadJsonObject<JsonObject> objectReader = getObjectReader(manifest);
			if (objectReader != null) {
				if (json.last() == '{') {
					json.getNextToken();
					return (TResult) objectReader.deserialize(json);
				} else throw json.expecting("{");
			}
		}
		final JsonReader.ReadObject<?> simpleReader = tryFindReader(manifest);
		if (simpleReader != null) {
			return (TResult) simpleReader.read(json);
		}
		if (manifest.isArray()) {
			if (json.last() != '[') {
				throw json.expecting("[");
			}
			final Class<?> elementManifest = manifest.getComponentType();
			if (json.getNextToken() == ']') {
				return (TResult) Array.newInstance(elementManifest, 0);
			}
			if (JsonObject.class.isAssignableFrom(elementManifest)) {
				final JsonReader.ReadJsonObject<JsonObject> objectReader = getObjectReader(elementManifest);
				if (objectReader != null) {
					List<?> list = json.deserializeNullableCollection(objectReader);
					return (TResult) convertResultToArray(elementManifest, list);
				}
			}
			final JsonReader.ReadObject<?> simpleElementReader = tryFindReader(elementManifest);
			if (simpleElementReader != null) {
				List<?> list = json.deserializeNullableCollection(simpleElementReader);
				return (TResult) convertResultToArray(elementManifest, list);
			}
		}
		if (fallback != null) {
			return (TResult) fallback.deserialize(context, manifest, json.streamFromStart());
		}
		throw createErrorMessage(manifest);
	}

	/**
	 * Deserialize API for working with streams.
	 * Deserialize provided stream input into target object.
	 * Use buffer for internal conversion from stream into byte[] for partial processing.
	 * <p>
	 * Since JSON is often though of as a series of char,
	 * most libraries will convert inputs into a sequence of chars and do processing on them.
	 * DslJson will treat input as a sequence of bytes which allows for various optimizations.
	 * <p>
	 * When working on InputStream DslJson will process JSON in chunks of byte[] inputs.
	 * Provided buffer will be used as input for partial processing.
	 * <p>
	 * For best performance buffer should be reused.
	 *
	 * @param manifest target type
	 * @param stream   input JSON
	 * @param buffer   buffer used for InputStream -&gt; byte[] conversion
	 * @return deserialized instance
	 * @throws IOException error during deserialization
	 */
	public Object deserialize(
			final Type manifest,
			final InputStream stream,
			final byte[] buffer) throws IOException {
		if (manifest instanceof Class<?>) {
			return deserialize((Class<?>) manifest, stream, buffer);
		}
		if (manifest == null) {
			throw new IllegalArgumentException("manifest can't be null");
		}
		if (stream == null) {
			throw new IllegalArgumentException("stream can't be null");
		}
		if (buffer == null) {
			throw new IllegalArgumentException("buffer can't be null");
		}
		final JsonStreamReader json = newReader(stream, buffer);
		json.getNextToken();
		if (json.wasNull()) {
			return null;
		}
		final Object result = deserializeWith(manifest, json);
		if (result != null) return result;
		if (fallback != null) {
			return fallback.deserialize(context, manifest, json.streamFromStart());
		}
		throw new IOException("Unable to find reader for provided type: " + manifest + " and fallback serialization is not registered.\n" +
				"Try initializing DslJson with custom fallback in case of unsupported objects or register specified type using registerReader into " + getClass());
	}

	private static final Iterator EMPTY_ITERATOR = new Iterator() {
		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public void remove() {
		}

		@Override
		public Object next() {
			return null;
		}
	};

	/**
	 * Streaming API for collection deserialization.
	 * DslJson will create iterator based on provided manifest info.
	 * It will attempt to deserialize from stream on each next() invocation.
	 * <p>
	 * Useful for processing very large streams if only one instance from collection is required at once.
	 * <p>
	 * Stream will be processed in chunks of specified buffer byte[].
	 * It will block on reading until buffer is full or end of stream is detected.
	 *
	 * @param manifest  type info
	 * @param stream    JSON data stream
	 * @param buffer    size of processing chunk
	 * @param <TResult> type info
	 * @return Iterator to instances deserialized from input JSON
	 * @throws IOException if reader is not found or there is an error processing input stream
	 */
	@SuppressWarnings("unchecked")
	public <TResult> Iterator<TResult> iterateOver(
			final Class<TResult> manifest,
			final InputStream stream,
			final byte[] buffer) throws IOException {
		if (manifest == null) {
			throw new IllegalArgumentException("manifest can't be null");
		}
		if (stream == null) {
			throw new IllegalArgumentException("stream can't be null");
		}
		if (buffer == null) {
			throw new IllegalArgumentException("buffer can't be null");
		}
		final JsonStreamReader json = newReader(stream, buffer);
		if (json.getNextToken() != '[') {
			if (json.wasNull()) {
				return null;
			}
			throw json.expecting("[");
		}
		if (json.getNextToken() == ']') {
			return EMPTY_ITERATOR;
		}
		if (JsonObject.class.isAssignableFrom(manifest)) {
			final JsonReader.ReadJsonObject<JsonObject> reader = getObjectReader(manifest);
			if (reader != null) {
				return json.iterateOver(reader);
			}
		}
		final JsonReader.ReadObject<?> simpleReader = tryFindReader(manifest);
		if (simpleReader != null) {
			return json.iterateOver(simpleReader);
		}
		if (fallback != null) {
			final Object array = Array.newInstance(manifest, 0);
			final TResult[] result = (TResult[]) fallback.deserialize(context, array.getClass(), json.streamFromStart());
			if (result == null) {
				return null;
			}
			final ArrayList<TResult> list = new ArrayList<TResult>(result.length);
			for (TResult aResult : result) {
				list.add(aResult);
			}
			return list.iterator();
		}
		throw createErrorMessage(manifest);
	}

	private final JsonWriter.WriteObject OBJECT_WRITER = new JsonWriter.WriteObject() {
		@Override
		public void write(JsonWriter writer, Object value) {
			((JsonObject) value).serialize(writer, omitDefaults);
		}
	};

	private final JsonWriter.WriteObject OBJECT_ARRAY_WRITER = new JsonWriter.WriteObject() {
		@Override
		public void write(JsonWriter writer, Object value) {
			serialize(writer, (JsonObject[]) value);
		}
	};

	private static final JsonWriter.WriteObject BOOL_ARRAY_WRITER = new JsonWriter.WriteObject() {
		@Override
		public void write(JsonWriter writer, Object value) {
			BoolConverter.serialize((boolean[]) value, writer);
		}
	};

	private static final JsonWriter.WriteObject INT_ARRAY_WRITER = new JsonWriter.WriteObject() {
		@Override
		public void write(JsonWriter writer, Object value) {
			NumberConverter.serialize((int[]) value, writer);
		}
	};

	private static final JsonWriter.WriteObject LONG_ARRAY_WRITER = new JsonWriter.WriteObject() {
		@Override
		public void write(JsonWriter writer, Object value) {
			NumberConverter.serialize((long[]) value, writer);
		}
	};

	private static final JsonWriter.WriteObject SHORT_ARRAY_WRITER = new JsonWriter.WriteObject() {
		@Override
		public void write(JsonWriter writer, Object value) {
			NumberConverter.serialize((short[]) value, writer);
		}
	};

	private static final JsonWriter.WriteObject FLOAT_ARRAY_WRITER = new JsonWriter.WriteObject() {
		@Override
		public void write(JsonWriter writer, Object value) {
			NumberConverter.serialize((float[]) value, writer);
		}
	};

	private static final JsonWriter.WriteObject DOUBLE_ARRAY_WRITER = new JsonWriter.WriteObject() {
		@Override
		public void write(JsonWriter writer, Object value) {
			NumberConverter.serialize((double[]) value, writer);
		}
	};

	private static final JsonWriter.WriteObject BYTE_ARRAY_WRITER = new JsonWriter.WriteObject() {
		@Override
		public void write(JsonWriter writer, Object value) {
			BinaryConverter.serialize((byte[]) value, writer);
		}
	};

	private static final JsonWriter.WriteObject CHAR_ARRAY_WRITER = new JsonWriter.WriteObject() {
		@Override
		public void write(JsonWriter writer, Object value) {
			StringConverter.serialize(new String((char[]) value), writer);
		}
	};

	@SuppressWarnings("unchecked")
	private JsonWriter.WriteObject getOrCreateWriter(final Object instance, final Class<?> instanceManifest) throws IOException {
		if (instance instanceof JsonObject) {
			return OBJECT_WRITER;
		}
		if (instance instanceof JsonObject[]) {
			return OBJECT_ARRAY_WRITER;
		}
		final Class<?> manifest = instanceManifest != null ? instanceManifest : instance.getClass();
		if (instanceManifest != null) {
			if (JsonObject.class.isAssignableFrom(manifest)) {
				return OBJECT_WRITER;
			}
		}
		final JsonWriter.WriteObject simpleWriter = tryFindWriter(manifest);
		if (simpleWriter != null) {
			return simpleWriter;
		}
		if (manifest.isArray()) {
			final Class<?> elementManifest = manifest.getComponentType();
			if (elementManifest.isPrimitive()) {
				if (elementManifest == boolean.class) {
					return BOOL_ARRAY_WRITER;
				} else if (elementManifest == int.class) {
					return INT_ARRAY_WRITER;
				} else if (elementManifest == long.class) {
					return LONG_ARRAY_WRITER;
				} else if (elementManifest == byte.class) {
					return BYTE_ARRAY_WRITER;
				} else if (elementManifest == short.class) {
					return SHORT_ARRAY_WRITER;
				} else if (elementManifest == float.class) {
					return FLOAT_ARRAY_WRITER;
				} else if (elementManifest == double.class) {
					return SHORT_ARRAY_WRITER;
				} else {
					return CHAR_ARRAY_WRITER;
				}
			} else {
				final JsonWriter.WriteObject elementWriter = tryFindWriter(elementManifest);
				if (elementWriter != null) {
					//TODO: cache writer for next lookup
					return new JsonWriter.WriteObject() {
						@Override
						public void write(JsonWriter writer, Object value) {
							writer.serialize((Object[]) value, elementWriter);
						}
					};
				}
			}
		}
		if (instance instanceof Collection || Collection.class.isAssignableFrom(manifest)) {
			return new JsonWriter.WriteObject() {
				@Override
				public void write(JsonWriter writer, final Object value) {
					final Collection items = (Collection) value;
					Class<?> baseType = null;
					final Iterator iterator = items.iterator();
					//TODO: pick lowest common denominator!?
					do {
						final Object item = iterator.next();
						if (item != null) {
							Class<?> elementType = item.getClass();
							if (elementType != baseType) {
								if (baseType == null || elementType.isAssignableFrom(baseType)) {
									baseType = elementType;
								}
							}
						}
					} while (iterator.hasNext());
					if (baseType == null) {
						writer.writeByte(JsonWriter.ARRAY_START);
						writer.writeNull();
						for (int i = 1; i < items.size(); i++) {
							writer.writeAscii(",null");
						}
						writer.writeByte(JsonWriter.ARRAY_END);
					} else if (JsonObject.class.isAssignableFrom(baseType)) {
						serialize(writer, (Collection<JsonObject>) items);
					} else {
						final JsonWriter.WriteObject elementWriter = tryFindWriter(baseType);
						if (elementWriter != null) {
							writer.serialize(items, elementWriter);
						} else if (fallback != null) {
							final ByteArrayOutputStream stream = new ByteArrayOutputStream();
							stream.reset();
							try {
								fallback.serialize(value, stream);
							} catch (IOException ex) {
								throw new SerializationException(ex);
							}
							writer.writeAscii(stream.toByteArray());
						} else {
							throw new SerializationException("Unable to serialize provided object. Failed to find serializer for: " + items.getClass());
						}
					}
				}
			};
		}
		throw new IOException("Unable to serialize provided object. Failed to find serializer for: " + manifest);
	}

	/**
	 * Streaming API for collection serialization.
	 * <p>
	 * It will iterate over entire iterator and serialize each instance into target output stream.
	 * After each instance serialization it will copy JSON into target output stream.
	 * During each serialization reader will be looked up based on next() instance which allows
	 * serializing collection with different types.
	 * If collection contains all instances of the same type, prefer the other streaming API.
	 * <p>
	 * If reader is not found an IOException will be thrown
	 * <p>
	 * If JsonWriter is provided it will be used, otherwise a new instance will be internally created.
	 *
	 * @param iterator input data
	 * @param stream   target JSON stream
	 * @param writer   temporary buffer for serializing a single item. Can be null
	 * @param <T>      input data type
	 * @throws IOException reader is not found, there is an error during serialization or problem with writing to target stream
	 */
	@SuppressWarnings("unchecked")
	public <T> void iterateOver(
			final Iterator<T> iterator,
			final OutputStream stream,
			final JsonWriter writer) throws IOException {
		if (iterator == null) {
			throw new IllegalArgumentException("iterator can't be null");
		}
		if (stream == null) {
			throw new IllegalArgumentException("stream can't be null");
		}
		stream.write(JsonWriter.ARRAY_START);
		if (!iterator.hasNext()) {
			stream.write(JsonWriter.ARRAY_END);
			return;
		}
		final JsonWriter buffer = writer == null ? new JsonWriter(this) : writer;
		T item = iterator.next();
		Class<?> lastManifest = null;
		JsonWriter.WriteObject lastWriter = null;
		if (item != null) {
			lastManifest = item.getClass();
			lastWriter = getOrCreateWriter(item, lastManifest);
			buffer.reset();
			try {
				lastWriter.write(buffer, item);
			} catch (Exception e) {
				throw new IOException(e);
			}
			buffer.toStream(stream);
		} else {
			stream.write(NULL);
		}
		while (iterator.hasNext()) {
			stream.write(JsonWriter.COMMA);
			item = iterator.next();
			if (item != null) {
				final Class<?> currentManifest = item.getClass();
				if (lastWriter == null || lastManifest == null || !lastManifest.equals(currentManifest)) {
					lastManifest = currentManifest;
					lastWriter = getOrCreateWriter(item, lastManifest);
				}
				buffer.reset();
				try {
					lastWriter.write(buffer, item);
				} catch (Exception e) {
					throw new IOException(e);
				}
				buffer.toStream(stream);
			} else {
				stream.write(NULL);
			}
		}
		stream.write(JsonWriter.ARRAY_END);
	}

	/**
	 * Streaming API for collection serialization.
	 * <p>
	 * It will iterate over entire iterator and serialize each instance into target output stream.
	 * After each instance serialization it will copy JSON into target output stream.
	 * <p>
	 * If reader is not found an IOException will be thrown
	 * <p>
	 * If JsonWriter is provided it will be used, otherwise a new instance will be internally created.
	 *
	 * @param iterator input data
	 * @param manifest type of elements in collection
	 * @param stream   target JSON stream
	 * @param writer   temporary buffer for serializing a single item. Can be null
	 * @param <T>      input data type
	 * @throws IOException reader is not found, there is an error during serialization or problem with writing to target stream
	 */
	@SuppressWarnings("unchecked")
	public <T> void iterateOver(
			final Iterator<T> iterator,
			final Class<T> manifest,
			final OutputStream stream,
			final JsonWriter writer) throws IOException {
		if (iterator == null) {
			throw new IllegalArgumentException("iterator can't be null");
		}
		if (manifest == null) {
			throw new IllegalArgumentException("manifest can't be null");
		}
		if (stream == null) {
			throw new IllegalArgumentException("stream can't be null");
		}
		final JsonWriter buffer = writer == null ? new JsonWriter(this) : writer;
		final JsonWriter.WriteObject instanceWriter = getOrCreateWriter(null, manifest);
		stream.write(JsonWriter.ARRAY_START);
		T item = iterator.next();
		if (item != null) {
			buffer.reset();
			try {
				instanceWriter.write(buffer, item);
			} catch (Exception e) {
				throw new IOException(e);
			}
			buffer.toStream(stream);
		} else {
			stream.write(NULL);
		}
		while (iterator.hasNext()) {
			stream.write(JsonWriter.COMMA);
			item = iterator.next();
			if (item != null) {
				buffer.reset();
				try {
					instanceWriter.write(buffer, item);
				} catch (Exception e) {
					throw new IOException(e);
				}
				buffer.toStream(stream);
			} else {
				stream.write(NULL);
			}
		}
		stream.write(JsonWriter.ARRAY_END);
	}

	public <T extends JsonObject> void serialize(final JsonWriter writer, final T[] array) {
		if (array == null) {
			writer.writeNull();
			return;
		}
		writer.writeByte(JsonWriter.ARRAY_START);
		if (array.length != 0) {
			T item = array[0];
			if (item != null) {
				item.serialize(writer, omitDefaults);
			} else {
				writer.writeNull();
			}
			for (int i = 1; i < array.length; i++) {
				writer.writeByte(JsonWriter.COMMA);
				item = array[i];
				if (item != null) {
					item.serialize(writer, omitDefaults);
				} else {
					writer.writeNull();
				}
			}
		}
		writer.writeByte(JsonWriter.ARRAY_END);
	}

	public <T extends JsonObject> void serialize(final JsonWriter writer, final T[] array, final int len) {
		if (writer == null) {
			throw new IllegalArgumentException("writer can't be null");
		}
		if (array == null) {
			writer.writeNull();
			return;
		}
		writer.writeByte(JsonWriter.ARRAY_START);
		if (len != 0) {
			T item = array[0];
			if (item != null) {
				item.serialize(writer, omitDefaults);
			} else {
				writer.writeNull();
			}
			for (int i = 1; i < len; i++) {
				writer.writeByte(JsonWriter.COMMA);
				item = array[i];
				if (item != null) {
					item.serialize(writer, omitDefaults);
				} else {
					writer.writeNull();
				}
			}
		}
		writer.writeByte(JsonWriter.ARRAY_END);
	}

	public <T extends JsonObject> void serialize(final JsonWriter writer, final List<T> list) {
		if (writer == null) {
			throw new IllegalArgumentException("writer can't be null");
		}
		if (list == null) {
			writer.writeNull();
			return;
		}
		writer.writeByte(JsonWriter.ARRAY_START);
		if (list.size() != 0) {
			T item = list.get(0);
			if (item != null) {
				item.serialize(writer, omitDefaults);
			} else {
				writer.writeNull();
			}
			for (int i = 1; i < list.size(); i++) {
				writer.writeByte(JsonWriter.COMMA);
				item = list.get(i);
				if (item != null) {
					item.serialize(writer, omitDefaults);
				} else {
					writer.writeNull();
				}
			}
		}
		writer.writeByte(JsonWriter.ARRAY_END);
	}

	public <T extends JsonObject> void serialize(final JsonWriter writer, final Collection<T> collection) {
		if (writer == null) {
			throw new IllegalArgumentException("writer can't be null");
		}
		if (collection == null) {
			writer.writeNull();
			return;
		}
		writer.writeByte(JsonWriter.ARRAY_START);
		if (!collection.isEmpty()) {
			final Iterator<T> it = collection.iterator();
			T item = it.next();
			if (item != null) {
				item.serialize(writer, omitDefaults);
			} else {
				writer.writeNull();
			}
			while (it.hasNext()) {
				writer.writeByte(JsonWriter.COMMA);
				item = it.next();
				if (item != null) {
					item.serialize(writer, omitDefaults);
				} else {
					writer.writeNull();
				}
			}
		}
		writer.writeByte(JsonWriter.ARRAY_END);
	}

	/**
	 * Generic serialize API.
	 * Based on provided type manifest converter will be chosen.
	 * If converter is not found method will return false.
	 * <p>
	 * Resulting JSON will be written into provided writer argument.
	 * In case of successful serialization true will be returned.
	 * <p>
	 * For best performance writer argument should be reused.
	 *
	 * @param writer   where to write resulting JSON
	 * @param manifest type manifest
	 * @param value    instance to serialize
	 * @param <T>      type
	 * @return successful serialization
	 */
	@SuppressWarnings("unchecked")
	public <T> boolean serialize(final JsonWriter writer, final Type manifest, final Object value) {
		if (writer == null) {
			throw new IllegalArgumentException("writer can't be null");
		}
		if (value == null) {
			writer.writeNull();
			return true;
		}
		if (value instanceof JsonObject) {
			((JsonObject) value).serialize(writer, omitDefaults);
			return true;
		}
		if (value instanceof JsonObject[]) {
			serialize(writer, (JsonObject[]) value);
			return true;
		}
		final JsonWriter.WriteObject<Object> simpleWriter = (JsonWriter.WriteObject<Object>) tryFindWriter(manifest);
		if (simpleWriter != null) {
			simpleWriter.write(writer, value);
			return true;
		}
		Class<?> container = null;
		if (manifest instanceof Class<?>) {
			container = (Class<?>) manifest;
		}
		if (container != null && container.isArray()) {
			if (Array.getLength(value) == 0) {
				writer.writeAscii("[]");
				return true;
			}
			final Class<?> elementManifest = container.getComponentType();
			if (elementManifest.isPrimitive()) {
				if (elementManifest == boolean.class) {
					BoolConverter.serialize((boolean[]) value, writer);
				} else if (elementManifest == int.class) {
					NumberConverter.serialize((int[]) value, writer);
				} else if (elementManifest == long.class) {
					NumberConverter.serialize((long[]) value, writer);
				} else if (elementManifest == byte.class) {
					BinaryConverter.serialize((byte[]) value, writer);
				} else if (elementManifest == short.class) {
					NumberConverter.serialize((short[]) value, writer);
				} else if (elementManifest == float.class) {
					NumberConverter.serialize((float[]) value, writer);
				} else if (elementManifest == double.class) {
					NumberConverter.serialize((double[]) value, writer);
				} else if (elementManifest == char.class) {
					//TODO? char[] !?
					StringConverter.serialize(new String((char[]) value), writer);
				} else {
					return false;
				}
				return true;
			} else {
				final JsonWriter.WriteObject<Object> elementWriter = (JsonWriter.WriteObject<Object>) tryFindWriter(elementManifest);
				if (elementWriter != null) {
					writer.serialize((Object[]) value, elementWriter);
					return true;
				}
			}
		}
		if (value instanceof Collection) {
			final Collection items = (Collection) value;
			if (items.isEmpty()) {
				writer.writeAscii("[]");
				return true;
			}
			Class<?> baseType = null;
			final Iterator iterator = items.iterator();
			//TODO: pick lowest common denominator!?
			do {
				final Object item = iterator.next();
				if (item != null) {
					Class<?> elementType = item.getClass();
					if (elementType != baseType) {
						if (baseType == null || elementType.isAssignableFrom(baseType)) {
							baseType = elementType;
						}
					}
				}
			} while (iterator.hasNext());
			if (baseType == null) {
				writer.writeByte(JsonWriter.ARRAY_START);
				writer.writeNull();
				for (int i = 1; i < items.size(); i++) {
					writer.writeAscii(",null");
				}
				writer.writeByte(JsonWriter.ARRAY_END);
				return true;
			}
			if (JsonObject.class.isAssignableFrom(baseType)) {
				serialize(writer, (Collection<JsonObject>) items);
				return true;
			}
			final JsonWriter.WriteObject<Object> elementWriter = (JsonWriter.WriteObject<Object>) tryFindWriter(baseType);
			if (elementWriter != null) {
				writer.serialize(items, elementWriter);
				return true;
			}
		}
		return false;
	}

	private static final byte[] NULL = new byte[]{'n', 'u', 'l', 'l'};

	/**
	 * Convenient serialize API.
	 * In most cases JSON is serialized into target `OutputStream`.
	 * This method will create a new instance of `JsonWriter` and serialize JSON into it.
	 * At the end `JsonWriter` will be copied into resulting stream.
	 *
	 * @param value  instance to serialize
	 * @param stream where to write resulting JSON
	 * @throws IOException error when unable to serialize instance
	 */
	public final void serialize(final Object value, final OutputStream stream) throws IOException {
		if (stream == null) {
			throw new IllegalArgumentException("stream can't be null");
		}
		if (value == null) {
			stream.write(NULL);
			return;
		}
		final JsonWriter jw = new JsonWriter(this);
		final Class<?> manifest = value.getClass();
		if (!serialize(jw, manifest, value)) {
			if (fallback == null) {
				throw new IOException("Unable to serialize provided object. Failed to find serializer for: " + manifest);
			}
			fallback.serialize(value, stream);
		} else {
			jw.toStream(stream);
		}
	}

	/**
	 * Main serialization API.
	 * Convert object instance into JSON.
	 * <p>
	 * JsonWriter contains a growable byte[] where JSON will be serialized.
	 * After serialization JsonWriter can be copied into OutputStream or it's byte[] can be obtained
	 * <p>
	 * For best performance reuse `JsonWriter` or even better call `JsonWriter.WriteObject` directly
	 *
	 * @param writer where to write resulting JSON
	 * @param value  object instance to serialize
	 * @throws IOException error when unable to serialize instance
	 */
	public final void serialize(final JsonWriter writer, final Object value) throws IOException {
		if (writer == null) {
			throw new IllegalArgumentException("writer can't be null");
		}
		if (value == null) {
			writer.writeNull();
			return;
		}
		final Class<?> manifest = value.getClass();
		if (!serialize(writer, manifest, value)) {
			if (fallback == null) {
				throw new IOException("Unable to serialize provided object. Failed to find serializer for: " + manifest);
			}
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			fallback.serialize(value, stream);
			writer.writeAscii(stream.toByteArray());
		}
	}
}
