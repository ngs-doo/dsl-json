package com.dslplatform.json.processor;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.Nullable;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AttributeInfo {
	public final String id;
	public final String name;
	public final ExecutableElement readMethod;
	public final ExecutableElement writeMethod;
	public final VariableElement field;
	public final TypeMirror type;
	public final AnnotationMirror annotation;
	public final Element element;
	public final boolean notNull;
	public final boolean mandatory;
	public final int index;
	public final String alias;
	public final boolean fullMatch;
	public final CompiledJson.TypeSignature typeSignature;
	public final ConverterInfo converter;
	public final boolean isJsonObject;
	public final List<String> alternativeNames = new ArrayList<String>();
	public final String readProperty;
	public final String typeName;
	public final boolean isArray;
	public final boolean isList;

	public AttributeInfo(
			String name,
			ExecutableElement readMethod,
			ExecutableElement writeMethod,
			@Nullable VariableElement field,
			TypeMirror type,
			AnnotationMirror annotation,
			boolean notNull,
			boolean mandatory,
			final int index,
			@Nullable String alias,
			boolean fullMatch,
			@Nullable CompiledJson.TypeSignature typeSignature,
			@Nullable ConverterInfo converter,
			boolean isJsonObject) {
		this.id = alias != null ? alias : name;
		this.name = name;
		this.readMethod = readMethod;
		this.writeMethod = writeMethod;
		this.field = field;
		this.element = field != null ? field : readMethod;
		this.type = type;
		this.annotation = annotation;
		this.notNull = notNull;
		this.mandatory = mandatory;
		this.index = index;
		this.alias = alias;
		this.fullMatch = fullMatch;
		this.typeSignature = typeSignature;
		this.converter = converter;
		this.isJsonObject = isJsonObject;
		this.typeName = type.toString();
		this.readProperty = field != null ? field.getSimpleName().toString() : readMethod.getSimpleName() + "()";
		this.isArray = type.getKind() == TypeKind.ARRAY;
		this.isList = typeName.startsWith("java.util.List<") || typeName.startsWith("java.util.ArrayList<");
	}

	public boolean isEnum(Map<String, StructInfo> structs) {
		StructInfo struct = typeName == null ? null : structs.get(typeName);
		return struct != null && struct.type == ObjectType.ENUM;
	}

	@Nullable
	public String collectionContent(Set<String> knownTypes) {
		if (isArray) {
			String content = typeName.substring(0, typeName.length() - 2);
			return knownTypes.contains(content) ? content : null;
		} else if (isList) {
			int ind = typeName.indexOf('<');
			String content = typeName.substring(ind + 1, typeName.length() - 1);
			return knownTypes.contains(content) ? content : null;
		}
		return null;
	}
}
