package com.dslplatform.json.runtime;

import com.dslplatform.json.*;
import com.dslplatform.json.processor.Analysis;

import java.lang.reflect.*;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;

public abstract class MixinAnalyzer {

	private static class LazyMixinDescription implements JsonWriter.WriteObject {

		private final DslJson json;
		private final Type type;
		private JsonWriter.WriteObject resolvedWriter;
		volatile ObjectFormatDescription resolved;

		LazyMixinDescription(DslJson json, Type type) {
			this.json = json;
			this.type = type;
		}

		private boolean checkSignatureNotFound() {
			int i = 0;
			while (i < 50) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					throw new ConfigurationException(e);
				}
				if (resolved != null) {
					resolvedWriter = resolved;
					break;
				}
				i++;
			}
			return resolved == null;
		}

		@Override
		public void write(final JsonWriter writer, @Nullable final Object value) {
			if (resolvedWriter == null) {
				if (checkSignatureNotFound()) {
					final JsonWriter.WriteObject tmp = json.tryFindWriter(type);
					if (tmp == null || tmp == this) {
						throw new ConfigurationException("Unable to find writer for " + type);
					}
					resolvedWriter = tmp;
				}
			}
			resolvedWriter.write(writer, value);
		}
	}

	public static final DslJson.ConverterFactory<ObjectFormatDescription> WRITER = new DslJson.ConverterFactory<ObjectFormatDescription>() {
		@Nullable
		@Override
		public ObjectFormatDescription tryCreate(Type manifest, DslJson dslJson) {
			if (manifest instanceof Class<?>) {
				return analyze(manifest, (Class<?>) manifest, dslJson);
			}
			if (manifest instanceof ParameterizedType) {
				final ParameterizedType pt = (ParameterizedType) manifest;
				return analyze(manifest, (Class<?>) pt.getRawType(), dslJson);
			}
			return null;
		}
	};

	@Nullable
	private static <T> ObjectFormatDescription<T, T> analyze(final Type manifest, final Class<T> raw, final DslJson json) {
		if (raw.isArray()
				|| Object.class == manifest
				|| !raw.isInterface() && (raw.getModifiers() & Modifier.ABSTRACT) == 0
				|| Collection.class.isAssignableFrom(raw)
				|| (raw.getDeclaringClass() != null && (raw.getModifiers() & Modifier.STATIC) == 0)) {
			return null;
		}
		final Set<Type> currentEncoders = json.getRegisteredEncoders();
		final Set<Type> currentDecoders = json.getRegisteredDecoders();
		final boolean hasEncoder = currentEncoders.contains(manifest);
		if (!currentDecoders.contains(manifest)) return null;
		final JsonReader.ReadObject currentReader = json.tryFindReader(manifest);
		if (currentReader instanceof FormatConverter == false) return null;
		final InstanceFactory newInstance = new InstanceFactory() {
			@Override
			public Object create() {
				throw new IllegalArgumentException("Internal DSL-JSON error. Should not be used for deserialization");			}
		};
		final LazyMixinDescription lazy = new LazyMixinDescription(json, manifest);
		if (!hasEncoder) json.registerWriter(manifest, lazy);
		final LinkedHashMap<String, JsonWriter.WriteObject> foundWrite = new LinkedHashMap<>();
		final GenericsMapper genericMappings = GenericsMapper.create(manifest, raw);
		for (final Field f : raw.getDeclaredFields()) {
			analyzeField(json, foundWrite, f, genericMappings);
		}
		for (final Method m : raw.getDeclaredMethods()) {
			analyzeMethods(m, json, foundWrite, m.getDeclaringClass(), genericMappings);
		}
		//TODO: don't register bean if something can't be serialized
		final JsonWriter.WriteObject[] writeProps = foundWrite.values().toArray(new JsonWriter.WriteObject[0]);
		final ObjectFormatDescription<T, T> converter = ObjectFormatDescription.create(raw, newInstance, writeProps, new DecodePropertyInfo[0], json, true);
		if (!hasEncoder) json.registerWriter(manifest, converter);
		lazy.resolved = converter;
		return converter;
	}

	private static void analyzeField(
			final DslJson json,
			final LinkedHashMap<String, JsonWriter.WriteObject> foundWrite,
			final Field field,
			final GenericsMapper genericMappings) {
		if (!canRead(field.getModifiers())) return;
		final Type type = field.getGenericType();
		final Type concreteType = genericMappings.makeConcrete(type, field.getDeclaringClass());
		final boolean isUnknown = Generics.isUnknownType(type);
		if (isUnknown || json.tryFindWriter(concreteType) != null && json.tryFindReader(concreteType) != null) {
			foundWrite.put(
					field.getName(),
					Settings.createEncoder(
							new Reflection.ReadField(field),
							field.getName(),
							json,
							isUnknown ? null : concreteType));
		}
	}

	private static void analyzeMethods(
			final Method mget,
			final DslJson json,
			final LinkedHashMap<String, JsonWriter.WriteObject> foundWrite,
			final Class<?> raw,
			final GenericsMapper genericMappings) {
		if (mget.getParameterTypes().length != 0) return;
		if (!canRead(mget.getModifiers())) return;
		final boolean isBoolean = boolean.class.equals(mget.getReturnType());
		final String name = Analysis.beanOrActualName(mget.getName(), isBoolean);
		if (foundWrite.containsKey(name)) return;
		final Type type = mget.getGenericReturnType();
		final Type concreteType = genericMappings.makeConcrete(type, raw);
		final boolean isUnknown = Generics.isUnknownType(type);
		if (isUnknown || json.tryFindWriter(concreteType) != null && json.tryFindReader(concreteType) != null) {
			foundWrite.put(
					name,
					Settings.createEncoder(
							new Reflection.ReadMethod(mget),
							name,
							json,
							isUnknown ? null : concreteType));
		}
	}

	private static boolean canRead(final int modifiers) {
		return (modifiers & Modifier.PUBLIC) != 0
				&& (modifiers & Modifier.STATIC) == 0;
	}
}
