package com.dslplatform.json.runtime;

import com.dslplatform.json.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

public final class FormatDescription<T> implements JsonWriter.WriteObject<T>, JsonReader.ReadObject<T>, JsonReader.BindObject<T> {

	private static final Charset utf8 = Charset.forName("UTF-8");

	final Type manifest;
	final boolean isObjectFormatFirst;
	final FormatConverter<T> objectFormat;
	private final JsonReader.BindObject<T> objectBinder;
	final FormatConverter<T> arrayFormat;
	private final JsonReader.BindObject<T> arrayBinder;
	final int typeHash;
	final byte[] typeName;
	final byte[] quotedTypeName;

	public FormatDescription(
			final Type manifest,
			@Nullable final FormatConverter<T> objectFormat,
			@Nullable final FormatConverter<T> arrayFormat,
			final boolean isObjectFormatFirst,
			final String typeName,
			final DslJson json) {
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (objectFormat == null && arrayFormat == null)
			throw new IllegalArgumentException("both objectConverter and arrayFormat can't be null at the same time");
		if (json == null) throw new IllegalArgumentException("json can't be null");
		if (typeName == null) throw new IllegalArgumentException("typeName can't be null");
		if (!json.allowArrayFormat && objectFormat == null) {
			throw new IllegalArgumentException("Since array format is not allowed, objectFormat can't be null");
		}
		if (isObjectFormatFirst && objectFormat == null) {
			throw new IllegalArgumentException("Object format is defined as primary format, but object format is not defined");
		}
		this.manifest = manifest;
		this.objectFormat = objectFormat;
		this.objectBinder = objectFormat instanceof JsonReader.BindObject ? (JsonReader.BindObject)objectFormat : null;
		this.arrayFormat = arrayFormat;
		this.arrayBinder = arrayFormat instanceof JsonReader.BindObject ? (JsonReader.BindObject)arrayFormat : null;
		this.isObjectFormatFirst = isObjectFormatFirst || !json.allowArrayFormat;
		String name = typeName.replace("$", ".");
		this.typeName = name.getBytes(utf8);
		this.quotedTypeName = ("\"" + name + "\"").getBytes(utf8);
		this.typeHash = DecodePropertyInfo.calcHash(name);
	}

	public final void write(final JsonWriter writer, @Nullable final T instance) {
		if (instance == null) {
			writer.writeNull();
		} else if (isObjectFormatFirst) {
			objectFormat.write(writer, instance);
		} else {
			arrayFormat.write(writer, instance);
		}
	}

	@Nullable
	public T read(final JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		if (reader.last() == '{') {
			if (objectFormat == null) throw new ConfigurationException("Object format for " + manifest.getTypeName() + " is not defined");
			return objectFormat.read(reader);
		} else if (reader.last() == '[') {
			if (arrayFormat == null) throw new ConfigurationException("Array format for " + manifest.getTypeName() + " is not defined");
			return arrayFormat.read(reader);
		} else if (objectFormat != null && arrayFormat != null) {
			throw new ParsingException("Expecting '{' or '[' " + reader.positionDescription() + " for decoding " + manifest.getTypeName() + ". Found " + (char) reader.last());
		} else if (objectFormat != null) {
			throw new ParsingException("Expecting '{' " + reader.positionDescription() + " for decoding " + manifest.getTypeName() + ". Found " + (char) reader.last());
		} else {
			throw new ParsingException("Expecting '[' " + reader.positionDescription() + " for decoding " + manifest.getTypeName() + ". Found " + (char) reader.last());
		}
	}

	public T bind(final JsonReader reader, final T instance) throws IOException {
		if (reader.last() == '{') {
			if (objectFormat == null) throw new ConfigurationException("Object format for " + manifest.getTypeName() + " is not defined");
			if (objectBinder == null) throw new ConfigurationException(manifest.getTypeName() + " does not support binding");
			return objectBinder.bind(reader, instance);
		} else if (reader.last() == '[') {
			if (arrayFormat == null) throw new ConfigurationException("Array format for " + manifest.getTypeName() + " is not defined");
			if (arrayBinder == null) throw new ConfigurationException(manifest.getTypeName() + " does not support binding");
			return arrayBinder.bind(reader, instance);
		} else if (objectFormat != null && arrayFormat != null) {
			throw new ParsingException("Expecting '{' or '[' " + reader.positionDescription() + " for decoding " + manifest.getTypeName() + ". Found " + (char) reader.last());
		} else if (objectFormat != null) {
			throw new ParsingException("Expecting '{' " + reader.positionDescription() + " for decoding " + manifest.getTypeName() + ". Found " + (char) reader.last());
		} else {
			throw new ParsingException("Expecting '[' " + reader.positionDescription() + " for decoding " + manifest.getTypeName() + ". Found " + (char) reader.last());
		}
	}
}