package com.dslplatform.json;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
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
	}

	private TypeElement compiledJsonElement;
	private DeclaredType compiledJsonType;
	private DeclaredType attributeType;
	private TypeElement converterElement;
	private DeclaredType converterType;
	private String namespace;
	private String compiler;
	private boolean showDsl;
	private AnnotationCompiler.LogLevel logLevel = AnnotationCompiler.LogLevel.ERRORS;
	private AnnotationUsage annotationUsage = AnnotationUsage.IMPLICIT;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		compiledJsonElement = processingEnv.getElementUtils().getTypeElement("com.dslplatform.json.CompiledJson");
		compiledJsonType = processingEnv.getTypeUtils().getDeclaredType(compiledJsonElement);
		TypeElement attributeElement = processingEnv.getElementUtils().getTypeElement("com.dslplatform.json.JsonAttribute");
		attributeType = processingEnv.getTypeUtils().getDeclaredType(attributeElement);
		converterElement = processingEnv.getElementUtils().getTypeElement("com.dslplatform.json.JsonConverter");
		converterType = processingEnv.getTypeUtils().getDeclaredType(converterElement);
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
			logLevel = AnnotationCompiler.LogLevel.valueOf(ll);
		}
		String au = options.get("dsljson.annotation");
		if (au != null && au.length() > 0) {
			annotationUsage = AnnotationUsage.valueOf(au);
		}
	}

	private enum AnnotationUsage {
		EXPLICIT,
		IMPLICIT,
		NON_JAVA
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

	private enum ObjectType {
		CLASS,
		ENUM,
		MIXIN
	}

	private static class StructInfo {
		final TypeElement element;
		final String name;
		final ObjectType type;
		final String converter;
		final Set<StructInfo> implementations = new HashSet<StructInfo>();
		final Map<String, String[]> properties = new HashMap<String, String[]>();
		final Map<String, String> minifiedNames = new HashMap<String, String>();
		final Boolean onUnknown;
		final Boolean withSignature;
		final TypeElement deserializeAs;

		StructInfo(TypeElement element, String name, ObjectType type, boolean isJsonObject, Boolean onUnknown, Boolean withSignature, TypeElement deserializeAs) {
			this.element = element;
			this.name = name;
			this.type = type;
			this.converter = isJsonObject ? "" : null;
			this.onUnknown = onUnknown;
			this.withSignature = withSignature;
			this.deserializeAs = deserializeAs;
		}

		StructInfo(TypeElement converter, TypeElement target, String name) {
			this.element = target;
			this.name = name;
			this.type = ObjectType.CLASS;
			this.converter = converter.getQualifiedName().toString();
			this.onUnknown = null;
			this.withSignature = null;
			this.deserializeAs = null;
		}
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			return false;
		}
		Set<? extends Element> compiledJsons = roundEnv.getElementsAnnotatedWith(compiledJsonElement);
		Set<? extends Element> jsonConverters = roundEnv.getElementsAnnotatedWith(converterElement);
		if (!compiledJsons.isEmpty()) {
			Map<String, StructInfo> structs = new HashMap<String, StructInfo>();
			CompileOptions options = new CompileOptions();
			List<String> configurations = new ArrayList<String>();
			for (Element el : jsonConverters) {
				findConverters(structs, options, el);
				if (el instanceof TypeElement) {
					TypeElement te = (TypeElement)el;
					if (!el.getModifiers().contains(Modifier.ABSTRACT)) {
						for (TypeElement it : getTypeHierarchy((TypeElement) el)) {
							if ("com.dslplatform.json.Configuration".equals(it.toString())) {
								if (te.getNestingKind().isNested()) {
									configurations.add(te.getEnclosingElement().asType().toString() + "$" + te.getSimpleName().toString());
								} else {
									configurations.add(te.asType().toString());
								}
								break;
							}
						}
					}
				}
			}
			for (Element el : compiledJsons) {
				findStructs(structs, options, el, "CompiledJson requires accessible public no argument constructor");
			}
			findRelatedReferences(structs, options);
			findImplementations(structs.values());
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
				List<String> constants = getEnumConstants(info.element);
				for (String c : constants) {
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
					Map<String, ExecutableElement> methods = getBeanProperties(info.element);
					for (Map.Entry<String, ExecutableElement> p : methods.entrySet()) {
						if (hasIgnoredAnnotation(p.getValue())) {
							continue;
						}
						String dslType = getPropertyType(p.getValue(), p.getValue().getReturnType(), structs);
						TypeMirror javaType = p.getValue().getReturnType();
						processProperty(dsl, options, checks, info, p, dslType, javaType, structs, false);
					}
					Map<String, VariableElement> fields = getPublicFields(info.element);
					for (Map.Entry<String, VariableElement> p : fields.entrySet()) {
						if (methods.containsKey(p.getKey()) || hasIgnoredAnnotation(p.getValue())) {
							continue;
						}
						String dslType = getPropertyType(p.getValue(), p.getValue().asType(), structs);
						TypeMirror javaType = p.getValue().asType();
						processProperty(dsl, options, checks, info, p, dslType, javaType, structs, true);
					}
					if (checkHashCollision(info)) {
						options.hasError = true;
						processingEnv.getMessager().printMessage(
								Diagnostic.Kind.ERROR,
								"Duplicate hash value detected. Unable to create binding for: '" + info.element.getQualifiedName() + "'. Remove (or reduce) alternativeNames from @JsonAttribute to resolve this issue.",
								info.element,
								getAnnotation(info.element, compiledJsonType));
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
				if (info.onUnknown != null) {
					dsl.append("    in ").append(info.name);
					dsl.append(info.onUnknown ? " fail on" : " ignore");
					dsl.append(" unknown;\n");
				} else if (info.deserializeAs != null) {
					StructInfo target = structs.get(info.deserializeAs.asType().toString());
					if (target == null) {
						options.hasError = true;
						processingEnv.getMessager().printMessage(
								Diagnostic.Kind.ERROR,
								"Unable to find DSL-JSON metadata for: '" + info.deserializeAs.getQualifiedName() + "'. Add @CompiledJson annotation to target type.",
								info.element,
								getAnnotation(info.element, compiledJsonType));
					} else {
						dsl.append("    deserialize ").append(info.name).append(" as ").append(target.name).append(";\n");
					}
				}
			}
			dsl.append("}\n");
		}
		dsl.append("}");
		return dsl.toString();
	}

	private static boolean checkHashCollision(StructInfo info) {
		boolean hasAliases = false;
		boolean hasDuplicates = false;
		Set<Integer> counters = new HashSet<Integer>();
		for (Map.Entry<String, String[]> kv : info.properties.entrySet()) {
			String[] aliases = kv.getValue();
			int hash = calcHash(kv.getKey());
			hasDuplicates = hasDuplicates || !counters.add(hash);
			if (aliases != null) {
				hasAliases = true;
				for (String name : aliases) {
					int aliasHash = calcHash(name);
					if (aliasHash == hash) {
						continue;
					}
					hasDuplicates = hasDuplicates || !counters.add(aliasHash);
				}
			}
		}
		return hasAliases && hasDuplicates;
	}

	private <T extends Element> void processProperty(
			StringBuilder dsl,
			CompileOptions options,
			TypeCheck[] checks,
			StructInfo info,
			Map.Entry<String, T> property,
			String dslType,
			TypeMirror javaTypeMirror,
			Map<String, StructInfo> structs,
			boolean fieldAccess) {
		String javaType = javaTypeMirror.toString();
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
							property.getValue(),
							getAnnotation(info.element, compiledJsonType));
				}
				tc.hasFirst = hasFirst;
				tc.hasSecond = hasSecond;
			}
		}
		TypeMirror converter = findConverter(property.getValue());
		if (dslType == null && converter != null) {
			//converters for unknown DSL type must fake some type
			dslType = "String?";
		}
		if (dslType != null) {
			options.useJodaTime = options.useJodaTime || javaType.startsWith("org.joda.time");
			options.useAndroid = options.useAndroid || javaType.startsWith("android.graphics");
			dsl.append("    ");
			dsl.append(dslType);
			dsl.append(" ");
			dsl.append(property.getKey());
			String alias = findNameAlias(property);
			if (info.minifiedNames.containsKey(property.getKey())) {
				alias = info.minifiedNames.get(property.getKey());
			}
			String name = alias != null ? alias : property.getKey();
			String[] deserializationAliases = getAliases(property.getValue());
			if (info.properties.containsKey(name)) {
				options.hasError = true;
				processingEnv.getMessager().printMessage(
						Diagnostic.Kind.ERROR,
						"Duplicate alias detected on " + (fieldAccess ? "field: " : "bean property: ") + property.getKey(),
						property.getValue(),
						getAnnotation(info.element, compiledJsonType));
				return;
			}
			info.properties.put(name, deserializationAliases);
			StructInfo target = findReferenced(javaTypeMirror, structs);
			if (target != null && target.type == ObjectType.MIXIN && target.implementations.size() == 0) {
				String what = target.element.getKind() == ElementKind.INTERFACE ? "interface" : "abstract class";
				String one = target.element.getKind() == ElementKind.INTERFACE ? "implementation" : "concrete extension";
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Property " + property.getKey() +
								" is referencing " + what + " (" + target.element.getQualifiedName() + ") which doesn't have registered " +
								"implementations with @CompiledJson. At least one " + one + " of specified " + what + " must be annotated " +
								"with CompiledJson annotation",
						property.getValue(),
						getAnnotation(info.element, compiledJsonType));
				options.hasError = true;
			}
			if (converter != null) {
				TypeElement typeConverter = processingEnv.getElementUtils().getTypeElement(converter.toString());
				String objectType = "int".equals(javaType) ? "java.lang.Integer"
						: "long".equals(javaType) ? "java.lang.Long"
						: "double".equals(javaType) ? "java.lang.Double"
						: "float".equals(javaType) ? "java.lang.Float"
						: "char".equals(javaType) ? "java.lang.Character"
						: javaType;
				Element declaredType = objectType.equals(javaType)
						? processingEnv.getTypeUtils().asElement(javaTypeMirror)
						: processingEnv.getElementUtils().getTypeElement(objectType);
				validConverter(options, typeConverter, declaredType, objectType);
			}
			boolean isFullMatch = isFullMatch(property.getValue());
			boolean isMandatory = hasMandatoryAnnotation(property.getValue());
			AnnotationMirror propertyAnn = getAnnotation(property.getValue(), attributeType);
			Boolean withSignature = propertyAnn != null ? typeSignatureValue(propertyAnn) : null;
			if (propertyAnn == null && target != null) withSignature = target.withSignature;
			boolean excludeTypeSignature = target != null && target.type == ObjectType.MIXIN && withSignature != null && !withSignature;
			if (fieldAccess || alias != null || deserializationAliases != null || isFullMatch || converter != null || isMandatory || excludeTypeSignature) {
				dsl.append(" {");
				if (fieldAccess) {
					dsl.append("  simple Java access;");
				}
				if (alias != null) {
					dsl.append("  serialization name '");
					dsl.append(alias);
					dsl.append("';");
				}
				if (deserializationAliases != null) {
					for (String da : deserializationAliases) {
						dsl.append("  deserialization alias '");
						dsl.append(da);
						dsl.append("';");
					}
				}
				if (isFullMatch) {
					dsl.append("  deserialization match full;");
				}
				if (isMandatory) {
					dsl.append("  mandatory;");
				}
				if (converter != null) {
					dsl.append("  external Java JSON converter '").append(converter).append("' for '").append(javaType).append("';");
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
					"Specified type not supported: '" + javaType + "'. If you wish to ignore this property,"
							+ " use one of the supported JsonIgnore annotations [such as Jackson @JsonIgnore or DSL-JSON @JsonAttribute(ignore = true) on "
							+ (fieldAccess ? "field" : "getter")
							+ "]. Alternatively register @JsonConverter for this type to support it with custom conversion.",
					property.getValue(),
					getAnnotation(info.element, compiledJsonType));
		}
	}

	private void findRelatedReferences(Map<String, StructInfo> structs, CompileOptions options) {
		int total;
		do {
			total = structs.size();
			List<StructInfo> items = new ArrayList<StructInfo>(structs.values());
			for (StructInfo info : items) {
				if (info.converter != null) {
					continue;
				}
				Map<String, ExecutableElement> properties = getBeanProperties(info.element);
				for (Map.Entry<String, ExecutableElement> p : properties.entrySet()) {
					String propertyType = getPropertyType(p.getValue(), p.getValue().getReturnType(), structs);
					if (propertyType != null) {
						continue;
					}
					checkRelatedProperty(structs, options, p.getValue().getReturnType(), "bean property", info.element, p.getValue());
				}
				Map<String, VariableElement> fields = getPublicFields(info.element);
				for (Map.Entry<String, VariableElement> f : fields.entrySet()) {
					if (properties.containsKey(f.getKey())) {
						continue;
					}
					String propertyType = getPropertyType(f.getValue(), f.getValue().asType(), structs);
					if (propertyType != null) {
						continue;
					}
					checkRelatedProperty(structs, options, f.getValue().asType(), "field", info.element, f.getValue());
				}
			}
		} while (total != structs.size());
	}

	private void findImplementations(Collection<StructInfo> structs) {
		for (StructInfo current : structs) {
			if (current.type == ObjectType.MIXIN) {
				String iface = current.element.asType().toString();
				for (StructInfo info : structs) {
					if (info.type == ObjectType.CLASS) {
						for (TypeMirror type : processingEnv.getTypeUtils().directSupertypes(info.element.asType())) {
							if (type.toString().equals(iface)) {
								current.implementations.add(info);
								break;
							}
						}
					}
				}
			}
		}
	}

	private void checkRelatedProperty(Map<String, StructInfo> structs, CompileOptions options, TypeMirror returnType, String access, Element inside, Element property) {
		TypeMirror converter = findConverter(property);
		if (converter != null) return;
		String typeName = returnType.toString();
		TypeElement el = processingEnv.getElementUtils().getTypeElement(typeName);
		if (el != null) {
			findStructs(structs, options, el, el + " is referenced as " + access + " from '" + inside.asType() + "' through CompiledJson annotation.");
			return;
		}
		if (returnType instanceof ArrayType) {
			ArrayType at = (ArrayType) returnType;
			el = processingEnv.getElementUtils().getTypeElement(at.getComponentType().toString());
			if (el != null) {
				findStructs(structs, options, el, el + " is referenced as array " + access + " from '" + inside.asType() + "' through CompiledJson annotation.");
				return;
			}
		}
		for (Map.Entry<String, String> kv : SupportedCollections.entrySet()) {
			if (!typeName.startsWith(kv.getKey())) {
				continue;
			}
			String elementType = typeName.substring(kv.getKey().length(), typeName.length() - 1);
			el = processingEnv.getElementUtils().getTypeElement(elementType);
			if (el != null) {
				findStructs(structs, options, el, el + " is referenced as collection " + access + " from '" + inside.asType() + "' through CompiledJson annotation.");
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private String[] getAliases(Element property) {
		AnnotationMirror dslAnn = getAnnotation(property, attributeType);
		if (dslAnn != null) {
			Map<? extends ExecutableElement, ? extends AnnotationValue> values = dslAnn.getElementValues();
			for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> ee : values.entrySet()) {
				if (ee.getKey().toString().equals("alternativeNames()")) {
					List<AnnotationValue> val = (List) ee.getValue().getValue();
					if (val == null) return null;
					String[] names = new String[val.size()];
					for (int i = 0; i < val.size(); i++) {
						names[i] = val.get(i).getValue().toString();
					}
					return names;
				}
			}
		}
		return null;
	}

	private boolean isFullMatch(Element property) {
		AnnotationMirror dslAnn = getAnnotation(property, attributeType);
		if (dslAnn != null) {
			Map<? extends ExecutableElement, ? extends AnnotationValue> values = dslAnn.getElementValues();
			for (ExecutableElement ee : values.keySet()) {
				if (ee.toString().equals("hashMatch()")) {
					Object val = values.get(ee).getValue();
					return val != null && !((Boolean) val);
				}
			}
		}
		return false;
	}

	private boolean hasIgnoredAnnotation(Element property) {
		AnnotationMirror dslAnn = getAnnotation(property, attributeType);
		if (dslAnn != null) {
			return booleanAnnotationValue(dslAnn, "ignore()", false);
		}
		for (AnnotationMirror ann : property.getAnnotationMirrors()) {
			if (JsonIgnore.contains(ann.getAnnotationType().toString())) {
				return true;
			}
		}
		return false;
	}

	private boolean hasMandatoryAnnotation(Element property) {
		AnnotationMirror dslAnn = getAnnotation(property, attributeType);
		if (dslAnn != null) {
			return booleanAnnotationValue(dslAnn, "mandatory()", false);
		}
		for (AnnotationMirror ann : property.getAnnotationMirrors()) {
			String value = JsonRequired.get(ann.getAnnotationType().toString());
			if (value != null && booleanAnnotationValue(ann, value, false)) {
				return true;
			}
		}
		return false;
	}

	private static boolean booleanAnnotationValue(AnnotationMirror ann, String method, boolean defaultValue) {
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = ann.getElementValues();
		for (ExecutableElement ee : values.keySet()) {
			if (ee.toString().equals(method)) {
				Object val = values.get(ee).getValue();
				return val == null ? defaultValue : (Boolean) val;
			}
		}
		return defaultValue;
	}

	private Boolean onUnknownValue(AnnotationMirror annotation) {
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();
		for (ExecutableElement ee : values.keySet()) {
			if (ee.toString().equals("onUnknown()")) {
				Object val = values.get(ee).getValue();
				if (val == null || "DEFAULT".equals(val.toString())) return null;
				return "FAIL".equals(val.toString());
			}
		}
		return null;
	}

	private Boolean typeSignatureValue(AnnotationMirror annotation) {
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();
		for (ExecutableElement ee : values.keySet()) {
			if (ee.toString().equals("typeSignature()")) {
				Object val = values.get(ee).getValue();
				if (val == null) return null;
				return !"EXCLUDE".equals(val.toString());
			}
		}
		return null;
	}

	private boolean isMinified(Element struct) {
		AnnotationMirror ann = getAnnotation(struct, compiledJsonType);
		if (ann != null) {
			for (ExecutableElement ee : ann.getElementValues().keySet()) {
				if ("minified()".equals(ee.toString())) {
					AnnotationValue minified = ann.getElementValues().get(ee);
					return (Boolean) minified.getValue();
				}
			}
		}
		return false;
	}

	private TypeMirror findConverter(Element property) {
		AnnotationMirror dslAnn = getAnnotation(property, attributeType);
		if (dslAnn != null) {
			Map<? extends ExecutableElement, ? extends AnnotationValue> values = dslAnn.getElementValues();
			for (ExecutableElement ee : values.keySet()) {
				if (ee.toString().equals("converter()")) {
					TypeMirror mirror = (TypeMirror) values.get(ee).getValue();
					return mirror != null && mirror.toString().equals("com.dslplatform.json.JsonAttribute") ? null : mirror;
				}
			}
			return null;
		}
		return null;
	}

	private <T extends Element> String findNameAlias(Map.Entry<String, T> property) {
		AnnotationMirror dslAnn = getAnnotation(property.getValue(), attributeType);
		if (dslAnn != null) {
			Map<? extends ExecutableElement, ? extends AnnotationValue> values = dslAnn.getElementValues();
			for (ExecutableElement ee : values.keySet()) {
				if (ee.toString().equals("name()")) {
					String val = (String) values.get(ee).getValue();
					if (val != null && val.length() == 0) return null;
					return val;
				}
			}
			return null;
		}
		for (AnnotationMirror ann : property.getValue().getAnnotationMirrors()) {
			if (PropertyAlias.contains(ann.getAnnotationType().toString())) {
				for (ExecutableElement ee : ann.getElementValues().keySet()) {
					if ("value()".equals(ee.toString())) {
						AnnotationValue alias = ann.getElementValues().get(ee);
						return alias.getValue().toString();
					}
				}
			}
		}
		return null;
	}

	private void findConverters(Map<String, StructInfo> structs, CompileOptions options, Element el) {
		AnnotationMirror dslAnn = getAnnotation(el, converterType);
		if (!(el instanceof TypeElement) || dslAnn == null) {
			return;
		}
		TypeElement converter = (TypeElement) el;
		DeclaredType target = null;
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = dslAnn.getElementValues();
		for (ExecutableElement ee : values.keySet()) {
			if (ee.toString().equals("target()")) {
				target = (DeclaredType) values.get(ee).getValue();
				break;
			}
		}
		if (target == null) return;
		if (validConverter(options, converter, target.asElement(), target.toString())) {
			StructInfo existing = structs.get(target.toString());
			String name = existing == null ? "struct" + structs.size() : existing.name;
			//TODO: throw an error if multiple non-compatible converters were found!?
			TypeElement element = (TypeElement) target.asElement();
			StructInfo info = new StructInfo(converter, element, name);
			structs.put(target.toString(), info);
		}
	}

	private boolean validConverter(CompileOptions options, TypeElement converter, Element target, String fullName) {
		VariableElement jsonReader = null;
		VariableElement jsonWriter = null;
		for (VariableElement field : ElementFilter.fieldsIn(converter.getEnclosedElements())) {
			if ("JSON_READER".equals(field.getSimpleName().toString())) {
				jsonReader = field;
			} else if ("JSON_WRITER".equals(field.getSimpleName().toString())) {
				jsonWriter = field;
			}
		}
		if (!converter.getModifiers().contains(Modifier.PUBLIC)) {
			options.hasError = true;
			processingEnv.getMessager().printMessage(
					Diagnostic.Kind.ERROR,
					"Specified converter: '" + converter.asType() + "' must be public",
					converter,
					getAnnotation(converter, converterType));
		} else if (!target.getModifiers().contains(Modifier.PUBLIC)) {
			options.hasError = true;
			processingEnv.getMessager().printMessage(
					Diagnostic.Kind.ERROR,
					"Specified converter target: '" + fullName + "' must be public",
					converter,
					getAnnotation(converter, converterType));
		} else if (converter.getNestingKind().isNested() && !converter.getModifiers().contains(Modifier.STATIC)) {
			options.hasError = true;
			processingEnv.getMessager().printMessage(
					Diagnostic.Kind.ERROR,
					"Specified converter: '" + converter.asType() + "' can't be a nested member. Only public static nested classes are supported",
					converter,
					getAnnotation(converter, converterType));
		} else if (converter.getQualifiedName().contentEquals(converter.getSimpleName())
				|| converter.getNestingKind().isNested() && converter.getModifiers().contains(Modifier.STATIC)
				&& converter.getEnclosingElement() instanceof TypeElement
				&& ((TypeElement) converter.getEnclosingElement()).getQualifiedName().contentEquals(converter.getEnclosingElement().getSimpleName())) {
			options.hasError = true;
			processingEnv.getMessager().printMessage(
					Diagnostic.Kind.ERROR,
					"Specified converter: '" + converter.getQualifiedName() + "' is defined without a package name and cannot be accessed",
					converter,
					getAnnotation(converter, converterType));
		} else if (jsonReader == null || jsonWriter == null) {
			options.hasError = true;
			processingEnv.getMessager().printMessage(
					Diagnostic.Kind.ERROR,
					"Specified converter: '" + converter.getQualifiedName() + "' doesn't have a JSON_READER or JSON_WRITER field. It must have public static JSON_READER/JSON_WRITER fields for conversion.",
					converter,
					getAnnotation(converter, converterType));
		} else if (!jsonReader.getModifiers().contains(Modifier.PUBLIC)
				|| !jsonReader.getModifiers().contains(Modifier.STATIC)
				|| !jsonWriter.getModifiers().contains(Modifier.PUBLIC)
				|| !jsonWriter.getModifiers().contains(Modifier.STATIC)) {
			options.hasError = true;
			processingEnv.getMessager().printMessage(
					Diagnostic.Kind.ERROR,
					"Specified converter: '" + converter.getQualifiedName() + "' doesn't have public and static JSON_READER and JSON_WRITER fields. They must be public and static for converter to work properly.",
					converter,
					getAnnotation(converter, converterType));
		} else if (!("com.dslplatform.json.JsonReader.ReadObject<" + fullName + ">").equals(jsonReader.asType().toString())) {
			options.hasError = true;
			processingEnv.getMessager().printMessage(
					Diagnostic.Kind.ERROR,
					"Specified converter: '" + converter.getQualifiedName() + "' has invalid type for JSON_READER field. It must be of type: 'com.dslplatform.json.JsonReader.ReadObject<" + target + ">'",
					converter,
					getAnnotation(converter, converterType));
		} else if (!("com.dslplatform.json.JsonWriter.WriteObject<" + fullName + ">").equals(jsonWriter.asType().toString())) {
			options.hasError = true;
			processingEnv.getMessager().printMessage(
					Diagnostic.Kind.ERROR,
					"Specified converter: '" + converter.getQualifiedName() + "' has invalid type for JSON_WRITER field. It must be of type: 'com.dslplatform.json.JsonWriter.WriteObject<" + target + ">'",
					converter,
					getAnnotation(converter, converterType));
		} else return true;
		return false;
	}

	private void findStructs(Map<String, StructInfo> structs, CompileOptions options, Element el, String errorMessge) {
		if (!(el instanceof TypeElement)) {
			return;
		}
		if (structs.containsKey(el.asType().toString())) return;
		TypeElement element = (TypeElement) el;
		boolean isMixin = element.getKind() == ElementKind.INTERFACE
				|| element.getKind() == ElementKind.CLASS && element.getModifiers().contains(Modifier.ABSTRACT);
		boolean isJsonObject = isJsonObject(element);
		AnnotationMirror annotation = getAnnotation(element, compiledJsonType);
		if (!isJsonObject && !isMixin && element.getKind() != ElementKind.ENUM && !hasEmptyCtor(element)) {
			options.hasError = true;
			processingEnv.getMessager().printMessage(
					Diagnostic.Kind.ERROR,
					errorMessge + ", therefore '" + element.asType() + "' requires public no argument constructor",
					element,
					annotation);
		} else if (!element.getModifiers().contains(Modifier.PUBLIC)) {
			options.hasError = true;
			processingEnv.getMessager().printMessage(
					Diagnostic.Kind.ERROR,
					errorMessge + ", therefore '" + element.asType() + "' must be public",
					element,
					annotation);
		} else if (element.getNestingKind().isNested() && !element.getModifiers().contains(Modifier.STATIC)) {
			options.hasError = true;
			processingEnv.getMessager().printMessage(
					Diagnostic.Kind.ERROR,
					errorMessge + ", therefore '" + element.asType() + "' can't be a nested member. Only static nested classes are supported",
					element,
					annotation);
		} else if (element.getQualifiedName().contentEquals(element.getSimpleName())
				|| element.getNestingKind().isNested() && element.getModifiers().contains(Modifier.STATIC)
				&& element.getEnclosingElement() instanceof TypeElement
				&& ((TypeElement) element.getEnclosingElement()).getQualifiedName().contentEquals(element.getEnclosingElement().getSimpleName())) {
			options.hasError = true;
			processingEnv.getMessager().printMessage(
					Diagnostic.Kind.ERROR,
					errorMessge + ", but class '" + element.getQualifiedName() + "' is defined without a package name and cannot be accessed",
					element,
					annotation);
		} else {
			ObjectType type = isMixin ? ObjectType.MIXIN : element.getKind() == ElementKind.ENUM ? ObjectType.ENUM : ObjectType.CLASS;
			Boolean onUnknown = null;
			Boolean withSignature = null;
			TypeElement deserializeAs = null;
			if (!isJsonObject) {
				if (annotation != null) {
					onUnknown = onUnknownValue(annotation);
					withSignature = typeSignatureValue(annotation);
					deserializeAs = deserializeAs(annotation);
					if (deserializeAs != null) {
						String error = validateDeserializeAs(element, deserializeAs);
						if (error != null) {
							options.hasError = true;
							processingEnv.getMessager().printMessage(
									Diagnostic.Kind.ERROR,
									errorMessge + ", but specified deserializeAs target: '" + deserializeAs.getQualifiedName() + "' " + error,
									element,
									annotation);
							deserializeAs = null;//reset it so that later lookup don't add another error message
						} else {
							if (deserializeAs.asType().toString().equals(element.asType().toString())) {
								deserializeAs = null;
							} else {
								findStructs(structs, options, deserializeAs, errorMessge);
							}
						}
					}
				} else if (annotationUsage != AnnotationUsage.IMPLICIT) {
					if (annotationUsage == AnnotationUsage.EXPLICIT) {
						processingEnv.getMessager().printMessage(
								Diagnostic.Kind.ERROR,
								"Annotation usage is set to explicit, but '" + element.getQualifiedName() + "' is used implicitly through references. " +
										"Either change usage to implicit or use @Ignore on property referencing this type. " + errorMessge,
								element);
					} else if (element.getQualifiedName().toString().startsWith("java.")) {
						processingEnv.getMessager().printMessage(
								Diagnostic.Kind.ERROR,
								"Annotation usage is set to non-java, but '" + element.getQualifiedName() + "' is found in java package. " +
										"Either change usage to implicit, use @Ignore on property referencing this type or add annotation to this type. " +
										errorMessge,
								element);
					}
				}
			}
			String name = "struct" + structs.size();
			StructInfo info = new StructInfo(element, name, type, isJsonObject, onUnknown, withSignature, deserializeAs);
			structs.put(element.asType().toString(), info);
			if (isMinified(element)) {
				prepareMinifiedNames(info);
			}
		}
	}

	private String validateDeserializeAs(TypeElement source, TypeElement target) {
		if (!target.getModifiers().contains(Modifier.PUBLIC)) {
			return "must be public";
		} else if (target.getNestingKind().isNested() && !target.getModifiers().contains(Modifier.STATIC)) {
			return "can't be a nested member. Only public static nested classes are supported";
		} else if (target.getQualifiedName().contentEquals(target.getSimpleName())
				|| target.getNestingKind().isNested() && target.getModifiers().contains(Modifier.STATIC)
				&& target.getEnclosingElement() instanceof TypeElement
				&& ((TypeElement) target.getEnclosingElement()).getQualifiedName().contentEquals(target.getEnclosingElement().getSimpleName())) {
			return "is defined without a package name and cannot be accessed";
		} else if (target.getKind() == ElementKind.INTERFACE || target.getModifiers().contains(Modifier.ABSTRACT)) {
			return "must be a concrete type";
		} else if (!source.asType().toString().equals(target.asType().toString()) && source.getKind() != ElementKind.INTERFACE && !source.getModifiers().contains(Modifier.ABSTRACT)) {
			return "can only be specified for interfaces and abstract classes. '" + source + "' is neither interface nor abstract class";
		} else if (!processingEnv.getTypeUtils().isAssignable(target.asType(), source.asType())) {
			return "is not assignable to '" + source.getQualifiedName() + "'";
		} else {
			return null;
		}
	}

	private void prepareMinifiedNames(StructInfo info) {
		Map<Character, Integer> counters = new HashMap<Character, Integer>();
		Map<String, ExecutableElement> methods = getBeanProperties(info.element);
		Map<String, VariableElement> fields = getPublicFields(info.element);
		Set<String> processedProperties = new HashSet<String>();
		Set<String> names = new HashSet<String>();
		for (Map.Entry<String, ExecutableElement> p : methods.entrySet()) {
			if (hasIgnoredAnnotation(p.getValue())) {
				continue;
			}
			String alias = findNameAlias(p);
			if (alias != null) {
				info.minifiedNames.put(p.getKey(), alias);
				processedProperties.add(p.getKey());
				names.add(alias);
			}
		}
		for (Map.Entry<String, VariableElement> p : fields.entrySet()) {
			if (methods.containsKey(p.getKey()) || hasIgnoredAnnotation(p.getValue())) {
				continue;
			}
			String alias = findNameAlias(p);
			if (alias != null) {
				info.minifiedNames.put(p.getKey(), alias);
				processedProperties.add(p.getKey());
				names.add(alias);
			}
		}
		for (Map.Entry<String, ExecutableElement> p : methods.entrySet()) {
			if (processedProperties.contains(p.getKey()) || hasIgnoredAnnotation(p.getValue())) {
				continue;
			}
			String shortName = buildShortName(p.getKey(), names, counters);
			info.minifiedNames.put(p.getKey(), shortName);
		}
		for (Map.Entry<String, VariableElement> p : fields.entrySet()) {
			if (processedProperties.contains(p.getKey()) || methods.containsKey(p.getKey()) || hasIgnoredAnnotation(p.getValue())) {
				continue;
			}
			String shortName = buildShortName(p.getKey(), names, counters);
			info.minifiedNames.put(p.getKey(), shortName);
		}
	}

	private static String buildShortName(String name, Set<String> names, Map<Character, Integer> counters) {
		String shortName = name.substring(0, 1);
		Character first = name.charAt(0);
		if (!names.contains(shortName)) {
			names.add(shortName);
			counters.put(first, 0);
			return shortName;
		}
		Integer next = counters.get(first);
		if (next == null) {
			next = 0;
		}
		do {
			shortName = first.toString() + next;
			next++;
		} while (names.contains(shortName));
		counters.put(first, next);
		names.add(shortName);
		return shortName;
	}

	private static boolean hasEmptyCtor(Element element) {
		for (ExecutableElement constructor : ElementFilter.constructorsIn(element.getEnclosedElements())) {
			List<? extends VariableElement> parameters = constructor.getParameters();
			if (parameters.isEmpty()
					&& (element.getKind() == ElementKind.ENUM || constructor.getModifiers().contains(Modifier.PUBLIC))) {
				return true;
			}
		}
		return false;
	}

	private static List<String> getEnumConstants(TypeElement element) {
		List<String> result = new ArrayList<String>();
		for (VariableElement field : ElementFilter.fieldsIn(element.getEnclosedElements())) {
			//Use to string comparison since compiler can create two separate instances which differ
			if (field.asType().toString().equals(element.asType().toString())) {
				result.add(field.getSimpleName().toString());
			}
		}
		return result;
	}

	private List<TypeElement> getTypeHierarchy(TypeElement element) {
		List<TypeElement> result = new ArrayList<TypeElement>();
		result.add(element);
		for (TypeMirror type : processingEnv.getTypeUtils().directSupertypes(element.asType())) {
			Element current = processingEnv.getTypeUtils().asElement(type);
			if (current instanceof TypeElement) {
				result.add((TypeElement) current);
			}
		}
		return result;
	}

	private boolean isJsonObject(TypeElement element) {
		for (TypeMirror type : element.getInterfaces()) {
			if ("com.dslplatform.json.JsonObject".equals(type.toString())) {
				for (VariableElement field : ElementFilter.fieldsIn(element.getEnclosedElements())) {
					if ("JSON_READER".equals(field.getSimpleName().toString())) {
						if (!field.getModifiers().contains(Modifier.PUBLIC)
								|| !field.getModifiers().contains(Modifier.STATIC)) {
							if (logLevel.isVisible(AnnotationCompiler.LogLevel.INFO)) {
								processingEnv.getMessager().printMessage(
										Diagnostic.Kind.WARNING,
										"'" + element.getQualifiedName() + "' is 'com.dslplatform.json.JsonObject', but it's JSON_READER field is not public and static. " +
												"It can't be used for serialization/deserialization this way. " +
												"You probably want to change JSON_READER field so it's public and static.",
										element);
							}
							return false;
						}
						String correctType = "com.dslplatform.json.JsonReader.ReadJsonObject<" + element.getQualifiedName() + ">";
						if (!(correctType.equals(field.asType().toString()))) {
							if (logLevel.isVisible(AnnotationCompiler.LogLevel.INFO)) {
								processingEnv.getMessager().printMessage(
										Diagnostic.Kind.WARNING,
										"'" + element.getQualifiedName() + "' is 'com.dslplatform.json.JsonObject', but it's JSON_READER field is not of correct type. " +
												"It can't be used for serialization/deserialization this way. " +
												"You probably want to change JSON_READER field to: '" + correctType + "'",
										element);
							}
							return false;
						}
						return true;
					}
				}
				if (logLevel.isVisible(AnnotationCompiler.LogLevel.INFO)) {
					processingEnv.getMessager().printMessage(
							Diagnostic.Kind.WARNING,
							"'" + element.getQualifiedName() + "' is 'com.dslplatform.json.JsonObject', but it doesn't have JSON_READER field. " +
									"It can't be used for serialization/deserialization this way. " +
									"You probably want to add public static JSON_READER field.",
							element);
				}
				return false;
			}
		}
		return false;
	}

	private Map<String, ExecutableElement> getBeanProperties(TypeElement element) {
		Map<String, VariableElement> setters = new HashMap<String, VariableElement>();
		Map<String, ExecutableElement> getters = new HashMap<String, ExecutableElement>();
		for (TypeElement inheritance : getTypeHierarchy(element)) {
			boolean isPublicInterface = inheritance.getKind() == ElementKind.INTERFACE
					&& inheritance.getModifiers().contains(Modifier.PUBLIC);
			for (ExecutableElement method : ElementFilter.methodsIn(inheritance.getEnclosedElements())) {
				String name = method.getSimpleName().toString();
				boolean isAccessible = isPublicInterface && !method.getModifiers().contains(Modifier.PRIVATE)
						|| method.getModifiers().contains(Modifier.PUBLIC)
						&& !method.getModifiers().contains(Modifier.STATIC)
						&& !method.getModifiers().contains(Modifier.ABSTRACT);
				if (name.length() < 4 || !isAccessible) {
					continue;
				}
				String property = name.substring(3).toUpperCase().equals(name.substring(3)) && name.length() > 4
						? name.substring(3)
						: name.substring(3, 4).toLowerCase() + name.substring(4);
				if (name.startsWith("get")
						&& method.getParameters().size() == 0
						&& method.getReturnType() != null) {
					if (!getters.containsKey(property)) {
						getters.put(property, method);
					}
				} else if (name.startsWith("set")
						&& method.getParameters().size() == 1) {
					setters.put(property, method.getParameters().get(0));
				}
			}
		}
		Map<String, ExecutableElement> result = new HashMap<String, ExecutableElement>();
		for (Map.Entry<String, ExecutableElement> kv : getters.entrySet()) {
			VariableElement setterArgument = setters.get(kv.getKey());
			if (setterArgument != null && setterArgument.asType().equals(kv.getValue().getReturnType())) {
				result.put(kv.getKey(), kv.getValue());
			} else if (setterArgument != null && (setterArgument.asType() + "<").startsWith(kv.getValue().getReturnType().toString())) {
				result.put(kv.getKey(), kv.getValue());
			}
		}
		return result;
	}

	private Map<String, VariableElement> getPublicFields(TypeElement element) {
		Map<String, VariableElement> fields = new HashMap<String, VariableElement>();
		for (TypeElement inheritance : getTypeHierarchy(element)) {
			for (VariableElement field : ElementFilter.fieldsIn(inheritance.getEnclosedElements())) {
				String name = field.getSimpleName().toString();
				boolean isAccessible = field.getModifiers().contains(Modifier.PUBLIC)
						&& !field.getModifiers().contains(Modifier.FINAL)
						&& !field.getModifiers().contains(Modifier.STATIC);
				if (!isAccessible) {
					continue;
				}
				fields.put(name, field);
			}
		}
		return fields;
	}

	private AnnotationMirror getAnnotation(Element element, DeclaredType annotationType) {
		for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
			if (processingEnv.getTypeUtils().isSameType(mirror.getAnnotationType(), annotationType)) {
				return mirror;
			}
		}
		return null;
	}

	private boolean hasNonNullable(Element property) {
		AnnotationMirror dslAnn = getAnnotation(property, attributeType);
		if (dslAnn != null) {
			Map<? extends ExecutableElement, ? extends AnnotationValue> values = dslAnn.getElementValues();
			for (ExecutableElement ee : values.keySet()) {
				if (ee.toString().equals("nullable()")) {
					Object val = values.get(ee).getValue();
					return val != null && !((Boolean) val);
				}
			}
			return false;
		}
		for (AnnotationMirror ann : property.getAnnotationMirrors()) {
			if (NonNullable.contains(ann.getAnnotationType().toString())) {
				return true;
			}
		}
		return false;
	}

	private TypeElement deserializeAs(AnnotationMirror annotation) {
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();
		for (ExecutableElement ee : values.keySet()) {
			if (ee.toString().equals("deserializeAs()")) {
				DeclaredType target = (DeclaredType) values.get(ee).getValue();
				return (TypeElement)target.asElement();
			}
		}
		return null;
	}

	private String getPropertyType(Element element, TypeMirror type, Map<String, StructInfo> structs) {
		String simpleType = SupportedTypes.get(type.toString());
		boolean hasNonNullable = hasNonNullable(element);
		if (simpleType != null) {
			return simpleType.endsWith("?") && hasNonNullable
					? simpleType.substring(0, simpleType.length() - 1)
					: simpleType;
		}
		if (type instanceof ArrayType) {
			ArrayType at = (ArrayType) type;
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
			if (type.toString().startsWith(kv.getKey())) {
				String typeName = type.toString().substring(kv.getKey().length(), type.toString().length() - 1);
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
		StructInfo info = structs.get(type.toString());
		if (info != null) {
			return "json." + info.name + (hasNonNullable ? "" : "?");
		}
		return null;
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

	private static int calcHash(String name) {
		long hash = 0x811c9dc5;
		for (int i = 0; i < name.length(); i++) {
			byte b = (byte) name.charAt(i);
			hash ^= b;
			hash *= 0x1000193;
		}
		return (int) hash;
	}
}
