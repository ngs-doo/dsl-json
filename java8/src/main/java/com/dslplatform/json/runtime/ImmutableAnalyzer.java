package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.SerializationException;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

public abstract class ImmutableAnalyzer {

	private static final Set<String> objectMethods = new HashSet<>();
	static {
		for (Method m : Object.class.getMethods()) {
			if (m.getParameterTypes().length == 0) {
				objectMethods.add(m.getName());
			}
		}
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
			while (i < 50) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					throw new SerializationException(e);
				}
				if (resolved != null) {
					encoder = resolved;
					decoder = resolved;
					break;
				}
				i++;
			}
			return resolved == null;
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
		public void write(final JsonWriter writer, final Object value) {
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

	public static final DslJson.ConverterFactory<ImmutableDescription> CONVERTER = (manifest, dslJson) -> {
		if (manifest instanceof Class<?>) {
			return analyze(manifest, (Class<?>) manifest, dslJson);
		}
		if (manifest instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) manifest;
			if (pt.getActualTypeArguments().length == 1 && pt.getRawType() instanceof Class<?>) {
				return analyze(manifest, (Class<?>) pt.getRawType(), dslJson);
			}
		}
		return null;
	};

	private static String[] tryParanamerIfPresent(Constructor<?> ctor) {
		com.thoughtworks.paranamer.AdaptiveParanamer paranamer = new com.thoughtworks.paranamer.AdaptiveParanamer();
		return paranamer.lookupParameterNames(ctor);
	}

	public static Optional<String[]> extractNames(Constructor<?> ctor) {
		final Parameter[] ctorParams = ctor.getParameters();
		final String[] names = new String[ctorParams.length];
		for (int i = 0; i < ctorParams.length; i++) {
			if (!ctorParams[i].isNamePresent()) {
				try {
					return Optional.ofNullable(tryParanamerIfPresent(ctor));
				} catch (NoClassDefFoundError | Exception ignore) {
					return Optional.empty();
				}
			}
			names[i] = ctorParams[i].getName();
		}
		return Optional.of(names);
	}

	private static <T> ImmutableDescription<T> analyze(final Type manifest, final Class<T> raw, final DslJson json) {
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
		if (ctors.size() != 1 || ctors.get(0).getParameterCount() == 0) return null;
		final Constructor<?> ctor = ctors.get(0);
		String[] names = extractNames(ctor).orElse(null);
		final Parameter[] ctorParams = ctor.getParameters();
		if (names == null) {
			final Set<Type> types = new HashSet<>();
			for(Parameter p : ctorParams) {
				//only allow registration without name when all types are different
				//TODO: ideally we could allow some ad hoc heuristics to test which value goes to which parameter.... but meh
				if (!types.add(p.getParameterizedType())) return null;
			}
		}
		final LazyImmutableDescription lazy = new LazyImmutableDescription(json, manifest);
		final JsonWriter.WriteObject oldWriter = json.registerWriter(manifest, lazy);
		final JsonReader.ReadObject oldReader = json.registerReader(manifest, lazy);
		final LinkedHashMap<String, JsonWriter.WriteObject> fields = new LinkedHashMap<>();
		final LinkedHashMap<String, JsonWriter.WriteObject> methods = new LinkedHashMap<>();
		final HashMap<Type, Type> genericMappings = Generics.analyze(manifest, raw);
		final Object[] defArgs = findDefaultArguments(ctorParams, genericMappings, json);
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
			if (matchingFields.size() == ctorParams.length) {
				for (int i = 0; i < ctorParams.length; i++) {
					final Field f = matchingFields.get(names[i]);
					if (f == null || !analyzeField(json, ctorParams[i], fields, f, genericMappings)) {
						return unregister(manifest, json, oldWriter, oldReader);
					}
				}
				writeProps = fields.values().toArray(new JsonWriter.WriteObject[0]);
			} else {
				for (int i = 0; i < ctorParams.length; i++) {
					final Method m = matchingMethods.get(names[i]);
					if (m == null || !analyzeMethod(m, json, ctorParams[i], names[i], methods, genericMappings)) {
						return unregister(manifest, json, oldWriter, oldReader);
					}
				}
				writeProps = methods.values().toArray(new JsonWriter.WriteObject[0]);
			}
		} else {
			names = new String[ctorParams.length];
			if (matchingFields.size() == ctorParams.length) {
				List<Field> orderedFields = new ArrayList<>(matchingFields.values());
				for (int i = 0; i < ctorParams.length; i++) {
					final Field f = orderedFields.get(i);
					if (!analyzeField(json, ctorParams[i], fields, f, genericMappings)) {
						return unregister(manifest, json, oldWriter, oldReader);
					}
					names[i] = f.getName();
				}
				writeProps = fields.values().toArray(new JsonWriter.WriteObject[0]);
			} else {
				for (Parameter p : ctorParams) {
					for (Map.Entry<String, Method> kv : matchingMethods.entrySet()) {
						final Method m = kv.getValue();
						if (analyzeMethod(m, json, p, kv.getKey(), methods, genericMappings)) {
							matchingMethods.remove(kv.getKey());
							break;
						}
					}
				}
				if (methods.size() == ctorParams.length) {
					writeProps = methods.values().toArray(new JsonWriter.WriteObject[0]);
				} else {
					return unregister(manifest, json, oldWriter, oldReader);
				}
				names = (fields.isEmpty() ? methods.keySet() : fields.keySet()).toArray(new String[0]);
			}
		}
		final DecodePropertyInfo<JsonReader.ReadObject>[] readProps = new DecodePropertyInfo[ctorParams.length];
		for (int i = 0; i < ctorParams.length; i++) {
			final Type concreteType = Generics.makeConcrete(ctorParams[i].getParameterizedType(), genericMappings);
			readProps[i] = new DecodePropertyInfo<>(names[i], false, false, i, false, new WriteCtor(json, concreteType, ctor));
		}
		final ImmutableDescription<T> converter = new ImmutableDescription<T>(
				manifest,
				defArgs,
				args -> {
					try {
						return (T) ctor.newInstance(args);
					} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
						throw new RuntimeException(e);
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

	private static <T> ImmutableDescription<T> unregister(Type manifest, DslJson json, JsonWriter.WriteObject oldWriter, JsonReader.ReadObject oldReader) {
		json.registerWriter(manifest, oldWriter);
		json.registerReader(manifest, oldReader);
		return null;
	}

	private static Object[] findDefaultArguments(
			final Parameter[] ctorParams,
			final HashMap<Type, Type> genericMappings,
			final DslJson json) {
		final Object[] defArgs = new Object[ctorParams.length];
		for (int i = 0; i < ctorParams.length; i++) {
			final Type concreteType = Generics.makeConcrete(ctorParams[i].getParameterizedType(), genericMappings);
			if (ctorParams[i].getType().isPrimitive()) {
				defArgs[i] = Array.get(Array.newInstance(ctorParams[i].getType(), 1), 0);
			} else {
				final JsonReader.ReadObject defReader = json.tryFindReader(concreteType);
				//TODO: hack to avoid timeouts during  cyclic dependency resolution
				if (defReader != null && !(defReader instanceof LazyImmutableDescription)) {
					try {
						final JsonReader nullJson = json.newReader(new byte[]{'n', 'u', 'l', 'l'});
						nullJson.read();
						defArgs[i] = defReader.read(nullJson);
					} catch (Exception ignore) {
					}
				}
			}
		}
		return defArgs;
	}

	private static boolean analyzeField(
			final DslJson json,
			final Parameter ctorParam,
			final LinkedHashMap<String, JsonWriter.WriteObject> found,
			final Field field,
			final HashMap<Type, Type> genericMappings) {
		final Type type = field.getGenericType();
		final Type concreteType = Generics.makeConcrete(type, genericMappings);
		final boolean isUnknown = Generics.isUnknownType(type);
		if (type.equals(ctorParam.getParameterizedType())
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
			final Parameter ctorParam,
			final String name,
			final HashMap<String, JsonWriter.WriteObject> found,
			final HashMap<Type, Type> genericMappings) {
		final Type type = mget.getGenericReturnType();
		final Type concreteType = Generics.makeConcrete(type, genericMappings);
		final boolean isUnknown = Generics.isUnknownType(type);
		if (type.equals(ctorParam.getParameterizedType())
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
