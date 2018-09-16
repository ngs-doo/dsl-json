package com.dslplatform.json.runtime;

import com.dslplatform.json.*;

import java.io.IOException;
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
		} catch (ClassNotFoundException e) {
			return false;
		}
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
					throw new SerializationException(e);
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
						throw new SerializationException("Unable to find reader for " + type);
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
						throw new SerializationException("Unable to find writer for " + type);
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
		final ArrayList<Constructor<?>> ctors = new ArrayList<>();
		for (Constructor<?> ctor : raw.getDeclaredConstructors()) {
			if ((ctor.getModifiers() & Modifier.PUBLIC) == 1) {
				ctors.add(ctor);
			}
		}
		if (ctors.size() != 1) return null;
		final Constructor<?> ctor = ctors.get(0);
		final Type[] paramTypes = ctor.getGenericParameterTypes();
		if (paramTypes.length == 0) {
			return null;
		}
		String[] names = extractNames(ctor);
		if (names == null) {
			final Set<Type> types = new HashSet<>();
			for(Type p : paramTypes) {
				//only allow registration without name when all types are different
				//TODO: ideally we could allow some ad hoc heuristics to test which value goes to which parameter.... but meh
				if (!types.add(p)) return null;
			}
		}
		final LazyImmutableDescription lazy = new LazyImmutableDescription(json, manifest);
		final JsonWriter.WriteObject oldWriter = json.registerWriter(manifest, lazy);
		final JsonReader.ReadObject oldReader = json.registerReader(manifest, lazy);
		final LinkedHashMap<String, JsonWriter.WriteObject> fields = new LinkedHashMap<>();
		final LinkedHashMap<String, JsonWriter.WriteObject> methods = new LinkedHashMap<>();
		final HashMap<Type, Type> genericMappings = Generics.analyze(manifest, raw);
		final Object[] defArgs = findDefaultArguments(paramTypes, genericMappings, json);
		final LinkedHashMap<String, Field> matchingFields = new LinkedHashMap<>();
		for (final Field f : raw.getFields()) {
			if (isPublicFinalNonStatic(f.getModifiers())) {
				matchingFields.put(f.getName(), f);
			}
		}
		final LinkedHashMap<String, Method> matchingMethods = new LinkedHashMap<>();
		for (final Method mget : raw.getMethods()) {
			if (mget.getParameterTypes().length != 0) continue;
			final String name = mget.getName().startsWith("get") && mget.getName().length() > 3
					? Character.toLowerCase(mget.getName().charAt(3)) + mget.getName().substring(4)
					: mget.getName();
			if (isPublicNonStatic(mget.getModifiers()) && !name.contains("$") && !objectMethods.contains(name)) {
				matchingMethods.put(name, mget);
			}
		}
		final JsonWriter.WriteObject[] writeProps;
		if (names != null) {
			if (matchingFields.size() == paramTypes.length) {
				for (int i = 0; i < paramTypes.length; i++) {
					final Field f = matchingFields.get(names[i]);
					if (f == null || !analyzeField(json, paramTypes[i], fields, f, genericMappings)) {
						return unregister(manifest, json, oldWriter, oldReader);
					}
				}
				writeProps = fields.values().toArray(new JsonWriter.WriteObject[0]);
			} else {
				for (int i = 0; i < paramTypes.length; i++) {
					final Method m = matchingMethods.get(names[i]);
					if (m == null || !analyzeMethod(m, json, paramTypes[i], names[i], methods, genericMappings)) {
						return unregister(manifest, json, oldWriter, oldReader);
					}
				}
				writeProps = methods.values().toArray(new JsonWriter.WriteObject[0]);
			}
		} else {
			names = new String[paramTypes.length];
			if (matchingFields.size() == paramTypes.length) {
				List<Field> orderedFields = new ArrayList<>(matchingFields.values());
				for (int i = 0; i < paramTypes.length; i++) {
					final Field f = orderedFields.get(i);
					if (!analyzeField(json, paramTypes[i], fields, f, genericMappings)) {
						return unregister(manifest, json, oldWriter, oldReader);
					}
					names[i] = f.getName();
				}
				writeProps = fields.values().toArray(new JsonWriter.WriteObject[0]);
			} else {
				for (Type p : paramTypes) {
					for (Map.Entry<String, Method> kv : matchingMethods.entrySet()) {
						final Method m = kv.getValue();
						if (analyzeMethod(m, json, p, kv.getKey(), methods, genericMappings)) {
							matchingMethods.remove(kv.getKey());
							break;
						}
					}
				}
				if (methods.size() == paramTypes.length) {
					writeProps = methods.values().toArray(new JsonWriter.WriteObject[0]);
				} else {
					return unregister(manifest, json, oldWriter, oldReader);
				}
				names = (fields.isEmpty() ? methods.keySet() : fields.keySet()).toArray(new String[0]);
			}
		}
		final DecodePropertyInfo<JsonReader.ReadObject>[] readProps = new DecodePropertyInfo[paramTypes.length];
		for (int i = 0; i < paramTypes.length; i++) {
			final Type concreteType = Generics.makeConcrete(paramTypes[i], genericMappings);
			readProps[i] = new DecodePropertyInfo<>(names[i], false, false, i, false, new WriteCtor(json, concreteType, ctor));
		}
		final ImmutableDescription<T> converter = new ImmutableDescription<>(
				manifest,
				defArgs,
				new Settings.Function<Object[], T>() {
					@Override
					public T apply(@Nullable Object[] args) {
						try {
							return raw.cast(ctor.newInstance(args));
						} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
							throw new RuntimeException(e);
						}
					}
				},
				writeProps,
				readProps,
				!json.omitDefaults,
				true);
		json.registerWriter(manifest, converter);
		json.registerReader(manifest, converter);
		lazy.resolved = converter;
		return converter;
	}

	private static class WriteCtor implements JsonReader.ReadObject {
		private final DslJson json;
		private final Type type;
		private final Constructor<?> ctor;
		private JsonReader.ReadObject decoder;

		WriteCtor(final DslJson json, final Type type, final Constructor<?> ctor) {
			this.json = json;
			this.type = type;
			this.ctor = ctor;
		}

		@Override
		public Object read(final JsonReader reader) throws IOException {
			if (decoder == null) {
				decoder = json.tryFindReader(type);
				if (decoder == null) {
					throw new IOException("Unable to find reader for " + type + " on " + ctor);
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
			final HashMap<Type, Type> genericMappings,
			final DslJson json) {
		final Object[] defArgs = new Object[paramTypes.length];
		for (int i = 0; i < paramTypes.length; i++) {
			final Type concreteType = Generics.makeConcrete(paramTypes[i], genericMappings);
			defArgs[i] = json.getDefault(concreteType);
		}
		return defArgs;
	}

	private static boolean analyzeField(
			final DslJson json,
			final Type paramType,
			final LinkedHashMap<String, JsonWriter.WriteObject> found,
			final Field field,
			final HashMap<Type, Type> genericMappings) {
		final Type type = field.getGenericType();
		final Type concreteType = Generics.makeConcrete(type, genericMappings);
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
			final HashMap<String, JsonWriter.WriteObject> found,
			final HashMap<Type, Type> genericMappings) {
		final Type type = mget.getGenericReturnType();
		final Type concreteType = Generics.makeConcrete(type, genericMappings);
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
