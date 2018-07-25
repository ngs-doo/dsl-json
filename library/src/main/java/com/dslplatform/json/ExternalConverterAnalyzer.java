package com.dslplatform.json;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

class ExternalConverterAnalyzer {
	private final Set<String> lookedUpClasses = new HashSet<String>();
	private final ClassLoader[] classLoaders;

	ExternalConverterAnalyzer(Collection<ClassLoader> classLoaders) {
		this.classLoaders = classLoaders.toArray(new ClassLoader[0]);
	}

	public synchronized void tryFindConverter(Type manifest, DslJson<?> dslJson) {
		if (manifest instanceof Class<?>) {
			tryFindConverter(((Class<?>) manifest).getName(), dslJson);
		} else if (manifest instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) manifest;
			tryFindConverter(((Class<?>) pt.getRawType()).getName(), dslJson);
		}
	}

	private void tryFindConverter(String className, DslJson<?> dslJson) {
		if (!lookedUpClasses.add(className)) return;
		List<String> converterClassNames = resolveExternalConverterClassNames(className);
		for (ClassLoader cl : classLoaders) {
			for (String ccn : converterClassNames) {
				try {
					Class<?> converterClass = cl.loadClass(ccn);
					if (!Configuration.class.isAssignableFrom(converterClass)) continue;
					Configuration converter = (Configuration) converterClass.newInstance();
					converter.configure(dslJson);
					return;
				} catch (ClassNotFoundException ignored) {
				} catch (IllegalAccessException ignored) {
				} catch (InstantiationException ignored) {
				}
			}
		}
	}

	private List<String> resolveExternalConverterClassNames(final String fullClassName) {
		int dotIndex = fullClassName.lastIndexOf('.');
		if (dotIndex == -1) {
			return Collections.singletonList(String.format("_%s_DslJsonConverter", fullClassName));
		}
		String packageName = fullClassName.substring(0, dotIndex);
		String className = fullClassName.substring(dotIndex + 1);
		return Arrays.asList(
				String.format("%s._%s_DslJsonConverter", packageName, className),
				String.format("dsl_json.%s._%s_DslJsonConverter", packageName, className),
				String.format("dsl_json.%s.%sDslJsonConverter", packageName, className));
	}
}