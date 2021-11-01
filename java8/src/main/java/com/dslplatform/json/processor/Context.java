package com.dslplatform.json.processor;

import com.dslplatform.json.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

final class Context {
	final Writer code;
	private final ProcessingEnvironment environment;
	final Map<String, OptimizedConverter> inlinedConverters;
	final Map<String, String> defaults;
	final Map<String, StructInfo> structs;
	final TypeSupport typeSupport;
	final boolean allowUnknown;

	Context(Writer code, ProcessingEnvironment environment, Map<String, OptimizedConverter> inlinedConverters, Map<String, String> defaults, Map<String, StructInfo> structs, TypeSupport typeSupport, boolean allowUnknown) {
		this.code = code;
		this.environment = environment;
		this.inlinedConverters = inlinedConverters;
		this.defaults = defaults;
		this.structs = structs;
		this.typeSupport = typeSupport;
		this.allowUnknown = allowUnknown;
	}

	String getDefault(AttributeInfo attr) {
		String type = attr.typeName;
		String defVal = defaults.get(type);
		if (defVal != null) return defVal;
		int genIndex = type.indexOf('<');
		if (genIndex != -1) {
			defVal = defaults.get(type.substring(0, genIndex));
			if (defVal != null) return defVal;
		}
		if (!attr.notNull) return "null";
		OptimizedConverter converter = inlinedConverters.get(type);
		if (converter != null && converter.defaultValue != null) {
			return converter.defaultValue;
		}
		int arrIndex = type.lastIndexOf('[');
		if (attr.isArray) {
			String rawType = genIndex != -1 ? type.substring(0, genIndex)
					: arrIndex != -1 ? type.substring(0, arrIndex)
					: type;
			return "new " + rawType + "[]{}";
		} else if (attr.isList && type.startsWith("java.util.List<")) {
			return "java.util.Collections.emptyList()";
		} else if (attr.isSet && type.startsWith("java.util.Set<")) {
			return "java.util.Collections.emptySet()";
		} else if (attr.isMap && type.startsWith("java.util.Map<")) {
			return "java.util.Collections.emptyMap()";
		}
		StructInfo target = structs.get(attr.typeName);
		if (target != null) {
			if (target.annotatedFactory != null && target.annotatedFactory.getParameters().isEmpty()) {
				return target.annotatedFactory.getEnclosingElement().toString() + "." + target.annotatedFactory.getSimpleName() + "()";
			} else if (target.converter == null && target.hasEmptyCtor()) {
				return "new " + attr.typeName + "()";
			} else if (target.type == ObjectType.ENUM && !target.constants.isEmpty()) {
				return attr.typeName + "." + target.constants.get(0);
			}
		}
		return "null";
	}

	static String nonGenericObject(String type) {
		String objectType = Analysis.objectName(type);
		int genInd = objectType.indexOf('<');
		if (genInd == -1) return objectType;
		return objectType.substring(0, genInd);
	}

	static String typeOrClass(String objectType, String typeName) {
		if (objectType.equals(typeName)) return objectType + ".class";
		int genInd = typeName.indexOf('<');
		if (genInd == -1) return typeName + ".class";
		return "new com.dslplatform.json.runtime.TypeDefinition<" + typeName + ">(){}.type";
	}

	static List<AttributeInfo> sortedAttributes(StructInfo info, boolean includeInherited) {
		ArrayList<AttributeInfo> result = new ArrayList<>(info.attributes.values());
		if (includeInherited) {
			for (AttributeInfo ai : info.inheritedAttributes()) {
				if (info.attributes.containsKey(ai.id)) continue;
				result.add(ai);
			}
		}
		result.sort((a, b) -> {
			if (b.index == -1) return -1;
			else if (a.index == -1) return 1;
			return a.index - b.index;
		});
		final ExecutableElement factoryOrCtor = info.annotatedFactory != null ? info.annotatedFactory : info.selectedConstructor();
		if (factoryOrCtor != null && !factoryOrCtor.getParameters().isEmpty()) {
			int firstNonSet = 0;
			while (firstNonSet < result.size()) {
				if (result.get(firstNonSet).index != -1) firstNonSet++;
				else break;
			}
			for (VariableElement ve : factoryOrCtor.getParameters()) {
				int i = firstNonSet;
				while (i < result.size()) {
					AttributeInfo attr = result.get(i);
					if (attr.name.equals(ve.getSimpleName().toString())) {
						if (firstNonSet != i) {
							result.remove(i);
							result.add(firstNonSet, attr);
						}
						firstNonSet++;
						break;
					}
					i++;
				}
			}
		}
		return result;
	}

	static String extractRawType(TypeMirror type, Map<String, TypeMirror> genericSignatures) {
		if (type.getKind() == TypeKind.DECLARED) {
			return ((DeclaredType) type).asElement().toString();
		}
		String typeName = Analysis.typeWithoutAnnotations(type.toString());
		if (type.getKind() == TypeKind.TYPEVAR) {
			TypeMirror mirror = genericSignatures.get(typeName);
			if (mirror != null && mirror != type) {
				return extractRawType(mirror, genericSignatures);
			}
		}
		return typeName;
	}

	boolean useLazyResolution(String type) {
		OptimizedConverter converter = inlinedConverters.get(type);
		StructInfo found = structs.get(type);
		return converter == null && found != null
				&& (found.type == ObjectType.MIXIN && findType(type) != null || found.hasCycles(structs));
	}

	void serializeKnownCollection(AttributeInfo attr, List<String> types, Map<String, TypeMirror> genericSignatures) throws IOException {
		if (attr.isArray) {
			String content = extractRawType(((ArrayType) attr.type).getComponentType(), genericSignatures);
			code.append("(").append(content).append("[])reader.readArray(reader_").append(attr.name);
			code.append(useLazyResolution(content) ? "()" : "");
			code.append(", emptyArray_").append(attr.name).append(")");
		} else if (attr.isList) {
			code.append("reader.readCollection(reader_").append(attr.name).append(useLazyResolution(types.get(0)) ? "()" : "").append(")");
		} else if (attr.isSet) {
			code.append("reader.readSet(reader_").append(attr.name).append(useLazyResolution(types.get(0)) ? "()" : "").append(")");
		} else if (attr.isMap) {
			code.append("reader.readMap(key_reader_").append(attr.name).append(useLazyResolution(types.get(0)) ? "()" : "");
			code.append(", value_reader_").append(attr.name).append(useLazyResolution(types.get(1)) ? "()" : "").append(")");
		} else {
			throw new IllegalArgumentException("Unknown attribute collection " + attr.name);
		}
	}

	@Nullable
	TypeMirror findType(String content) {
		TypeElement element = environment.getElementUtils().getTypeElement(content);
		return element != null ? element.asType() : null;
	}

	boolean isObjectInstance(AttributeInfo attr) {
		OptimizedConverter converter = inlinedConverters.get(attr.typeName);
		if (converter == null) return false;
		StructInfo target = structs.get(attr.typeName);
		return target != null && (target.annotatedFactory != null || target.hasEmptyCtor());
	}
}
