package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.SerializationException;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

public abstract class ObjectAnalyzer {

	private static class LazyObjectDescription<T> implements JsonWriter.WriteObject<T>, JsonReader.ReadObject<T>, JsonReader.BindObject<T> {

		private final DslJson<?> json;
		private final Type type;
		private JsonWriter.WriteObject<T> resolvedWriter;
		private JsonReader.BindObject<T> resolvedBinder;
		private JsonReader.ReadObject<T> resolvedReader;
		volatile ObjectFormatDescription<T, T> resolved;

		LazyObjectDescription(DslJson<?> json, Type type) {
			this.json = json;
			this.type = type;
		}

		private boolean checkSignatureNotFound() {
			int i = 0;
			ObjectFormatDescription<T, T> local = null;
			while (i < 50) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					throw new SerializationException(e);
				}
				local = resolved;
				if (local != null) {
					resolvedWriter = local;
					resolvedReader = local;
					resolvedBinder = local;
					break;
				}
				i++;
			}
			return local == null;
		}

		@Override
		public T read(JsonReader reader) throws IOException {
			if (resolvedReader == null) {
				if (checkSignatureNotFound()) {
					final JsonReader.ReadObject<T> tmp = (JsonReader.ReadObject<T>) json.tryFindReader(type);
					if (tmp == null || tmp == this) {
						throw new SerializationException("Unable to find reader for " + type);
					}
					resolvedReader = tmp;
				}
			}
			return resolvedReader.read(reader);
		}

		@Override
		public T bind(final JsonReader reader, final T instance) throws IOException {
			if (resolvedBinder == null) {
				if (checkSignatureNotFound()) {
					final JsonReader.BindObject<T> tmp = (JsonReader.BindObject<T>) json.tryFindBinder(type);
					if (tmp == null || tmp == this) {
						throw new SerializationException("Unable to find binder for " + type);
					}
					resolvedBinder = tmp;
				}
			}
			return resolvedBinder.bind(reader, instance);
		}

		@Override
		public void write(final JsonWriter writer, final T value) {
			if (resolvedWriter == null) {
				if (checkSignatureNotFound()) {
					final JsonWriter.WriteObject<T> tmp = (JsonWriter.WriteObject<T>) json.tryFindWriter(type);
					if (tmp == null || tmp == this) {
						throw new SerializationException("Unable to find writer for " + type);
					}
					resolvedWriter = tmp;
				}
			}
			resolvedWriter.write(writer, value);
		}
	}

	public static final DslJson.ConverterFactory<ObjectFormatDescription> CONVERTER = new DslJson.ConverterFactory<ObjectFormatDescription>() {
		@Override
		public ObjectFormatDescription tryCreate(Type manifest, DslJson dslJson) {
			if (manifest instanceof Class<?>) {
				return analyze(manifest, (Class<?>) manifest, dslJson);
			}
			if (manifest instanceof ParameterizedType) {
				final ParameterizedType pt = (ParameterizedType) manifest;
				if (pt.getRawType() instanceof Class<?>) {
					return analyze(manifest, (Class<?>) pt.getRawType(), dslJson);
				}
			}
			return null;
		}
	};

	private static <T> ObjectFormatDescription<T, T> analyze(final Type manifest, final Class<T> raw, final DslJson<?> json) {
		if (raw.isArray()
				|| Object.class == manifest
				|| Collection.class.isAssignableFrom(raw)
				|| raw.isInterface()
				|| (raw.getModifiers() & Modifier.ABSTRACT) != 0
				|| (raw.getDeclaringClass() != null && (raw.getModifiers() & Modifier.STATIC) == 0)) {
			return null;
		}
		final Set<Type> currentEncoders = json.getRegisteredEncoders();
		final Set<Type> currentDecoders = json.getRegisteredDecoders();
		final Set<Type> currentBinders = json.getRegisteredBinders();
		final boolean hasEncoder = currentEncoders.contains(manifest);
		final boolean hasDecoder = currentDecoders.contains(manifest);
		final boolean hasBinder = currentBinders.contains(manifest);
		try {
			raw.newInstance();
		} catch (Exception ignore) {
			return null;
		}
		final InstanceFactory<T> newInstance = new InstanceFactory<T>() {
			@Override
			public T create() {
				try {
					return raw.newInstance();
				} catch (Exception ex) {
					throw new SerializationException("Unable to create an instance of " + raw);
				}
			}
		};
		final LazyObjectDescription<T> lazy = new LazyObjectDescription<T>(json, manifest);
		if (!hasEncoder) json.registerWriter(manifest, lazy);
		if (!hasDecoder) json.registerReader(manifest, lazy);
		final LinkedHashMap<String, JsonWriter.WriteObject> foundWrite = new LinkedHashMap<String, JsonWriter.WriteObject>();
		final LinkedHashMap<String, DecodePropertyInfo<JsonReader.BindObject<T>>> foundRead = new LinkedHashMap<String, DecodePropertyInfo<JsonReader.BindObject<T>>>();
		final HashMap<Type, Type> genericMappings = Generics.analyze(manifest, raw);
		int index = 0;
		for (final Field f : raw.getFields()) {
			if (analyzeField(json, foundWrite, foundRead, f, index, genericMappings)) index++;
		}
		for (final Method m : raw.getMethods()) {
			if (analyzeMethods(m, raw, json, foundWrite, foundRead, index, genericMappings)) index++;
		}
		//TODO: don't register bean if something can't be serialized
		final JsonWriter.WriteObject[] writeProps = foundWrite.values().toArray(new JsonWriter.WriteObject[0]);
		final DecodePropertyInfo<JsonReader.BindObject<T>>[] readProps = foundRead.values().toArray(new DecodePropertyInfo[0]);
		final ObjectFormatDescription<T, T> converter = ObjectFormatDescription.create(raw, newInstance, writeProps, readProps, json, true);
		if (!hasEncoder) json.registerWriter(manifest, converter);
		if (!hasDecoder) json.registerReader(manifest, converter);
		if (!hasBinder) json.registerBinder(manifest, converter);
		lazy.resolved = converter;
		return converter;
	}

	private static <T> boolean analyzeField(
			final DslJson<?> json,
			final LinkedHashMap<String, JsonWriter.WriteObject> foundWrite,
			final LinkedHashMap<String, DecodePropertyInfo<JsonReader.BindObject<T>>> foundRead,
			final Field field,
			final int index,
			final HashMap<Type, Type> genericMappings) {
		if (!canRead(field.getModifiers()) || !canWrite(field.getModifiers())) return false;
		final Type type = field.getGenericType();
		final Type concreteType = Generics.makeConcrete(type, genericMappings);
		final boolean isUnknown = Generics.isUnknownType(type);
		if (isUnknown || json.tryFindWriter(concreteType) != null && json.tryFindReader(concreteType) != null) {
			foundWrite.put(
					field.getName(),
					Settings.createEncoder(
							new Reflection.ReadField(field),
							field.getName(),
							json,
							isUnknown ? null : concreteType));

			//noinspection unchecked
			foundRead.put(
					field.getName(),
					Settings.createDecoder(
							new Reflection.SetField(field),
							field.getName(),
							json,
							false,
							false,
							index,
							false,
							concreteType));
			return true;
		}
		return false;
	}

	private static <T> boolean analyzeMethods(
			final Method mget,
			final Class<?> manifest,
			final DslJson json,
			final LinkedHashMap<String, JsonWriter.WriteObject> foundWrite,
			final LinkedHashMap<String, DecodePropertyInfo<JsonReader.BindObject<T>>> foundRead,
			final int index,
			final HashMap<Type, Type> genericMappings) {
		if (mget.getParameterTypes().length != 0) return false;
		final String setName = mget.getName().startsWith("get") ? "set" + mget.getName().substring(3) : mget.getName();
		final Method mset;
		try {
			mset = manifest.getMethod(setName, mget.getReturnType());
		} catch (NoSuchMethodException ignore) {
			return false;
		}
		final String name = mget.getName().startsWith("get") && mget.getName().length() > 3
				? Character.toLowerCase(mget.getName().charAt(3)) + mget.getName().substring(4)
				: mget.getName();
		if (!canRead(mget.getModifiers()) || !canWrite(mset.getModifiers())) return false;
		if (foundRead.containsKey(name) && foundWrite.containsKey(name)) return false;
		final Type type = mget.getGenericReturnType();
		final Type concreteType = Generics.makeConcrete(type, genericMappings);
		final boolean isUnknown = Generics.isUnknownType(type);
		if (isUnknown || json.tryFindWriter(concreteType) != null && json.tryFindReader(concreteType) != null) {
			foundWrite.put(
					name,
					Settings.createEncoder(
							new Reflection.ReadMethod(mget),
							name,
							json,
							isUnknown ? null : concreteType));
			//noinspection unchecked
			foundRead.put(
					name,
					Settings.createDecoder(
							new Reflection.SetMethod(mset),
							name,
							json,
							false,
							false,
							index,
							false,
							concreteType));
			return true;
		}
		return false;
	}

	private static boolean canRead(final int modifiers) {
		return (modifiers & Modifier.PUBLIC) != 0
				&& (modifiers & Modifier.TRANSIENT) == 0
				&& (modifiers & Modifier.NATIVE) == 0
				&& (modifiers & Modifier.STATIC) == 0;
	}

	private static boolean canWrite(final int modifiers) {
		return (modifiers & Modifier.PUBLIC) != 0
				&& (modifiers & Modifier.TRANSIENT) == 0
				&& (modifiers & Modifier.NATIVE) == 0
				&& (modifiers & Modifier.FINAL) == 0
				&& (modifiers & Modifier.STATIC) == 0;
	}
}
