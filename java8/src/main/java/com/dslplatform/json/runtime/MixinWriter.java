package com.dslplatform.json.runtime;

import com.dslplatform.json.*;

import java.lang.reflect.Type;

public final class MixinWriter<T> implements JsonWriter.WriteObject<T> {

	private final Type manifest;
	private final FormatDescription<T>[] descriptions;

	public MixinWriter(
			final Class<T> manifest,
			final DslJson json,
			final FormatDescription<T>[] descriptions) {
		this((Type) manifest, json, descriptions);
	}

	MixinWriter(
			final Type manifest,
			final DslJson json,
			final FormatDescription<T>[] descriptions) {
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (descriptions == null || descriptions.length == 0) {
			throw new IllegalArgumentException("descriptions can't be null or empty");
		}
		this.manifest = manifest;
		this.descriptions = descriptions;
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
				od.objectFormat.write(writer, instance);
			} else {
				od.arrayFormat.write(writer, instance);
			}
			return;
		}
		throw new ConfigurationException("Unable to find encoder for '" + instance.getClass() + "' while encoding " + Reflection.typeDescription(manifest) + ". Add @CompiledJson to specified type to allow serialization from it");
	}
}
