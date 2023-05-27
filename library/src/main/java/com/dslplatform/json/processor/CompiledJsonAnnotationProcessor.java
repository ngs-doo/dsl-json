package com.dslplatform.json.processor;

import com.dslplatform.json.*;
import com.dslplatform.json.runtime.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

import static com.dslplatform.json.processor.Context.nonGenericObject;
import static com.dslplatform.json.processor.Context.typeOrClass;

@SupportedAnnotationTypes({
		"com.dslplatform.json.CompiledJson",
		"com.dslplatform.json.JsonAttribute",
		"com.dslplatform.json.JsonConverter",
		"com.dslplatform.json.JsonValue",
		"com.fasterxml.jackson.annotation.JsonCreator",
		"javax.json.bind.annotation.JsonbCreator"
})
public class CompiledJsonAnnotationProcessor extends AbstractProcessor {

	private static final Set<String> JsonIgnore;
	private static final Map<String, List<Analysis.AnnotationMapping<Boolean>>> NonNullable;
	private static final Map<String, String> PropertyAlias;
	private static final Map<String, List<Analysis.AnnotationMapping<Boolean>>> JsonRequired;
	private static final Set<String> Creators;
	private static final Map<String, String> Indexes;
	private static final Map<String, OptimizedConverter> InlinedConverters;
	private static final Map<String, String> Defaults;

	private static final String CONFIG = "META-INF/services/com.dslplatform.json.Configuration";

	private static final String GRADLE_OPTION_ISOLATING = "org.gradle.annotation.processing.isolating";
	private static final String GRADLE_OPTION_AGGREGATING = "org.gradle.annotation.processing.aggregating";

	private enum Options {
		LOG_LEVEL("dsljson.loglevel"),
		ANNOTATION("dsljson.annotation"),
		UNKNOWN("dsljson.unknown"),
		JACKSON("dsljson.jackson"),
		JSONB("dsljson.jsonb"),
		CONFIGURATION("dsljson.configuration"),
		NULLABLE("dsljson.nullable"),
		GENERATED_MARKER("dsljson.generatedmarker");

		final String value;

		Options(String value) {
			this.value = value;
		}
	}

