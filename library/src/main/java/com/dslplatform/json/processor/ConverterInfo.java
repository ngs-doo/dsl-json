package com.dslplatform.json.processor;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public class ConverterInfo {
	public final TypeElement converter;
	public final String fullName;
	public final String reader;
	public final String writer;

	public ConverterInfo(TypeElement converter, String reader, String writer) {
		this.converter = converter;
		this.fullName = converter.getQualifiedName().toString();
		this.reader = reader;
		this.writer = writer;
	}
}

