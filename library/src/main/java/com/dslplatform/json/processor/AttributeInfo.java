package com.dslplatform.json.processor;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.Nullable;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.*;

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
	public final boolean isSet;
	public final boolean isMap;
	public final boolean isGeneric;
	public final Map<String, Integer> typeVariablesIndex;
	public final boolean containsStructOwnerType;

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
			boolean isJsonObject,
			Map<String, Integer> typeVariablesIndex,
			boolean containsStructOwnerType) {
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
		this.isSet = typeName.startsWith("java.util.Set<") || typeName.startsWith("java.util.HashSet<")
				|| typeName.startsWith("java.util.LinkedHashSet<");
		this.isMap = typeName.startsWith("java.util.Map<") || typeName.startsWith("java.util.HashMap<")
				|| typeName.startsWith("java.util.LinkedHashMap<");
		this.typeVariablesIndex = typeVariablesIndex;
		this.isGeneric = !typeVariablesIndex.isEmpty();
		this.containsStructOwnerType = containsStructOwnerType;
	}

	public boolean isEnum(Map<String, StructInfo> structs) {
		StructInfo struct = typeName == null ? null : structs.get(typeName);
		return struct != null && struct.type == ObjectType.ENUM;
	}

	private boolean canResolveCollection(String content, Set<String> knownTypes, Map<String, StructInfo> structs) {
		if (knownTypes.contains(content)) return true;
		StructInfo target = structs.get(content);
		//we could also say that we know how to resolve other objects, but his has to be done lazily
		return target != null && target.hasKnownConversion();
	}

	@Nullable
	public List<String> collectionContent(Set<String> knownTypes, Map<String, StructInfo> structs) {
		if (isArray) {
			String content = typeName.substring(0, typeName.length() - 2);
			return canResolveCollection(content, knownTypes, structs) ? Collections.singletonList(content) : null;
		} else if (isList || isSet) {
			int ind = typeName.indexOf('<');
			String content = typeName.substring(ind + 1, typeName.length() - 1);
			return canResolveCollection(content, knownTypes, structs) ? Collections.singletonList(content) : null;
		} else if (isMap) {
			int indGen = typeName.indexOf('<');
			int indComma = typeName.indexOf(',', indGen + 1);
			String content1 = typeName.substring(indGen + 1, indComma);
			String content2 = typeName.substring(indComma + 1, typeName.length() - 1);
			return canResolveCollection(content1, knownTypes, structs) && canResolveCollection(content2, knownTypes, structs)
					? Arrays.asList(content1, content2)
					: null;
		}
		return null;
	}
}
