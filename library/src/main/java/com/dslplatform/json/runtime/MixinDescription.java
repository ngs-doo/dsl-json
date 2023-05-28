package com.dslplatform.json.runtime;

import com.dslplatform.json.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public final class MixinDescription<T> implements JsonWriter.WriteObject<T>, JsonReader.ReadObject<T>, ExplicitDescription {

	private static final int defaultTypeHash = DecodePropertyInfo.calcHash("$type");
	private static final byte[] defaultObjectStart = "{\"$type\":".getBytes(StandardCharsets.UTF_8);

	private final int typeHash;
	private final byte[] objectStart;
	private final Type manifest;
	private final FormatDescription<T>[] descriptions;
	private final boolean alwaysSerialize;
	private final boolean exactMatch;
	private final boolean canObjectFormat;
	private final boolean canArrayFormat;
	private final String discriminator;
	private final String discriminatorError;

	public MixinDescription(
			final Class<T> manifest,
			final DslJson json,
			final FormatDescription<T>[] descriptions) {
		this(manifest, json, descriptions, null);
	}

	public MixinDescription(
			final Class<T> manifest,
			final DslJson json,
			final String discriminator,
			final FormatDescription<T>[] descriptions) {
		this(manifest, json, descriptions, discriminator);
	}

	MixinDescription(
			final Type manifest,
			final DslJson json,
			final FormatDescription<T>[] descriptions,
			@Nullable final String discriminator) {
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (descriptions == null || descriptions.length == 0) {
			throw new IllegalArgumentException("descriptions can't be null or empty");
		}
		if (discriminator != null && (discriminator.length() == 0 || discriminator.contains("\""))) {
			throw new IllegalArgumentException("Invalid discriminator provided: " + discriminator);
		}
		this.typeHash = discriminator == null ? defaultTypeHash : DecodePropertyInfo.calcHash(discriminator);
		this.objectStart = discriminator == null ? defaultObjectStart : ("{\"" + discriminator + "\":").getBytes(StandardCharsets.UTF_8);
		this.discriminator = discriminator == null ? "$type" : discriminator;
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
		this.discriminatorError = String.format("Expecting \"%s\" attribute as first element of mixin %s", this.discriminator, Reflection.typeDescription(manifest));
	}

	@Nullable
	public T read(final JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		if (reader.last() == '{' && canObjectFormat) {
			return readObjectFormat(reader);
		} else if (canArrayFormat && reader.last() == '[') {
			return readArrayFormat(reader);
		}
		if (canObjectFormat && canArrayFormat) {
			throw reader.newParseError("Expecting '{' or '[' for object start");
		} else if (canObjectFormat) {
			throw reader.newParseError("Expecting '{' for object start");
		} else {
			throw reader.newParseError("Expecting '[' for object start");
		}
	}

	@Nullable
	private T readObjectFormat(final JsonReader reader) throws IOException {
		if (reader.getNextToken() != JsonWriter.QUOTE) {
			throw reader.newParseError(discriminatorError);
		}
		if (reader.fillName() != typeHash) {
			String name = reader.getLastName();
			throw reader.newParseErrorFormat(discriminatorError, name.length() + 2, "Expecting \"%s\" attribute as first element of mixin %s. Found: '%s'", discriminator, Reflection.typeDescription(manifest), name);
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
		throw new ConfigurationException("Unable to find decoder for '" + reader.getLastName() + "' for mixin: " + Reflection.typeDescription(manifest) + " which supports object format. Add @CompiledJson to specified type to allow deserialization into it");
	}

	@Nullable
	private T readArrayFormat(final JsonReader reader) throws IOException {
		if (reader.getNextToken() != JsonWriter.QUOTE) {
			throw reader.newParseError(discriminatorError);
		}
		final int hash = reader.calcHash();
		for (final FormatDescription<T> od : descriptions) {
			if (od.arrayFormat == null || od.typeHash != hash) continue;
			if (exactMatch && !reader.wasLastName(od.typeName)) continue;
			final FormatConverter<T> afd = od.arrayFormat;
			if (reader.getNextToken() == JsonWriter.COMMA) {
				return afd.readContent(reader);
			} else if (reader.last() != JsonWriter.ARRAY_END) {
				throw reader.newParseError("Expecting ']' for array format end");
			} else {
				//TODO: return new instance
				return afd.readContent(reader);
			}
		}
		throw new ConfigurationException("Unable to find decoder for '" + reader.getLastName() + "' for mixin: " + Reflection.typeDescription(manifest) + " which supports array format. Add @CompiledJson to specified type to allow deserialization into it");
	}

	@Override
	public void write(final JsonWriter writer, @Nullable final T instance) {
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
				writer.writeByte(JsonWriter.COMMA);
				final int pos = writer.size();
				final long flushed = writer.flushed();
				od.arrayFormat.writeContentFull(writer, instance);
				if (pos != writer.size() || flushed != writer.flushed()) {
					writer.writeByte(JsonWriter.ARRAY_END);
				} else {
					writer.getByteBuffer()[writer.size() - 1] = JsonWriter.ARRAY_END;
				}
			}
			return;
		}
		throw new ConfigurationException("Unable to find encoder for '" + instance.getClass() + "'. Add @CompiledJson to specified type to allow serialization from it");
	}
}
