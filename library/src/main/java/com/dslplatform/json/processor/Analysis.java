package com.dslplatform.json.processor;

import com.dslplatform.json.*;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.*;

public class Analysis {

	private final AnnotationUsage annotationUsage;
	private final LogLevel logLevel;
	private final boolean includeFields;
	private final boolean includeBeanMethods;
	private final boolean includeExactMethods;

	private final Elements elements;
	private final Types types;
	private final Messager messager;

	public final TypeElement compiledJsonElement;
	public final DeclaredType compiledJsonType;
	public final TypeElement attributeElement;
	public final DeclaredType attributeType;
	public final TypeElement converterElement;
	public final DeclaredType converterType;

	private final Set<String> supportedTypes;
	private final Set<String> alternativeIgnore;
	private final Set<String> alternativeNonNullable;
	private final Set<String> alternativeAlias;
	private final Map<String, String> alternativeMandatory;

	private final Map<String, StructInfo> structs = new LinkedHashMap<String, StructInfo>();

	private boolean hasError;

	public boolean hasError() {
		return hasError;
	}

	public Analysis(ProcessingEnvironment processingEnv, AnnotationUsage annotationUsage, LogLevel logLevel, Set<String> supportedTypes) {
		this(processingEnv, annotationUsage, logLevel, supportedTypes, null, null, null, null, true, true, true);
	}

	public Analysis(
			ProcessingEnvironment processingEnv,
			AnnotationUsage annotationUsage,
			LogLevel logLevel,
			Set<String> supportedTypes,
			Set<String> alternativeIgnore,
			Set<String> alternativeNonNullable,
			Set<String> alternativeAlias,
			Map<String, String> alternativeMandatory,
			boolean includeFields,
			boolean includeBeanMethods,
			boolean includeExactMethods) {
		this.annotationUsage = annotationUsage;
		this.logLevel = logLevel;
		elements = processingEnv.getElementUtils();
		types = processingEnv.getTypeUtils();
		messager = processingEnv.getMessager();
		compiledJsonElement = elements.getTypeElement(CompiledJson.class.getName());
		compiledJsonType = types.getDeclaredType(compiledJsonElement);
		attributeElement = elements.getTypeElement(JsonAttribute.class.getName());
		attributeType = types.getDeclaredType(attributeElement);
		converterElement = elements.getTypeElement(JsonConverter.class.getName());
		converterType = types.getDeclaredType(converterElement);
		this.supportedTypes = supportedTypes;
		this.alternativeIgnore = alternativeIgnore == null ? new HashSet<String>() : alternativeIgnore;
		this.alternativeNonNullable = alternativeNonNullable == null ? new HashSet<String>() : alternativeNonNullable;
		this.alternativeAlias = alternativeAlias == null ? new HashSet<String>() : alternativeAlias;
		this.alternativeMandatory = alternativeMandatory == null ? new LinkedHashMap<String, String>() : alternativeMandatory;
		this.includeFields = includeFields;
		this.includeBeanMethods = includeBeanMethods;
		this.includeExactMethods = includeExactMethods;
	}

