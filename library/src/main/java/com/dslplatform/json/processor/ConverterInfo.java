package com.dslplatform.json.processor;

import com.dslplatform.json.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.io.Writer;

public class ConverterInfo {
	public final TypeElement converter;
	public final boolean legacyDeclaration;
	public final String fullName;
	public final String reader;
	public final String binder;
	public final String writer;
	public final String targetSignature;
	public final Element targetType;
	private final String readPrefix;
	private final String bindPrefix;
	private final String writePrefix;

	public ConverterInfo(
			TypeElement converter,
			boolean legacyDeclaration,
			String reader,
			String binder,
			String writer,
			String targetSignature,
			@Nullable Element targetType) {
		this.converter = converter;
		this.legacyDeclaration = legacyDeclaration;
		this.fullName = converter.getQualifiedName().toString();
		this.reader = reader;
		this.binder = binder;
		this.writer = writer;
		this.targetSignature = targetSignature;
		this.targetType = targetType;
		readPrefix = detectPrefix(legacyDeclaration, reader);
		bindPrefix = detectPrefix(legacyDeclaration, binder);
		writePrefix = detectPrefix(legacyDeclaration, writer);
	}

	private static String detectPrefix(boolean legacy, String target) {
		if (legacy) return "." + target;
		int ind = target.lastIndexOf('.');
		return ind != -1
				? "." + target.substring(0, ind)
				: "";
	}

	public void read(Writer code) throws IOException {
		code.append(fullName).append(readPrefix);
	}

	public void bind(Writer code) throws IOException {
		code.append(fullName).append(bindPrefix);
	}

	public void write(Writer code) throws IOException {
		code.append(fullName).append(writePrefix);
	}
}

