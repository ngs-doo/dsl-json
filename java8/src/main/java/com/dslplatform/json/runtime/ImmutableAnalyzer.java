package com.dslplatform.json.runtime;

import com.dslplatform.json.*;
import com.dslplatform.json.processor.Analysis;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public abstract class ImmutableAnalyzer {

	private static final Set<String> objectMethods = new HashSet<>();
	private static final ParameterNameExtractor parameterNameExtractor;
	static {
		for (Method m : Object.class.getMethods()) {
			if (m.getParameterTypes().length == 0) {
				objectMethods.add(m.getName());
			}
		}

		List<ParameterNameExtractor> extractors = new ArrayList<>();
		if (isClassAvailable("java.lang.reflect.Parameter")) {
			extractors.add(new Java8ParameterNameExtractor());
		}
		if (isClassAvailable("com.thoughtworks.paranamer.Paranamer")) {
			extractors.add(new ParanamerParameterNameExtractor());
		}
		parameterNameExtractor = new CompositeParameterNameExtractor(extractors);
	}

	private static boolean isClassAvailable(String className) {
		try {
			Class.forName(className);
			return true;
		} catch (NoClassDefFoundError | ClassNotFoundException ignore) {
			return false;
		}
	}

	@Nullable
	public static String[] extractNames(Method factory) {
		if (factory == null) throw new IllegalArgumentException("factory can't be null");
		return parameterNameExtractor.extractNames(factory);
	}

	@Nullable
	public static String[] extractNames(Constructor<?> ctor) {
		if (ctor == null) throw new IllegalArgumentException("ctor can't be null");
		return parameterNameExtractor.extractNames(ctor);
	}

	private static class LazyImmutableDescription implements JsonWriter.WriteObject, JsonReader.ReadObject {

		private final DslJson json;
		private final Type type;
		private JsonWriter.WriteObject encoder;
		private JsonReader.ReadObject decoder;
		volatile ImmutableDescription resolved;

		LazyImmutableDescription(DslJson json, Type type) {
			this.json = json;
			this.type = type;
		}

		private boolean checkSignatureNotFound() {
			int i = 0;
			ImmutableDescription local = null;
			while (i < 50) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					throw new ConfigurationException(e);
				}
				local = resolved;
				if (local != null) {
					encoder = local;
					decoder = local;
					break;
				}
				i++;
			}
			return local == null;
		}

		@Override
		public Object read(final JsonReader reader) throws IOException {
			if (decoder == null) {
				if (checkSignatureNotFound()) {
					final JsonReader.ReadObject tmp = json.tryFindReader(type);
					if (tmp == null || tmp == this) {
						throw new ConfigurationException("Unable to find reader for " + type);
					}
					decoder = tmp;
				}
			}
			return decoder.read(reader);
		}

		@Override
		public void write(final JsonWriter writer, @Nullable final Object value) {
			if (encoder == null) {
				if (checkSignatureNotFound()) {
					final JsonWriter.WriteObject tmp = json.tryFindWriter(type);
					if (tmp == null || tmp == this) {
						throw new ConfigurationException("Unable to find writer for " + type);
					}
					encoder = tmp;
				}
			}
			encoder.write(writer, value);
		}
	}

	public static final DslJson.ConverterFactory<ImmutableDescription> CONVERTER = new DslJson.ConverterFactory<ImmutableDescription>() {
		@Nullable
		@Override
		public ImmutableDescription tryCreate(Type manifest, DslJson dslJson) {
			if (manifest instanceof Class<?>) {
				return analyze(manifest, (Class<?>) manifest, dslJson);
			}
			if (manifest instanceof ParameterizedType) {
				final ParameterizedType pt = (ParameterizedType) manifest;
				if (pt.getActualTypeArguments().length == 1) {
					return analyze(manifest, (Class<?>) pt.getRawType(), dslJson);
				}
			}
			return null;
		}
	};

	@Nullable
	private static <T> ImmutableDescription<T> analyze(final Type manifest, final Class<T> raw, final DslJson<?> json) {
		if (raw.isArray()
				|| Collection.class.isAssignableFrom(raw)
				|| (raw.getModifiers() & Modifier.ABSTRACT) != 0
				|| raw.isInterface()
				|| (raw.getDeclaringClass() != null && (raw.getModifiers() & Modifier.STATIC) == 0)
				|| (raw.getModifiers() & Modifier.PUBLIC) == 0) {
			return null;
		}
		final Constructor<?> ctor = findBestCtor(raw, json);
		final Method factory = findBestFactory(raw, json);
		if (ctor == null && factory == null) return null;
		final Type[] paramTypes = factory != null ? factory.getGenericParameterTypes() : ctor.getGenericParameterTypes();
		if (paramTypes.length == 0
			|| paramTypes.length == 1 && ObjectAnalyzer.matchesContext(paramTypes[0], json)) {
			return null;
		}
		String[] names = factory != null ? extractNames(factory) : extractNames(ctor);
		final Map<Type, Integer> typeIndex;
		if (names == null) {
			typeIndex = new HashMap<>();
			for (Type p : paramTypes) {
				//only allow registration without name when all types are different
				//TODO: ideally we could allow some ad hoc heuristics to test which value goes to which parameter.... but meh
				if (typeIndex.containsKey(p)) return null;
				typeIndex.put(p, typeIndex.size());
			}
		} else {
			typeIndex = null;
		}
		final LazyImmutableDescription lazy = new LazyImmutableDescription(json, manifest);
		final JsonWriter.WriteObject oldWriter = json.registerWriter(manifest, lazy);
		final JsonReader.ReadObject oldReader = json.registerReader(manifest, lazy);
		final LinkedHashMap<String, JsonWriter.WriteObject> fields = new LinkedHashMap<>();
		final LinkedHashMap<String, JsonWriter.WriteObject> methods = new LinkedHashMap<>();
		final GenericsMapper genericMappings = GenericsMapper.create(manifest, raw);
		final Object[] defArgs = findDefaultArguments(paramTypes, raw, genericMappings, json);
		final int contextCount = json.context != null && Arrays.asList(defArgs).contains(json.context) ? 1 : 0;
		final LinkedHashMap<String, Field> matchingFields = new LinkedHashMap<>();
		for (final Field f : raw.getFields()) {
			if (isPublicFinalNonStatic(f.getModifiers())) {
				matchingFields.put(f.getName(), f);
			}
		}
		final LinkedHashMap<String, Method> matchingMethods = new LinkedHashMap<>();
		for (final Method mget : raw.getMethods()) {
			if (mget.getParameterTypes().length != 0) continue;
			final String name = Analysis.beanOrActualName(mget.getName());
			if (isPublicNonStatic(mget.getModifiers()) && !name.contains("$") && !objectMethods.contains(name)) {
				matchingMethods.put(name, mget);
			}
		}
		final JsonWriter.WriteObject[] writeProps;
		final int attributesCount = paramTypes.length - contextCount;
		if (contextCount == 1 && paramTypes.length == 1) {
			return unregister(manifest, json, oldWriter, oldReader);
		}
		if (names != null) {
			if (matchingFields.size() == attributesCount) {
				for (int i = 0; i < paramTypes.length; i++) {
					final Field f = matchingFields.get(names[i]);
					if (f == null && json.context != null && json.context == defArgs[i]) continue;
					if (f == null || !analyzeField(json, paramTypes[i], fields, f, raw, genericMappings)) {
						return unregister(manifest, json, oldWriter, oldReader);
					}
				}
				writeProps = fields.values().toArray(new JsonWriter.WriteObject[0]);
			} else {
				for (int i = 0; i < paramTypes.length; i++) {
					final Method m = matchingMethods.get(names[i]);
					if (m == null && json.context != null && json.context == defArgs[i]) continue;
					if (m == null || !analyzeMethod(m, json, paramTypes[i], names[i], methods, raw, genericMappings)) {
						return unregister(manifest, json, oldWriter, oldReader);
					}
				}
				writeProps = methods.values().toArray(new JsonWriter.WriteObject[0]);
			}
		} else {
			names = new String[typeIndex.size()];
			if (matchingFields.size() == attributesCount) {
				for (Type p : paramTypes) {
					for (Map.Entry<String, Field> kv : matchingFields.entrySet()) {
						final Field f = kv.getValue();
						if (analyzeField(json, p, fields, f, raw, genericMappings)) {
							matchingFields.remove(kv.getKey());
							names[typeIndex.get(p)] = kv.getKey();
							break;
						}
					}
				}
				if (fields.size() != attributesCount) {
					return unregister(manifest, json, oldWriter, oldReader);
				}
				writeProps = fields.values().toArray(new JsonWriter.WriteObject[0]);
			} else {
				for (Type p : paramTypes) {
					for (Map.Entry<String, Method> kv : matchingMethods.entrySet()) {
						final Method m = kv.getValue();
						if (analyzeMethod(m, json, p, kv.getKey(), methods, raw, genericMappings)) {
							matchingMethods.remove(kv.getKey());
							names[typeIndex.get(p)] = kv.getKey();
							break;
						}
					}
				}
				if (methods.size() != attributesCount) {
					return unregister(manifest, json, oldWriter, oldReader);
				}
				writeProps = methods.values().toArray(new JsonWriter.WriteObject[0]);
			}
		}
		final DecodePropertyInfo<JsonReader.ReadObject>[] readProps = new DecodePropertyInfo[attributesCount];
		int idx = 0;
		for (int i = 0; i < paramTypes.length; i++) {
			final Type concreteType = genericMappings.makeConcrete(paramTypes[i], raw);
			if (json.context != null && defArgs[i] == json.context) continue;
			readProps[idx++] = new DecodePropertyInfo<>(names[i], false, false, i, false, new WriteMember(json, concreteType, factory != null ? factory : ctor));
		}
		final Settings.Function<Object[], T> instanceFactory = factory != null
				? new Settings.Function<Object[], T>() {
					@Override
					public T apply(@Nullable Object[] args) {
						try {
							return (T) factory.invoke(null, args);
						} catch (Exception ex) {
							throw new ConfigurationException("Unable to create an instance of " + raw);
						}
					}
				}
				: new Settings.Function<Object[], T>() {
					@Override
					public T apply(@Nullable Object[] args) {
						try {
							return raw.cast(ctor.newInstance(args));
						} catch (Exception ex) {
							throw new ConfigurationException("Unable to create an instance of " + raw);
						}
					}
				};
		final ImmutableDescription<T> converter = new ImmutableDescription<>(
				manifest,
				defArgs,
				instanceFactory,
				writeProps,
				readProps,
				!json.omitDefaults,
				true);
		json.registerWriter(manifest, converter);
		json.registerReader(manifest, converter);
		lazy.resolved = converter;
		return converter;
	}

	static @Nullable <T> Constructor<?> findBestCtor(Class<?> raw, DslJson<T> json) {
		final Map<Class<? extends Annotation>, Boolean> creatorMarkers = json.getRegisteredCreatorMarkers();
		final ArrayList<Constructor<?>> ctors = new ArrayList<>();
		boolean hasCtorWithMarker = false;
		for (Constructor<?> ctor : raw.getDeclaredConstructors()) {
			final boolean isPublic = (ctor.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC;
			boolean hasMarker = false;
			if (!creatorMarkers.isEmpty()) {
				for (Map.Entry<Class<? extends Annotation>, Boolean> kv : creatorMarkers.entrySet()) {
					if (ctor.getAnnotation(kv.getKey()) != null && (isPublic || kv.getValue())) {
						if (!isPublic) {
							try {
								ctor.setAccessible(true);
							} catch (Exception ex) {
								throw new ConfigurationException("Unable to promote access for private constructor " + ctor + ". Please check environment setup, or set marker on public constructor", ex);
							}
						}
						hasMarker = true;
						break;
					}
				}
			}
			if (hasMarker) {
				hasCtorWithMarker = true;
				ctors.add(0, ctor);
			} else if (isPublic) {
				ctors.add(ctor);
			}
		}
		return !hasCtorWithMarker && ctors.size() != 1 ? null : ctors.get(0);
	}

	private static @Nullable <T> Method findBestFactory(Class<?> raw, DslJson<T> json) {
		final Map<Class<? extends Annotation>, Boolean> creatorMarkers = json.getRegisteredCreatorMarkers();
		if (creatorMarkers.isEmpty()) return null;
		for (final Method factory : raw.getDeclaredMethods()) {
			final int modifiers = factory.getModifiers();
			if ((modifiers & Modifier.STATIC) != Modifier.STATIC || !raw.isAssignableFrom(factory.getReturnType())) continue;
			final boolean isPublic = (modifiers & Modifier.PUBLIC) == Modifier.PUBLIC;
			for (Map.Entry<Class<? extends Annotation>, Boolean> kv : creatorMarkers.entrySet()) {
				if (factory.getAnnotation(kv.getKey()) != null && (isPublic || kv.getValue())) {
					if (!isPublic) {
						try {
							factory.setAccessible(true);
						} catch (Exception ex) {
							throw new ConfigurationException("Unable to promote access for private factory " + factory + ". Please check environment setup, or set marker on public method", ex);
						}
					}
					return factory;
				}
			}
		}
		return null;
	}

	private static class WriteMember implements JsonReader.ReadObject {
		private final DslJson json;
		private final Type type;
		private final AccessibleObject ctorOrMethod;
		private JsonReader.ReadObject decoder;

		WriteMember(final DslJson json, final Type type, final AccessibleObject ctorOrMethod) {
			this.json = json;
			this.type = type;
			this.ctorOrMethod = ctorOrMethod;
		}

		@Override
		public Object read(final JsonReader reader) throws IOException {
			if (decoder == null) {
				decoder = json.tryFindReader(type);
				if (decoder == null) {
					throw new ConfigurationException("Unable to find reader for " + type + " on " + ctorOrMethod);
				}
			}
			return decoder.read(reader);
		}
	}

	@Nullable
	private static <T> ImmutableDescription<T> unregister(Type manifest, DslJson<?> json, @Nullable JsonWriter.WriteObject oldWriter, @Nullable JsonReader.ReadObject oldReader) {
		json.registerWriter(manifest, oldWriter);
		json.registerReader(manifest, oldReader);
		return null;
	}

	private static Object[] findDefaultArguments(
			final Type[] paramTypes,
			final Class<?> raw,
			final GenericsMapper genericMappings,
			final DslJson json) {
		final Object[] defArgs = new Object[paramTypes.length];
		for (int i = 0; i < paramTypes.length; i++) {
			final Type concreteType = genericMappings.makeConcrete(paramTypes[i], raw);
			if (json.context != null && json.context.getClass().equals(concreteType)) {
				defArgs[i] = json.context;
			} else {
				defArgs[i] = json.getDefault(concreteType);
			}
		}
		return defArgs;
	}

	private static boolean analyzeField(
			final DslJson json,
			final Type paramType,
			final LinkedHashMap<String, JsonWriter.WriteObject> found,
			final Field field,
			final Class<?> raw,
			final GenericsMapper genericMappings) {
		final Type type = field.getGenericType();
		final Type concreteType = genericMappings.makeConcrete(type, raw);
		final boolean isUnknown = Generics.isUnknownType(type);
		if (type.equals(paramType)
				&& (isUnknown || json.tryFindWriter(concreteType) != null && json.tryFindReader(concreteType) != null)) {
			found.put(
					field.getName(),
					Settings.createEncoder(
							new Reflection.ReadField(field),
							field.getName(),
							json,
							isUnknown ? null : concreteType));
			return true;
		}
		return false;
	}

	private static boolean analyzeMethod(
			final Method mget,
			final DslJson json,
			final Type paramType,
			final String name,
			final HashMap<String, JsonWriter.WriteObject> found, Class<?> raw,
			final GenericsMapper genericMappings) {
		final Type type = mget.getGenericReturnType();
		final Type concreteType = genericMappings.makeConcrete(type, raw);
		final boolean isUnknown = Generics.isUnknownType(type);
		if (type.equals(paramType)
				&& (isUnknown || json.tryFindWriter(concreteType) != null && json.tryFindReader(concreteType) != null)) {
			found.put(
					name,
					Settings.createEncoder(
							new Reflection.ReadMethod(mget),
							name,
							json,
							isUnknown ? null : concreteType));
			return true;
		}
		return false;
	}

	private static boolean isPublicFinalNonStatic(final int modifiers) {
		return (modifiers & Modifier.PUBLIC) != 0
				&& (modifiers & Modifier.TRANSIENT) == 0
				&& (modifiers & Modifier.NATIVE) == 0
				&& (modifiers & Modifier.FINAL) != 0
				&& (modifiers & Modifier.STATIC) == 0;
	}


	private static boolean isPublicNonStatic(final int modifiers) {
		return (modifiers & Modifier.PUBLIC) != 0
				&& (modifiers & Modifier.TRANSIENT) == 0
				&& (modifiers & Modifier.NATIVE) == 0
				&& (modifiers & Modifier.STATIC) == 0;
	}
}
