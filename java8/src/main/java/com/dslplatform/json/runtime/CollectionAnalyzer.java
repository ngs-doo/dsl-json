package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Callable;

public abstract class CollectionAnalyzer {

	private static final JsonWriter.WriteObject tmpWriter = (writer, value) -> {
	};
	private static final JsonReader.ReadObject tmpReader = reader -> null;

	public static final DslJson.ConverterFactory<CollectionDescription> CONVERTER = (manifest, dslJson) -> {
		if (manifest instanceof Class<?>) {
			return analyze(manifest, Object.class, (Class<?>)manifest, dslJson);
		}
		if (manifest instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) manifest;
			if (pt.getActualTypeArguments().length == 1 && pt.getRawType() instanceof Class<?>) {
				return analyze(manifest, pt.getActualTypeArguments()[0], (Class<?>) pt.getRawType(), dslJson);
			}
		}
		return null;
	};

	private static CollectionDescription analyze(final Type manifest, final Type element, final Class<?> collection, final DslJson json) {
		if (!Collection.class.isAssignableFrom(collection)) return null;
		final Callable newInstance;
		if (!collection.isInterface()) {
			try {
				collection.newInstance();
			} catch (Exception ex) {
				return null;
			}
			newInstance = collection::newInstance;
		} else if (Set.class.isAssignableFrom(collection)) {
			newInstance = () -> new LinkedHashSet<>(4);
		} else if (List.class.isAssignableFrom(collection) || Collection.class == collection) {
			newInstance = () -> new ArrayList<>(4);
		} else if (Queue.class.isAssignableFrom(collection)) {
			newInstance = LinkedList::new;
		} else {
			return null;
		}
		json.registerWriter(manifest, tmpWriter);
		json.registerReader(manifest, tmpReader);
		final JsonWriter.WriteObject<?> writer = json.tryFindWriter(element);
		final JsonReader.ReadObject<?> reader = json.tryFindReader(element);
		if (Object.class != element && writer == null || reader == null) {
			json.registerWriter(manifest, null);
			json.registerReader(manifest, null);
			return null;
		}
		final CollectionDescription converter =
				new CollectionDescription(
						manifest,
						newInstance,
						json,
						Object.class == element ? null : writer,
						reader);
		json.registerWriter(manifest, converter);
		json.registerReader(manifest, converter);
		return converter;
	}
}
