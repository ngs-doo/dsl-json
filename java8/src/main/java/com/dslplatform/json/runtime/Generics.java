package com.dslplatform.json.runtime;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

abstract class Generics {

	private static final ConcurrentMap<String, GenericType> typeCache = new ConcurrentHashMap<>();

	static HashMap<Type, Type> analyze(final Type manifest, final Class<?> raw) {
		final HashMap<Type, Type> genericMappings = new HashMap<>();
		if (manifest instanceof ParameterizedType) {
			final Type[] actual = ((ParameterizedType) manifest).getActualTypeArguments();
			final Type[] variables = raw.getTypeParameters();
			for (int i = 0; i < variables.length; i++) {
				genericMappings.put(variables[i], actual[i]);
			}
		} else {
			for (final TypeVariable tp : raw.getTypeParameters()) {
				genericMappings.put(tp, Object.class);
			}
		}
		return genericMappings;
	}

	static Type makeConcrete(final Type manifest, final HashMap<Type, Type> mappings) {
		if (mappings.isEmpty()) return manifest;
		if (manifest instanceof TypeVariable) return mappings.get(manifest);
		if (manifest instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) manifest;
			if (pt.getRawType() instanceof Class<?>) {
				final Type[] generics = pt.getActualTypeArguments();
				boolean changed = false;
				for (int i = 0;i < generics.length;i++) {
					final Type newType = makeConcrete(generics[i], mappings);
					changed = changed || newType != generics[i];
					generics[i] = newType;
				}
				if (changed) {
					return makeGenericType((Class<?>)pt.getRawType(), generics);
				}
			}
		}
		return manifest;
	}

	private static class GenericType implements ParameterizedType {

		private final String name;
		private final Type raw;
		private final Type[] arguments;

		GenericType(String name, Type raw, Type[] arguments) {
			this.name = name;
			this.raw = raw;
			this.arguments = arguments;
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(arguments) ^ raw.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) other;
				return raw.equals(pt.getRawType()) && Arrays.equals(arguments, pt.getActualTypeArguments());
			}
			return false;
		}

		@Override
		public Type[] getActualTypeArguments() {
			return arguments;
		}

		@Override
		public Type getRawType() {
			return raw;
		}

		@Override
		public Type getOwnerType() {
			return null;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static ParameterizedType makeGenericType(Class<?> container, Type[] arguments) {
		final StringBuilder sb = new StringBuilder();
		sb.append(container.getTypeName());
		sb.append("<");
		sb.append(arguments[0].getTypeName());
		for (int i = 1; i < arguments.length; i++) {
			sb.append(", ");
			sb.append(arguments[i].getTypeName());
		}
		sb.append(">");
		final String name = sb.toString();
		GenericType found = typeCache.get(name);
		if (found == null) {
			found = new GenericType(name, container, arguments);
			typeCache.put(name, found);
		}
		return found;
	}


}