	static {
		JsonIgnore = new HashSet<>();
		JsonIgnore.add("com.fasterxml.jackson.annotation.JsonIgnore");
		JsonIgnore.add("javax.json.bind.annotation.JsonbTransient");
		NonNullable = new HashMap<>();
		NonNullable.put("javax.validation.constraints.NotNull", null);
		NonNullable.put("javax.annotation.Nonnull", null);
		NonNullable.put("com.dslplatform.json.NonNull", null);
		NonNullable.put("android.support.annotation.NonNull", null);
		NonNullable.put("org.jetbrains.annotations.NotNull", null);
		NonNullable.put(
				"javax.json.bind.annotation.JsonbNillable",
				Arrays.asList(
						new Analysis.AnnotationMapping<>("value()", null),
						new Analysis.AnnotationMapping<>("value()", true)));
		NonNullable.put(
				"javax.json.bind.annotation.JsonbProperty",
				Collections.singletonList(new Analysis.AnnotationMapping<>("nillable()", true)));
		PropertyAlias = new HashMap<>();
		PropertyAlias.put("com.fasterxml.jackson.annotation.JsonProperty", "value()");
		PropertyAlias.put("com.google.gson.annotations.SerializedName", "value()");
		PropertyAlias.put("javax.json.bind.annotation.JsonbProperty", "value()");
		JsonRequired = new HashMap<>();
		JsonRequired.put(
				"com.fasterxml.jackson.annotation.JsonProperty",
				Collections.singletonList(new Analysis.AnnotationMapping<>("required()", true)));
		Creators = new HashSet<>();
		Creators.add("com.fasterxml.jackson.annotation.JsonCreator");
		Creators.add("javax.json.bind.annotation.JsonbCreator");
		Indexes = new HashMap<>();
		Indexes.put("com.fasterxml.jackson.annotation.JsonProperty", "index()");
		InlinedConverters = new HashMap<>();
		InlinedConverters.put("short", new OptimizedConverter("com.dslplatform.json.NumberConverter", "SHORT_WRITER", "serialize", "SHORT_READER", "deserializeShort", "(short)0"));
		InlinedConverters.put("short[]", new OptimizedConverter("com.dslplatform.json.NumberConverter", "SHORT_ARRAY_WRITER", "serialize", "SHORT_ARRAY_READER", null, "com.dslplatform.json.NumberConverter.SHORT_EMPTY_ARRAY"));
		InlinedConverters.put("java.lang.Short", new OptimizedConverter("com.dslplatform.json.NumberConverter", "SHORT_WRITER", "serialize", "NULLABLE_SHORT_READER", "deserializeShort", "com.dslplatform.json.NumberConverter.SHORT_ZERO"));
		InlinedConverters.put("int", new OptimizedConverter("com.dslplatform.json.NumberConverter", "INT_WRITER", "serialize", "INT_READER", "deserializeInt", "0"));
		InlinedConverters.put("int[]", new OptimizedConverter("com.dslplatform.json.NumberConverter", "INT_ARRAY_WRITER", "serialize", "INT_ARRAY_READER", null, "com.dslplatform.json.NumberConverter.INT_EMPTY_ARRAY"));
		InlinedConverters.put("java.lang.Integer", new OptimizedConverter("com.dslplatform.json.NumberConverter", "INT_WRITER", "serialize", "NULLABLE_INT_READER", "deserializeInt", "com.dslplatform.json.NumberConverter.INT_ZERO"));
		InlinedConverters.put("long", new OptimizedConverter("com.dslplatform.json.NumberConverter", "LONG_WRITER", "serialize", "LONG_READER", "deserializeLong", "0L"));
		InlinedConverters.put("long[]", new OptimizedConverter("com.dslplatform.json.NumberConverter", "LONG_ARRAY_WRITER", "serialize", "LONG_ARRAY_READER", null, "com.dslplatform.json.NumberConverter.LONG_EMPTY_ARRAY"));
		InlinedConverters.put("java.lang.Long", new OptimizedConverter("com.dslplatform.json.NumberConverter", "LONG_WRITER", "serialize", "NULLABLE_LONG_READER", "deserializeLong", "com.dslplatform.json.NumberConverter.LONG_ZERO"));
		InlinedConverters.put("float", new OptimizedConverter("com.dslplatform.json.NumberConverter", "FLOAT_WRITER", "serialize", "FLOAT_READER", "deserializeFloat", "0f"));
		InlinedConverters.put("float[]", new OptimizedConverter("com.dslplatform.json.NumberConverter", "FLOAT_ARRAY_WRITER", "serialize", "FLOAT_ARRAY_READER", null, "com.dslplatform.json.NumberConverter.FLOAT_EMPTY_ARRAY"));
		InlinedConverters.put("java.lang.Float", new OptimizedConverter("com.dslplatform.json.NumberConverter", "FLOAT_WRITER", "serialize", "NULLABLE_FLOAT_READER", "deserializeFloat", "com.dslplatform.json.NumberConverter.FLOAT_ZERO"));
		InlinedConverters.put("double", new OptimizedConverter("com.dslplatform.json.NumberConverter", "DOUBLE_WRITER", "serialize", "DOUBLE_READER", "deserializeDouble", "0.0"));
		InlinedConverters.put("double[]", new OptimizedConverter("com.dslplatform.json.NumberConverter", "DOUBLE_ARRAY_WRITER", "serialize", "DOUBLE_ARRAY_READER", null, "com.dslplatform.json.NumberConverter.DOUBLE_EMPTY_ARRAY"));
		InlinedConverters.put("java.lang.Double", new OptimizedConverter("com.dslplatform.json.NumberConverter", "DOUBLE_WRITER", "serialize", "NULLABLE_DOUBLE_READER", "deserializeDouble", "com.dslplatform.json.NumberConverter.DOUBLE_ZERO"));
		InlinedConverters.put("boolean", new OptimizedConverter("com.dslplatform.json.BoolConverter", "WRITER", "serialize", "READER", "deserialize", "false"));
		InlinedConverters.put("boolean[]", new OptimizedConverter("com.dslplatform.json.BoolConverter", "ARRAY_WRITER", "serialize", "ARRAY_READER", null, "com.dslplatform.json.BoolConverter.EMPTY_ARRAY"));
		InlinedConverters.put("java.lang.Boolean", new OptimizedConverter("com.dslplatform.json.BoolConverter", "WRITER", "serialize", "NULLABLE_READER", "deserialize", "Boolean.FALSE"));
		InlinedConverters.put("java.lang.String", new OptimizedConverter("com.dslplatform.json.StringConverter", "WRITER", "serialize", "READER", "deserialize", "\"\""));
		InlinedConverters.put("java.util.UUID", new OptimizedConverter("com.dslplatform.json.UUIDConverter", "WRITER", "serialize", "READER", "deserialize", "com.dslplatform.json.UUIDConverter.MIN_UUID"));
		InlinedConverters.put("java.time.LocalDate", new OptimizedConverter("com.dslplatform.json.JavaTimeConverter", "LOCAL_DATE_WRITER", "serialize", "LOCAL_DATE_READER", "deserializeLocalDate", null));
		InlinedConverters.put("java.time.OffsetDateTime", new OptimizedConverter("com.dslplatform.json.JavaTimeConverter", "DATE_TIME_WRITER", "serialize", "DATE_TIME_READER", "deserializeDateTime", null));
		Defaults = new HashMap<>();
		Defaults.put("byte", "(byte)0");
		Defaults.put("boolean", "false");
		Defaults.put("int", "0");
		Defaults.put("long", "0L");
		Defaults.put("short", "(short)0");
		Defaults.put("double", "0.0");
		Defaults.put("float", "0.0f");
		Defaults.put("char", "'\0'");
		Defaults.put("java.util.OptionalLong", "java.util.OptionalLong.empty()");
		Defaults.put("java.util.OptionalInt", "java.util.OptionalInt.empty()");
		Defaults.put("java.util.OptionalDouble", "java.util.OptionalDouble.empty()");
		Defaults.put("java.util.Optional", "java.util.Optional.empty()");
	}

