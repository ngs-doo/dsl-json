package com.dslplatform.json.processor;

import javax.lang.model.element.VariableElement;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

final class Context {
	final Writer code;
	private final boolean allowInline;
	final Map<String, OptimizedConverter> inlinedConverters;
	final Map<String, String> defaults;
	final Map<String, StructInfo> structs;
	final Set<String> knownTypes;

	Context(Writer code, boolean allowInline, Map<String, OptimizedConverter> inlinedConverters, Map<String, String> defaults, Map<String, StructInfo> structs, Set<String> knownTypes) {
		this.code = code;
		this.allowInline = allowInline;
		this.inlinedConverters = inlinedConverters;
		this.defaults = defaults;
		this.structs = structs;
		this.knownTypes = knownTypes;
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
		if (info.constructor != null && !info.constructor.getParameters().isEmpty()) {
			int firstNonSet = 0;
			while (firstNonSet < result.size()) {
				if (result.get(firstNonSet).index != -1) firstNonSet++;
				else break;
			}
			for (VariableElement ve : info.constructor.getParameters()) {
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

	void addAttributeWriter(final String className, final AttributeInfo attr) throws IOException {
		final String actualType = attr.type.toString();
		final String objectType = nonGenericObject(actualType);
		OptimizedConverter optimized = inlinedConverters.get(actualType);
		String inline = allowInline && optimized != null ? optimized.encoder(attr.name, attr.notNull) : null;
		code.append("com.dslplatform.json.runtime.Settings.<");
		code.append(className).append(", ").append(objectType).append(">createEncoder(");
		if (attr.readMethod != null) code.append(className).append("::").append(attr.readMethod.getSimpleName());
		else code.append("c -> c.").append(attr.field.getSimpleName());
		code.append(", \"").append(attr.id).append("\", json, ");
		if (attr.converter != null) {
			code.append(attr.converter.fullName).append(".").append(attr.converter.writer).append(")");
		}
		else if (inline != null) code.append(inline).append(")");
		else code.append(typeOrClass(objectType, actualType)).append(")");
	}

	void addArrayWriter(final String className, final AttributeInfo attr) throws IOException {
		final String actualType = attr.type.toString();
		final String objectType = nonGenericObject(actualType);
		OptimizedConverter optimized = inlinedConverters.get(actualType);
		String inline = allowInline && optimized != null ? optimized.encoder(attr.name, attr.notNull) : null;
		code.append("com.dslplatform.json.runtime.Settings.<");
		code.append(className).append(", ").append(objectType).append(">createArrayEncoder(");
		if (attr.readMethod != null) code.append(className).append("::").append(attr.readMethod.getSimpleName());
		else code.append("c -> c.").append(attr.field.getSimpleName());
		code.append(", ");
		if (attr.converter != null) {
			code.append(attr.converter.fullName).append(".").append(attr.converter.writer).append(")");
		}
		else if (inline != null) code.append(inline).append(")");
		else code.append("json ,").append(typeOrClass(objectType, actualType)).append(")");
	}

	void addAttributeReader(final String className, final AttributeInfo attr, final String alias, final String readValue) throws IOException {
		final String actualType = attr.type.toString();
		final String objectType = nonGenericObject(actualType);
		OptimizedConverter optimized = inlinedConverters.get(actualType);
		String inline = allowInline && optimized != null ? optimized.decoder(attr.name, attr.notNull) : null;
		code.append("com.dslplatform.json.runtime.Settings.<");
		code.append(className).append(", ").append(objectType).append(">createDecoder(");
		code.append(readValue);
		code.append(", \"").append(alias).append("\", json, ");
		code.append(attr.fullMatch ? "true" : "false").append(", ");
		code.append(attr.mandatory ? "true" : "false").append(", ");
		code.append(Integer.toString(attr.index)).append(", ");
		code.append(attr.notNull ? "true" : "false").append(", ");
		if (attr.converter != null) {
			code.append(attr.converter.fullName).append(".").append(attr.converter.reader).append(")");
		}
		else if (inline != null) code.append(inline).append(")");
		else code.append(typeOrClass(objectType, actualType)).append(")");
	}

	void addArrayReader(final String className, final AttributeInfo attr, final String readValue) throws IOException {
		final String actualType = attr.type.toString();
		final String objectType = nonGenericObject(actualType);
		OptimizedConverter optimized = inlinedConverters.get(actualType);
		String inline = allowInline && optimized != null ? optimized.decoder(attr.name, attr.notNull) : null;
		code.append("com.dslplatform.json.runtime.Settings.<");
		code.append(className).append(", ").append(objectType).append(">createArrayDecoder(");
		code.append(readValue);
		code.append(", ");
		if (attr.converter != null) {
			code.append(attr.converter.fullName).append(".").append(attr.converter.reader).append(")");
		}
		else if (inline != null) code.append(inline).append(")");
		else code.append("json, ").append(typeOrClass(objectType, actualType)).append(")");
	}

}
