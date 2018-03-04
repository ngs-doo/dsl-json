package com.dslplatform.json.processor;

import com.dslplatform.json.CompiledJson;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

public class AttributeInfo {
	public final String id;
	public final String name;
	public final ExecutableElement readMethod;
	public final ExecutableElement writeMethod;
	public final VariableElement field;
	public final TypeMirror type;
	public final Element element;
	public final boolean notNull;
	public final boolean mandatory;
	public final String alias;
	public final boolean fullMatch;
	public final CompiledJson.TypeSignature typeSignature;
	public final TypeMirror converter;
	public final List<String> alternativeNames = new ArrayList<String>();

	public AttributeInfo(
			String name,
			ExecutableElement readMethod,
			ExecutableElement writeMethod,
			VariableElement field,
			TypeMirror type,
			boolean notNull,
			boolean mandatory,
			String alias,
			boolean fullMatch,
			CompiledJson.TypeSignature typeSignature,
			TypeMirror converter) {
		this.id = alias != null ? alias : name;
		this.name = name;
		this.readMethod = readMethod;
		this.writeMethod = writeMethod;
		this.field = field;
		this.element = field != null ? field : readMethod;
		this.type = type;
		this.notNull = notNull;
		this.mandatory = mandatory;
		this.alias = alias;
		this.fullMatch = fullMatch;
		this.typeSignature = typeSignature;
		this.converter = converter;
	}
}