	private LogLevel logLevel = LogLevel.ERRORS;
	private AnnotationUsage annotationUsage = AnnotationUsage.IMPLICIT;
	private UnknownTypes unknownTypes = UnknownTypes.ERROR;
	private boolean withJackson = false;
	private boolean withJsonb = false;
	private boolean withNullable = true;
	private String configurationFileName = null;
	private String generatedMarker = null;

	private TypeElement jacksonCreatorElement;
	private DeclaredType jacksonCreatorType;
	private TypeElement jsonbCreatorElement;
	private DeclaredType jsonbCreatorType;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		Map<String, String> options = processingEnv.getOptions();
		String ll = options.get(Options.LOG_LEVEL.value);
		if (ll != null && ll.length() > 0) {
			logLevel = LogLevel.valueOf(ll);
		}
		String au = options.get(Options.ANNOTATION.value);
		if (au != null && au.length() > 0) {
			annotationUsage = AnnotationUsage.valueOf(au);
		}
		String unk = options.get(Options.UNKNOWN.value);
		if (unk != null && unk.length() > 0) {
			unknownTypes = UnknownTypes.valueOf(unk);
		}
		String jks = options.get(Options.JACKSON.value);
		if (jks != null && jks.length() > 0) {
			withJackson = Boolean.parseBoolean(jks);
		}
		String jsb = options.get(Options.JSONB.value);
		if (jsb != null && jsb.length() > 0) {
			withJsonb = Boolean.parseBoolean(jsb);
		}
		String con = options.get(Options.CONFIGURATION.value);
		if (con != null && con.length() > 0) {
			configurationFileName = con;
		}
		String nul = options.get(Options.NULLABLE.value);
		if (nul != null && nul.length() > 0) {
			withNullable = Boolean.parseBoolean(nul);
		}
		if (options.containsKey(Options.GENERATED_MARKER.value)) {
			String gm = options.get(Options.GENERATED_MARKER.value);
			generatedMarker = gm != null ? gm.trim() : "";
		}
		jacksonCreatorElement = processingEnv.getElementUtils().getTypeElement("com.fasterxml.jackson.annotation.JsonCreator");
		jacksonCreatorType = jacksonCreatorElement != null ? processingEnv.getTypeUtils().getDeclaredType(jacksonCreatorElement) : null;
		jsonbCreatorElement = processingEnv.getElementUtils().getTypeElement("javax.json.bind.annotation.JsonbCreator");
		jsonbCreatorType = jsonbCreatorElement != null ? processingEnv.getTypeUtils().getDeclaredType(jsonbCreatorElement) : null;
		if (generatedMarker == null && processingEnv.getElementUtils().getTypeElement("javax.annotation.processing.Generated") != null) {
			generatedMarker = "@javax.annotation.processing.Generated(\"dsl_json\")";
		} else if (generatedMarker == null && processingEnv.getElementUtils().getTypeElement("javax.annotation.Generated") != null) {
			generatedMarker = "@javax.annotation.Generated(\"dsl_json\")";
		}

	}

	@Override
	public Set<String> getSupportedOptions() {
		Set<String> options = new HashSet<>();
		for (Options option : Options.values()) {
			options.add(option.value);
		}
		//TODO: this is not fully correct. It should be only configurationFileName.isEmpty() but that requires additional configuration
		options.add(configurationFileName == null || configurationFileName.isEmpty() ? GRADLE_OPTION_ISOLATING : GRADLE_OPTION_AGGREGATING);
		return options;
	}

	private static boolean isAssignableFrom(Set<Type> known, Type test) {
		if (test instanceof Class<?>) {
			Class<?> tc = (Class<?>) test;
			for (Type k : known) {
				if (k instanceof Class<?>) {
					Class<?> kc = (Class<?>) k;
					if (kc.isAssignableFrom(tc)) return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver() || annotations.isEmpty()) {
			return false;
		}
		final DslJson.Settings<Object> settings = new DslJson.Settings<>()
				.resolveReader(Settings.UNKNOWN_READER)
				.resolveWriter(Settings.UNKNOWN_WRITER)
				.resolveReader(CollectionAnalyzer.READER)
				.resolveWriter(CollectionAnalyzer.WRITER)
				.resolveReader(ArrayAnalyzer.READER)
				.resolveWriter(ArrayAnalyzer.WRITER)
				.resolveReader(MapAnalyzer.READER)
				.resolveWriter(MapAnalyzer.WRITER)
				.includeServiceLoader(getClass().getClassLoader());

		final DslJson<Object> dslJson = new DslJson<>(settings);
		Set<Type> knownEncoders = dslJson.getRegisteredEncoders();
		Set<Type> knownDecoders = dslJson.getRegisteredDecoders();
		Set<String> allTypes = new HashSet<>();
		for (Type t : knownDecoders) {
			if (knownEncoders.contains(t) || isAssignableFrom(knownEncoders, t)) {
				allTypes.add(t.getTypeName());
			}
		}

		TypeSupport typeSupport = new CachedTypeSupport(type -> {
			if (allTypes.contains(type)) {
				return true;
			} else if ("java.lang.Object".equals(type)) {
				return false;
			}
			try {
				Class<?> raw = Class.forName(type);
				return dslJson.canSerialize(raw) && dslJson.canDeserialize(raw);
			} catch (NoClassDefFoundError | Exception ignore) {
				return false;
			}
		});

		final Analysis analysis = new Analysis(
				processingEnv,
				annotationUsage,
				logLevel,
				typeSupport,
				JsonIgnore,
				withNullable ? NonNullable : new HashMap<>(),
				PropertyAlias,
				JsonRequired,
				Creators,
				Indexes,
				unknownTypes,
				false,
				true,
				true,
				true);
		Set<? extends Element> compiledJsons = roundEnv.getElementsAnnotatedWith(analysis.compiledJsonElement);
		Set<? extends Element> jacksonCreators = withJackson && jacksonCreatorElement != null ? roundEnv.getElementsAnnotatedWith(jacksonCreatorElement) : new HashSet<>();
		Set<? extends Element> jsonbCreators = withJsonb && jsonbCreatorElement != null ? roundEnv.getElementsAnnotatedWith(jsonbCreatorElement) : new HashSet<>();
		if (!compiledJsons.isEmpty() || !jacksonCreators.isEmpty() || !jsonbCreators.isEmpty()) {
			Set<? extends Element> jsonConverters = roundEnv.getElementsAnnotatedWith(analysis.converterElement);
			Map<String, Element> configurations = analysis.processConverters(jsonConverters);
			if (!configurations.isEmpty() && "".equals(configurationFileName)) {
				for (Map.Entry<String, Element> kv : configurations.entrySet()) {
					if (logLevel.isVisible(LogLevel.INFO)) {
						processingEnv.getMessager().printMessage(
								Diagnostic.Kind.WARNING,
								"Configuration file is disabled, but @" + JsonConverter.class.getName() + " which implements " + Configuration.class.getName() + " found: '" + kv.getKey() + "'. Manual converter registration with DslJson is required.",
								kv.getValue());
					}
				}
				return false;
			}
			analysis.processAnnotation(analysis.compiledJsonType, compiledJsons);
			if (!jacksonCreators.isEmpty() && jacksonCreatorType != null) {
				analysis.processAnnotation(jacksonCreatorType, jacksonCreators);
			}
			if (!jsonbCreators.isEmpty() && jsonbCreatorType != null) {
				analysis.processAnnotation(jsonbCreatorType, jsonbCreators);
			}
			Map<String, StructInfo> structs = analysis.analyze();
			if (analysis.hasError()) {
				return false;
			}

			final Map<String, StructInfo> generatedFiles = new HashMap<>();
			final List<Element> originatingElements = new ArrayList<>();

			for (Map.Entry<String, StructInfo> entry : structs.entrySet()) {
				StructInfo structInfo = entry.getValue();
				if (structInfo.type == ObjectType.CLASS && structInfo.attributes.isEmpty() && !structInfo.hasAnnotation()) {
					continue;
				}

				String classNamePath = findConverterName(structInfo);
				try {
					JavaFileObject converterFile = processingEnv.getFiler().createSourceFile(classNamePath, structInfo.element);
					try (Writer writer = converterFile.openWriter()) {
						buildCode(writer, processingEnv, entry.getKey(), structInfo, structs, typeSupport, unknownTypes != UnknownTypes.ERROR, generatedMarker);
						generatedFiles.put(classNamePath, structInfo);
						originatingElements.add(structInfo.element);
					} catch (IOException e) {
						processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
								"Failed saving compiled json serialization file " + classNamePath);
					}
				} catch (IOException e) {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
							"Failed creating compiled json serialization file " + classNamePath);
				}
			}

			final List<String> allConfigurations = new ArrayList<>(configurations.keySet());
			if (configurationFileName != null) {
				try {
					FileObject configFile = processingEnv.getFiler()
							.createSourceFile(configurationFileName, originatingElements.toArray(new Element[0]));
					try (Writer writer = configFile.openWriter()) {
						if (!buildRootConfiguration(writer, configurationFileName, generatedFiles, processingEnv))
							return false;
						allConfigurations.add(configurationFileName);
					} catch (Exception e) {
						processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
								"Failed saving configuration file " + configurationFileName);
					}
				} catch (IOException e) {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
							"Failed creating configuration file " + configurationFileName);
				}
			}
			if (!allConfigurations.isEmpty()) {
				originatingElements.addAll(configurations.values());
				saveToServiceConfigFile(allConfigurations, originatingElements);
			}
		}
		return false;
	}

	private void saveToServiceConfigFile(List<String> configurations, List<Element> elements) {
		try {
			FileObject configFile = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", CONFIG, elements.toArray(new Element[0]));
			try (Writer writer = configFile.openWriter()) {
				for (String conf : configurations) {
					writer.write(conf);
					writer.write('\n');
				}
			} catch (Exception e) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed saving config file " + CONFIG);
			}
		} catch (IOException e) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed creating config file " + CONFIG);
		}
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		SourceVersion latest = SourceVersion.latest();
		if ("RELEASE_9".equals(latest.name())) {
			return latest;
		} else if (latest.name().length() > "RELEASE_9".length()) {
			return latest;
		}
		return SourceVersion.RELEASE_8;
	}

	static String findConverterName(StructInfo structInfo) {
		int dotIndex = structInfo.binaryName.lastIndexOf('.');
		String className = structInfo.binaryName.substring(dotIndex + 1);
		if (dotIndex == -1) return String.format("_%s_DslJsonConverter", className);
		String packageName = structInfo.binaryName.substring(0, dotIndex);
		Package packageClass = Package.getPackage(packageName);
		boolean useDslPackage = packageClass != null && packageClass.isSealed() || structInfo.binaryName.startsWith("java.");
		return String.format("%s%s._%s_DslJsonConverter", useDslPackage ? "dsl_json." : "", packageName, className);
	}

	private static void buildCode(
			final Writer code,
			final ProcessingEnvironment environment,
			final String className,
			final StructInfo si,
			final Map<String, StructInfo> structs,
			final TypeSupport typeSupport,
			final boolean allowUnknown,
			@Nullable final String generatedMarker) throws IOException {
		final Context context = new Context(code, environment, InlinedConverters, Defaults, structs, typeSupport, allowUnknown);
		final EnumTemplate enumTemplate = new EnumTemplate(context);
		final ConverterTemplate converterTemplate = new ConverterTemplate(context, enumTemplate);

		final String generateFullClassName = findConverterName(si);
		final int dotIndex = generateFullClassName.lastIndexOf('.');
		final String generateClassName = generateFullClassName.substring(dotIndex + 1);
		if (dotIndex != -1) {
			final String generatePackage = generateFullClassName.substring(0, dotIndex);
			code.append("package ").append(generatePackage).append(";\n\n");
		}
		code.append("\n\n");
		if (generatedMarker != null && !generatedMarker.isEmpty()) {
			code.append(generatedMarker).append("\n");
		}
		code.append("public class ").append(generateClassName).append(" implements com.dslplatform.json.Configuration {\n");
		code.append("\tprivate static final java.nio.charset.Charset utf8 = java.nio.charset.Charset.forName(\"UTF-8\");\n");
		code.append("\t@Override\n");
		code.append("\tpublic void configure(com.dslplatform.json.DslJson __dsljson) {\n");

		if (si.type == ObjectType.CLASS && si.isParameterized) {
			code.append("\t\tConverterFactory factory = new ConverterFactory();\n");
			code.append("\t\t__dsljson.registerReaderFactory(factory);\n");
			code.append("\t\t__dsljson.registerWriterFactory(factory);\n");
			if (si.createFromEmptyInstance()) {
				code.append("\t\t__dsljson.registerBinderFactory(factory);\n");
			}
		} else if (si.builder != null || si.type == ObjectType.CLASS && (si.selectedConstructor() != null || si.annotatedFactory != null)) {
			String objectFormatConverterName = "converter";
			if (si.formats.contains(CompiledJson.Format.OBJECT)) {
				code.append("\t\tObjectFormatConverter objectConverter = new ObjectFormatConverter(__dsljson);\n");
				objectFormatConverterName = "objectConverter";
			}
			if (si.formats.contains(CompiledJson.Format.ARRAY)) {
				code.append("\t\tArrayFormatConverter arrayConverter = new ArrayFormatConverter(__dsljson);\n");
				objectFormatConverterName = "arrayConverter";
			}
			if (si.formats.contains(CompiledJson.Format.OBJECT) && si.formats.contains(CompiledJson.Format.ARRAY)) {
				code.append("\t\tcom.dslplatform.json.runtime.FormatDescription description = new com.dslplatform.json.runtime.FormatDescription(\n");
				code.append("\t\t\t").append(className).append(".class,\n");
				code.append("\t\t\tobjectConverter,\n");
				code.append("\t\t\tarrayConverter,\n");
				if (si.isObjectFormatFirst) code.append("\t\t\ttrue,\n");
				else code.append("\t\t\tfalse,\n");
				String typeAlias = si.deserializeName.isEmpty() ? className : si.deserializeName;
				code.append("\t\t\t\"").append(typeAlias).append("\",\n");
				code.append("\t\t\t__dsljson);\n");
				if (si.createFromEmptyInstance()) {
					code.append("\t\t__dsljson.registerBinder(").append(className).append(".class, description);\n");
				}
				code.append("\t\t__dsljson.registerReader(").append(className).append(".class, description);\n");
				code.append("\t\t__dsljson.registerWriter(").append(className).append(".class, description);\n");
			} else {
				if (si.createFromEmptyInstance()) {
					code.append("\t\t__dsljson.registerBinder(").append(className).append(".class, ").append(objectFormatConverterName).append(");\n");
				}
				code.append("\t\t__dsljson.registerReader(").append(className).append(".class, ").append(objectFormatConverterName).append(");\n");
				code.append("\t\t__dsljson.registerWriter(").append(className).append(".class, ").append(objectFormatConverterName).append(");\n");
			}
		} else if (si.type == ObjectType.CONVERTER) {
			String type = typeOrClass(nonGenericObject(className), className);
			if (si.converter.legacyDeclaration) {
				code.append("\t\t__dsljson.registerWriter(").append(type).append(", ").append(si.converter.fullName).append(".").append(si.converter.writer).append(");\n");
				code.append("\t\t__dsljson.registerReader(").append(type).append(", ").append(si.converter.fullName).append(".").append(si.converter.reader).append(");\n");
			} else {
				String objectName = Analysis.objectName(className);
				code.append("\t\t__dsljson.registerWriter(").append(type).append(", ");
				code.append("new com.dslplatform.json.JsonWriter.WriteObject<").append(objectName).append(">() {\n");
				code.append("\t\t\t@Override\n\t\t\tpublic void write(com.dslplatform.json.JsonWriter writer, ").append(objectName).append(" value) {\n");
				code.append("\t\t\t\t");
				si.converter.write(code);
				code.append(".write(writer, value);\n");
				code.append("\t\t\t};\n\t\t});\n");
				code.append("\t\t__dsljson.registerReader(").append(type).append(", ");
				code.append("new com.dslplatform.json.JsonReader.ReadObject<").append(objectName).append(">() {\n");
				code.append("\t\t\t@Override\n\t\t\tpublic ").append(objectName).append(" read(com.dslplatform.json.JsonReader reader) throws java.io.IOException {\n");
				code.append("\t\t\t\treturn ");
				si.converter.read(code);
				code.append(".read(reader);\n");
				code.append("\t\t\t};\n\t\t});\n");
			}
		} else if (si.type == ObjectType.ENUM) {
			if (enumTemplate.isStatic(si)) {
				code.append("\t\tEnumConverter enumConverter = new EnumConverter();\n");
			} else {
				code.append("\t\tEnumConverter enumConverter = new EnumConverter(__dsljson);\n");
			}
			code.append("\t\t__dsljson.registerWriter(").append(className).append(".class, enumConverter);\n");
			code.append("\t\t__dsljson.registerReader(").append(className).append(".class, enumConverter);\n");
		}

		if (si.type == ObjectType.MIXIN && !si.implementations.isEmpty()) {
			mixin(code, si.deserializeAs != null, si, className);
		}
		if (si.type == ObjectType.MIXIN && si.deserializeAs != null) {
			String typeMixin = typeOrClass(nonGenericObject(className), className);
			StructInfo target = si.getDeserializeTarget();
			code.append("\t\t__dsljson.registerReader(").append(typeMixin).append(", ");
			if (!target.formats.contains(CompiledJson.Format.OBJECT)) {
				code.append("new ").append(findConverterName(target)).append(".ArrayFormatConverter(__dsljson));\n");
			} else if (!target.formats.contains(CompiledJson.Format.ARRAY)) {
				code.append("new ").append(findConverterName(target)).append(".ObjectFormatConverter(__dsljson));\n");
			}
		}

		code.append("\t}\n");

		if (si.type == ObjectType.CLASS || si.builder != null) {
			final String typeName;
			if (si.isParameterized) {
				converterTemplate.factoryForGenericConverter(si);
				typeName = className + "<" + String.join(", ", si.typeParametersNames) + ">";
			} else {
				typeName = className;
			}
			if (si.createFromEmptyInstance()) {
				if (si.formats.contains(CompiledJson.Format.OBJECT)) {
					converterTemplate.emptyObject(si, typeName);
				}
				if (si.formats.contains(CompiledJson.Format.ARRAY)) {
					converterTemplate.emptyArray(si, typeName);
				}
			} else if (si.selectedConstructor() != null || si.annotatedFactory != null || si.builder != null) {
				if (si.formats.contains(CompiledJson.Format.OBJECT)) {
					converterTemplate.fromObject(si, typeName);
				}
				if (si.formats.contains(CompiledJson.Format.ARRAY)) {
					converterTemplate.fromArray(si, typeName);
				}
			}
		} else if (si.type == ObjectType.ENUM) {
			enumTemplate.create(si, className);
		}

		code.append("}\n");
	}

	private static void mixin(final Writer code, final boolean writeOnly, final StructInfo si, final String className) throws IOException {
		final String mixinType = writeOnly ? "MixinWriter" : "MixinDescription";

		code.append("\t\tcom.dslplatform.json.runtime.").append(mixinType).append("<").append(className).append("> description = new com.dslplatform.json.runtime.").append(mixinType).append("<>(\n");
		code.append("\t\t\t").append(className).append(".class,\n");
		code.append("\t\t\t__dsljson,\n");
		if (si.discriminator.length() > 0) {
			code.append("\t\t\t\"").append(si.discriminator).append("\",\n");
		}
		code.append("\t\t\tnew com.dslplatform.json.runtime.FormatDescription[] {\n");
		int i = si.implementations.size();
		for (StructInfo im : si.implementations) {
			code.append("\t\t\t\tnew com.dslplatform.json.runtime.FormatDescription(");
			code.append(im.element.getQualifiedName()).append(".class, ");
			if (im.formats.contains(CompiledJson.Format.OBJECT)) {
				code.append("new ").append(findConverterName(im)).append(".ObjectFormatConverter(__dsljson), ");
			} else {
				code.append("null, ");
			}
			if (im.formats.contains(CompiledJson.Format.ARRAY)) {
				code.append("new ").append(findConverterName(im)).append(".ArrayFormatConverter(__dsljson), ");
			} else {
				code.append("null, ");
			}
			if (im.isObjectFormatFirst) code.append("true, ");
			else code.append("false, ");
			String typeAlias = im.deserializeName.isEmpty()
					? im.element.getQualifiedName().toString()
					: im.deserializeName;
			code.append("\"").append(typeAlias).append("\", __dsljson)");
			i--;
			if (i > 0) code.append(",\n");
		}
		code.append("\n\t\t\t}\n");
		code.append("\t\t);\n");
		if (!writeOnly) {
			code.append("\t\t__dsljson.registerReader(").append(className).append(".class, description);\n");
		}
		code.append("\t\t__dsljson.registerWriter(").append(className).append(".class, description);\n");
	}

	private static boolean buildRootConfiguration(
			final Writer code,
			final String configurationName,
			final Map<String, StructInfo> configurations,
			final ProcessingEnvironment processingEnv) throws IOException {
		final int dotIndex = configurationName.lastIndexOf('.');
		final String generateClassName = configurationName.substring(dotIndex + 1);
		final boolean hasNamespace = dotIndex != -1;
		if (hasNamespace) {
			code.append("package ").append(configurationName, 0, dotIndex).append(";\n\n");
		}
		code.append("public class ").append(generateClassName).append(" implements com.dslplatform.json.Configuration {\n");
		code.append("\t@Override\n");
		code.append("\tpublic void configure(com.dslplatform.json.DslJson __dsljson) {\n");
		boolean allValid = true;
		for (Map.Entry<String, StructInfo> kv : configurations.entrySet()) {
			if (hasNamespace && kv.getKey().indexOf('.') == -1) {
				processingEnv.getMessager().printMessage(
						Diagnostic.Kind.ERROR,
						"Configuration file: '" + configurationName + "' is not in the root package, but referenced element does not have a package specified: '"
								+ kv.getValue().binaryName + "'. Use configuration name without package, eg: 'dsl_json_Annotation_Processor_External_Serialization' to allow access to specified class.",
						kv.getValue().element,
						kv.getValue().annotation);
				allValid = false;
			}
			code.append("\t\tnew ").append(kv.getKey()).append("().configure(__dsljson);\n");
		}
		code.append("\t}\n");
		code.append("}");
		return allValid;
	}
}
