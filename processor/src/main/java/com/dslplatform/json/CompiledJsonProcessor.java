package com.dslplatform.json;

import com.dslplatform.json.processor.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import javax.tools.*;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

@SupportedAnnotationTypes({"com.dslplatform.json.CompiledJson", "com.dslplatform.json.JsonAttribute", "com.dslplatform.json.JsonConverter"})
@SupportedOptions({"dsljson.namespace", "dsljson.compiler", "dsljson.showdsl", "dsljson.loglevel", "dsljson.annotation"})
public class CompiledJsonProcessor extends AbstractProcessor {

	private static final Map<String, String> SupportedTypes;
	private static final Map<String, String> SupportedCollections;
	private static final Set<String> JsonIgnore;
	private static final Set<String> NonNullable;
	private static final Set<String> PropertyAlias;
	private static final Map<String, String> JsonRequired;
	private static final List<IncompatibleTypes> CheckTypes;
	private static final ContainerSupport CheckCollection;

	private static final String CONFIG = "META-INF/services/com.dslplatform.json.Configuration";

	private static class IncompatibleTypes {
		final String first;
		final String second;
		final String description;

		IncompatibleTypes(String first, String second, String description) {
			this.first = first;
			this.second = second;
			this.description = description;
		}
	}

	static {
		SupportedTypes = new HashMap<String, String>();
		SupportedTypes.put("int", "int");
		SupportedTypes.put("long", "long");
		SupportedTypes.put("float", "float");
		SupportedTypes.put("double", "double");
		SupportedTypes.put("boolean", "bool");
		SupportedTypes.put("java.lang.String", "string?");
		SupportedTypes.put("java.lang.Integer", "int?");
		SupportedTypes.put("java.lang.Long", "long?");
		SupportedTypes.put("java.lang.Float", "float?");
		SupportedTypes.put("java.lang.Double", "double?");
		SupportedTypes.put("java.lang.Boolean", "bool?");
		SupportedTypes.put("java.math.BigDecimal", "decimal?");
		SupportedTypes.put("java.time.LocalDate", "date?");
		SupportedTypes.put("java.time.OffsetDateTime", "timestamp?");
		SupportedTypes.put("org.joda.time.LocalDate", "date?");
		SupportedTypes.put("org.joda.time.DateTime", "timestamp?");
		SupportedTypes.put("byte[]", "binary");
		SupportedTypes.put("java.util.UUID", "uuid?");
		SupportedTypes.put("java.util.Map<java.lang.String,java.lang.String>", "properties?");
		SupportedTypes.put("java.util.Map<java.lang.String,java.lang.Object>", "map?");
		SupportedTypes.put("java.net.InetAddress", "ip?");
		SupportedTypes.put("java.net.URI", "url?");
		SupportedTypes.put("java.awt.Color", "color?");
		SupportedTypes.put("java.awt.geom.Rectangle2D", "rectangle?");
		SupportedTypes.put("java.awt.geom.Point2D", "location?");
		SupportedTypes.put("java.awt.Point", "point?");
		SupportedTypes.put("java.awt.image.BufferedImage", "image?");
		SupportedTypes.put("android.graphics.Rect", "rectangle?");
		SupportedTypes.put("android.graphics.PointF", "location?");
		SupportedTypes.put("android.graphics.Point", "point?");
		SupportedTypes.put("android.graphics.Bitmap", "image?");
		SupportedTypes.put("org.w3c.dom.Element", "xml?");
		SupportedCollections = new HashMap<String, String>();
		SupportedCollections.put("java.util.List<", "List");
		SupportedCollections.put("java.util.Set<", "Set");
		SupportedCollections.put("java.util.LinkedList<", "Linked List");
		SupportedCollections.put("java.util.Queue<", "Queue");
		SupportedCollections.put("java.util.Stack<", "Stack");
		SupportedCollections.put("java.util.Vector<", "Vector");
		SupportedCollections.put("java.util.Collection<", "Bag");
		JsonIgnore = new HashSet<String>();
		JsonIgnore.add("com.fasterxml.jackson.annotation.JsonIgnore");
		JsonIgnore.add("org.codehaus.jackson.annotate.JsonIgnore");
		NonNullable = new HashSet<String>();
		NonNullable.add("javax.validation.constraints.NotNull");
		NonNullable.add("edu.umd.cs.findbugs.annotations.NonNull");
		NonNullable.add("javax.annotation.Nonnull");
		NonNullable.add("org.jetbrains.annotations.NotNull");
		NonNullable.add("lombok.NonNull");
		NonNullable.add("android.support.annotation.NonNull");
		PropertyAlias = new HashSet<String>();
		PropertyAlias.add("com.fasterxml.jackson.annotation.JsonProperty");
		PropertyAlias.add("com.google.gson.annotations.SerializedName");
		JsonRequired = new HashMap<String, String>();
		JsonRequired.put("com.fasterxml.jackson.annotation.JsonProperty", "required()");
		CheckTypes = new ArrayList<IncompatibleTypes>();
		CheckTypes.add(
				new IncompatibleTypes(
						"java.time",
						"org.joda.time",
						"Both Joda Time and Java Time detected as property types. Only one supported at once."));
		CheckTypes.add(
				new IncompatibleTypes(
						"java.awt",
						"android.graphics",
						"Both Java AWT and Android graphics detected as property types. Only one supported at once."));
		final Set<String> collections = new HashSet<String>();
		for (String c : SupportedCollections.keySet()) {
			collections.add(c.substring(0, c.length() - 1));
		}
		CheckCollection = new ContainerSupport() {
			@Override
			public boolean isSupported(String rawClass) {
				return collections.contains(rawClass);
			}
		};
	}

