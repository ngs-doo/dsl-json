package com.dslplatform.json.processor;

import com.dslplatform.json.*;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
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

	private final TypeSupport typeSupport;
	private final Set<String> alternativeIgnore;
	private final Map<String, List<AnnotationMapping<Boolean>>> alternativeNonNullable;
	private final Map<String, String> alternativeAlias;
	private final Map<String, List<AnnotationMapping<Boolean>>> alternativeMandatory;
	private final Set<String> alternativeCreators;
	private final Map<String, String> alternativeIndex;

	private final TypeMirror baseListType;
	private final TypeMirror baseSetType;
	private final TypeMirror baseMapType;

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

	public Analysis(ProcessingEnvironment processingEnv, AnnotationUsage annotationUsage, LogLevel logLevel, TypeSupport typeSupport) {
		this(processingEnv, annotationUsage, logLevel, typeSupport, null, null, null, null, null, null, UnknownTypes.ERROR, false, true, true, true);
	}

	public Analysis(
			ProcessingEnvironment processingEnv,
			AnnotationUsage annotationUsage,
			LogLevel logLevel,
			TypeSupport typeSupport,
			@Nullable Set<String> alternativeIgnore,
			@Nullable Map<String, List<AnnotationMapping<Boolean>>> alternativeNonNullable,
			@Nullable Map<String, String> alternativeAlias,
			@Nullable Map<String, List<AnnotationMapping<Boolean>>> alternativeMandatory,
			@Nullable Set<String> alternativeCreators,
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
		this.typeSupport = typeSupport;
		this.alternativeIgnore = alternativeIgnore == null ? new HashSet<String>() : alternativeIgnore;
		this.alternativeNonNullable = alternativeNonNullable == null ? new HashMap<String, List<AnnotationMapping<Boolean>>>() : alternativeNonNullable;
		this.alternativeAlias = alternativeAlias == null ? new HashMap<String, String>() : alternativeAlias;
		this.alternativeMandatory = alternativeMandatory == null ? new HashMap<String, List<AnnotationMapping<Boolean>>>() : alternativeMandatory;
		this.alternativeCreators = alternativeCreators == null ? new HashSet<String>() : alternativeCreators;
		this.alternativeIndex = alternativeIndex == null ? new HashMap<String, String>() : alternativeIndex;
		this.unknownTypes = unknownTypes == null ? UnknownTypes.ERROR : unknownTypes;
		this.onlyBasicFeatures = onlyBasicFeatures;
		this.includeFields = includeFields;
		this.includeBeanMethods = includeBeanMethods;
		this.includeExactMethods = includeExactMethods;
		this.baseListType = types.erasure(elements.getTypeElement(List.class.getName()).asType());
		this.baseSetType = types.erasure(elements.getTypeElement(Set.class.getName()).asType());
		this.baseMapType = types.erasure(elements.getTypeElement(Map.class.getName()).asType());
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
			ExecutableElement factory = null;
			ExecutableElement builder = null;
			if (el instanceof TypeElement) {
				classElement = el;
			} else if (el instanceof ExecutableElement && el.getKind() == ElementKind.METHOD) {
				ExecutableElement ee = (ExecutableElement) el;
				Element returnClass = types.asElement(ee.getReturnType());
				Element enclosing = ee.getEnclosingElement();
				if (!el.getModifiers().contains(Modifier.STATIC)
						&& !types.isSameType(ee.getReturnType(), enclosing.asType())
						&& returnClass.toString().equals(enclosing.getEnclosingElement().toString())) {
					builder = ee;
				}
				factory = ee;
				classElement = returnClass;
			} else {
				classElement = el.getEnclosingElement();
			}
			findStructs(classElement, currentAnnotationType, currentAnnotationType + " requires accessible public constructor", path, factory, builder);
		}
		findRelatedReferences();
		findImplementations(structs.values());
		for (StructInfo si : structs.values()) {
			if (si.hasAnnotation() && si.type != ObjectType.CONVERTER && !requiresPublic(si.element) && !si.element.getModifiers().contains(Modifier.PUBLIC)) {
				String siName = si.binaryName.substring(0, si.binaryName.length() - si.element.getSimpleName().length());
				for(StructInfo parent : structs.values()) {
					if (!parent.hasAnnotation() || !parent.implementations.contains(si)) continue;
					String parentName = parent.binaryName.substring(0, parent.binaryName.length() - parent.element.getSimpleName().length());
					if (!siName.equals(parentName)) {
						messager.printMessage(
								Diagnostic.Kind.ERROR,
								"Inheritance detected on non-public type: '" + si.element.asType() + "'. Either make type public or remove @CompiledJson annotation from '" + si.element.asType() + "' or '" + parent.element.asType() + "'",
								si.element,
								si.annotation);
						break;
					}
				}
			}
		}
	}

	public Map<String, StructInfo> analyze() {
		for (Map.Entry<String, StructInfo> it : structs.entrySet()) {
			final StructInfo info = it.getValue();
			final String className = it.getKey();
			if (info.type == ObjectType.CLASS && info.selectedConstructor() == null && info.annotatedFactory == null
					&& info.builder == null && !info.hasKnownConversion() && info.matchingConstructors != null) {
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
					Map<String, PartKind> references = analyzeParts(kv.getValue());
					for (Map.Entry<String, PartKind> pair : references.entrySet()) {
						Diagnostic.Kind kind;
						switch (pair.getValue()) {
							case UNKNOWN:
								hasError = hasError || unknownTypes == UnknownTypes.ERROR;
								kind = unknownTypes == UnknownTypes.ERROR ? Diagnostic.Kind.ERROR : Diagnostic.Kind.WARNING;
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
								break;

							case RAW_TYPE:
								if (structs.containsKey("java.lang.Object")) continue;
								hasError = hasError || unknownTypes == UnknownTypes.ERROR;
								kind = unknownTypes == UnknownTypes.ERROR ? Diagnostic.Kind.ERROR : Diagnostic.Kind.WARNING;
								if (kind == Diagnostic.Kind.ERROR || logLevel.isVisible(LogLevel.INFO)) {
									if (kv.getValue().toString().equals(pair.getKey())) {
										messager.printMessage(
												kind,
												"Property " + kv.getKey() + " is referencing raw type: '" + kv.getValue()
														+ "'. Specify type arguments, register custom converter, mark property as ignored or enable unknown types",
												attr != null ? attr.element : info.element,
												info.annotation);
									} else {
										messager.printMessage(
												kind,
												"Property " + kv.getKey() + " is referencing type: '" + kv.getValue() + "' which has a raw type part: '"
														+ pair.getKey() + "'. Specify type arguments, register custom converter, mark property as ignored or enable unknown types",
												attr != null ? attr.element : info.element,
												info.annotation);
									}
								}
								break;
						}
					}
				}
			}
			if (unknownTypes != UnknownTypes.ALLOW) {
				for (AttributeInfo attr : info.attributes.values()) {
					if (attr.converter != null || attr.isJsonObject) continue;
					Map<String, PartKind> references = analyzeParts(attr.type);
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
			if (info.builder == null && unknownTypes != UnknownTypes.ALLOW && info.type == ObjectType.MIXIN && info.implementations.isEmpty()) {
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
			if (info.type == ObjectType.CLASS && !info.hasKnownConversion() && info.annotatedFactory != null) {
				if (onlyBasicFeatures) {
					hasError = true;
					messager.printMessage(
							Diagnostic.Kind.ERROR,
							"Factory methods are not available with current analysis setup. Use annotation processor which supports such feature",
							info.annotatedFactory,
							info.annotation);
				} else if (!types.isAssignable(info.annotatedFactory.getReturnType(), info.element.asType())) {
					hasError = true;
					messager.printMessage(
							Diagnostic.Kind.ERROR,
							"Wrong factory result type: '" + info.annotatedFactory.getReturnType() + "'. Result must be assignable to '" + info.element.asType() + "'",
							info.annotatedFactory,
							info.annotation);
				} else {
					for (VariableElement p : info.annotatedFactory.getParameters()) {
						boolean found = false;
						String argName = p.getSimpleName().toString();
						for (AttributeInfo attr : info.attributes.values()) {
							if (attr.name.equals(argName)) {
								found = true;
								break;
							}
						}
						if (!found) {
							for (AttributeInfo attr : info.inheritedAttributes()) {
								if (attr.name.equals(argName)) {
									found = true;
									break;
								}
							}
						}
						if (!found) {
							hasError = true;
							if (info.objectFormatPolicy == CompiledJson.ObjectFormatPolicy.EXPLICIT) {
								messager.printMessage(
										Diagnostic.Kind.ERROR,
										"Unable to find matching property: '" + argName + "' used in constructor. Since EXPLICIT object format policy is used, please check if all relevant fields are marked with @JsonAttribute.",
										info.selectedConstructor(),
										info.annotation);
							} else if (!info.inheritedAttributes().isEmpty()) {
								messager.printMessage(
										Diagnostic.Kind.ERROR,
										"Unable to find matching property: '" + argName + "' used in constructor. Please use the same name as the property in the base class to let dsl-json match them.",
										info.selectedConstructor(),
										info.annotation);
							} else {
								messager.printMessage(
										Diagnostic.Kind.ERROR,
										"Unable to find matching property: '" + argName + "' used in method factory. Either use annotation processor on source code, on bytecode with -parameters flag (to enable parameter names) or manually create an instance via converter",
										info.annotatedFactory,
										info.annotation);
							}
						}
					}
				}
			}
			if (info.type == ObjectType.CLASS && info.hasAnnotation() && !info.hasKnownConversion() && info.attributes.isEmpty() && info.implementations.isEmpty()) {
				boolean isUsedAsImplementation = false;
				for (StructInfo si : structs.values()) {
					if (si.implementations.contains(info)) {
						isUsedAsImplementation = true;
						break;
					}
				}
				if (!isUsedAsImplementation) {
					messager.printMessage(
							Diagnostic.Kind.WARNING,
							"No properties found on: '" + info.element + "'. Since it's not used as implementation for some mixin, it's most likely an invalid class configuration (name mismatch, missing setter, etc...)",
							info.annotatedFactory,
							info.annotation);
				}
			}
			if (info.type == ObjectType.CLASS && !info.hasKnownConversion() && info.usesCtorWithArguments()) {
				for (VariableElement p : info.selectedConstructor().getParameters()) {
					boolean found = false;
					String argName = p.getSimpleName().toString();
					for (AttributeInfo attr : info.attributes.values()) {
						if (attr.name.equals(argName)) {
							found = true;
							break;
						}
					}
					if (!found) {
						for (AttributeInfo attr : info.inheritedAttributes()) {
							if (attr.name.equals(argName)) {
								found = true;
								break;
							}
						}
					}
					if (!found) {
						hasError = true;
						if (info.objectFormatPolicy == CompiledJson.ObjectFormatPolicy.EXPLICIT) {
							messager.printMessage(
									Diagnostic.Kind.ERROR,
									"Unable to find matching property: '" + argName + "' used in constructor. Since EXPLICIT object format policy is used, please check if all relevant fields are marked with @JsonAttribute.",
									info.selectedConstructor(),
									info.annotation);
						} else if (!info.inheritedAttributes().isEmpty()) {
							messager.printMessage(
									Diagnostic.Kind.ERROR,
									"Unable to find matching property: '" + argName + "' used in constructor. Please use the same name as the property in the base class to let dsl-json match them.",
									info.selectedConstructor(),
									info.annotation);
						} else {
							messager.printMessage(
									Diagnostic.Kind.ERROR,
									"Unable to find matching property: '" + argName + "' used in constructor. Either use annotation processor on source code, on bytecode with -parameters flag (to enable parameter names) or manually create an instance via converter",
									info.selectedConstructor(),
									info.annotation);
						}
					}
				}
			}
			if (!info.hasKnownConversion() && info.annotatedFactory == null && info.selectedConstructor() == null && info.builder != null) {
				if (onlyBasicFeatures) {
					hasError = true;
					messager.printMessage(
							Diagnostic.Kind.ERROR,
							"Builder pattern is not available with current analysis setup. Use annotation processor which supports such feature",
							info.builder.type,
							info.builder.annotation);
				} else if (requiresPublic(info.builder.type) && !info.builder.type.getModifiers().contains(Modifier.PUBLIC)) {
					hasError = true;
					messager.printMessage(
							Diagnostic.Kind.ERROR,
							"Builder type: '" + info.builder.type + "' is not accessible",
							info.builder.build,
							info.builder.annotation);
				} else if (!types.isAssignable(info.builder.build.getReturnType(), info.element.asType())) {
					hasError = true;
					messager.printMessage(
							Diagnostic.Kind.ERROR,
							"Wrong builder result type: '" + info.builder.build.getReturnType() + "'. Result must be assignable to '" + info.element.asType() + "'",
							info.builder.build,
							info.builder.annotation);
				} else if (!info.builder.build.getParameters().isEmpty()) {
					hasError = true;
					messager.printMessage(
							Diagnostic.Kind.ERROR,
							"Builder method: '" + info.builder.build.getSimpleName() + "' can't have parameters",
							info.builder.build,
							info.builder.annotation);
				} else if (info.builder.ctor != null && info.builder.factory == null && !info.builder.ctor.getParameters().isEmpty()) {
					hasError = true;
					messager.printMessage(
							Diagnostic.Kind.ERROR,
							"Builder constructor for: '" + info.builder.type + "' can't have parameters",
							info.builder.ctor,
							info.builder.annotation);
				} else if (info.builder.factory != null && !types.isSameType(info.builder.factory.getReturnType(), info.builder.type.asType())) {
					hasError = true;
					messager.printMessage(
							Diagnostic.Kind.ERROR,
							"Wrong builder factory result type: '" + info.builder.build.getReturnType() + "'. Expecting: '" + info.builder.type + "'",
							info.builder.factory,
							info.builder.annotation);
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
				String discriminator = info.discriminator;
				if (discriminator.length() > 0 && onlyBasicFeatures) {
					hasError = true;
					messager.printMessage(
							Diagnostic.Kind.ERROR,
							"Custom $type discriminator is not supported with current analysis setup",
							info.element,
							info.annotation);
				}
				int invalidChartAt = -1;
				for (int i = 0; i < discriminator.length(); i++) {
					char c = discriminator.charAt(i);
					if (c < 32 || c == '"' || c == '\\' || c > 126) {
						invalidChartAt = i;
						break;
					}
				}
				if (invalidChartAt != -1) {
					hasError = true;
					messager.printMessage(
							Diagnostic.Kind.ERROR,
							"Invalid discriminator value: '" + discriminator + "' for mixin: " + className +". Invalid char at: " + invalidChartAt,
							info.element,
							info.annotation);
				}
				for (StructInfo im : info.implementations) {
					String actualName = im.deserializeName.isEmpty() ? im.element.getQualifiedName().toString() : im.deserializeName;
					AttributeInfo attr = im.attributes.get(discriminator);
					if (attr != null) {
						hasError = true;
						messager.printMessage(
								Diagnostic.Kind.ERROR,
								"Conflicting discriminator name detected: '" + discriminator + "' for mixin: " + className + " with property '" + attr.name + "' in class " + im.element.toString(),
								info.element,
								info.annotation);
					}
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
			if (info.type == ObjectType.MIXIN && info.discriminator.length() > 0 && info.implementations.isEmpty()) {
				messager.printMessage(
						Diagnostic.Kind.WARNING,
						"Custom discriminator found: '" + info.discriminator + "', but no implementation detected for mixin: " + className,
						info.element,
						info.annotation);
			}
			if (info.type == ObjectType.CLASS && info.discriminator.length() > 0) {
				if (info.attributes.containsKey(info.discriminator)) {
					messager.printMessage(
							Diagnostic.Kind.WARNING,
							"Discriminator has the same value as one of the attributes. Discriminator will be excluded in favor of attribute value",
							info.element,
							info.annotation);
				}
				int hash = StructInfo.calcHash(info.discriminator);
				for (AttributeInfo attr : info.attributes.values()) {
					boolean sameHash = StructInfo.calcHash(info.propertyName(attr)) == hash;
					for (String name : attr.alternativeNames) {
						sameHash = sameHash || StructInfo.calcHash(name) == hash;
					}
					if (sameHash) {
						hasError = true;
						messager.printMessage(
								Diagnostic.Kind.ERROR,
								"Discriminator has the same hash value as property: " + attr.name + ". Either simulate class discriminator via property, or remove the discriminator value from class",
								info.element,
								info.annotation);
					}
				}
			}
			if (info.type == ObjectType.CLASS && onlyBasicFeatures && !info.hasKnownConversion() && !info.hasEmptyCtor()) {
				hasError = true;
				messager.printMessage(
						Diagnostic.Kind.ERROR,
						"'" + className + "' requires public no argument constructor" + info.pathDescription(),
						info.element,
						info.annotation);
			} else if (info.type == ObjectType.CLASS && !onlyBasicFeatures && !info.hasEmptyCtor() && !info.hasKnownConversion()
					&& info.annotatedFactory == null && info.builder == null && (info.selectedConstructor() == null || info.selectedConstructor().getParameters().size() != info.attributes.size())) {
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
					if (attr.index == -1 && info.createFromEmptyInstance() && info.attributes.size() > 1) {
						hasError = true;
						messager.printMessage(
								Diagnostic.Kind.ERROR,
								"When array format is used on class with multiple properties all properties must have index order defined. Property " + attr.name + " doesn't have index defined",
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
			if (info.objectFormatPolicy == CompiledJson.ObjectFormatPolicy.FULL) {
				for (AttributeInfo attr : info.attributes.values()) {
					if (attr.includeToMinimal == JsonAttribute.IncludePolicy.ALWAYS) {
						messager.printMessage(
								Diagnostic.Kind.WARNING,
								"When object format is set to  FULL, all properties will always be included in the output. It is not necessary to explicitly mark property to be ALWAYS included, since minimal format is not used",
								attr.element,
								attr.annotation);
					}
				}
			}
			if (info.namingStrategy != null) {
				Map<String, String> customNames = info.namingStrategy.prepareNames(info.attributes);
				if (customNames != null) {
					info.serializedNames.putAll(customNames);
				}
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
		TypeMirror target = null;
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = dslAnn.getElementValues();
		for (ExecutableElement ee : values.keySet()) {
			if (ee.toString().equals("target()")) {
				target = (TypeMirror) values.get(ee).getValue();
				break;
			}
		}
		if (target == null) return;
		ConverterInfo signature = validateConverter(converter, target);
		String javaType = signature.targetSignature;
		//TODO: throw an error if multiple non-compatible converters were found!?
		if (!structs.containsKey(javaType)) {
			String objectType = objectName(javaType);
			Element declaredType = signature.targetType;
			String name = "struct" + structs.size();
			TypeElement element = (TypeElement) declaredType;
			String binaryName = elements.getBinaryName(element).toString();
			StructInfo info = new StructInfo(signature, converterType, element, name, objectType.equals(javaType) ? binaryName : javaType);
			structs.put(javaType, info);
		}
	}

	private @Nullable Element findElement(TypeMirror type) {
		String javaType = typeWithoutAnnotations(type.toString());
		String fullName = objectName(javaType);
		return fullName.equals(javaType)
				? types.asElement(type)
				: elements.getTypeElement(fullName);
	}

	private void findAllElements(TypeMirror type, Set<Element> usedTypes, Set<TypeMirror> processed) {
		if (!processed.add(type)) return;
		if (type instanceof ArrayType) {
			findAllElements(((ArrayType) type).getComponentType(), usedTypes, processed);
		} else if (type instanceof DeclaredType) {
			DeclaredType dt = (DeclaredType) type;
			usedTypes.add(dt.asElement());
			for (TypeMirror tm : dt.getTypeArguments()) {
				findAllElements(tm, usedTypes, processed);
			}
		} else if (type instanceof WildcardType) {
			WildcardType wt = (WildcardType) type;
			if (wt.getExtendsBound() != null) {
				findAllElements(wt.getExtendsBound(), usedTypes, processed);
			}
		} else {
			Element element = findElement(type);
			if (element != null) {
				usedTypes.add(element);
			}
		}
	}

	private ConverterInfo validateConverter(TypeElement converter, TypeMirror type) {
		String javaType = typeWithoutAnnotations(type.toString());
		String fullName = objectName(javaType);
		Element declaredType = findElement(type);
		Set<Element> usedTypes = new HashSet<Element>();
		findAllElements(type, usedTypes, new HashSet<TypeMirror>());
		VariableElement jsonReaderField = null;
		VariableElement jsonWriterField = null;
		VariableElement jsonBinderField = null;
		ExecutableElement jsonReaderMethod = null;
		ExecutableElement jsonWriterMethod = null;
		ExecutableElement jsonBinderMethod = null;
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
			}  else if ("JSON_BINDER".equals(field.getSimpleName().toString())) {
				jsonBinderField = field;
			}
		}
		if (!onlyBasicFeatures) {
			for (ExecutableElement method : ElementFilter.methodsIn(converter.getEnclosedElements())) {
				if ("JSON_READER".equals(method.getSimpleName().toString()) || "getJSON_READER".equals(method.getSimpleName().toString())) {
					jsonReaderMethod = method;
				} else if ("JSON_WRITER".equals(method.getSimpleName().toString()) || "getJSON_WRITER".equals(method.getSimpleName().toString())) {
					jsonWriterMethod = method;
				} else if ("JSON_BINDER".equals(method.getSimpleName().toString()) || "getJSON_BINDER".equals(method.getSimpleName().toString())) {
					jsonBinderMethod = method;
				}
			}
		}
		for(Element used : usedTypes) {
			if (used != null && !used.getModifiers().contains(Modifier.PUBLIC)) {
				hasError = true;
				messager.printMessage(
						Diagnostic.Kind.ERROR,
						"Specified target: '" + used + "' must be public",
						converter,
						getAnnotation(converter, converterType));
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
				|| jsonWriterMethod == null && (!jsonWriterField.getModifiers().contains(Modifier.PUBLIC) || !jsonWriterField.getModifiers().contains(Modifier.STATIC))
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
						? jsonWriterField.getSimpleName().toString() : "",
				jsonBinderMethod != null
						? (hasInstance ? "INSTANCE." : "") + jsonBinderMethod.getSimpleName().toString() + "()" : jsonBinderField != null
						? jsonBinderField.getSimpleName().toString() : "",
				javaType,
				declaredType
		);
	}

	private List<TypeElement> getTypeHierarchy(TypeElement element) {
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

	private class PropertyAnalysis {
		public final ExecutableElement creator;
		public final Map<String, AccessElements> allFieldDetails;
		public final Set<String> allKeys = new HashSet<String>();
		public final Map<String, AccessElements> beans;
		public final Map<String, AccessElements> exact;
		public final Map<String, AccessElements> fields;

		public PropertyAnalysis(ExecutableElement creator, TypeElement element, Map<String, VariableElement> arguments) {
			this.creator = creator;
			this.allFieldDetails = getFieldDetails(element);
			this.beans = includeBeanMethods ? getBeanProperties(element, arguments, allFieldDetails) : Collections.<String, AccessElements>emptyMap();
			this.exact = includeExactMethods ? getExactProperties(element, arguments, allFieldDetails) : Collections.<String, AccessElements>emptyMap();
			allKeys.addAll(beans.keySet());
			allKeys.addAll(exact.keySet());
			this.fields = includeFields ? getPublicFields(element, arguments, allKeys) : Collections.<String, AccessElements>emptyMap();
			allKeys.addAll(fields.keySet());
		}
	}

	private void findRelatedReferences() {
		int total;
		do {
			total = structs.size();
			List<StructInfo> items = new ArrayList<StructInfo>(structs.values());
			Stack<String> path = new Stack<String>();
			for (StructInfo info : items) {
				if (info.hasKnownConversion()) continue;
				path.push(info.element.getSimpleName().toString());
				if (!onlyBasicFeatures && info.builder != null && info.annotatedConstructor == null && info.annotatedFactory == null) {
					for (Map.Entry<String, AccessElements> p : getBuilderProperties(info.element, info.builder, includeBeanMethods, includeExactMethods, includeFields).entrySet()) {
						AccessElements ae = p.getValue();
						if (ae.field != null || ae.read != null) {
							analyzeAttribute(info, ae.field != null ? ae.field.asType() : ae.read.getReturnType(), p.getKey(), ae, "builder property", path, null);
						}
					}
				} else {
					Map<ExecutableElement, Map<String, VariableElement>> creatorArguments = new HashMap<ExecutableElement, Map<String, VariableElement>>();
					if (info.annotatedFactory != null) {
						creatorArguments.put(info.annotatedFactory, getArguments(info.annotatedFactory));
					} else if (info.annotatedConstructor != null) {
						creatorArguments.put(info.annotatedConstructor, getArguments(info.annotatedConstructor));
					} else if (info.matchingConstructors != null) {
						for (ExecutableElement ctor : info.matchingConstructors) {
							if (!onlyBasicFeatures || ctor.getParameters().size() == 0) {
								creatorArguments.put(ctor, getArguments(ctor));
							}
						}
					}
					PropertyAnalysis bestAnalysis = null;
					for(Map.Entry<ExecutableElement, Map<String, VariableElement>> kv : creatorArguments.entrySet()) {
						PropertyAnalysis analysis = new PropertyAnalysis(kv.getKey(), info.element, kv.getValue());
						if (bestAnalysis == null) {
							bestAnalysis = analysis;
						} else {
							int foundKeys = analysis.allKeys.size();
							int oldKeys = bestAnalysis.allKeys.size();
							if (foundKeys > oldKeys) {
								bestAnalysis = analysis;
							} else if (foundKeys == oldKeys) {
								if (analysis.creator.getParameters().size() == 0) {
									bestAnalysis = analysis;
								}
							}
						}
					}
					if (bestAnalysis != null) {
						if (info.annotatedFactory == null) {
							info.useConstructor(bestAnalysis.creator);
						}
						for (Map.Entry<String, AccessElements> p : bestAnalysis.beans.entrySet()) {
							AccessElements field = bestAnalysis.allFieldDetails.get(p.getKey());
							analyzeAttribute(info, p.getValue().read.getReturnType(), p.getKey(), p.getValue(), "bean property", path, field != null ? field.field : null);
						}
						for (Map.Entry<String, AccessElements> p : bestAnalysis.exact.entrySet()) {
							if (info.attributes.containsKey(p.getKey()) && info.annotation == null) continue;
							//TODO: check for conflict between getter and exact property name
							AccessElements field = bestAnalysis.allFieldDetails.get(p.getKey());
							analyzeAttribute(info, p.getValue().read.getReturnType(), p.getKey(), p.getValue(), "exact property", path, field != null ? field.field : null);
						}
						for (Map.Entry<String, AccessElements> f : bestAnalysis.fields.entrySet()) {
							if (info.attributes.containsKey(f.getKey()) && info.annotation == null
								|| info.propertyNames.contains(f.getKey())) continue;
							analyzeAttribute(info, f.getValue().field.asType(), f.getKey(), f.getValue(), "field", path, null);
						}
					}
				}

				path.pop();
			}
		} while (total != structs.size());
	}

	private Map<String, TypeMirror> findGenericSignatures(TypeMirror type) {
		Queue<TypeMirror> queue = new ArrayDeque<TypeMirror>(types.directSupertypes(type));
		Map<String, TypeMirror> genericAttributes = new HashMap<String, TypeMirror>();
		while (!queue.isEmpty()) {
			TypeMirror mirror = queue.poll();
			if (mirror instanceof DeclaredType) {
				DeclaredType declaredType = (DeclaredType) mirror;
				Element element = declaredType.asElement();
				if (element instanceof TypeElement) {
					List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
					List<? extends TypeParameterElement> typeParameters = ((TypeElement) element).getTypeParameters();
					for (int i = 0; i < typeParameters.size(); i++) {
						genericAttributes.put(typeParameters.get(i).toString(), typeArguments.get(i));
					}
					queue.addAll(types.directSupertypes(mirror));
				}
			}
		}
		return genericAttributes;
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

	private TypeMirror unpackType(TypeMirror type) {
		String typeName = type.toString();
		if (typeName.startsWith("(@") || typeName.startsWith("@")) {
			//TODO: hacky fix for annotation removal from types
			//To fix it nicely Java8 AnnotatedType signature is required ;(
			if (type.getKind().isPrimitive()) {
				return types.getPrimitiveType(type.getKind());
			}
			String actualType = typeWithoutAnnotations(typeName);
			if (!actualType.contains("<") && !actualType.contains("[")) {
				Element element = types.asElement(type);
				return element.asType();
			}
		}
		return type;
	}

	private void analyzeAttribute(
			StructInfo info,
			TypeMirror originalType,
			String name,
			AccessElements access,
			String target,
			Stack<String> path,
			@Nullable VariableElement field) {
		Element element = access.field != null ? access.field : access.read;
		if (element == null) return;
		path.push(name);
		AnnotationMirror annotation = access.annotation;
		if (!info.properties.contains(element) && !hasIgnoredAnnotation(element, annotation, field)
				&& (info.objectFormatPolicy != CompiledJson.ObjectFormatPolicy.EXPLICIT || annotation != null)) {
			TypeMirror referenceType = unpackType(access.field != null ? access.field.asType() : access.read.getReturnType());
			TypeMirror type = unpackType(originalType.getKind() == TypeKind.TYPEVAR && info.genericSignatures.containsKey(originalType.toString()) ? info.genericSignatures.get(originalType.toString()) : originalType);
			Element referenceElement = types.asElement(referenceType);
			TypeMirror converterMirror = findConverter(annotation);
			final ConverterInfo converter;
			if (converterMirror != null) {
				TypeElement typeConverter = elements.getTypeElement(converterMirror.toString());
				converter = validateConverter(typeConverter, type);
			} else converter = null;
			String referenceName = referenceType.toString();
			boolean isJsonObject = jsonObjectReaderPath(referenceElement, false) != null;
			boolean typeResolved = converter != null || isJsonObject || structs.containsKey(referenceName);
			boolean hasUnknown = false;
			boolean hasOwnerStructType = false;
			Map<String, Integer> typeVariablesIndex = new HashMap<String, Integer>();
			Map<String, PartKind> references = new HashMap<String, PartKind>();
			LinkedHashSet<TypeMirror> usedTypes = new LinkedHashSet<TypeMirror>();
			analyzePartsRecursively(referenceType, references, usedTypes);

			if (!typeResolved || info.isParameterized) {
				for (Map.Entry<String, PartKind> kv : references.entrySet()) {
					String partTypeName = kv.getKey();
					PartKind partKind = kv.getValue();

					if (partKind == PartKind.UNKNOWN || partKind == PartKind.RAW_TYPE) {
						hasUnknown = true;
					}
					if (partKind == PartKind.TYPE_VARIABLE) {
						int typeIndex = info.typeParametersNames.indexOf(partTypeName);
						if (typeIndex >= 0) {
							typeVariablesIndex.put(partTypeName, typeIndex);
						} else {
							TypeMirror mirror = info.genericSignatures.get(partTypeName);
							if (mirror == null) {
								hasError = true;
								messager.printMessage(
										Diagnostic.Kind.ERROR,
										"Unable to resolve generic signature on " + name + " in " + info.name,
										element,
										annotation);
							} else {
								partTypeName = mirror.toString();
							}
						}
					}
					if (partTypeName.equals(info.element.toString())) {
						hasOwnerStructType = true;
					}
				}
			}
			CompiledJson.TypeSignature typeSignature = typeSignatureValue(annotation);
			JsonAttribute.IncludePolicy includeToMinimal = includeToMinimalValue(annotation);

			boolean notNull = hasNonNullable(element, field, annotation);
			boolean mandatory = hasMandatoryAnnotation(element, annotation) || field != null && hasMandatoryAnnotation(field, null);
			if (converter != null && converter.binder != null) {
				// if value is null, the binding value will be lost
				if (!notNull) {
					hasError = true;
					messager.printMessage(
							Diagnostic.Kind.ERROR,
							"Attribute with binder can not be nullable on " + name + " in " + info.name,
							element,
							annotation);
				}
				// if value is omitted, we will need to have `reset` method
				if (!mandatory) {
					hasError = true;
					messager.printMessage(
							Diagnostic.Kind.ERROR,
							"Attribute with binder can not be omitted on " + name + " in " + info.name,
							element,
							annotation);
				}
			}

			AttributeInfo attr =
					new AttributeInfo(
							name,
							access.read,
							access.write,
							access.field,
							access.arg,
							type,
							isCompatibileCollection(type, baseListType),
							isCompatibileCollection(type, baseSetType),
							isCompatibileCollection(type, baseMapType),
							annotation,
							notNull,
							mandatory,
							index(element, annotation),
							findNameAlias(element, field, annotation, name),
							isFullMatch(annotation),
							typeSignature,
							includeToMinimal,
							converter,
							isJsonObject,
							usedTypes,
							typeVariablesIndex,
							info.genericSignatures,
							hasOwnerStructType);
			String[] alternativeNames = attr.annotation == null ? null : getAlternativeNames(attr.annotation);
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
			info.add(attr);
			checkRelatedProperty(type, info.discoveredBy, target, info.element, element, path);
		}
		path.pop();
	}

	private void checkRelatedProperty(TypeMirror returnType, DeclaredType discoveredBy, String access, Element inside, Element property, Stack<String> path) {
		TypeMirror converter = findConverter(property);
		if (converter != null) return;
		checkRelatedPropertyRecursively(returnType, discoveredBy, access, inside, path);
	}

	private void checkRelatedPropertyRecursively(TypeMirror returnType, DeclaredType discoveredBy, String access, Element inside, Stack<String> path) {
		String typeName = returnType.toString();
		if (structs.containsKey(typeName) || typeSupport.isSupported(typeName)) return;

		if (returnType.getKind() == TypeKind.DECLARED) {
			DeclaredType declaredType = (DeclaredType) returnType;
			String rawTypeName = declaredType.asElement().toString();
			if (!structs.containsKey(rawTypeName) && !typeSupport.isSupported(rawTypeName)) {
				Element el = declaredType.asElement();
				findStructs(el, discoveredBy, el + " is referenced as " + access + " from '" + inside.asType() + "' through CompiledJson annotation.", path, null, null);
			}
			for (TypeMirror typeArgument : declaredType.getTypeArguments()) {
				checkRelatedPropertyRecursively(typeArgument, discoveredBy, access, inside, path);
			}
		} else if (returnType.getKind() == TypeKind.ARRAY) {
			ArrayType at = (ArrayType) returnType;
			checkRelatedPropertyRecursively(at.getComponentType(), discoveredBy, access, inside, path);
		} else {
			Element el = elements.getTypeElement(typeName);
			if (el != null) {
				findStructs(el, discoveredBy, el + " is referenced as " + access + " from '" + inside.asType() + "' through CompiledJson annotation.", path, null, null);
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

	private void findStructs(
			Element el,
			DeclaredType discoveredBy,
			String errorMessge,
			Stack<String> path,
			@Nullable ExecutableElement factory,
			@Nullable ExecutableElement builder) {
		if (!(el instanceof TypeElement)) return;
		String typeName = el.toString();
		if (structs.containsKey(typeName) || typeSupport.isSupported(typeName) || "java.lang.Object".equals(typeName)) return;
		final TypeElement element = (TypeElement) el;
		boolean isMixin = element.getKind() == ElementKind.INTERFACE
				|| element.getKind() == ElementKind.CLASS && element.getModifiers().contains(Modifier.ABSTRACT);
		String jsonObjectReaderPath = jsonObjectReaderPath(element, true);
		boolean isJsonObject = jsonObjectReaderPath != null;
		final AnnotationMirror annotation = scanClassForAnnotation(element, discoveredBy, factory);
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
								findStructs(deserializeAs, discoveredBy, errorMessge, path, null, null);
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
			CompiledJson.ObjectFormatPolicy objectFormatPolicy = objectFormatPolicyValue(annotation);
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
			BuilderInfo builderInfo = findBuilder(element, discoveredBy, builder);
			ExecutableElement factoryAnn = findAnnotatedFactory(element, discoveredBy, factory, builderInfo);
			StructInfo info =
					new StructInfo(
							element,
							discoveredBy,
							name,
							binaryName,
							type,
							jsonObjectReaderPath,
							findMatchingConstructors(element, isMixin),
							findAnnotatedConstructor(element, discoveredBy),
							factoryAnn,
							builderInfo,
							annotation,
							onUnknown,
							typeSignature,
							objectFormatPolicy,
							deserializeAs,
							classDiscriminator(annotation),
							className(annotation),
							type == ObjectType.ENUM ? findEnumConstantNameSource(element) : null,
							namingStrategy(element, annotation),
							formats,
							findGenericSignatures(element.asType()));
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
	private List<ExecutableElement> findMatchingConstructors(Element element, boolean isMixin) {
		if (element.getKind() == ElementKind.INTERFACE
				|| element.getKind() == ElementKind.ENUM
				|| !isMixin && element.getKind() == ElementKind.CLASS && element.getModifiers().contains(Modifier.ABSTRACT)) {
			return null;
		}
		List<ExecutableElement> matchingCtors = new ArrayList<ExecutableElement>();
		for (ExecutableElement constructor : ElementFilter.constructorsIn(element.getEnclosedElements())) {
			if (!constructor.getModifiers().contains(Modifier.PRIVATE)
					&& (isMixin && constructor.getModifiers().contains(Modifier.PROTECTED)
						|| !requiresPublic(element)
						|| constructor.getModifiers().contains(Modifier.PUBLIC))) {
				matchingCtors.add(constructor);
			}
		}
		return matchingCtors;
	}

	@Nullable
	private ExecutableElement findAnnotatedConstructor(Element element, DeclaredType discoveredBy) {
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
				if (alternativeCreators.contains(ann.getAnnotationType().toString())) {
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
	private ExecutableElement findAnnotatedFactory(
			Element element,
			DeclaredType discoveredBy,
			@Nullable ExecutableElement factory,
			@Nullable BuilderInfo builder) {
		if (element.getKind() == ElementKind.INTERFACE
				|| element.getKind() == ElementKind.ENUM) {
			return null;
		}
		AnnotationMirror annotation = null;
		if (factory == null) {
			for (ExecutableElement method : ElementFilter.methodsIn(element.getEnclosedElements())) {
				AnnotationMirror discAnn = getAnnotation(method, discoveredBy);
				if (discAnn != null) {
					factory = method;
					annotation = discAnn;
					break;
				}
				for (AnnotationMirror ann : method.getAnnotationMirrors()) {
					if (alternativeCreators.contains(ann.getAnnotationType().toString())) {
						if (method.getModifiers().contains(Modifier.PRIVATE)
								|| requiresPublic(element) && !method.getModifiers().contains(Modifier.PUBLIC)) {
							factory = method;
							annotation = ann;
							break;
						}
					}
				}
			}
		}
		if (factory == null) return null;
		boolean isStaticMethod = factory.getModifiers().contains(Modifier.STATIC);
		boolean isSingletonInstanceMethod = false;
		TypeElement parent = (TypeElement)factory.getEnclosingElement();
		if (!isStaticMethod && parent.getEnclosingElement() instanceof TypeElement
				&& parent.getModifiers().contains(Modifier.STATIC)) {
			TypeElement grandparent = (TypeElement)parent.getEnclosingElement();
			for(VariableElement field : ElementFilter.fieldsIn(grandparent.getEnclosedElements())) {
				if (field.getSimpleName().equals(parent.getSimpleName())
					&& field.getModifiers().contains(Modifier.PUBLIC)
						&& field.getModifiers().contains(Modifier.STATIC)
						&& field.getModifiers().contains(Modifier.FINAL)) {
					isSingletonInstanceMethod = true;
					break;
				}
			}
		}
		if (factory.getModifiers().contains(Modifier.PRIVATE)
				|| !isStaticMethod && !isSingletonInstanceMethod
				|| requiresPublic(factory.getEnclosingElement()) && !factory.getModifiers().contains(Modifier.PUBLIC)) {
			if (builder != null) return null;
			hasError = true;
			messager.printMessage(
					Diagnostic.Kind.ERROR,
					"Factory method in '" + factory.getEnclosingElement().asType() + "' is annotated with " + discoveredBy + ", but it's not accessible.",
					factory,
					annotation != null ? annotation : getAnnotation(factory, discoveredBy));
		}
		return factory;
	}

	@Nullable
	private BuilderInfo findBuilder(Element element, DeclaredType discoveredBy, @Nullable ExecutableElement builder) {
		if (element.getKind() == ElementKind.ENUM) {
			return null;
		}
		ExecutableElement factory = null;
		ExecutableElement build = builder;
		TypeElement builderType = null;
		if (builder != null) {
			Element ee = builder.getEnclosingElement();
			if (ee instanceof TypeElement) {
				builderType = (TypeElement) ee;
			}
		}
		for (ExecutableElement method : ElementFilter.methodsIn(element.getEnclosedElements())) {
			if (method.getModifiers().contains(Modifier.STATIC) && method.getModifiers().contains(Modifier.PUBLIC)) {
				Element nested = types.asElement(method.getReturnType());
				if (nested instanceof TypeElement && element.getEnclosedElements().contains(nested)
						&& method.getParameters().isEmpty()) {
					if (builderType != null && !builderType.toString().equals(nested.toString())) continue;
					factory = method;
					builderType = (TypeElement) nested;
					break;
				}
			}
		}
		if (builderType == null) return null;
		if (build == null) {
			for (TypeElement inheritance : getTypeHierarchy(builderType)) {
				for (ExecutableElement method : ElementFilter.methodsIn(inheritance.getEnclosedElements())) {
					if (method.getParameters().isEmpty() && !method.getModifiers().contains(Modifier.STATIC)
							&& types.isSameType(method.getReturnType(), element.asType())) {
						build = method;
						break;
					}
				}
			}
			if (build == null) return null;
		}
		AnnotationMirror annotation = getAnnotation(build, discoveredBy);
		List<ExecutableElement> ctors = findMatchingConstructors(builderType, false);
		ExecutableElement ctor = ctors != null && ctors.size() == 1 ? ctors.get(0) : null;
		return new BuilderInfo(factory, ctor, builderType, build, annotation);
	}

	private enum PartKind {
		UNKNOWN, RAW_TYPE, TYPE_VARIABLE, OTHER
	}

	private Map<String, PartKind> analyzeParts(TypeMirror target) {
		Map<String, PartKind> parts = new HashMap<String, PartKind>();
		Set<TypeMirror> used = new HashSet<TypeMirror>();
		analyzePartsRecursively(target, parts, used);
		return parts;
	}

	private void analyzePartsRecursively(TypeMirror target, Map<String, PartKind> parts, Set<TypeMirror> usedTypes) {
		String typeName = typeWithoutAnnotations(target.toString());
		if (typeSupport.isSupported(typeName)) {
			usedTypes.add(target);
			if (isRawType(target)) {
				parts.put(typeName, PartKind.RAW_TYPE);
			} else {
				parts.put(typeName, PartKind.OTHER);
			}
			return;
		}

		switch (target.getKind()) {
			case ARRAY:
				ArrayType at = (ArrayType) target;
				analyzePartsRecursively(at.getComponentType(), parts, usedTypes);
				break;

			case DECLARED:
				DeclaredType declaredType = (DeclaredType) target;
				List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();

				String rawTypeName = declaredType.asElement().toString();
				usedTypes.add(declaredType.asElement().asType());

				StructInfo struct = structs.get(rawTypeName);
				boolean knownAndValidStruct = struct != null
						&& (struct.type != ObjectType.CLASS
						|| struct.hasAnnotation()
						|| !struct.attributes.isEmpty()
						|| !struct.implementations.isEmpty()
						|| struct.hasKnownConversion());

				if (knownAndValidStruct || typeSupport.isSupported(rawTypeName)) {
					if (isRawType(target)) {
						parts.put(rawTypeName, PartKind.RAW_TYPE);
					} else {
						parts.put(rawTypeName, PartKind.OTHER);
					}
				} else {
					parts.put(rawTypeName, PartKind.UNKNOWN);
				}

				for (TypeMirror typeArgument : typeArguments) {
					analyzePartsRecursively(typeArgument, parts, usedTypes);
				}
				break;

			case TYPEVAR:
				usedTypes.add(target);
				parts.put(typeName, PartKind.TYPE_VARIABLE);
				break;

			case WILDCARD:
				WildcardType wt = (WildcardType)target;
				analyzePartsRecursively(wt.getExtendsBound(), parts, usedTypes);
				break;

			default:
				usedTypes.add(target);
				parts.put(typeName, PartKind.UNKNOWN);
				break;
		}
	}

	private boolean isRawType(TypeMirror target) {
		if (target.getKind() == TypeKind.DECLARED) {
			DeclaredType declaredType = (DeclaredType) target;
			return declaredType.getTypeArguments().isEmpty()
					&& !((TypeElement) declaredType.asElement()).getTypeParameters().isEmpty();
		}
		return false;
	}

	private static List<String> getEnumConstants(TypeElement element) {
		List<String> result = new ArrayList<String>();
		for (Element enclosedElement : element.getEnclosedElements()) {
			if (enclosedElement.getKind() == ElementKind.ENUM_CONSTANT) {
				result.add(enclosedElement.getSimpleName().toString());
			}
		}
		return result;
	}

	@Nullable
	private Element findEnumConstantNameSource(TypeElement element) {
		Element nameSource = null;
		for (Element enclosedElement : element.getEnclosedElements()) {
			if (enclosedElement.getAnnotation(JsonValue.class) != null) {
				switch (enclosedElement.getKind()) {
					case FIELD:
					case METHOD:
						if (nameSource == null) {
							if (!enclosedElement.getModifiers().contains(Modifier.PUBLIC)) {
								printError((enclosedElement.getKind().isField() ? "Field '" : "Method '") + enclosedElement.toString() +
												"' annotated with @JsonValue must be public.", enclosedElement);
							} else if (!isSupportedEnumNameType(enclosedElement)) {
								printError((enclosedElement.getKind().isField() ? "Field '" : "Method '") + enclosedElement.toString() +
												"' annotated with @JsonValue must be of a supported type. Unknown types can be supported by enabling unknown types configuration option or whitelisting that specific unknown type", enclosedElement);
							} else {
								nameSource = enclosedElement;
							}
						} else {
							printError("Duplicate @JsonValue annotation found. Only one enum field or getter can be annotated.", enclosedElement);
						}
						break;

					default:
						printError("Unexpected @JsonValue annotation found. It must be placed on enum field or getter.", enclosedElement);
				}
			}
		}
		return nameSource;
	}

	private boolean isSupportedEnumNameType(Element element) {
		String enumNameType = extractReturnType(element);
		if (enumNameType == null) return false;
		if (typeSupport.isSupported(enumNameType) || unknownTypes == UnknownTypes.ALLOW) return true;
		StructInfo target = structs.get(enumNameType);
		if (target != null && target.hasKnownConversion()) return true;
		if (unknownTypes == UnknownTypes.WARNING) {
			messager.printMessage(
					Diagnostic.Kind.WARNING,
					(element.getKind().isField() ? "Field '" : "Method '") + element +
							"' annotated with @JsonValue is of unknown type.",
					element);
			return true;
		}
		return false;
	}

	@Nullable
	private String extractReturnType(Element element) {
		switch (element.getKind()) {
			case FIELD: return element.asType().toString();
			case METHOD: return ((ExecutableElement) element).getReturnType().toString();
			default: return null;
		}
	}

	private void printError(String message, Element element) {
		hasError = true;
		messager.printMessage(Diagnostic.Kind.ERROR, message, element);
	}

	@Nullable
	private String jsonObjectReaderPath(Element el, boolean includeErrors) {
		if (!(el instanceof TypeElement)) return null;
		TypeElement element = (TypeElement)el;
		boolean isJsonObject = false;
		for (TypeMirror type : element.getInterfaces()) {
			if (JsonObject.class.getName().equals(type.toString())) {
				isJsonObject = true;
				break;
			}
		}
		if (!isJsonObject) return null;
		VariableElement jsonReaderField = null;
		ExecutableElement jsonReaderMethod = null;
		Element companion = null;
		for (VariableElement field : ElementFilter.fieldsIn(el.getEnclosedElements())) {
			//Kotlin uses Companion field with static get method
			if ("Companion".equals(field.getSimpleName().toString())) {
				if (field.asType().toString().equals(el.asType().toString() + ".Companion")
						&& field.getModifiers().contains(Modifier.STATIC)
						&& field.getModifiers().contains(Modifier.PUBLIC)
						&& field.getModifiers().contains(Modifier.FINAL)) {
					companion = types.asElement(field.asType());
				}
			} else if ("JSON_READER".equals(field.getSimpleName().toString())) {
				jsonReaderField = field;
			}
		}
		String signatureType = "com.dslplatform.json.JsonReader.ReadJsonObject<" + element.getQualifiedName() + ">";
		if (!onlyBasicFeatures && companion != null && companion.getModifiers().contains(Modifier.STATIC)) {
			for (ExecutableElement method : ElementFilter.methodsIn(companion.getEnclosedElements())) {
				if ("JSON_READER".equals(method.getSimpleName().toString()) || "getJSON_READER".equals(method.getSimpleName().toString())) {
					jsonReaderMethod = method;
				}
			}
		}
		String used = jsonReaderMethod != null ? jsonReaderMethod.getSimpleName() + " method" : "JSON_READER field";
		if (includeErrors) {
			if (!el.getModifiers().contains(Modifier.PUBLIC)) {
				hasError = true;
				messager.printMessage(
						Diagnostic.Kind.ERROR,
						"'" + element.getQualifiedName() + "' is 'com.dslplatform.json.JsonObject', but it's not public. " +
								"Make it public so it can be used for serialization/deserialization.",
						el,
						getAnnotation(el, converterType));
			} else if (element.getNestingKind().isNested() && !el.getModifiers().contains(Modifier.STATIC)) {
				hasError = true;
				messager.printMessage(
						Diagnostic.Kind.ERROR,
						"'" + element.getQualifiedName() + "' is 'com.dslplatform.json.JsonObject', but it cant be non static nested member. " +
								"Add static modifier so it can be used for serialization/deserialization.",
						el,
						getAnnotation(el, converterType));
			} else if (onlyBasicFeatures && (element.getQualifiedName().contentEquals(element.getSimpleName())
					|| element.getNestingKind().isNested() && element.getModifiers().contains(Modifier.STATIC)
					&& element.getEnclosingElement() instanceof TypeElement
					&& ((TypeElement) element.getEnclosingElement()).getQualifiedName().contentEquals(element.getEnclosingElement().getSimpleName()))) {
				hasError = true;
				messager.printMessage(
						Diagnostic.Kind.ERROR,
						"'" + element.getQualifiedName() + "' is 'com.dslplatform.json.JsonObject', but its defined without a package name and cannot be accessed. " +
								"Either add package to it or use a different analysis configuration which support classes without packages.",
						element,
						getAnnotation(element, converterType));
			} else if (jsonReaderField == null && jsonReaderMethod == null) {
				String allowed = onlyBasicFeatures ? "field" : "field/method";
				hasError = true;
				messager.printMessage(
						Diagnostic.Kind.ERROR,
						"'" + element.getQualifiedName() + "' is 'com.dslplatform.json.JsonObject', but it doesn't have JSON_READER " + allowed + ". " +
								"It can't be used for serialization/deserialization this way. " +
								"You probably want to add public static JSON_READER " + allowed + ".",
						element,
						getAnnotation(element, converterType));
			} else if (jsonReaderMethod == null && (!jsonReaderField.getModifiers().contains(Modifier.PUBLIC) || !jsonReaderField.getModifiers().contains(Modifier.STATIC))
					|| jsonReaderMethod != null && (!jsonReaderMethod.getModifiers().contains(Modifier.PUBLIC) || jsonReaderMethod.getModifiers().contains(Modifier.STATIC))) {
				hasError = true;
				messager.printMessage(
						Diagnostic.Kind.ERROR,
						"'" + element.getQualifiedName() + "' is 'com.dslplatform.json.JsonObject', but its " + used + " is not public and static. " +
								"It can't be used for serialization/deserialization this way. " +
								"You probably want to change " + used + " so it's public and static.",
						element,
						getAnnotation(element, converterType));
			} else if (jsonReaderField != null && !signatureType.equals(jsonReaderField.asType().toString())
					|| jsonReaderMethod != null && !signatureType.equals(jsonReaderMethod.getReturnType().toString())) {
				hasError = true;
				messager.printMessage(
						Diagnostic.Kind.ERROR,
						"'" + element.getQualifiedName() + "' is 'com.dslplatform.json.JsonObject', but its " + used + " is not of correct type. " +
								"It can't be used for serialization/deserialization this way. " +
								"You probably want to change " + used + " to: '" + signatureType + "'",
						element,
						getAnnotation(element, converterType));
			}
		}
		String prefix = companion == null ? "" : "Companion.";
		return prefix + (jsonReaderMethod != null ? jsonReaderMethod.getSimpleName().toString() + "()" : "JSON_READER");
	}

	private static class AccessElements {
		@Nullable public final ExecutableElement read;
		@Nullable public final ExecutableElement write;
		@Nullable public final VariableElement field;
		@Nullable public final VariableElement arg;
		@Nullable public final AnnotationMirror annotation;

		private AccessElements(
				@Nullable ExecutableElement read,
				@Nullable ExecutableElement write,
				@Nullable VariableElement arg,
				@Nullable VariableElement field,
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

		public static AccessElements readOnly(VariableElement field, ExecutableElement write, @Nullable AnnotationMirror annotation) {
			return new AccessElements(null, write, null, field, annotation);
		}

		public static AccessElements collection(ExecutableElement read, @Nullable AnnotationMirror annotation) {
			return new AccessElements(read, null, null, null, annotation);
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

	static String typeWithoutAnnotations(String typeName) {
		//Java6 does not have access to Java8 annotated types, so resort to hacks
		if (typeName.startsWith("(@")) {
			int separatorAt = typeName.lastIndexOf(" :: ");
			int lastParenthesis = typeName.lastIndexOf(')');
			if (separatorAt != -1 && lastParenthesis > separatorAt) {
				String baseType = typeName.substring(separatorAt + 4, lastParenthesis);
				return lastParenthesis == typeName.length() - 1
						? baseType
						: baseType + typeName.substring(lastParenthesis + 1);
			}
		}
		//Alternative naming scheme :(
		while (typeName.startsWith("@")) {
			int nextSpace = typeName.indexOf(' ');
			typeName = typeName.substring(nextSpace + 1);
		}
		return typeName;
	}

	private boolean isCompatibileType(TypeMirror left, TypeMirror right) {
		if (left.equals(right)) return true;
		final String leftStr = typeWithoutAnnotations(left.toString());
		final String rightStr = typeWithoutAnnotations(right.toString());
		if (leftStr.equals(rightStr)) return true;
		int ind = leftStr.indexOf('<');
		if (ind == -1 || rightStr.indexOf('<') != ind) return false;
		if (left.getKind() != right.getKind()) return false;
		if (!leftStr.substring(0, ind).equals(rightStr.substring(0, ind))) return false;
		return types.isAssignable(right, left);
	}

	private boolean isCompatibileCollection(TypeMirror left, TypeMirror rawCollection) {
		final String leftStr = left.toString();
		final String rightStr = rawCollection.toString();
		if (leftStr.equals(rightStr)) return true;
		if (left.getKind() != rawCollection.getKind()) return false;
		TypeMirror leftRaw = types.erasure(left);
		return types.isAssignable(rawCollection, leftRaw);
	}

	@Deprecated
	public static String beanOrActualName(String name) {
		return beanOrActualName(name, true);
	}

	public static String beanOrActualName(String name, boolean isBoolean) {

		if (isBoolean && name.startsWith("is") && name.length() > 2) {
			String after = name.substring(2);
			if (name.length() == 3) return after.toLowerCase();
			return after.toUpperCase().equals(after)
					? after
					: Character.toLowerCase(after.charAt(0)) + after.substring(1);
		}

		if ((name.startsWith("get") || name.startsWith("set")) && name.length() > 3) {
			String after = name.substring(3);
			if (name.length() == 4) return after.toLowerCase();
			return after.toUpperCase().equals(after)
					? after
					: Character.toLowerCase(after.charAt(0)) + after.substring(1);
		}
		return name;
	}

	private Map<String, AccessElements> getBeanProperties(
			TypeElement element,
			Map<String, VariableElement> arguments,
			Map<String, AccessElements> fieldDetails) {
		Map<String, ExecutableElement> setters = new HashMap<String, ExecutableElement>();
		Map<String, ExecutableElement> getters = new HashMap<String, ExecutableElement>();
		for (TypeElement inheritance : getTypeHierarchy(element)) {
			boolean isPublicInterface = inheritance.getKind() == ElementKind.INTERFACE
					&& inheritance.getModifiers().contains(Modifier.PUBLIC);
			for (ExecutableElement method : ElementFilter.methodsIn(inheritance.getEnclosedElements())) {
				String name = method.getSimpleName().toString();
				if (name.length() < 3) {
					continue;
				}
				boolean isAccessible = isPublicInterface && !method.getModifiers().contains(Modifier.PRIVATE)
						|| method.getModifiers().contains(Modifier.PUBLIC)
						&& !method.getModifiers().contains(Modifier.STATIC)
						&& !method.getModifiers().contains(Modifier.NATIVE)
						&& !method.getModifiers().contains(Modifier.TRANSIENT)
						&& !method.getModifiers().contains(Modifier.ABSTRACT);
				AnnotationMirror annotation = getAnnotation(method, attributeType);
				boolean producesWarning = !isAccessible && annotation != null;
				boolean isBoolean = method.getReturnType() != null && "boolean".equals(method.getReturnType().toString());
				String property = beanOrActualName(name, isBoolean);

				if(name.startsWith("is")
						&& method.getParameters().size() == 0
						&& method.getReturnType() != null
						&& method.getReturnType().getKind() == TypeKind.BOOLEAN){
					if (producesWarning) {
						messager.printMessage(
								Diagnostic.Kind.WARNING,
								attributeType + " detected on non accessible bean getter method which is ignored during processing. Put annotation on public method instead.",
								method,
								annotation);
					} else if (!getters.containsKey(property)) {
						getters.put(property, method);
					}
				} else if (name.startsWith("get")
						&& method.getParameters().size() == 0
						&& method.getReturnType() != null) {
					if (producesWarning) {
						messager.printMessage(
								Diagnostic.Kind.WARNING,
								attributeType + " detected on non accessible bean getter method which is ignored during processing. Put annotation on public method instead.",
								method,
								annotation);
					} else if (!getters.containsKey(property)) {
						getters.put(property, method);
					}
				} else if (name.startsWith("set") && method.getParameters().size() == 1) {
					if (producesWarning) {
						messager.printMessage(
								Diagnostic.Kind.WARNING,
								attributeType + " detected on non accessible bean setter method which is ignored during processing. Put annotation on public method instead.",
								method,
								annotation);
					} else {
						setters.put(property, method);
					}
				}
			}
		}
		return findMatchingResult(setters, getters, arguments, fieldDetails);
	}

	private Map<String, AccessElements> findMatchingResult(
			Map<String, ExecutableElement> setters,
			Map<String, ExecutableElement> getters,
			Map<String, VariableElement> arguments,
			Map<String, AccessElements> fieldDetails) {
		Map<String, AccessElements> result = new HashMap<String, AccessElements>();
		for (Map.Entry<String, ExecutableElement> kv : getters.entrySet()) {
			ExecutableElement setter = setters.get(kv.getKey());
			VariableElement setterArgument = setter == null ? null : setter.getParameters().get(0);
			VariableElement arg = arguments.get(kv.getKey());
			String returnType = typeWithoutAnnotations(kv.getValue().getReturnType().toString());
			String setterType = setterArgument != null ? typeWithoutAnnotations(setterArgument.asType().toString()) : null;
			AnnotationMirror actualAnnotation = annotation(kv.getValue(), setter, null, arg);
			AccessElements field = fieldDetails.get(kv.getKey());
			AnnotationMirror annotation = actualAnnotation != null ? actualAnnotation : field != null ? field.annotation : null;
			if (setterType != null && setterType.equals(returnType)) {
				result.put(kv.getKey(), AccessElements.readWrite(kv.getValue(), setter, annotation));
			} else if (setterType != null && (setterType + "<").startsWith(returnType)) {
				result.put(kv.getKey(), AccessElements.readWrite(kv.getValue(), setter, annotation));
			} else if (!onlyBasicFeatures && arg != null && isCompatibileType(arg.asType(), kv.getValue().getReturnType())) {
				result.put(kv.getKey(), AccessElements.readOnly(kv.getValue(), arg, annotation));
			} else if (arg == null && setterArgument == null && isAppendableCollection(kv.getValue())) {
				boolean hasMarker = annotation != null || hasCustomMarker(kv.getValue()) || field != null && field.field != null && hasCustomMarker(field.field);
				if (!hasMarker) continue;
				if (hasNonNullable(kv.getValue(), field != null ? field.field : null, annotation)) {
					result.put(kv.getKey(), AccessElements.collection(kv.getValue(), annotation));
				} else {
					messager.printMessage(
							Diagnostic.Kind.WARNING,
							attributeType + " detected on collection property, but non-nullable marker is missing. Property will be ignored.",
							kv.getValue(),
							annotation);
				}
			} else if (!onlyBasicFeatures && arg == null && field != null && setterType != null && field.field != null
					&& setterType.equals(typeWithoutAnnotations(field.field.asType().toString())) && isCompatibileType(setterArgument.asType(), kv.getValue().getReturnType())) {
				result.put(kv.getKey(), AccessElements.readWrite(kv.getValue(), setter, annotation));
			}
		}
		return result;
	}

	private boolean isAppendableCollection(final ExecutableElement getter) {
		TypeMirror type = getter.getReturnType();
		return isCompatibileCollection(type, baseListType)
				|| isCompatibileCollection(type, baseSetType);
	}

	private Map<String, AccessElements> getExactProperties(
			TypeElement element,
			Map<String, VariableElement> arguments,
			Map<String, AccessElements> fieldDetails) {
		Map<String, ExecutableElement> setters = new HashMap<String, ExecutableElement>();
		Map<String, ExecutableElement> getters = new HashMap<String, ExecutableElement>();
		for (TypeElement inheritance : getTypeHierarchy(element)) {
			boolean isPublicInterface = inheritance.getKind() == ElementKind.INTERFACE
					&& inheritance.getModifiers().contains(Modifier.PUBLIC);
			for (ExecutableElement method : ElementFilter.methodsIn(inheritance.getEnclosedElements())) {
				String name = method.getSimpleName().toString();
				if (name.startsWith("get") || name.startsWith("is") || name.startsWith("set")) {
					continue;
				}
				boolean isAccessible = isPublicInterface && !method.getModifiers().contains(Modifier.PRIVATE)
						|| method.getModifiers().contains(Modifier.PUBLIC)
						&& !method.getModifiers().contains(Modifier.STATIC)
						&& !method.getModifiers().contains(Modifier.NATIVE)
						&& !method.getModifiers().contains(Modifier.TRANSIENT)
						&& !method.getModifiers().contains(Modifier.ABSTRACT);
				AnnotationMirror annotation = getAnnotation(method, attributeType);
				boolean producesWarning = !isAccessible && annotation != null;
				if (method.getParameters().size() == 0 && method.getReturnType() != null) {
					if (producesWarning) {
						messager.printMessage(
								Diagnostic.Kind.WARNING,
								attributeType + " detected on non accessible setter method which is ignored during processing. Put annotation on public method instead.",
								method,
								annotation);
					} else if (!getters.containsKey(name)) {
						getters.put(name, method);
					}
				} else if (method.getParameters().size() == 1) {
					if (producesWarning) {
						messager.printMessage(
								Diagnostic.Kind.WARNING,
								attributeType + " detected on non accessible setter method which is ignored during processing. Put annotation on public method instead.",
								method,
								annotation);
					} else {
						setters.put(name, method);
					}
				}
			}
		}
		return findMatchingResult(setters, getters, arguments, fieldDetails);
	}

	private Map<String, AccessElements> getPublicFields(
			TypeElement element,
			Map<String, VariableElement> arguments,
			Set<String> processed) {
		Map<String, AccessElements> result = new HashMap<String, AccessElements>();
		for (TypeElement inheritance : getTypeHierarchy(element)) {
			for (VariableElement field : ElementFilter.fieldsIn(inheritance.getEnclosedElements())) {
				String name = field.getSimpleName().toString();
				boolean isFinal = field.getModifiers().contains(Modifier.FINAL);
				VariableElement arg = arguments.get(name);
				boolean isAccessible = field.getModifiers().contains(Modifier.PUBLIC)
						&& (!isFinal || arg != null)
						&& !field.getModifiers().contains(Modifier.NATIVE)
						&& !field.getModifiers().contains(Modifier.TRANSIENT)
						&& !field.getModifiers().contains(Modifier.STATIC);
				if (!isAccessible) {
					AnnotationMirror fieldAnnotation = getAnnotation(field, attributeType);
					if (fieldAnnotation != null && !processed.contains(name)) {
						messager.printMessage(
								Diagnostic.Kind.WARNING,
								attributeType + " detected on non accessible field which is ignored during processing. Put annotation on public field instead.",
								field,
								fieldAnnotation);
					}
					continue;
				}
				AnnotationMirror annotation = annotation(null, null, field, arg);
				result.put(name, AccessElements.field(field, arg, annotation));
			}
		}
		return result;
	}

	private Map<String, AccessElements> getFieldDetails(TypeElement element) {
		Map<String, AccessElements> result = new HashMap<String, AccessElements>();
		for (TypeElement inheritance : getTypeHierarchy(element)) {
			for (VariableElement field : ElementFilter.fieldsIn(inheritance.getEnclosedElements())) {
				String name = field.getSimpleName().toString();
				if (field.getModifiers().contains(Modifier.NATIVE)
						|| field.getModifiers().contains(Modifier.TRANSIENT)
						|| field.getModifiers().contains(Modifier.STATIC)) continue;
				AnnotationMirror annotation = annotation(null, null, field, null);
				result.put(name, AccessElements.field(field, field, annotation));
			}
		}
		return result;
	}

	private Map<String, AccessElements> getBuilderProperties(
			TypeElement element,
			BuilderInfo builder,
			boolean withBeans,
			boolean withExact,
			boolean withFields) {
		Map<String, ExecutableElement> setters = new HashMap<String, ExecutableElement>();
		Map<String, ExecutableElement> getters = new HashMap<String, ExecutableElement>();
		Map<String, VariableElement> fields = new HashMap<String, VariableElement>();
		TypeMirror builderType = builder.type.asType();
		for (TypeElement inheritance : getTypeHierarchy(element)) {
			boolean isPublicInterface = inheritance.getKind() == ElementKind.INTERFACE
					&& inheritance.getModifiers().contains(Modifier.PUBLIC);
			for (ExecutableElement method : ElementFilter.methodsIn(inheritance.getEnclosedElements())) {
				String name = method.getSimpleName().toString();
				boolean isAccessible = isPublicInterface && !method.getModifiers().contains(Modifier.PRIVATE)
						|| method.getModifiers().contains(Modifier.PUBLIC)
						&& !method.getModifiers().contains(Modifier.STATIC)
						&& !method.getModifiers().contains(Modifier.NATIVE)
						&& !method.getModifiers().contains(Modifier.TRANSIENT);
				if (!isAccessible) {
					if (getAnnotation(method, attributeType) != null) {
						messager.printMessage(
								Diagnostic.Kind.WARNING,
								attributeType + " detected on non accessible builder method which is ignored during processing. Put annotation on public method instead.",
								element);
					}
					continue;
				}
				final boolean isBoolean = method.getReturnType() != null && "boolean".equals(method.getReturnType().toString());
				final String property = Analysis.beanOrActualName(name, isBoolean);
				if (method.getParameters().size() == 0 && method.getReturnType() != null) {
					boolean canAdd = withExact
							|| withBeans && name.startsWith("get") && name.length() > 4
							|| withBeans && name.startsWith("is") && name.length() > 3;
					if (canAdd && !getters.containsKey(property)) {
						getters.put(property, method);
					}
				}
			}
			if (withFields) {
				for (VariableElement field : ElementFilter.fieldsIn(inheritance.getEnclosedElements())) {
					String name = field.getSimpleName().toString();
					boolean isFinal = field.getModifiers().contains(Modifier.FINAL);
					boolean isAccessible = field.getModifiers().contains(Modifier.PUBLIC)
							&& !field.getModifiers().contains(Modifier.NATIVE)
							&& !field.getModifiers().contains(Modifier.TRANSIENT)
							&& !field.getModifiers().contains(Modifier.STATIC);
					if (!isAccessible || !isFinal) {
						if (getAnnotation(field, attributeType) != null && !getters.containsKey(name)) {
							messager.printMessage(
									Diagnostic.Kind.WARNING,
									attributeType + " detected on non accessible builder field which is ignored during processing. Put annotation on public field instead.",
									element);
						}
						continue;
					}
					fields.put(name, field);
				}
			}
		}
		for (TypeElement inheritance : getTypeHierarchy(builder.type)) {
			for (ExecutableElement method : ElementFilter.methodsIn(inheritance.getEnclosedElements())) {
				String name = method.getSimpleName().toString();
				boolean isAccessible = !method.getModifiers().contains(Modifier.PRIVATE)
						|| method.getModifiers().contains(Modifier.PUBLIC)
						&& !method.getModifiers().contains(Modifier.STATIC)
						&& !method.getModifiers().contains(Modifier.NATIVE)
						&& !method.getModifiers().contains(Modifier.TRANSIENT);
				if (!isAccessible) {
					if (getAnnotation(method, attributeType) != null) {
						messager.printMessage(
								Diagnostic.Kind.WARNING,
								attributeType + " detected on non accessible builder method which is ignored during processing. Put annotation on public method instead.",
								element);
					}
					continue;
				}
				boolean isBoolean = method.getReturnType() != null && "boolean".equals(method.getReturnType().toString());
				String property = beanOrActualName(name, isBoolean);
				boolean canAdd = withExact || withBeans && name.startsWith("set") && name.length() > 4;
				if (method.getParameters().size() == 1 && canAdd && !setters.containsKey(property)) {
					if (types.isSameType(method.getReturnType(), builderType)) {
						setters.put(property, method);
					} else {
						messager.printMessage(
								Diagnostic.Kind.WARNING,
								"Skipping over method '" + method.getSimpleName() + "' because its return type is not the expected '" + builderType + "'",
								method);
					}
				}
			}
		}
		Map<String, AccessElements> result = new HashMap<String, AccessElements>();
		for (Map.Entry<String, ExecutableElement> kv : getters.entrySet()) {
			ExecutableElement setter = setters.get(kv.getKey());
			VariableElement setArg = setter == null ? null : setter.getParameters().get(0);
			String returnType = typeWithoutAnnotations(kv.getValue().getReturnType().toString());
			AnnotationMirror annotation = annotation(kv.getValue(), setter, null, null);
			String setterType = setArg != null ? typeWithoutAnnotations(setArg.asType().toString()) : null;
			if (setterType != null && (setterType.equals(returnType) || (setterType + "<").startsWith(returnType))) {
				result.put(kv.getKey(), AccessElements.readWrite(kv.getValue(), setter, annotation));
			}
		}
		for (Map.Entry<String, VariableElement> kv : fields.entrySet()) {
			if (result.containsKey(kv.getKey())) continue;
			ExecutableElement setter = setters.get(kv.getKey());
			VariableElement setArg = setter == null ? null : setter.getParameters().get(0);
			String returnType = typeWithoutAnnotations(kv.getValue().asType().toString());
			AnnotationMirror annotation = annotation(null, setter, kv.getValue(), null);
			String setterType = setArg != null ? typeWithoutAnnotations(setArg.asType().toString()) : null;
			if (setterType != null && (setterType.equals(returnType) || (setterType + "<").startsWith(returnType))) {
				result.put(kv.getKey(), AccessElements.readOnly(kv.getValue(), setter, annotation));
			}
		}
		return result;
	}

	private void findImplementations(Collection<StructInfo> structs) {
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
				StructInfo child = structs.get(signature);
				info.supertype(child);
			}
		}
	}

	@Nullable
	private String[] getAlternativeNames(AnnotationMirror dslAnn) {
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

	private boolean isFullMatch(@Nullable AnnotationMirror dslAnn) {
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

	private int index(Element property, @Nullable AnnotationMirror dslAnn) {
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
	private AnnotationMirror annotation(
			@Nullable ExecutableElement read,
			@Nullable ExecutableElement write,
            @Nullable VariableElement field,
			@Nullable VariableElement arg) {
		AnnotationMirror dslAnn = read == null ? null : getAnnotation(read, attributeType);
		if (dslAnn != null) return dslAnn;
		dslAnn = write == null ? null : getAnnotation(write, attributeType);
		if (dslAnn != null) return dslAnn;
		dslAnn = field == null ? null : getAnnotation(field, attributeType);
		if (dslAnn != null) return dslAnn;
		return arg == null ? null : getAnnotation(arg, attributeType);
	}

	private boolean hasIgnoredAnnotation(Element property, @Nullable AnnotationMirror dslAnn, @Nullable VariableElement field) {
		if (dslAnn != null) {
			return booleanAnnotationValue(dslAnn, "ignore()", false);
		}
		for (AnnotationMirror ann : property.getAnnotationMirrors()) {
			if (alternativeIgnore.contains(ann.getAnnotationType().toString())) {
				return true;
			}
		}
		if (field != null) {
			for (AnnotationMirror ann : field.getAnnotationMirrors()) {
				if (alternativeIgnore.contains(ann.getAnnotationType().toString())) {
					return true;
				}
			}
		}
		return false;
	}

	@Nullable
	private AnnotationMirror scanClassForAnnotation(TypeElement element, DeclaredType annotationType, @Nullable ExecutableElement custom) {
		AnnotationMirror target = custom != null ? getAnnotation(custom, annotationType) : null;
		if (target != null) return target;
		target = getAnnotation(element, annotationType);
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
	private AnnotationMirror getAnnotation(Element element, DeclaredType annotationType) {
		for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
			if (types.isSameType(mirror.getAnnotationType(), annotationType)) {
				return mirror;
			}
		}
		return null;
	}

	private boolean hasNonNullable(Element property, @Nullable VariableElement field, @Nullable AnnotationMirror dslAnn) {
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
		if (field != null) {
			for (AnnotationMirror ann : field.getAnnotationMirrors()) {
				Boolean match = matchCustomBoolean(ann, alternativeNonNullable);
				if (match != null) return match;
			}
		}
		return false;
	}

	@Nullable
	private static TypeElement deserializeAs(AnnotationMirror annotation) {
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();
		for (ExecutableElement ee : values.keySet()) {
			if (ee.toString().equals("deserializeAs()")) {
				DeclaredType target = (DeclaredType) values.get(ee).getValue();
				return (TypeElement) target.asElement();
			}
		}
		return null;
	}

	private static final Map<String, NamingStrategy> namingCache = new HashMap<String, NamingStrategy>();

	@Nullable
	private NamingStrategy namingStrategy(TypeElement element, @Nullable AnnotationMirror annotation) {
		if (annotation == null) return null;
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();
		String strategyName = null;
		for (ExecutableElement ee : values.keySet()) {
			if (ee.toString().equals("namingStrategy()")) {
				DeclaredType target = (DeclaredType) values.get(ee).getValue();
				Element targetElement = target.asElement();
				if (targetElement instanceof TypeElement) {
					strategyName = elements.getBinaryName((TypeElement) targetElement).toString();
				} else {
					strategyName = targetElement.toString();
				}
				break;
			}
		}
		if (strategyName == null && isMinified(annotation)) {
			strategyName = MinifiedNaming.class.getName();
		}
		if (strategyName == null || NamingStrategy.class.getName().equals(strategyName)) return null;
		NamingStrategy strategy = namingCache.get(strategyName);
		if (strategy != null) return strategy;
		try {
			Class<?> manifest = NamingStrategy.class.getClassLoader().loadClass(strategyName);
			strategy = (NamingStrategy) manifest.newInstance();
		} catch (Exception ignore) {
			try {
				Class<?> manifest = Thread.currentThread().getContextClassLoader().loadClass(strategyName);
				strategy = (NamingStrategy) manifest.newInstance();
			} catch (Exception ex) {
				hasError = true;
				messager.printMessage(
						Diagnostic.Kind.ERROR,
						"Unable to create an instance of NamingStrategy from the provided class: '" + strategyName + "'. Try moving naming strategy to a different jar. Details: " + ex.getMessage(),
						element,
						annotation);
				return null;
			}
		}
		namingCache.put(strategyName, strategy);
		return strategy;
	}

	private static String classDiscriminator(@Nullable AnnotationMirror annotation) {
		if (annotation == null) return "";
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();
		for (ExecutableElement ee : values.keySet()) {
			if (ee.toString().equals("discriminator()")) {
				return values.get(ee).getValue().toString();
			}
		}
		return "";
	}

	private static String className(@Nullable AnnotationMirror annotation) {
		if (annotation == null) return "";
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();
		for (ExecutableElement ee : values.keySet()) {
			if (ee.toString().equals("name()")) {
				return values.get(ee).getValue().toString();
			}
		}
		return "";
	}

	private boolean hasMandatoryAnnotation(Element property, @Nullable AnnotationMirror dslAnn) {
		if (dslAnn != null) {
			return booleanAnnotationValue(dslAnn, "mandatory()", false);
		}
		for (AnnotationMirror ann : property.getAnnotationMirrors()) {
			Boolean match = matchCustomBoolean(ann, alternativeMandatory);
			if (match != null) return match;
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

	@Nullable
	private CompiledJson.Behavior onUnknownValue(@Nullable AnnotationMirror annotation) {
		return enumAnnotationElementValue(annotation, "onUnknown()", CompiledJson.Behavior.class);
	}

	@Nullable
	private CompiledJson.TypeSignature typeSignatureValue(@Nullable AnnotationMirror annotation) {
		return enumAnnotationElementValue(annotation, "typeSignature()", CompiledJson.TypeSignature.class);
	}

	private CompiledJson.ObjectFormatPolicy objectFormatPolicyValue(@Nullable AnnotationMirror annotation) {
		CompiledJson.ObjectFormatPolicy value = enumAnnotationElementValue(annotation,
				"objectFormatPolicy()", CompiledJson.ObjectFormatPolicy.class);
		return value != null ? value : CompiledJson.ObjectFormatPolicy.DEFAULT;
	}

	private JsonAttribute.IncludePolicy includeToMinimalValue(@Nullable AnnotationMirror annotation) {
		JsonAttribute.IncludePolicy value = enumAnnotationElementValue(annotation,
				"includeToMinimal()", JsonAttribute.IncludePolicy.class);
		return value != null ? value : JsonAttribute.IncludePolicy.NON_DEFAULT;
	}

	private CompiledJson.Format[] getFormats(@Nullable AnnotationMirror ann) {
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

	@Nullable
	private static <T extends Enum<T>> T enumAnnotationElementValue(@Nullable AnnotationMirror annotation,
																	String elementName, Class<T> enumClass) {
		if (annotation == null) return null;
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();
		for (ExecutableElement ee : values.keySet()) {
			if (ee.toString().equals(elementName)) {
				Object val = values.get(ee).getValue();
				if (val == null) return null;
				return T.valueOf(enumClass, val.toString());
			}
		}
		return null;
	}

	private static boolean isMinified(@Nullable AnnotationMirror ann) {
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
	private TypeMirror findConverter(Element property) {
		return findConverter(getAnnotation(property, attributeType));
	}

	@Nullable
	private TypeMirror findConverter(@Nullable AnnotationMirror dslAnn) {
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
	private String findNameAlias(Element property, @Nullable VariableElement field, @Nullable AnnotationMirror dslAnn, String member) {
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
		if (field != null) {
			if (field.getModifiers().contains(Modifier.NATIVE)
					|| field.getModifiers().contains(Modifier.TRANSIENT)
					|| field.getModifiers().contains(Modifier.STATIC)) return null;
			for (AnnotationMirror ann : field.getAnnotationMirrors()) {
				String customName = matchCustomString(ann, alternativeAlias);
				if (customName != null && !customName.isEmpty()) return customName;
			}
		}
		return null;
	}

	private boolean hasCustomMarker(Element property) {
		for (AnnotationMirror ann : property.getAnnotationMirrors()) {
			if (alternativeAlias.containsKey(ann.getAnnotationType().toString())) return true;
		}
		return false;
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