	public List<String> processConverters(Set<? extends Element> converters) {
		List<String> configurations = new ArrayList<String>();
		for (Element el : converters) {
			findConverters(el);
			if (el instanceof TypeElement) {
				TypeElement te = (TypeElement) el;
				if (!el.getModifiers().contains(Modifier.ABSTRACT)) {
					for (TypeElement it : getTypeHierarchy((TypeElement) el)) {
						if (Configuration.class.getName().equals(it.toString())) {
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
		return configurations;
	}

	public Map<String, StructInfo> processCompiledJson(Set<? extends Element> classes) {
		Stack<String> path = new Stack<String>();
		for (Element el : classes) {
			findStructs(el, "CompiledJson requires accessible public no argument constructor", path);
		}
		findRelatedReferences();
		findImplementations(structs.values());
		for (StructInfo info : structs.values()) {
			if (info.checkHashCollision()) {
				hasError = true;
				messager.printMessage(
						Diagnostic.Kind.ERROR,
						"Duplicate hash value detected. Unable to create binding for: '" + info.element.getQualifiedName() + "'. Remove (or reduce) alternativeNames from @JsonAttribute to resolve this issue." + info.pathDescription(),
						info.element,
						getAnnotation(info.element, compiledJsonType));
			}
			if (info.deserializeAs != null) {
				StructInfo target = structs.get(info.deserializeAs.asType().toString());
				info.deserializeTarget(target);
				if (target == null) {
					hasError = true;
					messager.printMessage(
							Diagnostic.Kind.ERROR,
							"Unable to find DSL-JSON metadata for: '" + info.deserializeAs.getQualifiedName() + "'. Add @CompiledJson annotation to target type.",
							info.element,
							getAnnotation(info.element, compiledJsonType));
				}
			}
			if (info.isMinified) {
				info.prepareMinifiedNames();
			}
		}
		return new LinkedHashMap<String, StructInfo>(structs);
	}

	private void findConverters(Element el) {
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
		validateConverter(converter, target.asElement(), target.toString());
		//TODO: throw an error if multiple non-compatible converters were found!?
		if (!structs.containsKey(target.toString())) {
			String name = "struct" + structs.size();
			TypeElement element = (TypeElement) target.asElement();
			StructInfo info = new StructInfo(converter, element, name);
			structs.put(target.toString(), info);
		}
	}

	private void validateConverter(TypeElement converter, Element target, String fullName) {
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
			hasError = true;
			messager.printMessage(
					Diagnostic.Kind.ERROR,
					"Specified converter: '" + converter.asType() + "' must be public",
					converter,
					getAnnotation(converter, converterType));
		} else if (!target.getModifiers().contains(Modifier.PUBLIC)) {
			hasError = true;
			messager.printMessage(
					Diagnostic.Kind.ERROR,
					"Specified converter target: '" + fullName + "' must be public",
					converter,
					getAnnotation(converter, converterType));
		} else if (converter.getNestingKind().isNested() && !converter.getModifiers().contains(Modifier.STATIC)) {
			hasError = true;
			messager.printMessage(
					Diagnostic.Kind.ERROR,
					"Specified converter: '" + converter.asType() + "' can't be a nested member. Only public static nested classes are supported",
					converter,
					getAnnotation(converter, converterType));
		} else if (converter.getQualifiedName().contentEquals(converter.getSimpleName())
				|| converter.getNestingKind().isNested() && converter.getModifiers().contains(Modifier.STATIC)
				&& converter.getEnclosingElement() instanceof TypeElement
				&& ((TypeElement) converter.getEnclosingElement()).getQualifiedName().contentEquals(converter.getEnclosingElement().getSimpleName())) {
			hasError = true;
			messager.printMessage(
					Diagnostic.Kind.ERROR,
					"Specified converter: '" + converter.getQualifiedName() + "' is defined without a package name and cannot be accessed",
					converter,
					getAnnotation(converter, converterType));
		} else if (jsonReader == null || jsonWriter == null) {
			hasError = true;
			messager.printMessage(
					Diagnostic.Kind.ERROR,
					"Specified converter: '" + converter.getQualifiedName() + "' doesn't have a JSON_READER or JSON_WRITER field. It must have public static JSON_READER/JSON_WRITER fields for conversion.",
					converter,
					getAnnotation(converter, converterType));
		} else if (!jsonReader.getModifiers().contains(Modifier.PUBLIC)
				|| !jsonReader.getModifiers().contains(Modifier.STATIC)
				|| !jsonWriter.getModifiers().contains(Modifier.PUBLIC)
				|| !jsonWriter.getModifiers().contains(Modifier.STATIC)) {
			hasError = true;
			messager.printMessage(
					Diagnostic.Kind.ERROR,
					"Specified converter: '" + converter.getQualifiedName() + "' doesn't have public and static JSON_READER and JSON_WRITER fields. They must be public and static for converter to work properly.",
					converter,
					getAnnotation(converter, converterType));
		} else if (!("com.dslplatform.json.JsonReader.ReadObject<" + fullName + ">").equals(jsonReader.asType().toString())) {
			hasError = true;
			messager.printMessage(
					Diagnostic.Kind.ERROR,
					"Specified converter: '" + converter.getQualifiedName() + "' has invalid type for JSON_READER field. It must be of type: 'com.dslplatform.json.JsonReader.ReadObject<" + target + ">'",
					converter,
					getAnnotation(converter, converterType));
		} else if (!("com.dslplatform.json.JsonWriter.WriteObject<" + fullName + ">").equals(jsonWriter.asType().toString())) {
			hasError = true;
			messager.printMessage(
					Diagnostic.Kind.ERROR,
					"Specified converter: '" + converter.getQualifiedName() + "' has invalid type for JSON_WRITER field. It must be of type: 'com.dslplatform.json.JsonWriter.WriteObject<" + target + ">'",
					converter,
					getAnnotation(converter, converterType));
		}
	}

	public List<TypeElement> getTypeHierarchy(TypeElement element) {
		List<TypeElement> result = new ArrayList<TypeElement>();
		result.add(element);
		for (TypeMirror type : types.directSupertypes(element.asType())) {
			Element current = types.asElement(type);
			if (current instanceof TypeElement) {
				result.add((TypeElement) current);
			}
		}
		return result;
	}

	public void findRelatedReferences() {
		int total;
		do {
			total = structs.size();
			List<StructInfo> items = new ArrayList<StructInfo>(structs.values());
			Stack<String> path = new Stack<String>();
			for (StructInfo info : items) {
				if (info.converter != null) {
					continue;
				}
				path.push(info.element.getSimpleName().toString());
				if (includeBeanMethods) {
					for (Map.Entry<String, ExecutableElement> p : getBeanProperties(info.element).entrySet()) {
						analyzeAttribute(info, p.getValue().getReturnType(), p.getKey(), p.getValue(), null, "bean property", path);
					}
				}
				if (includeExactMethods) {
					for (Map.Entry<String, ExecutableElement> p : getExactProperties(info.element).entrySet()) {
						analyzeAttribute(info, p.getValue().getReturnType(), p.getKey(), p.getValue(), null, "exact property", path);
					}
				}
				if (includeFields) {
					for (Map.Entry<String, VariableElement> f : getPublicFields(info.element).entrySet()) {
						analyzeAttribute(info, f.getValue().asType(), f.getKey(), null, f.getValue(), "field", path);
					}
				}
				path.pop();
			}
		} while (total != structs.size());
	}

	private void analyzeAttribute(StructInfo info, TypeMirror type, String name, ExecutableElement method, VariableElement field, String target, Stack<String> path) {
		Element element = method != null ? method : field;
		path.push(name);
		if (!info.properties.contains(element) && !hasIgnoredAnnotation(element)) {
			AnnotationMirror annotation = getAnnotation(element, attributeType);
			CompiledJson.TypeSignature typeSignature = typeSignatureValue(annotation);
			AttributeInfo attr =
					new AttributeInfo(
							name,
							method,
							field,
							type,
							hasNonNullable(element),
							hasMandatoryAnnotation(element),
							findNameAlias(element),
							isFullMatch(element),
							typeSignature,
							findConverter(element));
			String[] alternativeNames = getAlternativeNames(attr.element);
			if (alternativeNames != null) {
				attr.alternativeNames.addAll(Arrays.asList(alternativeNames));
			}
			if (attr.converter != null) {
				TypeElement typeConverter = elements.getTypeElement(attr.converter.toString());
				String javaType = type.toString();
				String objectType = "int".equals(javaType) ? "java.lang.Integer"
						: "long".equals(javaType) ? "java.lang.Long"
						: "double".equals(javaType) ? "java.lang.Double"
						: "float".equals(javaType) ? "java.lang.Float"
						: "char".equals(javaType) ? "java.lang.Character"
						: javaType;
				Element declaredType = objectType.equals(javaType)
						? types.asElement(type)
						: elements.getTypeElement(objectType);
				validateConverter(typeConverter, declaredType, objectType);
			}
			if (info.attributes.containsKey(attr.id)) {
				hasError = true;
				messager.printMessage(
						Diagnostic.Kind.ERROR,
						"Duplicate alias detected on " + (attr.field != null ? "field: " : "property: ") + attr.name,
						attr.element,
						getAnnotation(info.element, compiledJsonType));
			}
			info.attributes.put(attr.id, attr);
			info.properties.add(attr.element);
			checkRelatedProperty(type, target, info.element, element, path);
		}
		path.pop();
	}

	private void checkRelatedProperty(TypeMirror returnType, String access, Element inside, Element property, Stack<String> path) {
		TypeMirror converter = findConverter(property);
		if (converter != null) return;
		String typeName = returnType.toString();
		if (supportedTypes.contains(typeName)) return;
		TypeElement el = elements.getTypeElement(typeName);
		if (el != null) {
			findStructs(el, el + " is referenced as " + access + " from '" + inside.asType() + "' through CompiledJson annotation.", path);
			return;
		}
		if (returnType instanceof ArrayType) {
			ArrayType at = (ArrayType) returnType;
			el = elements.getTypeElement(at.getComponentType().toString());
			if (el != null) {
				findStructs(el, el + " is referenced as array " + access + " from '" + inside.asType() + "' through CompiledJson annotation.", path);
				return;
			}
		}
		int genInd = typeName.indexOf('<');
		if (genInd == -1) return;
		for (String st : typeName.substring(genInd + 1, typeName.length() - 1).split(",")) {
			el = elements.getTypeElement(st);
			if (el != null) {
				findStructs(el, el + " is referenced as collection " + access + " from '" + inside.asType() + "' through CompiledJson annotation.", path);
			}
		}
	}

	private void findStructs(Element el, String errorMessge, Stack<String> path) {
		if (!(el instanceof TypeElement)) {
			return;
		}
		String typeName = el.asType().toString();
		if (structs.containsKey(typeName)) return;
		if (supportedTypes.contains(typeName)) return;
		TypeElement element = (TypeElement) el;
		boolean isMixin = element.getKind() == ElementKind.INTERFACE
				|| element.getKind() == ElementKind.CLASS && element.getModifiers().contains(Modifier.ABSTRACT);
		boolean isJsonObject = isJsonObject(element);
		AnnotationMirror annotation = getAnnotation(element, compiledJsonType);
		if (!element.getModifiers().contains(Modifier.PUBLIC)) {
			hasError = true;
			messager.printMessage(
					Diagnostic.Kind.ERROR,
					errorMessge + ", therefore '" + element.asType() + "' must be public ",
					element,
					annotation);
		} else if (element.getNestingKind().isNested() && !element.getModifiers().contains(Modifier.STATIC)) {
			hasError = true;
			messager.printMessage(
					Diagnostic.Kind.ERROR,
					errorMessge + ", therefore '" + element.asType() + "' can't be a nested member. Only static nested classes are supported.",
					element,
					annotation);
		} else if (element.getQualifiedName().contentEquals(element.getSimpleName())
				|| element.getNestingKind().isNested() && element.getModifiers().contains(Modifier.STATIC)
				&& element.getEnclosingElement() instanceof TypeElement
				&& ((TypeElement) element.getEnclosingElement()).getQualifiedName().contentEquals(element.getEnclosingElement().getSimpleName())) {
			hasError = true;
			messager.printMessage(
					Diagnostic.Kind.ERROR,
					errorMessge + ", but class '" + element.getQualifiedName() + "' is defined without a package name and cannot be accessed.",
					element,
					annotation);
		} else {
			ObjectType type = isMixin ? ObjectType.MIXIN : element.getKind() == ElementKind.ENUM ? ObjectType.ENUM : ObjectType.CLASS;
			CompiledJson.Behavior onUnknown = CompiledJson.Behavior.DEFAULT;
			CompiledJson.TypeSignature typeSignature = CompiledJson.TypeSignature.DEFAULT;
			TypeElement deserializeAs = null;
			if (!isJsonObject) {
				if (annotation != null) {
					onUnknown = onUnknownValue(annotation);
					typeSignature = typeSignatureValue(annotation);
					deserializeAs = deserializeAs(annotation);
					if (deserializeAs != null) {
						String error = validateDeserializeAs(element, deserializeAs);
						if (error != null) {
							hasError = true;
							messager.printMessage(
									Diagnostic.Kind.ERROR,
									errorMessge + ", but specified deserializeAs target: '" + deserializeAs.getQualifiedName() + "' " + error,
									element,
									annotation);
							deserializeAs = null;//reset it so that later lookup don't add another error message
						} else {
							if (deserializeAs.asType().toString().equals(element.asType().toString())) {
								deserializeAs = null;
							} else {
								findStructs(deserializeAs, errorMessge, path);
							}
						}
					}
				} else if (annotationUsage != AnnotationUsage.IMPLICIT) {
					if (annotationUsage == AnnotationUsage.EXPLICIT) {
						hasError = true;
						messager.printMessage(
								Diagnostic.Kind.ERROR,
								"Annotation usage is set to explicit, but '" + element.getQualifiedName() + "' is used implicitly through references. " +
										"Either change usage to implicit or use @Ignore on property referencing this type. " + errorMessge,
								element);
					} else if (element.getQualifiedName().toString().startsWith("java.")) {
						hasError = true;
						messager.printMessage(
								Diagnostic.Kind.ERROR,
								"Annotation usage is set to non-java, but '" + element.getQualifiedName() + "' is found in java package. " +
										"Either change usage to implicit, use @Ignore on property referencing this type or add annotation to this type. " +
										errorMessge,
								element);
					}
				}
			}
			String name = "struct" + structs.size();
			StructInfo info =
					new StructInfo(
							element,
							name,
							type,
							isJsonObject,
							annotation != null,
							onUnknown,
							typeSignature,
							deserializeAs,
							isMinified(element),
							hasEmptyCtor(element));
			info.path.addAll(path);
			if (type == ObjectType.ENUM) {
				info.constants.addAll(getEnumConstants(info.element));
			}
			structs.put(element.asType().toString(), info);
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
			//TODO: create converters in root package
			return "is defined without a package name and cannot be accessed";
		} else if (target.getKind() == ElementKind.INTERFACE || target.getModifiers().contains(Modifier.ABSTRACT)) {
			return "must be a concrete type";
		} else if (!source.asType().toString().equals(target.asType().toString()) && source.getKind() != ElementKind.INTERFACE && !source.getModifiers().contains(Modifier.ABSTRACT)) {
			return "can only be specified for interfaces and abstract classes. '" + source + "' is neither interface nor abstract class";
		} else if (!types.isAssignable(target.asType(), source.asType())) {
			return "is not assignable to '" + source.getQualifiedName() + "'";
		} else {
			return null;
		}
	}

	public static boolean hasEmptyCtor(Element element) {
		for (ExecutableElement constructor : ElementFilter.constructorsIn(element.getEnclosedElements())) {
			List<? extends VariableElement> parameters = constructor.getParameters();
			if (parameters.isEmpty()
					&& (element.getKind() == ElementKind.ENUM || constructor.getModifiers().contains(Modifier.PUBLIC))) {
				return true;
			}
		}
		return false;
	}

	public static List<String> getEnumConstants(TypeElement element) {
		List<String> result = new ArrayList<String>();
		for (VariableElement field : ElementFilter.fieldsIn(element.getEnclosedElements())) {
			//Use to string comparison since compiler can create two separate instances which differ
			if (field.asType().toString().equals(element.asType().toString())) {
				result.add(field.getSimpleName().toString());
			}
		}
		return result;
	}

	public boolean isJsonObject(TypeElement element) {
		for (TypeMirror type : element.getInterfaces()) {
			if (JsonObject.class.getName().equals(type.toString())) {
				for (VariableElement field : ElementFilter.fieldsIn(element.getEnclosedElements())) {
					if ("JSON_READER".equals(field.getSimpleName().toString())) {
						if (!field.getModifiers().contains(Modifier.PUBLIC)
								|| !field.getModifiers().contains(Modifier.STATIC)) {
							if (logLevel.isVisible(LogLevel.INFO)) {
								messager.printMessage(
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
							if (logLevel.isVisible(LogLevel.INFO)) {
								messager.printMessage(
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
				if (logLevel.isVisible(LogLevel.INFO)) {
					messager.printMessage(
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

	public Map<String, ExecutableElement> getBeanProperties(TypeElement element) {
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

	public Map<String, ExecutableElement> getExactProperties(TypeElement element) {
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
				if (name.startsWith("get") || name.startsWith("set") || !isAccessible) {
					continue;
				}
				if (method.getParameters().size() == 0 && method.getReturnType() != null) {
					if (!getters.containsKey(name)) {
						getters.put(name, method);
					}
				} else if (method.getParameters().size() == 1) {
					setters.put(name, method.getParameters().get(0));
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

	public Map<String, VariableElement> getPublicFields(TypeElement element) {
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

	public void findImplementations(Collection<StructInfo> structs) {
		for (StructInfo current : structs) {
			if (current.type == ObjectType.MIXIN) {
				String iface = current.element.asType().toString();
				for (StructInfo info : structs) {
					if (info.type == ObjectType.CLASS) {
						for (TypeMirror type : types.directSupertypes(info.element.asType())) {
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

	public String[] getAlternativeNames(Element property) {
		AnnotationMirror dslAnn = getAnnotation(property, attributeType);
		if (dslAnn != null) {
			Map<? extends ExecutableElement, ? extends AnnotationValue> values = dslAnn.getElementValues();
			for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> ee : values.entrySet()) {
				if (ee.getKey().toString().equals("alternativeNames()")) {
					@SuppressWarnings("unchecked")
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

	public boolean isFullMatch(Element property) {
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

	public boolean hasIgnoredAnnotation(Element property) {
		AnnotationMirror dslAnn = getAnnotation(property, attributeType);
		if (dslAnn != null) {
			return booleanAnnotationValue(dslAnn, "ignore()", false);
		}
		for (AnnotationMirror ann : property.getAnnotationMirrors()) {
			if (alternativeIgnore.contains(ann.getAnnotationType().toString())) {
				return true;
			}
		}
		return false;
	}

	public AnnotationMirror getAnnotation(Element element, DeclaredType annotationType) {
		for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
			if (types.isSameType(mirror.getAnnotationType(), annotationType)) {
				return mirror;
			}
		}
		return null;
	}

	public boolean hasNonNullable(Element property) {
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
			if (alternativeNonNullable.contains(ann.getAnnotationType().toString())) {
				return true;
			}
		}
		return false;
	}

	public static TypeElement deserializeAs(AnnotationMirror annotation) {
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();
		for (ExecutableElement ee : values.keySet()) {
			if (ee.toString().equals("deserializeAs()")) {
				DeclaredType target = (DeclaredType) values.get(ee).getValue();
				return (TypeElement) target.asElement();
			}
		}
		return null;
	}

	public boolean hasMandatoryAnnotation(Element property) {
		AnnotationMirror dslAnn = getAnnotation(property, attributeType);
		if (dslAnn != null) {
			return booleanAnnotationValue(dslAnn, "mandatory()", false);
		}
		for (AnnotationMirror ann : property.getAnnotationMirrors()) {
			String value = alternativeMandatory.get(ann.getAnnotationType().toString());
			if (value != null && booleanAnnotationValue(ann, value, false)) {
				return true;
			}
		}
		return false;
	}

	public static boolean booleanAnnotationValue(AnnotationMirror ann, String method, boolean defaultValue) {
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = ann.getElementValues();
		for (ExecutableElement ee : values.keySet()) {
			if (ee.toString().equals(method)) {
				Object val = values.get(ee).getValue();
				return val == null ? defaultValue : (Boolean) val;
			}
		}
		return defaultValue;
	}

	public CompiledJson.Behavior onUnknownValue(AnnotationMirror annotation) {
		if (annotation == null) return null;
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();
		for (ExecutableElement ee : values.keySet()) {
			if (ee.toString().equals("onUnknown()")) {
				Object val = values.get(ee).getValue();
				if (val == null) return null;
				return CompiledJson.Behavior.valueOf(val.toString());
			}
		}
		return null;
	}

	public CompiledJson.TypeSignature typeSignatureValue(AnnotationMirror annotation) {
		if (annotation == null) return null;
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();
		for (ExecutableElement ee : values.keySet()) {
			if (ee.toString().equals("typeSignature()")) {
				Object val = values.get(ee).getValue();
				if (val == null) return null;
				return CompiledJson.TypeSignature.valueOf(val.toString());
			}
		}
		return null;
	}

	public boolean isMinified(Element struct) {
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

	public TypeMirror findConverter(Element property) {
		AnnotationMirror dslAnn = getAnnotation(property, attributeType);
		if (dslAnn != null) {
			Map<? extends ExecutableElement, ? extends AnnotationValue> values = dslAnn.getElementValues();
			for (ExecutableElement ee : values.keySet()) {
				if (ee.toString().equals("converter()")) {
					TypeMirror mirror = (TypeMirror) values.get(ee).getValue();
					return mirror != null && mirror.toString().equals(JsonAttribute.class.getName()) ? null : mirror;
				}
			}
			return null;
		}
		return null;
	}

	public String findNameAlias(Element property) {
		AnnotationMirror dslAnn = getAnnotation(property, attributeType);
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
		for (AnnotationMirror ann : property.getAnnotationMirrors()) {
			if (alternativeAlias.contains(ann.getAnnotationType().toString())) {
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
}
