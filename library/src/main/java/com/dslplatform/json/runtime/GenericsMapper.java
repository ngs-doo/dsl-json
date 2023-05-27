package com.dslplatform.json.runtime;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class GenericsMapper {

	private static class GenericsMappingKey {
		private final Type type;
		private final Class<?> declaringClass;

		public GenericsMappingKey(Type type, Class<?> declaringClass) {
			this.type = type;
			this.declaringClass = declaringClass;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof GenericsMappingKey)) return false;
			GenericsMappingKey key = (GenericsMappingKey) o;
			return Objects.equals(type, key.type) &&
					Objects.equals(declaringClass, key.declaringClass);
		}

		@Override
		public int hashCode() {
			return Objects.hash(type, declaringClass);
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("Key{");
			sb.append("type=").append(type);
			sb.append(", declaringClass=").append(declaringClass);
			sb.append('}');
			return sb.toString();
		}
	}

	private void analyzeType(Type tp) {
		if (tp instanceof Class<?>) {
			analyze(tp, (Class<?>) tp);
		}
		if (tp instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) tp;
			analyze(tp, (Class<?>) pt.getRawType());
		}
	}

	private void addPassThroughMappings(Type superType, Class<?> subRaw) {
		if (superType instanceof ParameterizedType) {
			final Type[] superActual = ((ParameterizedType) superType).getActualTypeArguments();
			Class<?> superRaw = (Class<?>) ((ParameterizedType) superType).getRawType();
			final Type[] variables = superRaw.getTypeParameters();
			for (int i = 0; i < superActual.length; i++) {
				if (!(superActual[i] instanceof TypeVariable)) {
					continue;
				}
				Type passThroughMapping = getActualType(superActual[i], subRaw);
				put(variables[i], superRaw, passThroughMapping);
			}
		}
	}

	public static GenericsMapper create(final Type manifest, final Class<?> raw) {
		GenericsMapper genericsMapper = new GenericsMapper();
		genericsMapper.analyze(manifest, raw);
		return genericsMapper;
	}

	private final Map<GenericsMappingKey, Type> mappings = new HashMap<>();

	private void analyze(final Type manifest, final Class<?> raw) {
		if (manifest instanceof ParameterizedType) {
			final Type[] actual = ((ParameterizedType) manifest).getActualTypeArguments();
			final Type[] variables = raw.getTypeParameters();
			for (int i = 0; i < variables.length; i++) {
				putIfAbsent(variables[i], raw, actual[i]);
				analyzeType(actual[i]);
			}
		}
		Type genericSuperclass = raw.getGenericSuperclass();
		if (genericSuperclass != Object.class) {
			addPassThroughMappings(genericSuperclass, raw);
			analyzeType(genericSuperclass);
		}
		for (final TypeVariable tp : raw.getTypeParameters()) {
			Type[] bounds = tp.getBounds();
			if (bounds.length > 1) {
				throw new UnsupportedOperationException("Reflection with multiple upper bounds for type parameters not supported. Offending class: " + raw.getCanonicalName());
			}
			putIfAbsent(tp, raw, bounds[0]);
		}
	}

	private boolean isEmpty() {
		return mappings.isEmpty();
	}

	private void put(Type typeParameter, Class<?> declaringClass, Type actualType) {
		mappings.put(new GenericsMappingKey(typeParameter, declaringClass), actualType);
	}

	private void putIfAbsent(Type typeParameter, Class<?> declaringClass, Type actualType) {
		mappings.putIfAbsent(new GenericsMappingKey(typeParameter, declaringClass), actualType);
	}

	private Type getActualType(Type typeParameter, Class<?> declaringClass) {
		return mappings.get(new GenericsMappingKey(typeParameter, declaringClass));
	}

	//TODO: currently used by Scala
	Map<Type, Type> mappingByType() {
		Map<Type, Type> nameMapping = new HashMap<>();
		for (Map.Entry<GenericsMappingKey, Type> kv : mappings.entrySet()) {
			nameMapping.put(kv.getKey().type, kv.getValue());
		}
		return nameMapping;
	}

	public Type makeConcrete(final Type manifest, final Class<?> raw) {
		if (isEmpty()) return manifest;
		if (manifest instanceof TypeVariable) return getActualType(manifest, raw);
		if (manifest instanceof GenericArrayType) {
			GenericArrayType gat = (GenericArrayType) manifest;
			final Type newType = makeConcrete(gat.getGenericComponentType(), raw);
			if (newType instanceof Class<?>) {
				return Array.newInstance((Class<?>) newType, 0).getClass();
			}
		}
		if (manifest instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) manifest;
			final Type[] generics = pt.getActualTypeArguments();
			boolean changed = false;
			for (int i = 0; i < generics.length; i++) {
				final Type newType = makeConcrete(generics[i], raw);
				changed = changed || newType != generics[i];
				generics[i] = newType;
			}
			if (changed) {
				return Generics.makeParameterizedType((Class<?>) pt.getRawType(), generics);
			}
		}
		return manifest;
	}
}
