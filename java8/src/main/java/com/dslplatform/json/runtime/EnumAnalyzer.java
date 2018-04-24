package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;

import java.lang.reflect.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

public abstract class EnumAnalyzer {

	public static final DslJson.ConverterFactory<EnumDescription> CONVERTER = new DslJson.ConverterFactory<EnumDescription>() {
		@Override
		public EnumDescription tryCreate(Type manifest, DslJson dslJson) {
			if (manifest instanceof Class<?> && ((Class<?>) manifest).isEnum()) {
				return analyze(manifest, (Class<Enum>) manifest, dslJson);
			}
			if (manifest instanceof ParameterizedType) {
				final ParameterizedType pt = (ParameterizedType) manifest;
				if (pt.getActualTypeArguments().length == 1
						&& pt.getRawType() instanceof Class<?>
						&& ((Class<?>) pt.getRawType()).isEnum()) {
					return analyze(manifest, (Class<Enum>) pt.getRawType(), dslJson);
				}
			}
			return null;
		}
	};

	private static EnumDescription analyze(final Type manifest, final Class<Enum> raw, final DslJson json) {
		if (raw.isArray()
				|| Collection.class.isAssignableFrom(raw)
				|| (raw.getModifiers() & Modifier.ABSTRACT) != 0
				|| (raw.getDeclaringClass() != null && (raw.getModifiers() & Modifier.STATIC) == 0)) {
			return null;
		}
		final EnumDescription converter = new EnumDescription<>(raw, raw.getEnumConstants());
		json.registerWriter(manifest, converter);
		json.registerReader(manifest, converter);
		return converter;
	}
}
