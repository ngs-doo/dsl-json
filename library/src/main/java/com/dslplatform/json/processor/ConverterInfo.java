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
	public final String writer;
	public final String targetSignature;
	public final Element targetType;
	private final String readPrefix;
	private final String writePrefix;

	public ConverterInfo(
			TypeElement converter,
			boolean legacyDeclaration,
			String reader,
			String writer,
			String targetSignature,
			@Nullable Element targetType) {
		this.converter = converter;
		this.legacyDeclaration = legacyDeclaration;
		this.fullName = converter.getQualifiedName().toString();
		this.reader = reader;
		this.writer = writer;
		this.targetSignature = targetSignature;
		this.targetType = targetType;
		if (legacyDeclaration) {
			readPrefix = "." + reader;
			writePrefix = "." + writer;
		} else {
			int readInd = reader.lastIndexOf('.');
			int writeInd = writer.lastIndexOf('.');
			if (readInd != -1) {
				readPrefix = "." + reader.substring(0, readInd);
			} else {
				readPrefix = "";
			}
			if (writeInd != -1) {
				writePrefix = "." + writer.substring(0, writeInd);
			} else {
				writePrefix = "";
			}
		}
	}

	public void read(Writer code) throws IOException {
		code.append(fullName).append(readPrefix);
	}

	public void write(Writer code) throws IOException {
		code.append(fullName).append(writePrefix);
	}
}