	private String namespace;
	private String compiler;
	private boolean showDsl;
	private LogLevel logLevel = LogLevel.ERRORS;
	private AnnotationUsage annotationUsage = AnnotationUsage.IMPLICIT;

	private Analysis analysis;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		Map<String, String> options = processingEnv.getOptions();
		String ns = options.get("dsljson.namespace");
		if (ns != null && ns.length() > 0) {
			namespace = ns;
		} else {
			namespace = "dsl_json";
		}
		compiler = options.get("dsljson.compiler");
		String sd = options.get("dsljson.showdsl");
		if (sd != null && sd.length() > 0) {
			try {
				showDsl = Boolean.parseBoolean(sd);
			} catch (Exception ignore) {
			}
		} else {
			showDsl = false;
		}
		String ll = options.get("dsljson.loglevel");
		if (ll != null && ll.length() > 0) {
			logLevel = LogLevel.valueOf(ll);
		}
		String au = options.get("dsljson.annotation");
		if (au != null && au.length() > 0) {
			annotationUsage = AnnotationUsage.valueOf(au);
		}
		analysis = new Analysis(
				processingEnv,
				annotationUsage,
				logLevel,
				SupportedTypes.keySet(),
				CheckCollection,
				JsonIgnore,
				NonNullable,
				PropertyAlias,
				JsonRequired,
				Collections.<String>emptySet(),
				UnknownTypes.ERROR,
				true,
				true,
				true,
				false);
	}

	private static class CompileOptions {
		boolean useJodaTime;
		boolean useAndroid;
		boolean hasError;

		AnnotationCompiler.CompileOptions toOptions(String namespace, String compiler) {
			AnnotationCompiler.CompileOptions options = new AnnotationCompiler.CompileOptions();
			options.namespace = namespace;
			options.compiler = compiler;
			options.useAndroid = useAndroid;
			options.useJodaTime = useJodaTime;
			return options;
		}
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			return false;
		}
		Set<? extends Element> compiledJsons = roundEnv.getElementsAnnotatedWith(analysis.compiledJsonElement);
		if (!compiledJsons.isEmpty()) {
			Set<? extends Element> jsonConverters = roundEnv.getElementsAnnotatedWith(analysis.converterElement);
			List<String> configurations = analysis.processConverters(jsonConverters);
			Map<String, StructInfo> structs = analysis.processCompiledJson(compiledJsons);
			CompileOptions options = new CompileOptions();
			options.hasError = analysis.hasError();
			String dsl = buildDsl(structs, options);
			if (options.hasError) {
				return false;
			}
			if (showDsl) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, dsl);
			}

			String fileContent;
			try {
				fileContent = AnnotationCompiler.buildExternalJson(dsl, options.toOptions(namespace, compiler), logLevel, processingEnv.getMessager());
			} catch (Exception e) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "DSL compilation error\n" + e.getMessage());
				return false;
			}
			try {
				String className = namespace + ".json.ExternalSerialization";
				Writer writer = processingEnv.getFiler().createSourceFile(className).openWriter();
				writer.write(fileContent);
				writer.close();
				writer = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", CONFIG).openWriter();
				writer.write(className);
				for (String conf : configurations) {
					writer.write('\n');
					writer.write(conf);
				}
				writer.close();
			} catch (IOException e) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed saving compiled json serialization files");
			}
		}
		return false;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		SourceVersion latest = SourceVersion.latest();
		if ("RELEASE_9".equals(latest.name())
				|| "RELEASE_8".equals(latest.name())
				|| "RELEASE_7".equals(latest.name())) {
			return latest;
		}
		return SourceVersion.RELEASE_6;
	}

	private static class TypeCheck {
		boolean hasFirst;
		boolean hasSecond;
	}

	private String buildDsl(Map<String, StructInfo> structs, CompileOptions options) {
		StringBuilder dsl = new StringBuilder();
		dsl.append("module json {\n");
		TypeCheck[] checks = new TypeCheck[CheckTypes.size()];
		for (int i = 0; i < checks.length; i++) {
			checks[i] = new TypeCheck();
		}
		boolean requiresExtraSetup = false;
		for (StructInfo info : structs.values()) {
			if (info.formats.contains(CompiledJson.Format.ARRAY)) {
				options.hasError = true;
				processingEnv.getMessager().printMessage(
						Diagnostic.Kind.ERROR,
						"Array format is not supported in the DSL compiler. Found on: '" + info.element.getQualifiedName().toString() + "'.",
						info.element,
						analysis.getAnnotation(info.element, analysis.compiledJsonType));
			} else if (info.formats.size() > 1) {
				options.hasError = true;
				processingEnv.getMessager().printMessage(
						Diagnostic.Kind.ERROR,
						"Only one format at a time is supported in the DSL compiler. Multiple formats found on: '" + info.element.getQualifiedName().toString() + "'.",
						info.element,
						analysis.getAnnotation(info.element, analysis.compiledJsonType));
			}
			if (info.type == ObjectType.ENUM) {
				dsl.append("  enum ");
			} else if (info.type == ObjectType.MIXIN) {
				dsl.append("  mixin ");
			} else {
				dsl.append("  struct ");
			}
			dsl.append(info.name);
			dsl.append(" {\n");

			if (info.type == ObjectType.ENUM) {
				for (String c : info.constants) {
					dsl.append("    ");
					dsl.append(c);
					dsl.append(";\n");
				}
			} else {
				for (StructInfo impl : info.implementations) {
					dsl.append("    with mixin ");
					dsl.append(impl.name);
					dsl.append(";\n");
				}
				if (info.converter == null) {
					for (AttributeInfo attr : info.attributes.values()) {
						String dslType = getDslType(attr, structs);
						processProperty(dsl, options, checks, info, attr, dslType, structs);
					}
				} else if (info.converter.length() == 0) {
					dsl.append("    external Java JSON converter;\n");
				} else {
					dsl.append("    external Java JSON converter '").append(info.converter).append("';\n");
				}
			}
			requiresExtraSetup = requiresExtraSetup || info.onUnknown != null || info.deserializeAs != null;
			dsl.append("    external name Java '");
			dsl.append(info.element.getQualifiedName());
			dsl.append("';\n  }\n");
		}
		if (requiresExtraSetup) {
			dsl.append("  JSON serialization {\n");
			for (StructInfo info : structs.values()) {
				if (info.onUnknown != null && info.onUnknown != CompiledJson.Behavior.DEFAULT) {
					dsl.append("    in ").append(info.name);
					dsl.append(info.onUnknown == CompiledJson.Behavior.FAIL ? " fail on" : " ignore");
					dsl.append(" unknown;\n");
				} else if (info.deserializeTarget() != null) {
					dsl.append("    deserialize ").append(info.name).append(" as ").append(info.deserializeTarget().name).append(";\n");
				}
			}
			dsl.append("  }\n");
		}
		dsl.append("}");
		return dsl.toString();
	}

	private String getDslType(AttributeInfo attr, Map<String, StructInfo> structs) {
		String simpleType = SupportedTypes.get(attr.type.toString());
		boolean hasNonNullable = attr.notNull;
		if (simpleType != null) {
			return simpleType.endsWith("?") && hasNonNullable
					? simpleType.substring(0, simpleType.length() - 1)
					: simpleType;
		}
		if (attr.type instanceof ArrayType) {
			ArrayType at = (ArrayType) attr.type;
			String elementType = at.getComponentType().toString();
			String ending = hasNonNullable ? "[]" : "[]?";
			simpleType = SupportedTypes.get(elementType);
			if (simpleType != null) {
				return simpleType + ending;
			}
			StructInfo item = structs.get(elementType);
			if (item != null) {
				return "json." + item.name + "?" + ending;
			}
		}
		String collectionEnding = hasNonNullable ? ">" : ">?";
		for (Map.Entry<String, String> kv : SupportedCollections.entrySet()) {
			if (attr.type.toString().startsWith(kv.getKey())) {
				String typeName = attr.type.toString().substring(kv.getKey().length(), attr.type.toString().length() - 1);
				simpleType = SupportedTypes.get(typeName);
				if (simpleType != null) {
					return kv.getValue() + "<" + simpleType + collectionEnding;
				}
				StructInfo item = structs.get(typeName);
				if (item != null) {
					return kv.getValue() + "<json." + item.name + "?" + collectionEnding;
				}
			}
		}
		StructInfo info = structs.get(attr.type.toString());
		if (info != null) {
			return "json." + info.name + (hasNonNullable ? "" : "?");
		}
		return null;
	}

	private void processProperty(
			StringBuilder dsl,
			CompileOptions options,
			TypeCheck[] checks,
			StructInfo info,
			AttributeInfo attr,
			String dslType,
			Map<String, StructInfo> structs) {
		String javaType = attr.type.toString();
		boolean fieldAccess = attr.field != null;
		for (int i = 0; i < CheckTypes.size(); i++) {
			IncompatibleTypes it = CheckTypes.get(i);
			if (javaType.startsWith(it.first) || javaType.startsWith(it.second)) {
				TypeCheck tc = checks[i];
				boolean hasFirst = tc.hasFirst || javaType.startsWith(it.first);
				boolean hasSecond = tc.hasSecond || javaType.startsWith(it.second);
				if (hasFirst && hasSecond && !tc.hasFirst && !tc.hasSecond) {
					options.hasError = true;
					processingEnv.getMessager().printMessage(
							Diagnostic.Kind.ERROR,
							"Both Joda Time and Java Time detected as property types. Only one supported at once.",
							attr.element,
							analysis.getAnnotation(info.element, analysis.compiledJsonType));
				}
				tc.hasFirst = hasFirst;
				tc.hasSecond = hasSecond;
			}
		}
		if (dslType == null && attr.converter != null) {
			//converters for unknown DSL type must fake some type
			dslType = "String?";
		}
		if (dslType != null) {
			options.useJodaTime = options.useJodaTime || javaType.startsWith("org.joda.time");
			options.useAndroid = options.useAndroid || javaType.startsWith("android.graphics");
			dsl.append("    ");
			dsl.append(dslType);
			dsl.append(" ");
			dsl.append(attr.name);

			StructInfo target = findReferenced(attr.type, structs);
			String alias = attr.id;
			if (info.minifiedNames.containsKey(attr.id)) {
				alias = info.minifiedNames.get(attr.id);
			}
			boolean excludeTypeSignature = target != null
					&& target.type == ObjectType.MIXIN
					&& (CompiledJson.TypeSignature.EXCLUDE.equals(attr.typeSignature)
						|| attr.typeSignature == null && CompiledJson.TypeSignature.EXCLUDE.equals(target.typeSignature));
			if (info.type == ObjectType.CLASS && (fieldAccess || !attr.name.equals(alias) || !attr.alternativeNames.isEmpty() || attr.fullMatch || attr.converter != null || attr.mandatory || excludeTypeSignature)) {
				dsl.append(" {");
				if (fieldAccess) {
					dsl.append("  simple Java access;");
				}
				if (!attr.name.equals(alias)) {
					dsl.append("  serialization name '");
					dsl.append(alias);
					dsl.append("';");
				}
				for (String da : attr.alternativeNames) {
					dsl.append("  deserialization alias '");
					dsl.append(da);
					dsl.append("';");
				}
				if (attr.fullMatch) {
					dsl.append("  deserialization match full;");
				}
				if (attr.mandatory) {
					dsl.append("  mandatory;");
				}
				if (attr.converter != null) {
					dsl.append("  external Java JSON converter '").append(attr.converter).append("' for '").append(javaType).append("';");
				}
				if (excludeTypeSignature) {
					dsl.append("  exclude serialization signature;");
				}
				dsl.append("  }\n");
			} else {
				dsl.append(";\n");
			}
		} else {
			options.hasError = true;
			processingEnv.getMessager().printMessage(
					Diagnostic.Kind.ERROR,
					"Specified type is not supported: '" + javaType + "'. If you wish to ignore this property,"
							+ " use one of the supported JsonIgnore annotations [such as Jackson @JsonIgnore or DSL-JSON @JsonAttribute(ignore = true) on "
							+ (fieldAccess ? "field" : "getter")
							+ "]. Alternatively register @JsonConverter for this type to support it with custom conversion.",
					attr.element,
					analysis.getAnnotation(info.element, analysis.compiledJsonType));
		}
	}

	private static StructInfo findReferenced(TypeMirror type, Map<String, StructInfo> structs) {
		if (type instanceof ArrayType) {
			ArrayType at = (ArrayType) type;
			String elementType = at.getComponentType().toString();
			return structs.get(elementType);
		}
		for (Map.Entry<String, String> kv : SupportedCollections.entrySet()) {
			if (type.toString().startsWith(kv.getKey())) {
				String typeName = type.toString().substring(kv.getKey().length(), type.toString().length() - 1);
				return structs.get(typeName);
			}
		}
		return structs.get(type.toString());
	}
}
