package com.dslplatform.json.runtime;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class Generics {

	private static final ConcurrentMap<String, GenericType> typeCache = new ConcurrentHashMap<String, GenericType>();

	public static HashMap<Type, Type> analyze(final Type manifest, final Class<?> raw) {
		final HashMap<Type, Type> genericMappings = new HashMap<Type, Type>();
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

	public static Type makeConcrete(final Type manifest, final HashMap<Type, Type> mappings) {
		if (mappings.isEmpty()) return manifest;
		if (manifest instanceof TypeVariable) return mappings.get(manifest);
		if (manifest instanceof GenericArrayType) {
			GenericArrayType gat = (GenericArrayType)manifest;
			final Type newType = makeConcrete(gat.getGenericComponentType(), mappings);
			if (newType instanceof Class<?>) {
				return Array.newInstance((Class<?>) newType, 0).getClass();
			}
		}
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

	private static ParameterizedType makeGenericType(final Class<?> container, final Type[] arguments) {
		if (container == null) throw new IllegalArgumentException("container can't be null");
		if (arguments == null || arguments.length < 1) throw new IllegalArgumentException("arguments must have at least one element");
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

	public static boolean isUnknownType(final Type type) {
		if (type instanceof GenericArrayType) {
			GenericArrayType gat = (GenericArrayType) type;
			return isUnknownType(gat.getGenericComponentType());
		}
		//This is commented out because each container should cope with generic arguments
		/*if (type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			for (Type t : pt.getActualTypeArguments()) {
				if (isUnknownType(t)) return true;
			}
		}*/
		return Object.class == type || type instanceof TypeVariable;
	}

	public static String typeName(Type manifest) {
		String name = manifest.toString();
		if (name.startsWith("class ")) {
			return name.substring(6);
		} else if (name.startsWith("interface ")) {
			return name.substring(10);
		} else {
			return name;
		}
	}
}
