package com.dslplatform.json.runtime;

import com.dslplatform.json.*;
import com.dslplatform.json.processor.Analysis;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public abstract class ObjectAnalyzer {

	public static class Runtime {
		public static final JsonReader.ReadObject<Object> JSON_READER = new JsonReader.ReadObject<Object>() {
			@Override
			public Object read(JsonReader r) throws IOException {
				if (r.wasNull()) return null;
				return ObjectConverter.deserializeObject(r);
			}
		};
		public static final JsonWriter.WriteObject<Object> JSON_WRITER = new JsonWriter.WriteObject<Object>() {
			@Override
			public void write(JsonWriter writer, @Nullable Object value) {
				if (value != null) writer.serializeObject(value);
				else writer.writeNull();
			}
		};
	}

	private static class LazyObjectDescription implements JsonWriter.WriteObject, JsonReader.ReadObject, JsonReader.BindObject {

		private final DslJson json;
		private final Type type;
		private JsonWriter.WriteObject resolvedWriter;
		private JsonReader.BindObject resolvedBinder;
		private JsonReader.ReadObject resolvedReader;
		volatile ObjectFormatDescription resolved;

		LazyObjectDescription(DslJson json, Type type) {
			this.json = json;
			this.type = type;
		}

		private boolean checkSignatureNotFound() {
			int i = 0;
			ObjectFormatDescription local = null;
			while (i < 50) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					throw new ConfigurationException(e);
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
		public Object read(JsonReader reader) throws IOException {
			if (resolvedReader == null) {
				if (checkSignatureNotFound()) {
					final JsonReader.ReadObject tmp = json.tryFindReader(type);
					if (tmp == null || tmp == this) {
						throw new ConfigurationException("Unable to find reader for " + type);
					}
					resolvedReader = tmp;
				}
			}
			return resolvedReader.read(reader);
		}

		@Override
		public Object bind(final JsonReader reader, final Object instance) throws IOException {
			if (resolvedBinder == null) {
				if (checkSignatureNotFound()) {
					final JsonReader.BindObject tmp = json.tryFindBinder(type);
					if (tmp == null || tmp == this) {
						throw new ConfigurationException("Unable to find binder for " + type);
					}
					resolvedBinder = tmp;
				}
			}
			return resolvedBinder.bind(reader, instance);
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

	public static final DslJson.ConverterFactory<ObjectFormatDescription> CONVERTER = new DslJson.ConverterFactory<ObjectFormatDescription>() {
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
		InstanceFactory newInstance = pickMarkedFactory(raw, json);
		if (json.context != null && newInstance == null) {
			newInstance = pickCtorFactory(raw, json);
		}
		if (newInstance == null) {
			try {
				raw.newInstance();
			} catch (InstantiationException | IllegalAccessException ignore) {
				return null;
			}
			newInstance = new InstanceFactory() {
				@Override
				public Object create() {
					try {
						return raw.newInstance();
					} catch (Exception ex) {
						throw new ConfigurationException("Unable to create an instance of " + raw);
					}
				}
			};
		}
		final LazyObjectDescription lazy = new LazyObjectDescription(json, manifest);
		if (!hasEncoder) json.registerWriter(manifest, lazy);
		if (!hasDecoder) json.registerReader(manifest, lazy);
		final LinkedHashMap<String, JsonWriter.WriteObject> foundWrite = new LinkedHashMap<>();
		final LinkedHashMap<String, DecodePropertyInfo<JsonReader.BindObject>> foundRead = new LinkedHashMap<>();
		final GenericsMapper genericMappings = GenericsMapper.create(manifest, raw);
		int index = 0;
		for (final Field f : raw.getFields()) {
			if (analyzeField(json, foundWrite, foundRead, f, index, f.getDeclaringClass(), genericMappings)) index++;
		}
		for (final Method m : raw.getMethods()) {
			if (analyzeMethods(m, raw, json, foundWrite, foundRead, index, m.getDeclaringClass(), genericMappings)) index++;
		}
		//TODO: don't register bean if something can't be serialized
		final JsonWriter.WriteObject[] writeProps = foundWrite.values().toArray(new JsonWriter.WriteObject[0]);
		final DecodePropertyInfo<JsonReader.BindObject>[] readProps = foundRead.values().toArray(new DecodePropertyInfo[0]);
		final ObjectFormatDescription<T, T> converter = ObjectFormatDescription.create(raw, newInstance, writeProps, readProps, json, true);
		if (!hasEncoder) json.registerWriter(manifest, converter);
		if (!hasDecoder) json.registerReader(manifest, converter);
		if (!hasBinder) json.registerBinder(manifest, converter);
		lazy.resolved = converter;
		return converter;
	}

	static boolean matchesContext(Type manifest, DslJson json) {
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (json == null) throw new IllegalArgumentException("json can't be null");
		if (json.context == null) return false;
		final Class<?> signature = json.context.getClass();
		if (manifest.equals(signature)) return true;
		if (manifest instanceof Class<?>) {
			return ((Class<?>) manifest).isAssignableFrom(signature);
		}
		if (manifest instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) manifest;
			return ((Class<?>) pt.getRawType()).isAssignableFrom(signature);
		}
		return false;
	}

	private static @Nullable <T> InstanceFactory pickCtorFactory(Class<?> raw, DslJson<T> json) {
		if (json.context == null) return null;
		final Map<Class<? extends Annotation>, Boolean> creatorMarkers = json.getRegisteredCreatorMarkers();
		ArrayList<Constructor<?>> matchedCtors = null;
		for (Constructor<?> ctor : raw.getDeclaredConstructors()) {
			//TODO: ignore generics for now
			if (ctor.getParameterCount() != 1 || !matchesContext(ctor.getGenericParameterTypes()[0], json))
				continue;
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
			try {
				ctor.newInstance(json.context);
				if (matchedCtors == null) matchedCtors = new ArrayList<>(1);
				if (hasMarker) {
					matchedCtors.add(0, ctor);
				} else {
					matchedCtors.add(ctor);
				}
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
				if (hasMarker) {
					throw new ConfigurationException("Unable to test marked constructor " + ctor + ". Please check environment setup or constructor implementation", ex);
				}
			}
		}
		if (matchedCtors == null) return null;
		final Constructor<?> ctor = matchedCtors.get(0);
		return new InstanceFactory() {
			@Override
			public Object create() {
				try {
					return ctor.newInstance(json.context);
				} catch (Exception ex) {
					throw new ConfigurationException("Unable to create an instance of " + raw);
				}
			}
		};
	}

	private static @Nullable <T> InstanceFactory pickMarkedFactory(Class<?> raw, DslJson<T> json) {
		final Map<Class<? extends Annotation>, Boolean> creatorMarkers = json.getRegisteredCreatorMarkers();
		if (creatorMarkers.isEmpty()) return null;
		for (final Method factory : raw.getDeclaredMethods()) {
			final int modifiers = factory.getModifiers();
			if ((modifiers & Modifier.STATIC) != Modifier.STATIC
					|| factory.getParameterCount() > 1
					|| !raw.isAssignableFrom(factory.getReturnType())
					|| factory.getParameterCount() == 1 && !matchesContext(factory.getGenericParameterTypes()[0], json))
				continue;
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
					try {
						if (factory.getParameterCount() == 1) {
							factory.invoke(null, json.context);
							return new InstanceFactory() {
								@Override
								public Object create() {
									try {
										return factory.invoke(null, json.context);
									} catch (Exception ex) {
										throw new ConfigurationException("Unable to create an instance of " + raw);
									}
								}
							};
						} else {
							factory.invoke(null);
							return new InstanceFactory() {
								@Override
								public Object create() {
									try {
										return factory.invoke(null);
									} catch (Exception ex) {
										throw new ConfigurationException("Unable to create an instance of " + raw);
									}
								}
							};
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
						throw new ConfigurationException("Unable to test marked factory " + factory + ". Please check environment setup or factory implementation", ex);
					}
				}
			}
		}
		return null;
	}

	private static boolean analyzeField(
			final DslJson json,
			final LinkedHashMap<String, JsonWriter.WriteObject> foundWrite,
			final LinkedHashMap<String, DecodePropertyInfo<JsonReader.BindObject>> foundRead,
			final Field field,
			final int index,
			final Class<?> raw,
			final GenericsMapper genericMappings) {
		if (!canRead(field.getModifiers()) || !canWrite(field.getModifiers())) return false;
		final Type type = field.getGenericType();
		final Type concreteType = genericMappings.makeConcrete(type, raw);
		final boolean isUnknown = Generics.isUnknownType(type);
		if (isUnknown || json.tryFindWriter(concreteType) != null && json.tryFindReader(concreteType) != null) {
			foundWrite.put(
					field.getName(),
					Settings.createEncoder(
							new Reflection.ReadField(field),
							field.getName(),
							json,
							isUnknown ? null : concreteType));
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

	private static boolean analyzeMethods(
			final Method mget,
			final Class<?> manifest,
			final DslJson json,
			final LinkedHashMap<String, JsonWriter.WriteObject> foundWrite,
			final LinkedHashMap<String, DecodePropertyInfo<JsonReader.BindObject>> foundRead,
			final int index,
			final Class<?> declaringClass,
			final GenericsMapper genericMappings) {
		if (mget.getParameterTypes().length != 0) return false;
		final String setName = mget.getName().startsWith("get") ? "set" + mget.getName().substring(3) : mget.getName();
		final Method mset;
		try {
			mset = manifest.getMethod(setName, mget.getReturnType());
		} catch (NoSuchMethodException ignore) {
			return false;
		}
		final String name = Analysis.beanOrActualName(mget.getName());
		if (!canRead(mget.getModifiers()) || !canWrite(mset.getModifiers())) return false;
		if (foundRead.containsKey(name) && foundWrite.containsKey(name)) return false;
		final Type type = mget.getGenericReturnType();
		final Type concreteType = genericMappings.makeConcrete(type,declaringClass);
		final boolean isUnknown = Generics.isUnknownType(type);
		if (isUnknown || json.tryFindWriter(concreteType) != null && json.tryFindReader(concreteType) != null) {
			foundWrite.put(
					name,
					Settings.createEncoder(
							new Reflection.ReadMethod(mget),
							name,
							json,
							isUnknown ? null : concreteType));
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
