package com.dslplatform.json;

import java.lang.reflect.Type;
import java.util.*;

class ExternalConverterAnalyzer {
	private final Set<String> lookedUpClasses = new HashSet<String>();
	private final ClassLoader[] classLoaders;

	ExternalConverterAnalyzer(Collection<ClassLoader> classLoaders) {
		this.classLoaders = classLoaders.toArray(new ClassLoader[0]);
	}

	final DslJson.ConverterFactory<JsonWriter.WriteObject> writerFactory = new DslJson.ConverterFactory<JsonWriter.WriteObject>() {
		@Nullable
		@Override
		public JsonWriter.WriteObject<?> tryCreate(Type manifest, DslJson dslJson) {
			tryFindAndRegisterExternalConverter(manifest, dslJson);
			return dslJson.getRegisteredEncoder(manifest);
		}
	};

	final DslJson.ConverterFactory<JsonReader.ReadObject> readerFactory = new DslJson.ConverterFactory<JsonReader.ReadObject>() {
		@Nullable
		@Override
		public JsonReader.ReadObject<?> tryCreate(Type manifest, DslJson dslJson) {
			tryFindAndRegisterExternalConverter(manifest, dslJson);
			return dslJson.getRegisteredDecoder(manifest);
		}
	};

	final DslJson.ConverterFactory<JsonReader.BindObject> binderFactory = new DslJson.ConverterFactory<JsonReader.BindObject>() {
		@Nullable
		@Override
		public JsonReader.BindObject<?> tryCreate(Type manifest, DslJson dslJson) {
			tryFindAndRegisterExternalConverter(manifest, dslJson);
			return dslJson.getRegisteredBinder(manifest);
		}
	};

	private synchronized void tryFindAndRegisterExternalConverter(final Type manifest, DslJson<?> dslJson) {
		if (!(manifest instanceof Class<?>)) {
			return;
		}
		List<String> converterClassNames = resolveExternalConverterClassNames(((Class<?>) manifest).getName());
		if (converterClassNames == null) {
			return;
		}
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

	@Nullable
	private List<String> resolveExternalConverterClassNames(final String fullClassName) {
		if (!lookedUpClasses.add(fullClassName)) return null;
		int dotIndex = fullClassName.lastIndexOf('.');
		if (dotIndex == -1) {
			return Collections.singletonList(String.format("_%s_DslJsonConverter", fullClassName));
		}
		String packageName = fullClassName.substring(0, dotIndex);
		Package classPackage = Package.getPackage(packageName);
		if (classPackage == null) {
			return null;
		}
		String className = fullClassName.substring(dotIndex + 1);
		return Arrays.asList(
				String.format("%s._%s_DslJsonConverter", packageName, className),
				String.format("dsl_json.%s._%s_DslJsonConverter", packageName, className),
				String.format("dsl_json.%s.%sDslJsonConverter", packageName, className));
	}
}