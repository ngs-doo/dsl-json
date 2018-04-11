package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.SerializationException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

public final class MixinDescription<T> implements JsonWriter.WriteObject<T>, JsonReader.ReadObject<T> {

	private static final Charset utf8 = Charset.forName("UTF-8");
	private static final int typeHash = DecodePropertyInfo.calcHash("$type");
	private static final byte[] objectStart = "{\"$type\":".getBytes(utf8);

	private final Type manifest;
	private final FormatDescription<T>[] descriptions;
	private final boolean alwaysSerialize;
	private final boolean exactMatch;
	private final boolean canObjectFormat;
	private final boolean canArrayFormat;

	public MixinDescription(
			final Class<T> manifest,
			final DslJson json,
			final FormatDescription<T>[] descriptions) {
		this((Type) manifest, json, descriptions);
	}

	MixinDescription(
			final Type manifest,
			final DslJson json,
			final FormatDescription<T>[] descriptions) {
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (descriptions == null || descriptions.length == 0) {
			throw new IllegalArgumentException("descriptions can't be null or empty");
		}
		this.manifest = manifest;
		this.descriptions = descriptions;
		Set<Integer> uniqueHashNames = new HashSet<>();
		boolean canObject = false;
		boolean canArray = false;
		for (FormatDescription od : descriptions) {
			uniqueHashNames.add(od.typeHash);
			canObject = canObject || od.objectFormat != null;
			canArray = canArray || od.arrayFormat != null;
		}
		this.alwaysSerialize = !json.omitDefaults;
		this.canObjectFormat = canObject;
		this.canArrayFormat = canArray;
		this.exactMatch = uniqueHashNames.size() != descriptions.length;
	}

	public T read(final JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		if (reader.last() == '{' && canObjectFormat) {
			return readObjectFormat(reader);
		} else if (canArrayFormat && reader.last() == '[') {
			return readArrayFormat(reader);
		}
		if (canObjectFormat && canArrayFormat) {
			throw new IOException("Expecting '{' or '[' " + reader.positionDescription() + " while decoding " + manifest.getTypeName() + ". Found " + (char) reader.last());
		} else if (canObjectFormat) {
			throw new IOException("Expecting '{' " + reader.positionDescription() + " while decoding " + manifest.getTypeName() + ". Found " + (char) reader.last());
		} else {
			throw new IOException("Expecting '[' " + reader.positionDescription() + " while decoding " + manifest.getTypeName() + ". Found " + (char) reader.last());
		}
	}

	private T readObjectFormat(final JsonReader reader) throws IOException {
		if (reader.getNextToken() != JsonWriter.QUOTE) {
			throw new IOException("Expecting \"$type\" attribute as first element of mixin " + reader.positionDescription() + ". Found " + (char) reader.last());
		}
		if (reader.fillName() != typeHash) {
			String name = reader.getLastName();
			throw new IOException("Expecting \"$type\" attribute as first element of mixin " + reader.positionDescription(name.length() + 2) + ". Found: " + name);
		}
		reader.getNextToken();
		final int hash = reader.calcHash();
		for (final FormatDescription<T> od : descriptions) {
			if (od.objectFormat == null || od.typeHash != hash) continue;
			if (exactMatch && !reader.wasLastName(od.typeName)) continue;
			final FormatConverter<T> ofd = od.objectFormat;
			if (reader.getNextToken() == JsonWriter.COMMA) {
				reader.getNextToken();
			}
			return ofd.readContent(reader);
		}
		throw new IOException("Unable to find decoder for '" + reader.getLastName() + "' for mixin: " + manifest.getTypeName() + " which supports object format. Add @CompiledJson to specified type to allow deserialization into it");
	}

	private T readArrayFormat(final JsonReader reader) throws IOException {
		if (reader.getNextToken() != JsonWriter.QUOTE) {
			throw new IOException("Expecting \"$type\" value as first element of mixin " + reader.positionDescription() + ". Found " + (char) reader.last());
		}
		reader.getNextToken();
		final int hash = reader.calcHash();
		for (final FormatDescription<T> od : descriptions) {
			if (od.arrayFormat == null || od.typeHash != hash) continue;
			if (exactMatch && !reader.wasLastName(od.typeName)) continue;
			final FormatConverter<T> afd = od.arrayFormat;
			if (reader.getNextToken() == JsonWriter.COMMA) {
				reader.getNextToken();
			}
			return afd.readContent(reader);
		}
		throw new IOException("Unable to find decoder for '" + reader.getLastName() + "' for mixin: " + manifest.getTypeName() + " which supports array format. Add @CompiledJson to specified type to allow deserialization into it");
	}

	@Override
	public void write(final JsonWriter writer, final T instance) {
		if (instance == null) {
			writer.writeNull();
			return;
		}
		final Class<?> current = instance.getClass();
		for (FormatDescription<T> od : descriptions) {
			if (current != od.manifest) continue;
			if (od.isObjectFormatFirst) {
				writer.writeAscii(objectStart);
				writer.writeAscii(od.quotedTypeName);
				FormatConverter<T> ofd = od.objectFormat;
				if (alwaysSerialize) {
					writer.writeByte(JsonWriter.COMMA);
					final int pos = writer.size();
					final long flushed = writer.flushed();
					ofd.writeContentFull(writer, instance);
					if (pos != writer.size() || flushed != writer.flushed()) {
						writer.writeByte(JsonWriter.OBJECT_END);
					} else {
						writer.getByteBuffer()[writer.size() - 1] = JsonWriter.OBJECT_END;
					}
				} else {
					writer.writeByte(JsonWriter.COMMA);
					ofd.writeContentMinimal(writer, instance);
					writer.getByteBuffer()[writer.size() - 1] = JsonWriter.OBJECT_END;
				}
			} else {
				writer.writeByte(JsonWriter.ARRAY_START);
				writer.writeAscii(od.quotedTypeName);
				od.arrayFormat.writeContentFull(writer, instance);
				writer.writeByte(JsonWriter.ARRAY_END);
			}
			return;
		}
		throw new SerializationException("Unable to find encoder for '" + instance.getClass() + "'. Add @CompiledJson to specified type to allow serialization from it");
	}
}
