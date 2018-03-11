package com.dslplatform.json.runtime;

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
	private static final byte[] hashAscii = "\"$type\":".getBytes(utf8);

	private final Type manifest;
	private final BeanDescription<Object, T>[] descriptions;
	private final boolean exactMatch;

	public MixinDescription(
			final Class<T> manifest,
			final BeanDescription<Object, T>[] descriptions) {
		this((Type) manifest, descriptions);
	}

	MixinDescription(
			final Type manifest,
			final BeanDescription<Object, T>[] descriptions) {
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (descriptions == null || descriptions.length == 0) {
			throw new IllegalArgumentException("descriptions can't be null or empty");
		}
		this.manifest = manifest;
		this.descriptions = descriptions;
		Set<Integer> uniqueHashNames = new HashSet<>();
		for (BeanDescription bd : descriptions) {
			uniqueHashNames.add(bd.typeHash);
		}
		exactMatch = uniqueHashNames.size() != descriptions.length;
	}

	public T read(final JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		if (reader.last() != '{') {
			throw new IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char) reader.last());
		}
		if (reader.getNextToken() != JsonWriter.QUOTE) {
			throw new IOException("Expecting \"$type\" attribute as first element of mixin at position " + reader.positionInStream() + ". Found " + (char) reader.last());
		}
		if (reader.fillName() != typeHash) {
			String name = reader.getLastName();
			throw new IOException("Expecting \"$type\" attribute as first element of mixin at position " + reader.positionInStream(name.length() + 2) + ". Found: " + name);
		}
		reader.getNextToken();
		final int hash = reader.calcHash();
		for (final BeanDescription<Object, T> bd : descriptions) {
			if (bd.typeHash != hash) continue;
			if (exactMatch && !reader.wasLastName(bd.typeName)) continue;
			final Object instance;
			try {
				instance = bd.newInstance.call();
			} catch (Exception e) {
				throw new IOException("Unable to create an instance of " + new String(bd.typeName, utf8), e);
			}
			if (reader.getNextToken() == JsonWriter.COMMA) {
				reader.getNextToken();
			}
			bd.bindObject(reader, instance);
			return bd.finalize.apply(instance);
		}
		throw new IOException("Unable to find decoder for '" + reader.getLastName() + "' for mixin: " + manifest + ". Add @CompiledJson to specified type to allow deserialization into it");
	}

	@Override
	public void write(final JsonWriter writer, final T instance) {
		if (instance == null) {
			writer.writeNull();
			return;
		}
		writer.writeByte(JsonWriter.OBJECT_START);
		writer.writeAscii(hashAscii);
		final Class<?> current = instance.getClass();
		for (BeanDescription<Object, T> bd : descriptions) {
			if (current == bd.manifest) {
				writer.writeAscii(bd.typeName);
				writer.writeByte(JsonWriter.COMMA);
				int pos = writer.size();
				long flushed = writer.flushed();
				bd.writeObject(writer, instance);
				if (writer.size() != pos || writer.flushed() != flushed) {
					writer.writeByte(JsonWriter.OBJECT_END);
				} else {
					//No properties have been written, replace comma with object end
					//this is safe since buffer is enlarged before written into
					writer.getByteBuffer()[writer.size() - 1] = JsonWriter.OBJECT_END;
				}
				return;
			}
		}
		throw new SerializationException("Unable to find encoder for '" + instance.getClass() + "'. Add @CompiledJson to specified type to allow serialization from it");
	}
}
