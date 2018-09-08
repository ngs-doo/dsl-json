package com.dslplatform.json.processor;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.io.Writer;
import java.util.*;

final class Context {
	final Writer code;
	final Map<String, OptimizedConverter> inlinedConverters;
	final Map<String, String> defaults;
	final Map<String, StructInfo> structs;
	final Set<String> knownTypes;
	final boolean allowUnknown;

	Context(Writer code, Map<String, OptimizedConverter> inlinedConverters, Map<String, String> defaults, Map<String, StructInfo> structs, Set<String> knownTypes, boolean allowUnknown) {
		this.code = code;
		this.inlinedConverters = inlinedConverters;
		this.defaults = defaults;
		this.structs = structs;
		this.knownTypes = knownTypes;
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
		final ExecutableElement factoryOrCtor = info.factory != null ? info.factory : info.constructor;
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
}
