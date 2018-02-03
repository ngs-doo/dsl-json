package com.dslplatform.json.runtime;

import com.dslplatform.json.*;

import java.io.IOException;
import java.lang.reflect.*;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;

public abstract class BeanAnalyzer {

	private static final Charset utf8 = Charset.forName("UTF-8");

	private static final JsonWriter.WriteObject tmpWriter = (writer, value) -> {
	};
	private static final JsonReader.ReadObject tmpReader = reader -> null;

	public static final DslJson.ConverterFactory<BeanDescription> CONVERTER = (manifest, dslJson) -> {
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

	private static <T> BeanDescription<T> analyze(final Type manifest, final Class<T> raw, final DslJson json) {
		if (raw.isArray()
				|| Collection.class.isAssignableFrom(raw)
				|| (raw.getModifiers() & Modifier.ABSTRACT) != 0
				|| (raw.getDeclaringClass() != null && (raw.getModifiers() & Modifier.STATIC) == 0)) {
			return null;
		}
		try {
			raw.newInstance();
		} catch (InstantiationException | IllegalAccessException ignore) {
			return null;
		}
		json.registerWriter(manifest, tmpWriter);
		json.registerReader(manifest, tmpReader);
		final HashMap<String, JsonWriter.WriteObject> foundWrite = new HashMap<>();
		final HashMap<String, ReadPropertyInfo<JsonReader.BindObject>> foundRead = new HashMap<>();
		final HashMap<Type, Type> genericMappings = Generics.analyze(manifest, raw);
		for (final Field f : raw.getFields()) {
			analyzeField(json, foundWrite, foundRead, f, genericMappings);
		}
		for (final Method m : raw.getMethods()) {
			analyzeMethods(m, raw, json, foundWrite, foundRead, genericMappings);
		}
		final JsonWriter.WriteObject[] writeProps = foundWrite.values().toArray(new JsonWriter.WriteObject[0]);
		final ReadPropertyInfo<JsonReader.BindObject>[] readProps = foundRead.values().toArray(new ReadPropertyInfo[0]);
		final BeanDescription<T> converter = new BeanDescription<T>(manifest, raw::newInstance, writeProps, readProps);
		json.registerWriter(manifest, converter);
		json.registerReader(manifest, converter);
		json.registerBinder(manifest, converter);
		return converter;
	}

	private static class ReadField implements JsonWriter.WriteObject {
		private final DslJson json;
		private final Field field;
		private final Type type;
		private final byte[] quotedName;
		private final boolean alwaysSerialize;
		private JsonWriter.WriteObject fieldWriter;

		ReadField(final DslJson json, final Field field, final Type type) {
			this.json = json;
			this.field = field;
			this.type = type.equals(Object.class) ? null : type;
			quotedName = ("\"" + field.getName() + "\":").getBytes(utf8);
			this.alwaysSerialize = !json.omitDefaults;
		}

		@Override
		public void write(JsonWriter writer, Object value) {
			if (type != null && fieldWriter == null) {
				final JsonWriter.WriteObject tmp = json.tryFindWriter(type);
				if (tmp != null && !tmpWriter.equals(tmp)) {
					fieldWriter = tmp;
				} else {
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
					if (tmp == null || tmpWriter.equals(tmp)) {
						throw new SerializationException("Unable to find writer for " + attr.getClass() + " on field " + field.getName() + " of " + field.getDeclaringClass());
					}
					writer.writeAscii(quotedName);
					tmp.write(writer, attr);
				}
			} else if (alwaysSerialize || attr != null) {
				writer.writeAscii(quotedName);
				fieldWriter.write(writer, attr);
			}
		}
	}

	private static class SetField implements JsonReader.BindObject {
		private final DslJson json;
		private final Field field;
		private final Type type;
		private JsonReader.ReadObject fieldReader;

		SetField(final DslJson json, final Field field, final Type type) {
			this.json = json;
			this.field = field;
			this.type = type;
		}

		@Override
		public Object bind(JsonReader reader, Object instance) throws IOException {
			if (reader.wasNull()) return instance;
			if (fieldReader == null) {
				JsonReader.ReadObject tmp = json.tryFindReader(type);
				if (tmp != null && !tmpReader.equals(tmp)) {
					fieldReader = tmp;
				} else {
					throw new IOException("Unable to find reader for " + type + " on field " + field.getName() + " of " + field.getDeclaringClass());
				}
			}
			final Object attr = fieldReader.read(reader);
			try {
				field.set(instance, attr);
			} catch (IllegalAccessException e) {
				throw new IOException("Unable to set field " + field.getName() + " of " + field.getDeclaringClass(), e);
			}
			return instance;
		}
	}

	private static void analyzeField(
			final DslJson json,
			final HashMap<String, JsonWriter.WriteObject> foundWrite,
			final HashMap<String, ReadPropertyInfo<JsonReader.BindObject>> foundRead,
			final Field field,
			final HashMap<Type, Type> genericMappings) {
		if (canRead(field.getModifiers()) && canWrite(field.getModifiers())) {
			final Type type = field.getGenericType();
			final Type concreteType = Generics.makeConcrete(type, genericMappings);
			if (isUnknownType(type) || json.tryFindWriter(type) != null && json.tryFindReader(type) != null) {
				foundWrite.put(field.getName(), new ReadField(json, field, concreteType));
				foundRead.put(field.getName(), new ReadPropertyInfo<>(field.getName(), false, new SetField(json, field, concreteType)));
			}
		}
	}

	private static class ReadMethod implements JsonWriter.WriteObject {
		private final DslJson json;
		private final Method method;
		private final Type type;
		private final byte[] quotedName;
		private final boolean alwaysSerialize;
		private JsonWriter.WriteObject methodWriter;

		ReadMethod(final DslJson json, final Method method, final String name, final Type type) {
			this.json = json;
			this.method = method;
			this.type = type.equals(Object.class) ? null : type;
			quotedName = ("\"" + name + "\":").getBytes(utf8);
			alwaysSerialize = !json.omitDefaults;
		}

		@Override
		public void write(JsonWriter writer, Object value) {
			if (type != null && methodWriter == null) {
				final JsonWriter.WriteObject tmp = json.tryFindWriter(type);
				if (tmp != null && !tmpWriter.equals(tmp)) {
					methodWriter = tmp;
				} else {
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
					final JsonWriter.WriteObject tmp = json.tryFindWriter(method.getGenericReturnType());
					if (tmp == null || tmpWriter.equals(tmp)) {
						throw new SerializationException("Unable to find writer for " + attr.getClass() + " on method " + method.getName() + " of " + method.getDeclaringClass());
					}
				}
			} else if (alwaysSerialize || attr != null) {
				writer.writeAscii(quotedName);
				methodWriter.write(writer, attr);
			}
		}
	}

	private static class SetMethod implements JsonReader.BindObject {
		private final DslJson json;
		private final Method method;
		private final Type type;
		private JsonReader.ReadObject methodReader;

		SetMethod(final DslJson json, final Method method, final Type type) {
			this.json = json;
			this.method = method;
			this.type = type;
		}

		@Override
		public Object bind(JsonReader reader, Object instance) throws IOException {
			if (reader.wasNull()) return instance;
			if (methodReader == null) {
				JsonReader.ReadObject tmp = json.tryFindReader(type);
				if (tmp != null && !tmpReader.equals(tmp)) {
					methodReader = tmp;
				} else {
					throw new IOException("Unable to find reader for " + type + " on method " + method.getName() + " of " + method.getDeclaringClass());
				}
			}
			final Object attr = methodReader.read(reader);
			try {
				method.invoke(instance, attr);
				return instance;
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new SerializationException("Unable to call method " + method.getName() + " of " + method.getDeclaringClass(), e);
			}
		}
	}

	private static void analyzeMethods(
			final Method mget,
			final Class<?> manifest,
			final DslJson json,
			final HashMap<String, JsonWriter.WriteObject> foundWrite,
			final HashMap<String, ReadPropertyInfo<JsonReader.BindObject>> foundRead,
			final HashMap<Type, Type> genericMappings) {
		if (mget.getParameterTypes().length != 0) return;
		final String setName = mget.getName().startsWith("get") ? "set" + mget.getName().substring(3) : mget.getName();
		final Method mset;
		try {
			mset = manifest.getMethod(setName, mget.getReturnType());
		} catch (NoSuchMethodException ignore) {
			return;
		}
		final String name = mget.getName().startsWith("get")
				? Character.toLowerCase(mget.getName().charAt(3)) + mget.getName().substring(4)
				: mget.getName();
		if (canRead(mget.getModifiers()) && canWrite(mset.getModifiers())) {
			final Type type = mget.getGenericReturnType();
			final Type concreteType = Generics.makeConcrete(type, genericMappings);
			if (isUnknownType(type) || json.tryFindWriter(type) != null && json.tryFindReader(type) != null) {
				foundWrite.put(name, new ReadMethod(json, mget, name, concreteType));
				foundRead.put(name, new ReadPropertyInfo<>(name, false, new SetMethod(json, mset, concreteType)));
			}
		}
	}

	private static boolean canRead(final int modifiers) {
		return (modifiers & Modifier.PUBLIC) != 0
				&& (modifiers & Modifier.STATIC) == 0;
	}

	private static boolean canWrite(final int modifiers) {
		return (modifiers & Modifier.PUBLIC) != 0
				&& (modifiers & Modifier.FINAL) == 0
				&& (modifiers & Modifier.STATIC) == 0;
	}

	private static boolean isUnknownType(final Type type) {
		return Object.class == type || type instanceof TypeVariable;
	}
}
