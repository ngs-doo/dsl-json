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
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

@SupportedAnnotationTypes({"com.dslplatform.json.CompiledJson"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions({"dsljson.namespace", "dsljson.timeApi"})
public class CompiledJsonProcessor extends AbstractProcessor {

	private static final Map<String, String> SupportedTypes;
	private static final Map<String, String> SupportedCollections;
	private static final HashSet<String> JsonIgnore;
	private static final HashSet<String> NonNullable;
	private static final HashSet<String> PropertyAlias;

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
		SupportedTypes.put("java.util.Map<java.lang.String,java.lang.String>", "map?");
		SupportedTypes.put("java.net.InetAddress", "ip?");
		SupportedTypes.put("java.awt.Color", "color?");
		SupportedTypes.put("java.awt.geom.Rectangle2D", "rectangle?");
		SupportedTypes.put("java.awt.image.BufferedImage", "image?");
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
	}

	private TypeElement jsonTypeElement;
	private DeclaredType jsonDeclaredType;
	private String namespace = "dsl_" + UUID.randomUUID().toString().replace("-", "");
	private boolean useJodaTime = false;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		jsonTypeElement = processingEnv.getElementUtils().getTypeElement("com.dslplatform.json.CompiledJson");
		jsonDeclaredType = processingEnv.getTypeUtils().getDeclaredType(jsonTypeElement);
		Map<String, String> options = processingEnv.getOptions();
		String ns = options.get("dsljson.namespace");
		if (ns != null && ns.length() > 0) {
			namespace = ns;
		}
		String timeApi = options.get("dsljson.timeApi");
		if ("joda-time".equals(timeApi)) {
			useJodaTime = true;
		}
	}

	static class StructInfo {
		public final TypeElement element;
		public final String name;
		public final boolean isEnum;

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
			StringBuilder dsl = new StringBuilder();
			dsl.append("module json {\n");
			for (Element el : jsonAnnotated) {
				findStructs(structs, el, "CompiledJson requires public no argument constructor");
			}
			findRelatedReferences(structs);
			buildDsl(structs, dsl);
			dsl.append("}");

			if (dsl.length() < 20) {
				return false;
			}

			String fullDsl = dsl.toString();
			processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, fullDsl);

			String fileContent;
			try {
				fileContent = AnnotationCompiler.buildExternalJson(fullDsl, namespace, useJodaTime);
			} catch (Exception e) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "DSL compilation error\n" + e.getMessage());
				return false;
			}
			try {
				JavaFileObject jfo = processingEnv.getFiler().createSourceFile("ExternalSerialization");
				BufferedWriter bw = new BufferedWriter(jfo.openWriter());
				bw.write(fileContent);
				bw.close();
				FileObject rfo = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/com.dslplatform.json.Configuration");
				bw = new BufferedWriter(rfo.openWriter());
				bw.write(namespace + ".json.ExternalSerialization");
				bw.close();
			} catch (IOException e) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed saving compiled json serialization files");
			}
		}
		return false;
	}

	private void buildDsl(Map<String, StructInfo> structs, StringBuilder dsl) {
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
				Map<String, ExecutableElement> properties = getBeanProperties(info.element);
				for (Map.Entry<String, ExecutableElement> p : properties.entrySet()) {
					if (hasIgnoredProperty(p.getValue())) {
						continue;
					}
					String propertyType = getPropertyType(p.getValue(), structs);
					if (propertyType != null) {
						dsl.append("    ");
						dsl.append(propertyType);
						dsl.append(" ");
						dsl.append(p.getKey());
						dsl.append(findNameAlias(p));
					} else {
						processingEnv.getMessager().printMessage(
								Diagnostic.Kind.ERROR,
								"Specified property type not supported. If you wish to ignore this property,\n" +
										" use one of the supported JsonIgnore annotations (such as Jackson @JsonIgnore) on getter",
								p.getValue(),
								getAnnotation(info.element, jsonDeclaredType));
					}
				}
			}
			dsl.append("    external name Java '");
			dsl.append(info.element.getQualifiedName());
			dsl.append("';\n  }\n");
		}
	}

	private void findRelatedReferences(Map<String, StructInfo> structs) {
		int total;
		do {
			total = structs.size();
			for (StructInfo info : structs.values()) {
				Map<String, ExecutableElement> properties = getBeanProperties(info.element);
				for (Map.Entry<String, ExecutableElement> p : properties.entrySet()) {
					String propertyType = getPropertyType(p.getValue(), structs);
					if (propertyType != null) {
						continue;
					}
					TypeMirror returnType = p.getValue().getReturnType();
					String typeName = returnType.toString();
					TypeElement el = processingEnv.getElementUtils().getTypeElement(typeName);
					if (el != null) {
						findStructs(structs, el, el + " is referenced as property from POJO with CompiledJson annotation.\n" +
								"Therefore it requires public no argument constructor");
						continue;
					}
					if (returnType instanceof ArrayType) {
						ArrayType at = (ArrayType) returnType;
						el = processingEnv.getElementUtils().getTypeElement(at.getComponentType().toString());
						if (el != null) {
							findStructs(structs, el, el + " is referenced as array property from POJO with CompiledJson annotation.\n" +
									"Therefore it requires public no argument constructor");
							continue;
						}
					}
					for (Map.Entry<String, String> kv : SupportedCollections.entrySet()) {
						if (!typeName.startsWith(kv.getKey())) {
							continue;
						}
						String elementType = typeName.substring(kv.getKey().length(), typeName.length() - 1);
						el = processingEnv.getElementUtils().getTypeElement(elementType);
						if (el != null) {
							findStructs(structs, el, el + " is referenced as collection property from POJO with CompiledJson annotation.\n" +
									"Therefore it requires public no argument constructor");
							break;
						}
					}
				}
			}
		} while (total != structs.size());
	}

	private static boolean hasIgnoredProperty(ExecutableElement property) {
		for (AnnotationMirror ann : property.getAnnotationMirrors()) {
			if (JsonIgnore.contains(ann.getAnnotationType().toString())) {
				return true;
			}
		}
		return false;
	}

	private static String findNameAlias(Map.Entry<String, ExecutableElement> property) {
		for (AnnotationMirror ann : property.getValue().getAnnotationMirrors()) {
			if (PropertyAlias.contains(ann.getAnnotationType().toString())) {
				for (ExecutableElement ee : ann.getElementValues().keySet()) {
					if ("value()".equals(ee.toString())) {
						AnnotationValue alias = ann.getElementValues().get(ee);
						return " { serialization name '" + alias.getValue() + "'; }\n";
					}
				}
			}
		}
		return ";\n";
	}

	private void findStructs(Map<String, StructInfo> structs, Element el, String errorMessge) {
		if (!(el instanceof TypeElement)) {
			return;
		}
		TypeElement element = (TypeElement) el;
		if (!hasEmptyCtor(element)) {
			AnnotationMirror entityAnnotation = getAnnotation(element, jsonDeclaredType);
			processingEnv.getMessager().printMessage(
					Diagnostic.Kind.ERROR,
					errorMessge,
					element,
					entityAnnotation);
			//TODO: other checks
		} else {
			String name = "struct" + structs.size();
			structs.put(element.asType().toString(), new StructInfo(element, name, element.getKind() == ElementKind.ENUM));
		}
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
			String property = name.substring(3).toUpperCase().equals(name.substring(3))
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

	private AnnotationMirror getAnnotation(Element element, DeclaredType annotationType) {
		for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
			if (processingEnv.getTypeUtils().isSameType(mirror.getAnnotationType(), annotationType)) {
				return mirror;
			}
		}
		return null;
	}

	private static boolean hasNonNullable(ExecutableElement property) {
		for (AnnotationMirror ann : property.getAnnotationMirrors()) {
			if (NonNullable.contains(ann.getAnnotationType().toString())) {
				return true;
			}
		}
		return false;
	}

	private static String getPropertyType(ExecutableElement property, Map<String, StructInfo> structs) {
		TypeMirror type = property.getReturnType();
		String simpleType = SupportedTypes.get(type.toString());
		boolean hasNonNullable = hasNonNullable(property);
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
