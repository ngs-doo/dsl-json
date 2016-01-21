package com.dslplatform.json;

import com.dslplatform.compiler.client.AnnotationCompiler;

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

@SupportedAnnotationTypes({"com.dslplatform.json.CompiledJson"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions({"dsljson.namespace"})
public class CompiledJsonProcessor extends AbstractProcessor {

	private static final Map<String, String> SupportedTypes;
	private static final Map<String, String> SupportedCollections;
	private static final Set<String> JsonIgnore;
	private static final Set<String> NonNullable;
	private static final Set<String> PropertyAlias;
	private static final List<IncompatibleTypes> CheckTypes;

	private static final String CONFIG = "META-INF/services/com.dslplatform.json.Configuration";

	private static class IncompatibleTypes {
		public final String first;
		public final String second;
		public final String description;

		public IncompatibleTypes(String first, String second, String description) {
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

	private TypeElement jsonTypeElement;
	private DeclaredType jsonDeclaredType;
	private String namespace;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		jsonTypeElement = processingEnv.getElementUtils().getTypeElement("com.dslplatform.json.CompiledJson");
		jsonDeclaredType = processingEnv.getTypeUtils().getDeclaredType(jsonTypeElement);
		Map<String, String> options = processingEnv.getOptions();
		String ns = options.get("dsljson.namespace");
		if (ns != null && ns.length() > 0) {
			namespace = ns;
		} else {
			namespace = "dsl_json";
		}
	}

	private static class CompileOptions {
		public boolean useJodaTime;
		public boolean useAndroid;
		public boolean hasError;

		public AnnotationCompiler.CompileOptions toOptions(String namespace) {
			AnnotationCompiler.CompileOptions options = new AnnotationCompiler.CompileOptions();
			options.namespace = namespace;
			options.useAndroid = useAndroid;
			options.useJodaTime = useJodaTime;
			return options;
		}
	}

	private static class StructInfo {
		public final TypeElement element;
		public final String name;
		public final boolean isEnum;
		public final Set<String> properties = new HashSet<String>();
		public final Map<String, String> minifiedNames = new HashMap<String, String>();

		public StructInfo(TypeElement element, String name, boolean isEnum) {
			this.element = element;
			this.name = name;
			this.isEnum = isEnum;
		}
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			return false;
		}
		Set<? extends Element> jsonAnnotated = roundEnv.getElementsAnnotatedWith(jsonTypeElement);
		if (!jsonAnnotated.isEmpty()) {
			Map<String, StructInfo> structs = new HashMap<String, StructInfo>();
			CompileOptions options = new CompileOptions();
			for (Element el : jsonAnnotated) {
				findStructs(structs, options, el, "CompiledJson requires public no argument constructor");
			}
			findRelatedReferences(structs, options);
			String dsl = buildDsl(structs, options);

			if (options.hasError) {
				return false;
			}

			processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, dsl);

			String fileContent;
			try {
				fileContent = AnnotationCompiler.buildExternalJson(dsl, options.toOptions(namespace));
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
				writer.close();
			} catch (IOException e) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed saving compiled json serialization files");
			}
		}
		return false;
	}

	private static class TypeCheck {
		public boolean hasFirst;
		public boolean hasSecond;
	}

	private String buildDsl(Map<String, StructInfo> structs, CompileOptions options) {
		StringBuilder dsl = new StringBuilder();
		dsl.append("module json {\n");
		TypeCheck[] checks = new TypeCheck[CheckTypes.size()];
		for (int i = 0; i < checks.length; i++) {
			checks[i] = new TypeCheck();
		}
		for (StructInfo info : structs.values()) {
			if (info.isEnum) {
				dsl.append("  enum ");
			} else {
				dsl.append("  struct ");
			}
			dsl.append(info.name);
			dsl.append(" {\n");

			if (info.isEnum) {
				List<String> constants = getEnumConstants(info.element);
				for (String c : constants) {
					dsl.append("    ");
					dsl.append(c);
					dsl.append(";\n");
				}
			} else {
				Map<String, ExecutableElement> methods = getBeanProperties(info.element);
				for (Map.Entry<String, ExecutableElement> p : methods.entrySet()) {
					if (hasIgnoredAnnotation(p.getValue())) {
						continue;
					}
					String dslType = getPropertyType(p.getValue(), p.getValue().getReturnType(), structs);
					String javaType = p.getValue().getReturnType().toString();
					processProperty(dsl, options, checks, info, p, dslType, javaType, false);
				}
				Map<String, VariableElement> fields = getPublicFields(info.element);
				for (Map.Entry<String, VariableElement> p : fields.entrySet()) {
					if (methods.containsKey(p.getKey()) || hasIgnoredAnnotation(p.getValue())) {
						continue;
					}
					String dslType = getPropertyType(p.getValue(), p.getValue().asType(), structs);
					String javaType = p.getValue().asType().toString();
					processProperty(dsl, options, checks, info, p, dslType, javaType, true);
				}
			}
			dsl.append("    external name Java '");
			dsl.append(info.element.getQualifiedName());
			dsl.append("';\n  }\n");
		}
		dsl.append("}");
		return dsl.toString();
	}

	private <T extends Element> void processProperty(
			StringBuilder dsl,
			CompileOptions options,
			TypeCheck[] checks,
			StructInfo info,
			Map.Entry<String, T> property,
			String dslType,
			String javaType,
			boolean fieldAccess) {
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
							getAnnotation(info.element, jsonDeclaredType));
				}
				tc.hasFirst = hasFirst;
				tc.hasSecond = hasSecond;
			}
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
			if (!info.properties.add(name)) {
				options.hasError = true;
				processingEnv.getMessager().printMessage(
						Diagnostic.Kind.ERROR,
						"Duplicate alias detected on " + (fieldAccess ? "field: " : "bean property: ") + property.getKey(),
						property.getValue(),
						getAnnotation(info.element, jsonDeclaredType));
				return;
			}
			if (fieldAccess || alias != null) {
				dsl.append(" {");
				if (fieldAccess) {
					dsl.append("  simple Java access;");
				}
				if (alias != null) {
					dsl.append("  serialization name '");
					dsl.append(alias);
					dsl.append("';");
				}
				dsl.append("  }\n");
			} else {
				dsl.append(";\n");
			}
		} else {
			options.hasError = true;
			processingEnv.getMessager().printMessage(
					Diagnostic.Kind.ERROR,
					"Specified type not supported. If you wish to ignore this property,\n"
							+ " use one of the supported JsonIgnore annotations (such as Jackson @JsonIgnore) on "
							+ (fieldAccess ? "field" : "getter"),
					property.getValue(),
					getAnnotation(info.element, jsonDeclaredType));
		}
	}

	private void findRelatedReferences(Map<String, StructInfo> structs, CompileOptions options) {
		int total;
		do {
			total = structs.size();
			List<StructInfo> items = new ArrayList<StructInfo>(structs.values());
			for (StructInfo info : items) {
				Map<String, ExecutableElement> properties = getBeanProperties(info.element);
				for (Map.Entry<String, ExecutableElement> p : properties.entrySet()) {
					String propertyType = getPropertyType(p.getValue(), p.getValue().getReturnType(), structs);
					if (propertyType != null) {
						continue;
					}
					checkRelatedProperty(structs, options, p.getValue().getReturnType(), "bean property");
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
					checkRelatedProperty(structs, options, f.getValue().asType(), "field");
				}
			}
		} while (total != structs.size());
	}

	private void checkRelatedProperty(Map<String, StructInfo> structs, CompileOptions options, TypeMirror returnType, String access) {
		String typeName = returnType.toString();
		TypeElement el = processingEnv.getElementUtils().getTypeElement(typeName);
		if (el != null) {
			findStructs(structs, options, el, el + " is referenced as " + access + " from POJO with CompiledJson annotation.");
			return;
		}
		if (returnType instanceof ArrayType) {
			ArrayType at = (ArrayType) returnType;
			el = processingEnv.getElementUtils().getTypeElement(at.getComponentType().toString());
			if (el != null) {
				findStructs(structs, options, el, el + " is referenced as array " + access + " from POJO with CompiledJson annotation.");
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
				findStructs(structs, options, el, el + " is referenced as collection " + access + " from POJO with CompiledJson annotation.");
				break;
			}
		}
	}

	private static boolean hasIgnoredAnnotation(Element property) {
		for (AnnotationMirror ann : property.getAnnotationMirrors()) {
			if (JsonIgnore.contains(ann.getAnnotationType().toString())) {
				return true;
			}
		}
		return false;
	}

	private static boolean isMinified(Element struct) {
		for (AnnotationMirror ann : struct.getAnnotationMirrors()) {
			if ("com.dslplatform.json.CompiledJson".equals(ann.getAnnotationType().toString())) {
				for (ExecutableElement ee : ann.getElementValues().keySet()) {
					if ("minified()".equals(ee.toString())) {
						AnnotationValue minified = ann.getElementValues().get(ee);
						return (Boolean) minified.getValue();
					}
				}
			}
		}
		return false;
	}

	private static <T extends Element> String findNameAlias(Map.Entry<String, T> property) {
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

	private void findStructs(Map<String, StructInfo> structs, CompileOptions options, Element el, String errorMessge) {
		if (!(el instanceof TypeElement)) {
			return;
		}
		TypeElement element = (TypeElement) el;
		if (!hasEmptyCtor(element)) {
			options.hasError = true;
			AnnotationMirror entityAnnotation = getAnnotation(element, jsonDeclaredType);
			processingEnv.getMessager().printMessage(
					Diagnostic.Kind.ERROR,
					errorMessge + ", therefore it requires public no argument constructor",
					element,
					entityAnnotation);
		} else if (!element.getModifiers().contains(Modifier.PUBLIC)) {
			options.hasError = true;
			AnnotationMirror entityAnnotation = getAnnotation(element, jsonDeclaredType);
			processingEnv.getMessager().printMessage(
					Diagnostic.Kind.ERROR,
					errorMessge + ", therefore class must be public",
					element,
					entityAnnotation);
		} else if (element.getNestingKind().isNested() && !element.getModifiers().contains(Modifier.STATIC)) {
			options.hasError = true;
			AnnotationMirror entityAnnotation = getAnnotation(element, jsonDeclaredType);
			processingEnv.getMessager().printMessage(
					Diagnostic.Kind.ERROR,
					errorMessge + ", therefore class can't be nested member. Only static nested classes class are supported",
					element,
					entityAnnotation);
			//TODO: other checks
		} else {
			String name = "struct" + structs.size();
			StructInfo info = new StructInfo(element, name, element.getKind() == ElementKind.ENUM);
			structs.put(element.asType().toString(), info);
			if (isMinified(element)) {
				prepareMinifiedNames(info);
			}
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
		Integer next = counters.get(first);
		if (!names.contains(shortName)) {
			names.add(shortName);
			counters.put(first, 0);
			return shortName;
		}
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

	private static Map<String, ExecutableElement> getBeanProperties(TypeElement element) {
		Map<String, VariableElement> setters = new HashMap<String, VariableElement>();
		Map<String, ExecutableElement> getters = new HashMap<String, ExecutableElement>();
		for (ExecutableElement method : ElementFilter.methodsIn(element.getEnclosedElements())) {
			String name = method.getSimpleName().toString();
			boolean isAccessible = method.getModifiers().contains(Modifier.PUBLIC)
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
				getters.put(property, method);
			} else if (name.startsWith("set")
					&& method.getParameters().size() == 1) {
				setters.put(property, method.getParameters().get(0));
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

	private static Map<String, VariableElement> getPublicFields(TypeElement element) {
		Map<String, VariableElement> fields = new HashMap<String, VariableElement>();
		for (VariableElement field : ElementFilter.fieldsIn(element.getEnclosedElements())) {
			String name = field.getSimpleName().toString();
			boolean isAccessible = field.getModifiers().contains(Modifier.PUBLIC)
					&& !field.getModifiers().contains(Modifier.FINAL)
					&& !field.getModifiers().contains(Modifier.STATIC);
			if (!isAccessible) {
				continue;
			}
			fields.put(name, field);
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

	private static boolean hasNonNullable(Element property) {
		for (AnnotationMirror ann : property.getAnnotationMirrors()) {
			if (NonNullable.contains(ann.getAnnotationType().toString())) {
				return true;
			}
		}
		return false;
	}

	private static String getPropertyType(Element element, TypeMirror type, Map<String, StructInfo> structs) {
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
}
