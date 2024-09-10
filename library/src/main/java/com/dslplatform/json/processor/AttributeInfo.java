package com.dslplatform.json.processor;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;
import com.dslplatform.json.Nullable;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Types;
import java.util.*;

public class AttributeInfo {
	public final String id;
	public final String name;
	@Nullable public final ExecutableElement readMethod;
	@Nullable public final ExecutableElement writeMethod;
	@Nullable public final VariableElement field;
	@Nullable public final VariableElement argument;
	public final TypeMirror type;
	@Nullable public final AnnotationMirror annotation;
	public final Element element;
	public final boolean notNull;
	public final boolean mandatory;
	public final int index;
	public final String alias;
	public final boolean fullMatch;
	public final CompiledJson.TypeSignature typeSignature;
	public final JsonAttribute.IncludePolicy includeToMinimal;
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
	public final LinkedHashSet<TypeMirror> usedTypes;
	public final Map<String, Integer> typeVariablesIndex;
	public final boolean containsStructOwnerType;

	public AttributeInfo(
			String name,
			@Nullable ExecutableElement readMethod,
			@Nullable ExecutableElement writeMethod,
			@Nullable VariableElement field,
			@Nullable VariableElement argument,
			TypeMirror type,
			boolean isList,
			boolean isSet,
			boolean isMap,
			@Nullable AnnotationMirror annotation,
			boolean notNull,
			boolean mandatory,
			final int index,
			@Nullable String alias,
			boolean fullMatch,
			@Nullable CompiledJson.TypeSignature typeSignature,
			JsonAttribute.IncludePolicy includeToMinimal,
			@Nullable ConverterInfo converter,
			boolean isJsonObject,
			LinkedHashSet<TypeMirror> usedTypes,
			String typeName,
			Map<String, Integer> typeVariablesIndex,
			Map<String, TypeMirror> genericSignatures,
			boolean containsStructOwnerType) {
		this.id = alias != null ? alias : name;
		this.name = name;
		this.readMethod = readMethod;
		this.writeMethod = writeMethod;
		this.field = field;
		this.argument = argument;
		this.element = field != null ? field : readMethod;
		this.type = type;
		this.annotation = annotation;
		this.notNull = notNull;
		this.mandatory = mandatory;
		this.index = index;
		this.alias = alias;
		this.fullMatch = fullMatch;
		this.typeSignature = typeSignature;
		this.includeToMinimal = includeToMinimal;
		this.converter = converter;
		this.isJsonObject = isJsonObject;
		this.typeName = typeName;
		this.readProperty = field != null ? field.getSimpleName().toString() : readMethod.getSimpleName() + "()";
		this.isArray = type.getKind() == TypeKind.ARRAY;
		this.isList = isList;
		this.isSet = isSet;
		this.isMap = isMap;
		this.usedTypes = usedTypes;
		this.typeVariablesIndex = typeVariablesIndex;
		this.isGeneric = !typeVariablesIndex.isEmpty();
		this.containsStructOwnerType = containsStructOwnerType;
	}

	public AttributeInfo asConcreteType(Types types, LinkedHashMap<String, TypeMirror> genericSignatures) {
		TypeMirror concreteType = genericSignatures.get(this.type.toString());
		if (concreteType == null) return null;
		return new AttributeInfo(
				this.name,
				this.readMethod,
				this.writeMethod,
				this.field,
				this.argument,
				concreteType,
				this.isList,
				this.isSet,
				this.isMap,
				this.annotation,
				this.notNull,
				this.mandatory,
				this.index,
				this.alias,
				this.fullMatch,
				this.typeSignature,
				this.includeToMinimal,
				this.converter,
				this.isJsonObject,
				this.usedTypes,
				Analysis.createTypeSignature(types, concreteType, this.usedTypes, genericSignatures),
				this.typeVariablesIndex,
				genericSignatures,
				this.containsStructOwnerType
		);
	}

	public boolean isEnum(Map<String, StructInfo> structs) {
		StructInfo struct = typeName == null ? null : structs.get(typeName);
		return struct != null && struct.type == ObjectType.ENUM;
	}

	private boolean canResolveCollection(String content, TypeSupport typeSupport, Map<String, StructInfo> structs) {
		if (typeSupport.isSupported(content)) return true;
		StructInfo target = structs.get(content);
		return target != null && (target.hasKnownConversion() || !target.isParameterized && target.unknowns.isEmpty());
	}

	public boolean canReadInput() {
		if (converter != null || isJsonObject) return true;
		if (field != null || writeMethod != null || argument != null) return true;
		if (readMethod != null && notNull) return isList || isSet;
		return false;
	}

	public boolean canWriteOutput() {
		if (converter != null || isJsonObject) return true;
		return field != null || readMethod != null;
	}

	@Nullable
	public List<String> collectionContent(TypeSupport typeSupport, Map<String, StructInfo> structs) {
		if (isArray) {
			String content = typeName.substring(0, typeName.length() - 2);
			return canResolveCollection(content, typeSupport, structs) ? Collections.singletonList(content) : null;
		} else if (isList || isSet) {
			int ind = typeName.indexOf('<');
			if (ind == -1) return null;
			String content = typeName.substring(ind + 1, typeName.length() - 1);
			return canResolveCollection(content, typeSupport, structs) ? Collections.singletonList(content) : null;
		} else if (isMap) {
			int indGen = typeName.indexOf('<');
			if (indGen == -1) return null;
			int indComma = typeName.indexOf(',', indGen + 1);
			String content1 = typeName.substring(indGen + 1, indComma);
			String content2 = typeName.substring(indComma + 1, typeName.length() - 1);
			return canResolveCollection(content1, typeSupport, structs) && canResolveCollection(content2, typeSupport, structs)
					? Arrays.asList(content1, content2)
					: null;
		}
		return null;
	}
	String createTypeSignature(
			Types types,
			TypeMirror type,
			Map<String, TypeMirror> genericSignatures) {
		if (this.usedTypes.isEmpty()) return Analysis.unpackType(type, types).toString();
		StringBuilder builder = new StringBuilder();
		Analysis.createTypeSignature(types, type, genericSignatures, builder);
		return builder.toString();
	}
	public String rawTypeName() {
		if (typeName.indexOf('<') == -1) return typeName;
		return typeName.substring(0, typeName.indexOf('<')) +
				typeName.substring(typeName.lastIndexOf('>') + 1);
    }
}
