package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.SerializationException;

import java.io.IOException;
import java.lang.reflect.*;
import java.nio.charset.Charset;
import java.util.*;

public abstract class ImmutableAnalyzer {

	private static final Charset utf8 = Charset.forName("UTF-8");

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
			while (resolved == null && i < 50) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					throw new SerializationException(e);
				}
				i++;
			}
			if (resolved != null) {
				encoder = resolved;
				decoder = resolved;
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

	private static <T> ImmutableDescription<T> analyze(final Type manifest, final Class<T> raw, final DslJson json) {
		if (raw.isArray()
				|| Collection.class.isAssignableFrom(raw)
				|| (raw.getModifiers() & Modifier.ABSTRACT) != 0
				|| (raw.getDeclaringClass() != null && (raw.getModifiers() & Modifier.STATIC) == 0)
				|| (raw.getModifiers() & Modifier.PUBLIC) == 0) {
			return null;
		}
		final ArrayList<Constructor<?>> ctors = new ArrayList<>();
		for(Constructor<?> ctor : raw.getDeclaredConstructors()) {
			if ((ctor.getModifiers() & Modifier.PUBLIC) == 1) {
				ctors.add(ctor);
			}
		}
		if (ctors.size() != 1 || ctors.get(0).getParameterCount() == 0) return null;
		final Constructor<?> ctor = ctors.get(0);
		final LazyImmutableDescription lazy = new LazyImmutableDescription(json, manifest);
		final JsonWriter.WriteObject oldWriter = json.registerWriter(manifest, lazy);
		final JsonReader.ReadObject oldReader = json.registerReader(manifest, lazy);
		final LinkedHashMap<String, JsonWriter.WriteObject> fields = new LinkedHashMap<>();
		final LinkedHashMap<String, JsonWriter.WriteObject> methods = new LinkedHashMap<>();
		final HashMap<Type, Type> genericMappings = Generics.analyze(manifest, raw);
		final Parameter[] ctorParams = ctor.getParameters();
		for (final Field f : raw.getFields()) {
			analyzeField(json, ctorParams, fields, f, genericMappings);
		}
		for (final Method m : raw.getMethods()) {
			analyzeMethods(m, json, ctorParams, methods, genericMappings);
		}
		final JsonWriter.WriteObject[] writeProps;
		final String[] names;
		if (methods.size() == ctorParams.length) {
			writeProps = methods.values().toArray(new JsonWriter.WriteObject[0]);
			names = methods.keySet().toArray(new String[0]);
		} else if (fields.size() == ctorParams.length) {
			writeProps = fields.values().toArray(new JsonWriter.WriteObject[0]);
			names = fields.keySet().toArray(new String[0]);
		} else {
			json.registerWriter(manifest, oldWriter);
			json.registerReader(manifest, oldReader);
			return null;
		}
		final DecodePropertyInfo<JsonReader.ReadObject>[] readProps = new DecodePropertyInfo[ctorParams.length];
		final Object[] defArgs = new Object[ctorParams.length];
		for (int i = 0; i < ctorParams.length; i++) {
			final Type concreteType = Generics.makeConcrete(ctorParams[i].getParameterizedType(), genericMappings);
			readProps[i] = new DecodePropertyInfo<>(names[i], false, new WriteCtor(json, concreteType, ctor));
			final JsonReader.ReadObject defReader = json.tryFindReader(concreteType);
			if (defReader != null) {
				if (ctorParams[i].getType().isPrimitive()) {
					defArgs[i] = Array.get(Array.newInstance(ctorParams[i].getType(), 1), 0);
				} else {
					try {
						final JsonReader nullJson = json.newReader(new byte[]{'n', 'u', 'l', 'l'});
						nullJson.read();
						defArgs[i] = defReader.read(nullJson);
					} catch (Exception ignore) {
						json.registerWriter(manifest, oldWriter);
						json.registerReader(manifest, oldReader);
						return null;
					}
				}
			}
		}
		final ImmutableDescription<T> converter = new ImmutableDescription<T>(
				manifest,
				defArgs,
				args -> {
					if (args == null) return null;
					try {
						return (T) ctor.newInstance(args);
					} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				},
				writeProps,
				readProps);
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


	private static class ReadField implements JsonWriter.WriteObject {
		private final DslJson json;
		private final Field field;
		private final Type type;
		private final byte[] quotedName;
		private final boolean alwaysSerialize;
		private JsonWriter.WriteObject encoder;

		ReadField(final DslJson json, final Field field, final Type type) {
			this.json = json;
			this.field = field;
			this.type = type;
			quotedName = ("\"" + field.getName() + "\":").getBytes(utf8);
			this.alwaysSerialize = !json.omitDefaults;
		}

		@Override
		public void write(final JsonWriter writer, final Object value) {
			if (type != null && encoder == null) {
				encoder = json.tryFindWriter(type);
				if (encoder == null) {
					throw new SerializationException("Unable to find writer for " + type + " on field " + field.getName() + " of " + field.getDeclaringClass());
				}
			}
			final Object attr;
			try {
				attr = field.get(value);
			} catch (IllegalAccessException e) {
				throw new SerializationException("Unable to read field " + field.getName() + " of " + field.getDeclaringClass(), e);
			}
			if (type == null) {
				if (attr == null) {
					if (alwaysSerialize) {
						writer.writeAscii(quotedName);
						writer.writeNull();
					}
				} else {
					final JsonWriter.WriteObject tmp = json.tryFindWriter(attr.getClass());
					if (tmp == null) {
						throw new SerializationException("Unable to find writer for " + attr.getClass() + " on field " + field.getName() + " of " + field.getDeclaringClass());
					}
					writer.writeAscii(quotedName);
					tmp.write(writer, attr);
				}
			} else if (alwaysSerialize || attr != null) {
				writer.writeAscii(quotedName);
				encoder.write(writer, attr);
			}
		}
	}

	private static void analyzeField(
			final DslJson json,
			final Parameter[] ctorParams,
			final LinkedHashMap<String, JsonWriter.WriteObject> found,
			final Field field,
			final HashMap<Type, Type> genericMappings) {
		if (isPublicFinalNonStatic(field.getModifiers()) && found.size() < ctorParams.length) {
			final Type type = field.getGenericType();
			final Type concreteType = Generics.makeConcrete(type, genericMappings);
			final boolean isUnknown = Generics.isUnknownType(type);
			if (type.equals(ctorParams[found.size()].getParameterizedType())
				&& (isUnknown || json.tryFindWriter(concreteType) != null && json.tryFindReader(concreteType) != null)) {
				found.put(field.getName(), new ReadField(json, field, isUnknown ? null : concreteType));
			}
		}
	}

	private static class ReadMethod implements JsonWriter.WriteObject {
		private final DslJson json;
		private final Method method;
		private final Type type;
		private final byte[] quotedName;
		private final boolean alwaysSerialize;
		private JsonWriter.WriteObject encoder;

		ReadMethod(final DslJson json, final Method method, final String name, final Type type) {
			this.json = json;
			this.method = method;
			this.type = type;
			quotedName = ("\"" + name + "\":").getBytes(utf8);
			alwaysSerialize = !json.omitDefaults;
		}

		@Override
		public void write(final JsonWriter writer, final Object value) {
			if (type != null && encoder == null) {
				encoder = json.tryFindWriter(type);
				if (encoder == null) {
					throw new SerializationException("Unable to find writer for " + type + " on method " + method.getName() + " of " + method.getDeclaringClass());
				}
			}
			final Object attr;
			try {
				attr = method.invoke(value);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new SerializationException("Unable to read method " + method.getName() + " of " + method.getDeclaringClass(), e);
			}
			if (type == null) {
				if (attr == null) {
					if (alwaysSerialize) {
						writer.writeAscii(quotedName);
						writer.writeNull();
					}
				} else {
					final JsonWriter.WriteObject tmp = json.tryFindWriter(attr.getClass());
					if (tmp == null) {
						throw new SerializationException("Unable to find writer for " + attr.getClass() + " on method " + method.getName() + " of " + method.getDeclaringClass());
					}
					writer.writeAscii(quotedName);
					tmp.write(writer, attr);
				}
			} else if (alwaysSerialize || attr != null) {
				writer.writeAscii(quotedName);
				encoder.write(writer, attr);
			}
		}
	}

	private static void analyzeMethods(
			final Method mget,
			final DslJson json,
			final Parameter[] ctorParams,
			final HashMap<String, JsonWriter.WriteObject> found,
			final HashMap<Type, Type> genericMappings) {
		if (mget.getParameterTypes().length != 0) return;
		final String name = mget.getName().startsWith("get")
				? Character.toLowerCase(mget.getName().charAt(3)) + mget.getName().substring(4)
				: mget.getName();
		if (isPublicNonStatic(mget.getModifiers()) && found.size() < ctorParams.length) {
			final Type type = mget.getGenericReturnType();
			final Type concreteType = Generics.makeConcrete(type, genericMappings);
			final boolean isUnknown = Generics.isUnknownType(type);
			if (type.equals(ctorParams[found.size()].getParameterizedType())
					&& (isUnknown || json.tryFindWriter(concreteType) != null && json.tryFindReader(concreteType) != null)) {
				found.put(name, new ReadMethod(json, mget, name, isUnknown ? null : concreteType));
			}
		}
	}

	private static boolean isPublicFinalNonStatic(final int modifiers) {
		return (modifiers & Modifier.PUBLIC) != 0
				&& (modifiers & Modifier.FINAL) != 0
				&& (modifiers & Modifier.STATIC) == 0;
	}


	private static boolean isPublicNonStatic(final int modifiers) {
		return (modifiers & Modifier.PUBLIC) != 0
				&& (modifiers & Modifier.FINAL) != 0
				&& (modifiers & Modifier.STATIC) == 0;
	}
}
