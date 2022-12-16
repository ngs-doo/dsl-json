package com.dslplatform.json.processor;

import com.dslplatform.json.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public class ConverterInfo {
	public final TypeElement converter;
	public final String fullName;
	public final String reader;
	public final String writer;
	public final String binder;
	public final String targetSignature;
	public final Element targetType;

	public ConverterInfo(
			TypeElement converter,
			String reader,
			String writer,
			String binder,
			String targetSignature,
			@Nullable Element targetType) {
		this.converter = converter;
		this.fullName = converter.getQualifiedName().toString();
		this.reader = reader;
		this.writer = writer;
		this.binder = binder;
		this.targetSignature = targetSignature;
		this.targetType = targetType;
	}
}

