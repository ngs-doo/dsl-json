package com.dslplatform.json.processor;

import javax.lang.model.element.ExecutableElement;
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
	final Map<String, OptimizedConverter> inlinedConverters;
	final Map<String, String> defaults;
	final Map<String, StructInfo> structs;
	final TypeSupport typeSupport;
	final boolean allowUnknown;

	Context(Writer code, Map<String, OptimizedConverter> inlinedConverters, Map<String, String> defaults, Map<String, StructInfo> structs, TypeSupport typeSupport, boolean allowUnknown) {
		this.code = code;
		this.inlinedConverters = inlinedConverters;
		this.defaults = defaults;
		this.structs = structs;
		this.typeSupport = typeSupport;
		this.allowUnknown = allowUnknown;
	}

	String getDefault(String type) {
		OptimizedConverter converter = inlinedConverters.get(type);
		if (converter != null && converter.defaultValue != null) return converter.defaultValue;
		String defVal = defaults.get(type);
		if (defVal != null) return defVal;
		if (type.contains("<")) {
			defVal = defaults.get(type.substring(0, type.indexOf('<')));
			if (defVal != null) return defVal;
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

	static List<AttributeInfo> sortedAttributes(StructInfo info) {
		ArrayList<AttributeInfo> result = new ArrayList<>(info.attributes.values());
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

	static String extractRawType(TypeMirror type) {
		if (type.getKind() == TypeKind.DECLARED) {
			return ((DeclaredType) type).asElement().toString();
		} else {
			return type.toString();
		}
	}

	void serializeKnownCollection(AttributeInfo attr) throws IOException {
		if (attr.isArray) {
			String content = extractRawType(((ArrayType) attr.type).getComponentType());
			code.append("(").append(content).append("[])reader.readArray(reader_").append(attr.name);
			code.append(", emptyArray_").append(attr.name).append(")");
		} else if (attr.isList) {
			code.append("reader.readCollection(reader_").append(attr.name).append(")");
		} else if (attr.isSet) {
			code.append("reader.readSet(reader_").append(attr.name).append(")");
		} else if (attr.isMap) {
			code.append("reader.readMap(key_reader_").append(attr.name).append(", value_reader_").append(attr.name).append(")");
		} else {
			throw new IllegalArgumentException("Unknown attribute collection " + attr.name);
		}
	}
}
