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
	private final UnknownTypes unknownTypes;
	private final boolean onlyBasicFeatures;
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
	private final ContainerSupport containerSupport;
	private final Set<String> alternativeIgnore;
	private final Map<String, List<AnnotationMapping<Boolean>>> alternativeNonNullable;
	private final Map<String, String> alternativeAlias;
	private final Map<String, List<AnnotationMapping<Boolean>>> alternativeMandatory;
	private final Set<String> alternativeCtors;
	private final Set<String> alternativeFactories;
	private final Map<String, String> alternativeIndex;

	public static class AnnotationMapping<T> {
		public final String name;
		public final T value;
		public AnnotationMapping(String name, @Nullable T value) {
			this.name = name;
			this.value = value;
		}
	}

	private final Map<String, StructInfo> structs = new LinkedHashMap<String, StructInfo>();

	private boolean hasError;

	public boolean hasError() {
		return hasError;
	}

	public Analysis(ProcessingEnvironment processingEnv, AnnotationUsage annotationUsage, LogLevel logLevel, Set<String> supportedTypes, ContainerSupport containerSupport) {
		this(processingEnv, annotationUsage, logLevel, supportedTypes, containerSupport, null, null, null, null, null, null, null, UnknownTypes.ERROR, false, true, true, true);
	}

	public Analysis(
			ProcessingEnvironment processingEnv,
			AnnotationUsage annotationUsage,
			LogLevel logLevel,
			Set<String> supportedTypes,
			ContainerSupport containerSupport,
			@Nullable Set<String> alternativeIgnore,
			@Nullable Map<String, List<AnnotationMapping<Boolean>>> alternativeNonNullable,
			@Nullable Map<String, String> alternativeAlias,
			@Nullable Map<String, List<AnnotationMapping<Boolean>>> alternativeMandatory,
			@Nullable Set<String> alternativeCtors,
			@Nullable Set<String> alternativeFactories,
			@Nullable Map<String, String> alternativeIndex,
			@Nullable UnknownTypes unknownTypes,
			boolean onlyBasicFeatures,
			boolean includeFields,
			boolean includeBeanMethods,
			boolean includeExactMethods) {
		this.annotationUsage = annotationUsage;
		this.logLevel = logLevel;
		this.elements = processingEnv.getElementUtils();
		this.types = processingEnv.getTypeUtils();
		this.messager = processingEnv.getMessager();
		this.compiledJsonElement = elements.getTypeElement(CompiledJson.class.getName());
		this.compiledJsonType = types.getDeclaredType(compiledJsonElement);
		this.attributeElement = elements.getTypeElement(JsonAttribute.class.getName());
		this.attributeType = types.getDeclaredType(attributeElement);
		this.converterElement = elements.getTypeElement(JsonConverter.class.getName());
		this.converterType = types.getDeclaredType(converterElement);
		this.supportedTypes = supportedTypes;
		this.containerSupport = containerSupport;
		this.alternativeIgnore = alternativeIgnore == null ? new HashSet<String>() : alternativeIgnore;
		this.alternativeNonNullable = alternativeNonNullable == null ? new HashMap<String, List<AnnotationMapping<Boolean>>>() : alternativeNonNullable;
		this.alternativeAlias = alternativeAlias == null ? new HashMap<String, String>() : alternativeAlias;
		this.alternativeMandatory = alternativeMandatory == null ? new HashMap<String, List<AnnotationMapping<Boolean>>>() : alternativeMandatory;
		this.alternativeCtors = alternativeCtors == null ? new HashSet<String>() : alternativeCtors;
		this.alternativeFactories = alternativeFactories == null ? new HashSet<String>() : alternativeFactories;
		this.alternativeIndex = alternativeIndex == null ? new HashMap<String, String>() : alternativeIndex;
		this.unknownTypes = unknownTypes == null ? UnknownTypes.ERROR : unknownTypes;
		this.onlyBasicFeatures = onlyBasicFeatures;
		this.includeFields = includeFields;
		this.includeBeanMethods = includeBeanMethods;
		this.includeExactMethods = includeExactMethods;
	}

	public Map<String, Element> processConverters(Set<? extends Element> converters) {
		Map<String, Element> configurations = new LinkedHashMap<String, Element>();
		for (Element el : converters) {
			findConverters(el);
			if (el instanceof TypeElement) {
				TypeElement te = (TypeElement) el;
				if (!el.getModifiers().contains(Modifier.ABSTRACT)) {
					for (TypeElement it : getTypeHierarchy((TypeElement) el)) {
						if (Configuration.class.getName().equals(it.toString())) {
							if (te.getNestingKind().isNested()) {
								configurations.put(te.getEnclosingElement().asType().toString() + "$" + te.getSimpleName().toString(), te);
							} else {
								configurations.put(te.asType().toString(), te);
							}
							break;
						}
					}
				}
			}
		}
		return configurations;
	}

	public void processAnnotation(DeclaredType currentAnnotationType, Set<? extends Element> targets) {
		Stack<String> path = new Stack<String>();
		for (Element el : targets) {
			Element classElement;
			if (el instanceof TypeElement) classElement = el;
			else classElement = el.getEnclosingElement();
			findStructs(classElement, currentAnnotationType, currentAnnotationType + " requires accessible public constructor", path);
		}
		findRelatedReferences();
		findImplementations(structs.values());
	}

	public Map<String, StructInfo> analyze() {
		for (Map.Entry<String, StructInfo> it : structs.entrySet()) {
			final StructInfo info = it.getValue();
			final String className = it.getKey();
			if (info.type == ObjectType.CLASS && info.constructor == null && info.factory == null
					&& info.converter == null && info.matchingConstructors != null) {
				hasError = true;
				if (info.matchingConstructors.size() == 0) {
					messager.printMessage(
							Diagnostic.Kind.ERROR,
							"No matching constructors found for '" + info.element.asType() + "'. Make sure there is at least one matching constructor available.",
							info.element,
							info.annotation);
				} else {
					messager.printMessage(
							Diagnostic.Kind.ERROR,
							"Multiple matching constructors found for '" + info.element.asType() + "'. Use @CompiledJson or alternative annotations to select the appropriate constructor.",
							info.element,
							info.annotation);
				}
			}
			if (unknownTypes != UnknownTypes.ALLOW && !info.unknowns.isEmpty()) {
				for (Map.Entry<String, TypeMirror> kv : info.unknowns.entrySet()) {
					AttributeInfo attr = info.attributes.get(kv.getKey());
					if (attr != null && (attr.converter != null || attr.isJsonObject)) continue;
					Map<String, Boolean> references = analyzeParts(kv.getValue());
					for (Map.Entry<String, Boolean> pair : references.entrySet()) {
						if (!pair.getValue()) {
							hasError = hasError || unknownTypes == UnknownTypes.ERROR;
							Diagnostic.Kind kind = unknownTypes == UnknownTypes.ERROR ? Diagnostic.Kind.ERROR : Diagnostic.Kind.WARNING;
							if (kind == Diagnostic.Kind.ERROR || logLevel.isVisible(LogLevel.INFO)) {
								if (kv.getValue().toString().equals(pair.getKey())) {
									messager.printMessage(
											kind,
											"Property " + kv.getKey() + " is referencing unknown type: '" + kv.getValue()
													+ "'. Register custom converter, mark property as ignored or enable unknown types",
											attr != null ? attr.element : info.element,
											info.annotation);
								} else {
									messager.printMessage(
											kind,
											"Property " + kv.getKey() + " is referencing unknown type: '" + kv.getValue() + "' which has an unknown part: '"
													+ pair.getKey() + "'. Register custom converter, mark property as ignored or enable unknown types",
											attr != null ? attr.element : info.element,
											info.annotation);
								}
							}
						}
					}
				}
			}
			if (unknownTypes != UnknownTypes.ALLOW) {
				for (AttributeInfo attr : info.attributes.values()) {
					if (attr.converter != null || attr.isJsonObject) continue;
					Map<String, Boolean> references = analyzeParts(attr.type);
					for (String r : references.keySet()) {
						StructInfo target = structs.get(r);
						if (target != null && target.type == ObjectType.MIXIN && target.implementations.size() == 0) {
							String what = target.element.getKind() == ElementKind.INTERFACE ? "interface" : "abstract class";
							String one = target.element.getKind() == ElementKind.INTERFACE ? "implementation" : "concrete extension";
							hasError = true;
							messager.printMessage(Diagnostic.Kind.ERROR, "Property " + attr.name +
											" is referencing " + what + " (" + target.element.getQualifiedName() + ") which doesn't have registered " +
											"implementations with @CompiledJson. At least one " + one + " of specified " + what + " must be annotated " +
											"with CompiledJson annotation or allow unknown types during analysis",
									attr.element,
									info.annotation);
						}
					}
				}
			}
			if (unknownTypes != UnknownTypes.ALLOW && info.type == ObjectType.MIXIN && info.implementations.isEmpty()) {
				String what = info.element.getKind() == ElementKind.INTERFACE ? "Interface" : "Abstract class";
				String one = info.element.getKind() == ElementKind.INTERFACE ? "implementation" : "concrete extension";
				hasError = hasError || unknownTypes == UnknownTypes.ERROR;
				Diagnostic.Kind kind = unknownTypes == UnknownTypes.ERROR ? Diagnostic.Kind.ERROR : Diagnostic.Kind.WARNING;
				if (kind == Diagnostic.Kind.ERROR || logLevel.isVisible(LogLevel.INFO)) {
					messager.printMessage(
							kind,
							what + " (" + className + ") is referenced, but it doesn't have registered " +
									"implementations with @CompiledJson. At least one " + one + " of specified " + what + " must be annotated " +
									"with CompiledJson annotation or allow unknown types during analysis",
							info.element,
							info.annotation);
				}
			}
			if (info.type == ObjectType.CLASS && info.converter == null && info.factory != null) {
				if (onlyBasicFeatures) {
					hasError = true;
					messager.printMessage(
							Diagnostic.Kind.ERROR,
							"Factory methods are not available with curent setup",
							info.factory,
							info.annotation);
				} else if (!types.isAssignable(info.factory.getReturnType(), info.element.asType())) {
					hasError = true;
					messager.printMessage(
							Diagnostic.Kind.ERROR,
							"Wrong factory result type: '" + info.factory.getReturnType() + "'. Result must be assignable to '" + info.element.asType() + "'",
							info.factory,
							info.annotation);
				} else {
					for (VariableElement p : info.factory.getParameters()) {
						boolean found = false;
						String argName = p.getSimpleName().toString();
						for (AttributeInfo attr : info.attributes.values()) {
							if (attr.name.equals(argName)) {
								found = true;
								break;
							}
						}
						if (!found) {
							hasError = true;
							messager.printMessage(
									Diagnostic.Kind.ERROR,
									"Unable to find matching property: '" + argName + "' used in method factory. Either use annotation processor on source code, on bytecode with -parameters flag (to enable parameter names) or manually create an instance via converter",
									info.factory,
									info.annotation);
						}
					}
				}
			}
			if (info.type == ObjectType.CLASS && info.converter == null && !info.hasEmptyCtor() && info.constructor != null && info.factory == null) {
				for (VariableElement p : info.constructor.getParameters()) {
					boolean found = false;
					String argName = p.getSimpleName().toString();
					for (AttributeInfo attr : info.attributes.values()) {
						if (attr.name.equals(argName)) {
							found = true;
							break;
						}
					}
					if (!found) {
						hasError = true;
						messager.printMessage(
								Diagnostic.Kind.ERROR,
								"Unable to find matching property: '" + argName + "' used in constructor. Either use annotation processor on source code, on bytecode with -parameters flag (to enable parameter names) or manually create an instance via converter",
								info.constructor,
								info.annotation);
					}
				}
			}
			if (info.checkHashCollision()) {
				hasError = true;
				messager.printMessage(
						Diagnostic.Kind.ERROR,
						"Duplicate hash value detected. Unable to create binding for: '" + className + "'. Remove (or reduce) alternativeNames from @JsonAttribute to resolve this issue." + info.pathDescription(),
						info.element,
						info.annotation);
			}
			if (info.deserializeAs != null) {
				StructInfo target = structs.get(info.deserializeAs.asType().toString());
				info.setDeserializeTarget(target);
				if (target == null) {
					hasError = true;
					messager.printMessage(
							Diagnostic.Kind.ERROR,
							"Unable to find DSL-JSON metadata for: '" + info.deserializeAs.getQualifiedName() + "'. Add @CompiledJson annotation to target type.",
							info.element,
							info.annotation);
				}
			}
			if (info.deserializeAs == null && info.type == ObjectType.MIXIN) {
				Set<String> names = new HashSet<String>();
				for(StructInfo im : info.implementations) {
					String actualName = im.deserializeName.isEmpty() ? im.element.getQualifiedName().toString() : im.deserializeName;
					if (!names.add(actualName)) {
						hasError = true;
						messager.printMessage(
								Diagnostic.Kind.ERROR,
								"Duplicate deserialization name detected: '" + actualName + "' for mixin: " + className,
								info.element,
								info.annotation);
					} else if (actualName.contains("\\") || actualName.contains("\"")) {
						hasError = true;
						messager.printMessage(
								Diagnostic.Kind.ERROR,
								"Invalid deserialization name (with quotes or escape chars) detected: '" + actualName + "' for mixin: " + className,
								info.element,
								info.annotation);
					}
				}
			}
			if (info.type == ObjectType.CLASS && onlyBasicFeatures && info.converter == null && !info.hasEmptyCtor()) {
				hasError = true;
				messager.printMessage(
						Diagnostic.Kind.ERROR,
						"'" + className + "' requires public no argument constructor" + info.pathDescription(),
						info.element,
						info.annotation);
			} else if (info.type == ObjectType.CLASS && !onlyBasicFeatures && !info.hasEmptyCtor() && info.converter == null
					&& info.factory == null && (info.constructor == null || info.constructor.getParameters().size() != info.attributes.size())) {
				hasError = true;
				messager.printMessage(
						Diagnostic.Kind.ERROR,
						"'" + className + "' does not have an empty or matching constructor" + info.pathDescription(),
						info.element,
						info.annotation);
			}
			if (info.formats.contains(CompiledJson.Format.ARRAY)) {
				HashSet<Integer> ids = new HashSet<Integer>();
				for (AttributeInfo attr : info.attributes.values()) {
					if (attr.index == -1 && info.canCreateEmptyInstance()) {
						hasError = true;
						messager.printMessage(
								Diagnostic.Kind.ERROR,
								"When array format is used all properties must have index order defined. Property " + attr.name + " doesn't have index defined",
								attr.element,
								attr.annotation);
					} else if (attr.index != -1 && !ids.add(attr.index)) {
						hasError = true;
						messager.printMessage(
								Diagnostic.Kind.ERROR,
								"Duplicate index detected on " + attr.name + ". Index values must be distinct to be used in array format",
								attr.element,
								attr.annotation);
					}
				}
			}
			if (info.isMinified) {
				info.prepareMinifiedNames();
			}
			info.sortAttributes();
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
		ConverterInfo signature = validateConverter(converter, target.asElement(), target.toString());
		//TODO: throw an error if multiple non-compatible converters were found!?
		if (!structs.containsKey(target.toString())) {
			String name = "struct" + structs.size();
			TypeElement element = (TypeElement) target.asElement();
			String binaryName = elements.getBinaryName(element).toString();
			StructInfo info = new StructInfo(converter, converterType, element, name, binaryName, signature.reader, signature.writer);
			structs.put(target.toString(), info);
		}
	}

	private ConverterInfo validateConverter(TypeElement converter, Element target, String fullName) {
		VariableElement jsonReaderField = null;
		VariableElement jsonWriterField = null;
		ExecutableElement jsonReaderMethod = null;
		ExecutableElement jsonWriterMethod = null;
		boolean hasInstance = false;
		for (VariableElement field : ElementFilter.fieldsIn(converter.getEnclosedElements())) {
			//Kotlin uses INSTANCE field with non static get methods
			if ("INSTANCE".equals(field.getSimpleName().toString())) {
				if (field.asType().toString().equals(converter.getQualifiedName().toString())
						&& field.getModifiers().contains(Modifier.STATIC)
						&& field.getModifiers().contains(Modifier.PUBLIC)
						&& field.getModifiers().contains(Modifier.FINAL)) {
					hasInstance = true;
				}
			} else if ("JSON_READER".equals(field.getSimpleName().toString())) {
				jsonReaderField = field;
			} else if ("JSON_WRITER".equals(field.getSimpleName().toString())) {
				jsonWriterField = field;
			}
		}
		if (!onlyBasicFeatures) {
			for (ExecutableElement method : ElementFilter.methodsIn(converter.getEnclosedElements())) {
				if ("JSON_READER".equals(method.getSimpleName().toString()) || "getJSON_READER".equals(method.getSimpleName().toString())) {
					jsonReaderMethod = method;
				} else if ("JSON_WRITER".equals(method.getSimpleName().toString()) || "getJSON_WRITER".equals(method.getSimpleName().toString())) {
					jsonWriterMethod = method;
				}
			}
		}
		String allowed = onlyBasicFeatures ? "field" : "field/method";
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
		} else if (onlyBasicFeatures && (converter.getQualifiedName().contentEquals(converter.getSimpleName())
				|| converter.getNestingKind().isNested() && converter.getModifiers().contains(Modifier.STATIC)
				&& converter.getEnclosingElement() instanceof TypeElement
				&& ((TypeElement) converter.getEnclosingElement()).getQualifiedName().contentEquals(converter.getEnclosingElement().getSimpleName()))) {
			hasError = true;
			messager.printMessage(
					Diagnostic.Kind.ERROR,
					"Specified converter: '" + converter.getQualifiedName() + "' is defined without a package name and cannot be accessed",
					converter,
					getAnnotation(converter, converterType));
		} else if (jsonReaderField == null && jsonReaderMethod == null || jsonWriterField == null && jsonWriterMethod == null) {
			hasError = true;
			messager.printMessage(
					Diagnostic.Kind.ERROR,
					"Specified converter: '" + converter.getQualifiedName() + "' doesn't have a JSON_READER or JSON_WRITER " + allowed + ". It must have public static JSON_READER/JSON_WRITER " + allowed + " for conversion.",
					converter,
					getAnnotation(converter, converterType));
		} else if (jsonReaderMethod == null && (!jsonReaderField.getModifiers().contains(Modifier.PUBLIC) || !jsonReaderField.getModifiers().contains(Modifier.STATIC))
				|| jsonWriterMethod == null && (!jsonWriterField.getModifiers().contains(Modifier.PUBLIC) || !jsonReaderField.getModifiers().contains(Modifier.STATIC))
				|| jsonReaderMethod != null && (!jsonReaderMethod.getModifiers().contains(Modifier.PUBLIC) || !hasInstance && !jsonReaderMethod.getModifiers().contains(Modifier.STATIC))
				|| jsonWriterMethod != null && (!jsonWriterMethod.getModifiers().contains(Modifier.PUBLIC) || !hasInstance && !jsonWriterMethod.getModifiers().contains(Modifier.STATIC))) {
			hasError = true;
			messager.printMessage(
					Diagnostic.Kind.ERROR,
					"Specified converter: '" + converter.getQualifiedName() + "' doesn't have public and static JSON_READER and JSON_WRITER " + allowed + ". They must be public and static for converter to work properly.",
					converter,
					getAnnotation(converter, converterType));
		} else if (jsonReaderField != null && !("com.dslplatform.json.JsonReader.ReadObject<" + fullName + ">").equals(jsonReaderField.asType().toString())
				|| jsonReaderMethod != null && !("com.dslplatform.json.JsonReader.ReadObject<" + fullName + ">").equals(jsonReaderMethod.getReturnType().toString())) {
			hasError = true;
			messager.printMessage(
					Diagnostic.Kind.ERROR,
					"Specified converter: '" + converter.getQualifiedName() + "' has invalid type for JSON_READER field. It must be of type: 'com.dslplatform.json.JsonReader.ReadObject<" + fullName + ">'",
					converter,
					getAnnotation(converter, converterType));
		} else if (jsonWriterField != null && !("com.dslplatform.json.JsonWriter.WriteObject<" + fullName + ">").equals(jsonWriterField.asType().toString())
				|| jsonWriterMethod != null && !("com.dslplatform.json.JsonWriter.WriteObject<" + fullName + ">").equals(jsonWriterMethod.getReturnType().toString())) {
			hasError = true;
			messager.printMessage(
					Diagnostic.Kind.ERROR,
					"Specified converter: '" + converter.getQualifiedName() + "' has invalid type for JSON_WRITER " + allowed + ". It must be of type: 'com.dslplatform.json.JsonWriter.WriteObject<" + fullName + ">'",
					converter,
					getAnnotation(converter, converterType));
		}
		return new ConverterInfo(
				converter,
				jsonReaderMethod != null
						? (hasInstance ? "INSTANCE." : "") + jsonReaderMethod.getSimpleName().toString() + "()" : jsonReaderField != null
						?  jsonReaderField.getSimpleName().toString() : "",
				jsonWriterMethod != null
						? (hasInstance ? "INSTANCE." : "") + jsonWriterMethod.getSimpleName().toString() + "()" : jsonWriterField != null
						? jsonWriterField.getSimpleName().toString() : ""
		);
	}

	public List<TypeElement> getTypeHierarchy(TypeElement element) {
		List<TypeElement> result = new ArrayList<TypeElement>();
		getAllTypes(element, result, new HashSet<TypeElement>());
		return result;
	}

	private void getAllTypes(TypeElement element, List<TypeElement> result, Set<TypeElement> processed) {
		if (!processed.add(element) || element.getQualifiedName().contentEquals("java.lang.Object")) return;
		result.add(element);
		for (TypeMirror type : types.directSupertypes(element.asType())) {
			Element current = types.asElement(type);
			if (current instanceof TypeElement) {
				getAllTypes((TypeElement) current, result, processed);
			}
		}
	}

	public void findRelatedReferences() {
		int total;
		do {
			total = structs.size();
			List<StructInfo> items = new ArrayList<StructInfo>(structs.values());
			Stack<String> path = new Stack<String>();
			for (StructInfo info : items) {
				if (info.converter != null) continue;
				path.push(info.element.getSimpleName().toString());
				if (includeBeanMethods) {
					for (Map.Entry<String, AccessElements> p : getBeanProperties(info.element, info.constructor, info.factory).entrySet()) {
						analyzeAttribute(info, p.getValue().read.getReturnType(), p.getKey(), p.getValue(), "bean property", path);
					}
				}
				if (includeExactMethods) {
					for (Map.Entry<String, AccessElements> p : getExactProperties(info.element, info.constructor, info.factory).entrySet()) {
						if (!info.attributes.containsKey(p.getKey()) || info.annotation != null) {
							analyzeAttribute(info, p.getValue().read.getReturnType(), p.getKey(), p.getValue(), "exact property", path);
						}
					}
				}
				if (includeFields) {
					for (Map.Entry<String, AccessElements> f : getPublicFields(info.element, onlyBasicFeatures, info.constructor, info.factory).entrySet()) {
						if (!info.attributes.containsKey(f.getKey()) || info.annotation != null) {
							analyzeAttribute(info, f.getValue().field.asType(), f.getKey(), f.getValue(), "field", path);
						}
					}
				}
				path.pop();
			}
		} while (total != structs.size());
	}

	public static String objectName(final String type) {
		return "int".equals(type) ? "java.lang.Integer"
				: "long".equals(type) ? "java.lang.Long"
				: "double".equals(type) ? "java.lang.Double"
				: "float".equals(type) ? "java.lang.Float"
				: "char".equals(type) ? "java.lang.Character"
				: "byte".equals(type) ? "java.lang.Byte"
				: "short".equals(type) ? "java.lang.Short"
				: "boolean".equals(type) ? "java.lang.Boolean"
				: type;
	}

	private void analyzeAttribute(StructInfo info, TypeMirror type, String name, AccessElements access, String target, Stack<String> path) {
		Element element = access.field != null ? access.field : access.read;
		path.push(name);
		if (!info.properties.contains(element) && !hasIgnoredAnnotation(element)) {
			TypeMirror referenceType = access.field != null ? access.field.asType() : access.read.getReturnType();
			Element referenceElement = types.asElement(referenceType);
			TypeMirror converterMirror = findConverter(element);
			final ConverterInfo converter;
			if (converterMirror != null) {
				TypeElement typeConverter = elements.getTypeElement(converterMirror.toString());
				String javaType = type.toString();
				String objectType = objectName(javaType);
				Element declaredType = objectType.equals(javaType)
						? types.asElement(type)
						: elements.getTypeElement(objectType);
				converter = validateConverter(typeConverter, declaredType, objectType);
			} else converter = null;
			String referenceName = referenceType.toString();
			boolean isJsonObject = isJsonObject(referenceElement);
			boolean typeResolved = converter != null || isJsonObject || supportedTypes.contains(referenceName) || structs.containsKey(referenceName);
			boolean hasUnknown = false;
			if (!typeResolved) {
				Map<String, Boolean> references = analyzeParts(referenceType);
				for (Map.Entry<String, Boolean> kv : references.entrySet()) {
					if (!kv.getValue()) {
						hasUnknown = true;
					}
				}
			}
			AnnotationMirror annotation = access.annotation;
			CompiledJson.TypeSignature typeSignature = typeSignatureValue(annotation);
			AttributeInfo attr =
					new AttributeInfo(
							name,
							access.read,
							access.write,
							access.field,
							type,
							annotation,
							hasNonNullable(element, annotation),
							hasMandatoryAnnotation(element, annotation),
							index(element, annotation),
							findNameAlias(element, annotation),
							isFullMatch(element, annotation),
							typeSignature,
							converter,
							isJsonObject);
			String[] alternativeNames = getAlternativeNames(attr.element);
			if (alternativeNames != null) {
				attr.alternativeNames.addAll(Arrays.asList(alternativeNames));
			}
			AttributeInfo other = info.attributes.get(attr.id);
			if (other != null
					&& (other.annotation != null && attr.annotation == null
						|| other.annotation == null && attr.annotation == null && other.field == null && attr.field != null)) {
				//if other property has annotation, but this does not, skip over this property
				//if both properties don't have annotation, use the non field one
				path.pop();
				return;
			} else if (other != null
					&& (!other.name.equals(attr.name)
						|| other.id.equals(attr.id) && other.field == null && attr.field == null)) {
				//if properties have different name or both are method based raise an error
				hasError = true;
				messager.printMessage(
						Diagnostic.Kind.ERROR,
						"Duplicate alias detected on " + (attr.field != null ? "field: " : "property: ") + attr.name,
						attr.element,
						info.annotation);
			}
			if (!typeResolved && hasUnknown) {
				info.unknowns.put(attr.id, referenceType);
			}
			info.attributes.put(attr.id, attr);
			info.properties.add(attr.element);
			checkRelatedProperty(type, info.discoveredBy, target, info.element, element, path);
		}
		path.pop();
	}

	private void checkRelatedProperty(TypeMirror returnType, DeclaredType discoveredBy, String access, Element inside, Element property, Stack<String> path) {
		TypeMirror converter = findConverter(property);
		if (converter != null) return;
		String typeName = returnType.toString();
		if (supportedTypes.contains(typeName)) return;
		TypeElement el = elements.getTypeElement(typeName);
		if (el != null) {
			findStructs(el, discoveredBy, el + " is referenced as " + access + " from '" + inside.asType() + "' through CompiledJson annotation.", path);
			return;
		}
		if (returnType instanceof ArrayType) {
			ArrayType at = (ArrayType) returnType;
			el = elements.getTypeElement(at.getComponentType().toString());
			if (el != null) {
				findStructs(el, discoveredBy,el + " is referenced as array " + access + " from '" + inside.asType() + "' through CompiledJson annotation.", path);
				return;
			}
		}
		int genInd = typeName.indexOf('<');
		if (genInd == -1) return;
		String subtype = typeName.substring(genInd + 1, typeName.lastIndexOf('>'));
		if (supportedTypes.contains(subtype)) return;
		Set<String> parts = new LinkedHashSet<String>();
		extractTypes(subtype, parts);
		for (String st : parts) {
			if (!structs.containsKey(st) && !supportedTypes.contains(st)) {
				el = elements.getTypeElement(st);
				if (el != null) {
					if (!el.getTypeParameters().isEmpty()) {
						if (containerSupport.isSupported(st)) continue;
					}
					findStructs(el, discoveredBy, el + " is referenced as collection " + access + " from '" + inside.asType() + "' through CompiledJson annotation.", path);
				}
			}
		}
	}

	private boolean requiresPublic(Element element) {
		if (onlyBasicFeatures) return true;
		final String name = element.asType().toString();
		if (name.startsWith("java.")) return true; //TODO: maybe some other namespaces !?
		final PackageElement pkg = elements.getPackageOf(element);
		if (pkg == null) return false;
		final Package packageClass = Package.getPackage(pkg.getQualifiedName().toString());
		return packageClass != null && packageClass.isSealed();
	}

	private void findStructs(Element el, DeclaredType discoveredBy, String errorMessge, Stack<String> path) {
		if (!(el instanceof TypeElement)) return;
		String typeName = el.asType().toString();
		if (structs.containsKey(typeName) || supportedTypes.contains(typeName)) return;
		final TypeElement element = (TypeElement) el;
		boolean isMixin = element.getKind() == ElementKind.INTERFACE
				|| element.getKind() == ElementKind.CLASS && element.getModifiers().contains(Modifier.ABSTRACT);
		boolean isJsonObject = isJsonObject(element);
		final AnnotationMirror annotation = scanClassForAnnotation(element, discoveredBy);
		if (element.getModifiers().contains(Modifier.PRIVATE)) {
			hasError = true;
			messager.printMessage(
					Diagnostic.Kind.ERROR,
					errorMessge + ", therefore '" + element.asType() + "' can't be private ",
					element,
					annotation);
		} else if (requiresPublic(element) && !element.getModifiers().contains(Modifier.PUBLIC)) {
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
		} else if (onlyBasicFeatures && (element.getQualifiedName().contentEquals(element.getSimpleName())
				|| element.getNestingKind().isNested() && element.getModifiers().contains(Modifier.STATIC)
				&& element.getEnclosingElement() instanceof TypeElement
				&& ((TypeElement) element.getEnclosingElement()).getQualifiedName().contentEquals(element.getEnclosingElement().getSimpleName()))) {
			hasError = true;
			messager.printMessage(
					Diagnostic.Kind.ERROR,
					errorMessge + ", but class '" + element.getQualifiedName() + "' is defined without a package name and cannot be accessed.",
					element,
					annotation);
		} else if (element.getNestingKind().isNested() && requiresPublic(element.getEnclosingElement()) && !element.getEnclosingElement().getModifiers().contains(Modifier.PUBLIC)) {
			hasError = true;
			messager.printMessage(
					Diagnostic.Kind.ERROR,
					errorMessge + ", therefore '" + element.getEnclosingElement().asType() + "' must be public ",
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
								findStructs(deserializeAs, discoveredBy, errorMessge, path);
							}
						}
					}
				} else if (annotationUsage != AnnotationUsage.IMPLICIT) {
					if (annotationUsage == AnnotationUsage.EXPLICIT) {
						hasError = true;
						messager.printMessage(
								Diagnostic.Kind.ERROR,
								"Annotation usage is set to explicit, but '" + element.getQualifiedName() + "' is used implicitly through references. " +
										"Either change usage to implicit, use @Ignore on property referencing this type or register custom converter for problematic type. " + errorMessge,
								element);
					} else if (element.getQualifiedName().toString().startsWith("java.")) {
						hasError = true;
						messager.printMessage(
								Diagnostic.Kind.ERROR,
								"Annotation usage is set to non-java, but '" + element.getQualifiedName() + "' is found in java package. " +
										"Either change usage to implicit, use @Ignore on property referencing this type, register custom converter for problematic type or add annotation to this type. " +
										errorMessge,
								element);
					}
				}
			}
			CompiledJson.Format[] formats = getFormats(annotation);
			if ((new HashSet<CompiledJson.Format>(Arrays.asList(formats))).size() != formats.length) {
				hasError = true;
				messager.printMessage(
						Diagnostic.Kind.ERROR,
						"Duplicate format detected on '" + element.getQualifiedName() + "'.",
						element,
						annotation);
			}
			String name = "struct" + structs.size();
			String binaryName = elements.getBinaryName(element).toString();
			StructInfo info =
					new StructInfo(
							element,
							discoveredBy,
							name,
							binaryName,
							type,
							isJsonObject,
							findMatchingConstructors(element),
							findAnnotatedConstructor(element, discoveredBy),
							findAnnotatedFactory(element, discoveredBy),
							annotation,
							onUnknown,
							typeSignature,
							deserializeAs,
							deserializeName(annotation),
							isMinified(annotation),
							formats);
			info.path.addAll(path);
			if (type == ObjectType.ENUM) {
				info.constants.addAll(getEnumConstants(info.element));
			}
			structs.put(typeName, info);
		}
	}

	@Nullable
	private String validateDeserializeAs(TypeElement source, TypeElement target) {
		if (target.getModifiers().contains(Modifier.PRIVATE)) {
			return "can't be private";
		} else if (requiresPublic(target) && !target.getModifiers().contains(Modifier.PUBLIC)) {
			return "must be public";
		} else if (target.getNestingKind().isNested() && !target.getModifiers().contains(Modifier.STATIC)) {
			return "can't be a nested member. Only public static nested classes are supported";
		} else if (target.getNestingKind().isNested() && !target.getModifiers().contains(Modifier.PUBLIC)) {
			return "must be public when nested in another class";
		} else if (onlyBasicFeatures && (target.getQualifiedName().contentEquals(target.getSimpleName())
				|| target.getNestingKind().isNested() && target.getModifiers().contains(Modifier.STATIC)
				&& target.getEnclosingElement() instanceof TypeElement
				&& ((TypeElement) target.getEnclosingElement()).getQualifiedName().contentEquals(target.getEnclosingElement().getSimpleName()))) {
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

	@Nullable
	public List<ExecutableElement> findMatchingConstructors(Element element) {
		if (element.getKind() == ElementKind.INTERFACE
				|| element.getKind() == ElementKind.ENUM
				|| element.getKind() == ElementKind.CLASS && element.getModifiers().contains(Modifier.ABSTRACT)) {
			return null;
		}
		List<ExecutableElement> matchingCtors = new ArrayList<ExecutableElement>();
		for (ExecutableElement constructor : ElementFilter.constructorsIn(element.getEnclosedElements())) {
			if (!constructor.getModifiers().contains(Modifier.PRIVATE)
					&& !constructor.getModifiers().contains(Modifier.PROTECTED)
					&& (!requiresPublic(element) || constructor.getModifiers().contains(Modifier.PUBLIC))) {
				matchingCtors.add(constructor);
			}
		}
		return matchingCtors;
	}

	@Nullable
	public ExecutableElement findAnnotatedConstructor(Element element, DeclaredType discoveredBy) {
		if (element.getKind() == ElementKind.INTERFACE
				|| element.getKind() == ElementKind.ENUM
				|| element.getKind() == ElementKind.CLASS && element.getModifiers().contains(Modifier.ABSTRACT)) {
			return null;
		}
		for (ExecutableElement constructor : ElementFilter.constructorsIn(element.getEnclosedElements())) {
			AnnotationMirror discAnn = getAnnotation(constructor, discoveredBy);
			if (discAnn != null) {
				if (constructor.getModifiers().contains(Modifier.PRIVATE)
						|| constructor.getModifiers().contains(Modifier.PROTECTED)
						|| requiresPublic(element) && !constructor.getModifiers().contains(Modifier.PUBLIC)) {
					hasError = true;
					messager.printMessage(
							Diagnostic.Kind.ERROR,
							"Constructor in '" + element.asType() + "' is annotated with " + discoveredBy + ", but it's not accessible.",
							constructor,
							discAnn);
				}
				return constructor;
			}
			for (AnnotationMirror ann : constructor.getAnnotationMirrors()) {
				if (alternativeCtors.contains(ann.getAnnotationType().toString())) {
					if (constructor.getModifiers().contains(Modifier.PRIVATE)
							|| constructor.getModifiers().contains(Modifier.PROTECTED)
							|| requiresPublic(element) && !constructor.getModifiers().contains(Modifier.PUBLIC)) {
						hasError = true;
						messager.printMessage(
								Diagnostic.Kind.ERROR,
								"Constructor in '" + element.asType() + "' is annotated with " + ann.getAnnotationType() + ", but it's not public.",
								constructor,
								ann);
					}
					return constructor;
				}
			}
		}
		return null;
	}

	@Nullable
	public ExecutableElement findAnnotatedFactory(Element element, DeclaredType discoveredBy) {
		if (element.getKind() == ElementKind.INTERFACE
				|| element.getKind() == ElementKind.ENUM) {
			return null;
		}
		for (ExecutableElement method : ElementFilter.methodsIn(element.getEnclosedElements())) {
			AnnotationMirror discAnn = getAnnotation(method, discoveredBy);
			if (discAnn != null) {
				if (method.getModifiers().contains(Modifier.PRIVATE)
						|| !method.getModifiers().contains(Modifier.STATIC)
						|| requiresPublic(element) && !method.getModifiers().contains(Modifier.PUBLIC)) {
					hasError = true;
					messager.printMessage(
							Diagnostic.Kind.ERROR,
							"Factory method in '" + element.asType() + "' is annotated with " + discoveredBy + ", but it's not accessible.",
							method,
							discAnn);
				}
				return method;
			}
			for (AnnotationMirror ann : method.getAnnotationMirrors()) {
				if (alternativeFactories.contains(ann.getAnnotationType().toString())) {
					if (method.getModifiers().contains(Modifier.PRIVATE)
							|| !method.getModifiers().contains(Modifier.STATIC)
							|| requiresPublic(element) && !method.getModifiers().contains(Modifier.PUBLIC)) {
						hasError = true;
						messager.printMessage(
								Diagnostic.Kind.ERROR,
								"Factory method in '" + element.asType() + "' is annotated with " + ann.getAnnotationType() + ", but it's not accessible.",
								method,
								ann);
					}
					return method;
				}
			}
		}
		return null;
	}

	private Map<String, Boolean> analyzeParts(TypeMirror target) {
		String typeName = target.toString();
		if (structs.containsKey(typeName)) {
			return Collections.singletonMap(typeName, true);
		} else if (supportedTypes.contains(typeName)) {
			return Collections.singletonMap(typeName, true);
		}
		if (target instanceof ArrayType) {
			ArrayType at = (ArrayType) target;
			return analyzeParts(at.getComponentType());
		}
		int genInd = typeName.indexOf('<');
		if (genInd == -1) return Collections.singletonMap(typeName, false);
		Map<String, Boolean> found = new LinkedHashMap<String, Boolean>();
		String rawClass = typeName.substring(0, genInd);
		TypeElement raw = elements.getTypeElement(rawClass);
		if (raw != null) {
			if (supportedTypes.contains(rawClass)) {
				found.put(rawClass, true);
			} else {
				found.put(rawClass, containerSupport.isSupported(rawClass));
			}
		}
		String subtype = typeName.substring(genInd + 1, typeName.lastIndexOf('>'));
		if (structs.containsKey(subtype) || supportedTypes.contains(subtype)) {
			found.put(subtype, true);
			return found;
		}
		Set<String> parts = new LinkedHashSet<String>();
		extractTypes(subtype, parts);
		for (String st : parts) {
			if (structs.containsKey(st) || supportedTypes.contains(st)) {
				found.put(st, true);
			} else {
				TypeElement el = elements.getTypeElement(st);
				if (el == null || !el.getTypeParameters().isEmpty()) {
					found.put(st, containerSupport.isSupported(st));
				} else if (isJsonObject(el)) {
					found.put(st, true);
				} else {
					found.putAll(analyzeParts(el.asType()));
				}
			}
		}
		return found;
	}

	private void extractTypes(String signature, Set<String> parts) {
		if (signature.isEmpty()) return;
		if (supportedTypes.contains(signature) || structs.containsKey(signature)) {
			parts.add(signature);
			return;
		}
		int nextComma = signature.indexOf(',');
		int nextGen = signature.indexOf('<');
		if (nextComma == -1 && nextGen == -1) {
			parts.add(signature);
		} else if (nextComma != -1 && (nextGen == -1 || nextGen > nextComma)) {
			String first = signature.substring(0, nextComma);
			String second = signature.substring(nextComma + 1, signature.length());
			parts.add(first);
			extractTypes(second, parts);
		} else {
			String first = signature.substring(0, nextGen);
			String second = signature.substring(nextGen + 1, signature.length() - 1);
			parts.add(first);
			extractTypes(second, parts);
		}
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

	public boolean isJsonObject(Element el) {
		if (!(el instanceof TypeElement)) return false;
		TypeElement element = (TypeElement)el;
		for (TypeMirror type : element.getInterfaces()) {
			if (JsonObject.class.getName().equals(type.toString())) {
				//TODO: JSON_READER method support
				for (VariableElement field : ElementFilter.fieldsIn(element.getEnclosedElements())) {
					if ("JSON_READER".equals(field.getSimpleName().toString())) {
						if (!field.getModifiers().contains(Modifier.PUBLIC)
								|| !field.getModifiers().contains(Modifier.STATIC)) {
							hasError = true;
							messager.printMessage(
									Diagnostic.Kind.ERROR,
									"'" + element.getQualifiedName() + "' is 'com.dslplatform.json.JsonObject', but it's JSON_READER field is not public and static. " +
											"It can't be used for serialization/deserialization this way. " +
											"You probably want to change JSON_READER field so it's public and static.",
									element);
						}
						String correctType = "com.dslplatform.json.JsonReader.ReadJsonObject<" + element.getQualifiedName() + ">";
						if (!(correctType.equals(field.asType().toString()))) {
							hasError = true;
							messager.printMessage(
									Diagnostic.Kind.ERROR,
									"'" + element.getQualifiedName() + "' is 'com.dslplatform.json.JsonObject', but it's JSON_READER field is not of correct type. " +
											"It can't be used for serialization/deserialization this way. " +
											"You probably want to change JSON_READER field to: '" + correctType + "'",
									element);
						}
						return true;
					}
				}
				messager.printMessage(
						Diagnostic.Kind.ERROR,
						"'" + element.getQualifiedName() + "' is 'com.dslplatform.json.JsonObject', but it doesn't have JSON_READER field. " +
								"It can't be used for serialization/deserialization this way. " +
								"You probably want to add public static JSON_READER field.",
						element);
				return true;
			}
		}
		return false;
	}

	public static class AccessElements {
		public final ExecutableElement read;
		public final ExecutableElement write;
		public final VariableElement field;
		public final VariableElement arg;
		public final AnnotationMirror annotation;

		private AccessElements(@Nullable ExecutableElement read, @Nullable ExecutableElement write,
                               @Nullable VariableElement arg, @Nullable VariableElement field,
                               @Nullable AnnotationMirror annotation) {
			this.read = read;
			this.write = write;
			this.field = field;
			this.arg = arg;
			this.annotation = annotation;
		}

		public static AccessElements readWrite(ExecutableElement read, ExecutableElement write, @Nullable AnnotationMirror annotation) {
			return new AccessElements(read, write, null, null, annotation);
		}

		public static AccessElements field(VariableElement field, VariableElement arg, @Nullable AnnotationMirror annotation) {
			return new AccessElements(null, null, arg, field, annotation);
		}

		public static AccessElements readOnly(ExecutableElement read, VariableElement arg, @Nullable AnnotationMirror annotation) {
			return new AccessElements(read, null, arg, null, annotation);
		}
	}

	private Map<String, VariableElement> getArguments(@Nullable ExecutableElement element) {
		if (element == null) return Collections.emptyMap();
		Map<String, VariableElement> arguments = new HashMap<String, VariableElement>();
		for (VariableElement p : element.getParameters()) {
			arguments.put(p.getSimpleName().toString(), p);
		}
		return arguments;
	}

	private boolean isCompatibileType(TypeMirror left, TypeMirror right) {
		if (left.equals(right)) return true;
		final String leftStr = left.toString();
		final String rightStr = right.toString();
		if (leftStr.equals(rightStr)) return true;
		int ind = leftStr.indexOf('<');
		if (ind == -1 || rightStr.indexOf('<') != ind) return false;
		if (left.getKind() != right.getKind()) return false;
		if (!leftStr.substring(0, ind).equals(rightStr.substring(0, ind))) return false;
		return types.isAssignable(right, left);
	}

	public Map<String, AccessElements> getBeanProperties(TypeElement element, ExecutableElement ctor, @Nullable ExecutableElement factory) {
		Map<String, ExecutableElement> setters = new HashMap<String, ExecutableElement>();
		Map<String, ExecutableElement> getters = new HashMap<String, ExecutableElement>();
		Map<String, VariableElement> arguments = getArguments(factory != null ? factory : ctor);
		for (TypeElement inheritance : getTypeHierarchy(element)) {
			boolean isPublicInterface = inheritance.getKind() == ElementKind.INTERFACE
					&& inheritance.getModifiers().contains(Modifier.PUBLIC);
			for (ExecutableElement method : ElementFilter.methodsIn(inheritance.getEnclosedElements())) {
				String name = method.getSimpleName().toString();
				boolean isAccessible = isPublicInterface && !method.getModifiers().contains(Modifier.PRIVATE)
						|| method.getModifiers().contains(Modifier.PUBLIC)
						&& !method.getModifiers().contains(Modifier.STATIC)
						&& !method.getModifiers().contains(Modifier.NATIVE)
						&& !method.getModifiers().contains(Modifier.TRANSIENT)
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
					setters.put(property, method);
				}
			}
		}
		Map<String, AccessElements> result = new HashMap<String, AccessElements>();
		for (Map.Entry<String, ExecutableElement> kv : getters.entrySet()) {
			ExecutableElement setter = setters.get(kv.getKey());
			VariableElement setterArgument = setter == null ? null : setter.getParameters().get(0);
			VariableElement arg = arguments.get(kv.getKey());
			String returnType = kv.getValue().getReturnType().toString();
			AnnotationMirror annotation = annotation(kv.getValue(), setter, null, arg);
			if (setterArgument != null && setterArgument.asType().toString().equals(returnType)) {
				result.put(kv.getKey(), AccessElements.readWrite(kv.getValue(), setter, annotation));
			} else if (setterArgument != null && (setterArgument.asType() + "<").startsWith(returnType)) {
				result.put(kv.getKey(), AccessElements.readWrite(kv.getValue(), setter, annotation));
			} else if (!onlyBasicFeatures && arg != null && isCompatibileType(arg.asType(), kv.getValue().getReturnType())) {
				result.put(kv.getKey(), AccessElements.readOnly(kv.getValue(), arg, annotation));
			}
		}
		return result;
	}

	public Map<String, AccessElements> getExactProperties(TypeElement element, ExecutableElement ctor, @Nullable ExecutableElement factory) {
		Map<String, ExecutableElement> setters = new HashMap<String, ExecutableElement>();
		Map<String, ExecutableElement> getters = new HashMap<String, ExecutableElement>();
		Map<String, VariableElement> arguments = getArguments(factory != null ? factory : ctor);
		for (TypeElement inheritance : getTypeHierarchy(element)) {
			boolean isPublicInterface = inheritance.getKind() == ElementKind.INTERFACE
					&& inheritance.getModifiers().contains(Modifier.PUBLIC);
			for (ExecutableElement method : ElementFilter.methodsIn(inheritance.getEnclosedElements())) {
				String name = method.getSimpleName().toString();
				boolean isAccessible = isPublicInterface && !method.getModifiers().contains(Modifier.PRIVATE)
						|| method.getModifiers().contains(Modifier.PUBLIC)
						&& !method.getModifiers().contains(Modifier.STATIC)
						&& !method.getModifiers().contains(Modifier.NATIVE)
						&& !method.getModifiers().contains(Modifier.TRANSIENT)
						&& !method.getModifiers().contains(Modifier.ABSTRACT);
				if (name.startsWith("get") || name.startsWith("set") || !isAccessible) {
					continue;
				}
				if (method.getParameters().size() == 0 && method.getReturnType() != null) {
					if (!getters.containsKey(name)) {
						getters.put(name, method);
					}
				} else if (method.getParameters().size() == 1) {
					setters.put(name, method);
				}
			}
		}
		Map<String, AccessElements> result = new HashMap<String, AccessElements>();
		for (Map.Entry<String, ExecutableElement> kv : getters.entrySet()) {
			ExecutableElement setter = setters.get(kv.getKey());
			VariableElement setterArgument = setter == null ? null : setter.getParameters().get(0);
			VariableElement arg = arguments.get(kv.getKey());
			String returnType = kv.getValue().getReturnType().toString();
			AnnotationMirror annotation = annotation(kv.getValue(), setter, null, arg);
			if (setterArgument != null && setterArgument.asType().toString().equals(returnType)) {
				result.put(kv.getKey(), AccessElements.readWrite(kv.getValue(), setter, annotation));
			} else if (setterArgument != null && (setterArgument.asType() + "<").startsWith(returnType)) {
				result.put(kv.getKey(), AccessElements.readWrite(kv.getValue(), setter, annotation));
			} else if (!onlyBasicFeatures && arg != null && isCompatibileType(arg.asType(), kv.getValue().getReturnType())) {
				result.put(kv.getKey(), AccessElements.readOnly(kv.getValue(), arg, annotation));
			}
		}
		return result;
	}

	public Map<String, AccessElements> getPublicFields(TypeElement element, boolean mustHaveEmptyCtor, ExecutableElement ctor, @Nullable ExecutableElement factory) {
		Map<String, AccessElements> result = new HashMap<String, AccessElements>();
		Map<String, VariableElement> arguments = getArguments(factory != null ? factory : ctor);
		for (TypeElement inheritance : getTypeHierarchy(element)) {
			for (VariableElement field : ElementFilter.fieldsIn(inheritance.getEnclosedElements())) {
				String name = field.getSimpleName().toString();
				boolean isFinal = field.getModifiers().contains(Modifier.FINAL);
				boolean isAccessible = field.getModifiers().contains(Modifier.PUBLIC)
						&& (!isFinal || !mustHaveEmptyCtor)
						&& !field.getModifiers().contains(Modifier.NATIVE)
						&& !field.getModifiers().contains(Modifier.TRANSIENT)
						&& !field.getModifiers().contains(Modifier.STATIC);
				if (!isAccessible) {
					continue;
				}
				VariableElement arg = arguments.get(name);
				AnnotationMirror annotation = annotation(null, null, field, arg);
				result.put(name, AccessElements.field(field, arg, annotation));
			}
		}
		return result;
	}

	public void findImplementations(Collection<StructInfo> structs) {
		for (StructInfo current : structs) {
			if (current.type == ObjectType.MIXIN) {
				String signature = current.element.asType().toString();
				for (StructInfo info : structs) {
					if (info.type == ObjectType.CLASS) {
						checkParentSignatures(info, info.element, current.implementations, signature, new HashSet<TypeElement>());
					}
				}
			}
		}
	}

	private void checkParentSignatures(StructInfo info, TypeElement element, Set<StructInfo> implementations, String signature, Set<TypeElement> processed) {
		if (!processed.add(element) || element.getQualifiedName().contentEquals("java.lang.Object")) return;
		if (element.asType().toString().equals(signature)) {
			implementations.add(info);
		}
		for (TypeMirror type : types.directSupertypes(element.asType())) {
			Element current = types.asElement(type);
			if (current instanceof TypeElement) {
				checkParentSignatures(info, (TypeElement) current, implementations, signature, processed);
			}
		}
	}

	@Nullable
	public String[] getAlternativeNames(Element property) {
		AnnotationMirror dslAnn = getAnnotation(property, attributeType);
		if (dslAnn == null) return null;
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
		return null;
	}

	public boolean isFullMatch(Element property, @Nullable AnnotationMirror dslAnn) {
		if (dslAnn == null) return false;
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = dslAnn.getElementValues();
		for (ExecutableElement ee : values.keySet()) {
			if (ee.toString().equals("hashMatch()")) {
				Object val = values.get(ee).getValue();
				return val != null && !((Boolean) val);
			}
		}
		return false;
	}

	public int index(Element property, @Nullable AnnotationMirror dslAnn) {
		if (dslAnn != null) {
			Map<? extends ExecutableElement, ? extends AnnotationValue> values = dslAnn.getElementValues();
			for (ExecutableElement ee : values.keySet()) {
				if (ee.toString().equals("index()")) {
					Object val = values.get(ee).getValue();
					if (val == null) return -1;
					return (Integer) val;
				}
			}
		}
		for (AnnotationMirror ann : property.getAnnotationMirrors()) {
			Integer index = matchCustomInteger(ann, alternativeIndex);
			if (index != null && index != -1) return index;
		}
		return -1;
	}

	@Nullable
	private AnnotationMirror annotation(@Nullable ExecutableElement read, @Nullable ExecutableElement write,
                                        @Nullable VariableElement field, @Nullable VariableElement arg) {
		AnnotationMirror dslAnn = read == null ? null : getAnnotation(read, attributeType);
		if (dslAnn != null) return dslAnn;
		dslAnn = write == null ? null : getAnnotation(write, attributeType);
		if (dslAnn != null) return dslAnn;
		dslAnn = field == null ? null : getAnnotation(field, attributeType);
		if (dslAnn != null) return dslAnn;
		return arg == null ? null : getAnnotation(arg, attributeType);
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

	@Nullable
	public AnnotationMirror scanClassForAnnotation(TypeElement element, DeclaredType annotationType) {
		AnnotationMirror target = getAnnotation(element, annotationType);
		if (target != null) return target;
		for (ExecutableElement method : ElementFilter.methodsIn(element.getEnclosedElements())) {
			AnnotationMirror discAnn = getAnnotation(method, annotationType);
			if (discAnn != null) return discAnn;
		}
		for (ExecutableElement constructor : ElementFilter.constructorsIn(element.getEnclosedElements())) {
			AnnotationMirror discAnn = getAnnotation(constructor, annotationType);
			if (discAnn != null) return discAnn;
		}
		return null;
	}

	@Nullable
	public AnnotationMirror getAnnotation(Element element, DeclaredType annotationType) {
		for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
			if (types.isSameType(mirror.getAnnotationType(), annotationType)) {
				return mirror;
			}
		}
		return null;
	}

	public boolean hasNonNullable(Element property, @Nullable AnnotationMirror dslAnn) {
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
			Boolean match = matchCustomBoolean(ann, alternativeNonNullable);
			if (match != null) return match;
		}
		return false;
	}

	@Nullable
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

	public static String deserializeName(@Nullable AnnotationMirror annotation) {
		if (annotation == null) return "";
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();
		for (ExecutableElement ee : values.keySet()) {
			if (ee.toString().equals("deserializeName()")) {
				return values.get(ee).getValue().toString();
			}
		}
		return "";
	}

	public boolean hasMandatoryAnnotation(Element property, @Nullable AnnotationMirror dslAnn) {
		if (dslAnn != null) {
			return booleanAnnotationValue(dslAnn, "mandatory()", false);
		}
		for (AnnotationMirror ann : property.getAnnotationMirrors()) {
			Boolean match = matchCustomBoolean(ann, alternativeMandatory);
			if (match != null) return match;
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

	@Nullable
	public CompiledJson.Behavior onUnknownValue(@Nullable AnnotationMirror annotation) {
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

	@Nullable
	public CompiledJson.TypeSignature typeSignatureValue(@Nullable AnnotationMirror annotation) {
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

	public CompiledJson.Format[] getFormats(@Nullable AnnotationMirror ann) {
		if (ann == null) return new CompiledJson.Format[]{CompiledJson.Format.OBJECT};
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = ann.getElementValues();
		for (ExecutableElement ee : values.keySet()) {
			if ("formats()".equals(ee.toString())) {
				Object val = values.get(ee).getValue();
				if (val == null) return new CompiledJson.Format[]{CompiledJson.Format.OBJECT};
				List list = (List)val;
				CompiledJson.Format[] result = new CompiledJson.Format[list.size()];
				for (int i = 0; i < result.length; i++) {
					AnnotationValue enumVal = (AnnotationValue)list.get(i);
					result[i] = CompiledJson.Format.valueOf(enumVal.getValue().toString());
				}
				return result;
			}
		}
		return new CompiledJson.Format[]{CompiledJson.Format.OBJECT};
	}

	public boolean isMinified(@Nullable AnnotationMirror ann) {
		if (ann == null) return false;
		for (ExecutableElement ee : ann.getElementValues().keySet()) {
			if ("minified()".equals(ee.toString())) {
				AnnotationValue minified = ann.getElementValues().get(ee);
				return (Boolean) minified.getValue();
			}
		}
		return false;
	}

	@Nullable
	public TypeMirror findConverter(Element property) {
		AnnotationMirror dslAnn = getAnnotation(property, attributeType);
		if (dslAnn == null) return null;
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = dslAnn.getElementValues();
		for (ExecutableElement ee : values.keySet()) {
			if (ee.toString().equals("converter()")) {
				TypeMirror mirror = (TypeMirror) values.get(ee).getValue();
				return mirror != null && mirror.toString().equals(JsonAttribute.class.getName()) ? null : mirror;
			}
		}
		return null;
	}

	@Nullable
	public String findNameAlias(Element property, @Nullable AnnotationMirror dslAnn) {
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
			String name = matchCustomString(ann, alternativeAlias);
			if (name != null && !name.isEmpty()) return name;
		}
		return null;
	}

	@Nullable
	private static Boolean matchCustomBoolean(
			AnnotationMirror ann,
			Map<String, List<AnnotationMapping<Boolean>>> alternatives) {
		String name = ann.getAnnotationType().toString();
		if (alternatives.containsKey(name)) {
			List<AnnotationMapping<Boolean>> mappings = alternatives.get(name);
			if (mappings == null || mappings.isEmpty()) return true;
			for (AnnotationMapping<Boolean> m : mappings) {
				Map<? extends ExecutableElement, ? extends AnnotationValue> values = ann.getElementValues();
				for (ExecutableElement ee : values.keySet()) {
					if (ee.toString().equals(m.name)) {
						Object val = values.get(ee).getValue();
						if (val == null && m.value == null) return true;
						return val != null && val == m.value;
					}
				}
			}
		}
		return null;
	}

	@Nullable
	private static String matchCustomString(
			AnnotationMirror ann,
			Map<String, String> alternatives) {
		String value = alternatives.get(ann.getAnnotationType().toString());
		if (value != null) {
			Map<? extends ExecutableElement, ? extends AnnotationValue> values = ann.getElementValues();
			for (ExecutableElement ee : values.keySet()) {
				if (ee.toString().equals(value)) {
					AnnotationValue val = values.get(ee);
					if (val == null) return null;
					if (val.getValue() == null) return null;
					return val.getValue().toString();
				}
			}
		}
		return null;
	}

	@Nullable
	private static Integer matchCustomInteger(
			AnnotationMirror ann,
			Map<String, String> alternatives) {
		String value = alternatives.get(ann.getAnnotationType().toString());
		if (value != null) {
			Map<? extends ExecutableElement, ? extends AnnotationValue> values = ann.getElementValues();
			for (ExecutableElement ee : values.keySet()) {
				if (ee.toString().equals(value)) {
					AnnotationValue val = values.get(ee);
					if (val == null) return null;
					if (val.getValue() == null) return null;
					return (Integer)val.getValue();
				}
			}
		}
		return null;
	}
}
