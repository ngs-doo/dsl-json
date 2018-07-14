package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Callable;

public abstract class CollectionAnalyzer {

	public static final DslJson.ConverterFactory<CollectionDecoder> READER = new DslJson.ConverterFactory<CollectionDecoder>() {
		@Override
		public CollectionDecoder tryCreate(Type manifest, DslJson dslJson) {
			if (manifest instanceof Class<?>) {
				return analyzeDecoding(manifest, Object.class, (Class<?>) manifest, dslJson);
			}
			if (manifest instanceof ParameterizedType) {
				final ParameterizedType pt = (ParameterizedType) manifest;
				if (pt.getActualTypeArguments().length == 1 && pt.getRawType() instanceof Class<?>) {
					return analyzeDecoding(manifest, pt.getActualTypeArguments()[0], (Class<?>) pt.getRawType(), dslJson);
				}
			}
			return null;
		}
	};

	public static final DslJson.ConverterFactory<CollectionEncoder> WRITER = new DslJson.ConverterFactory<CollectionEncoder>() {
		@Override
		public CollectionEncoder tryCreate(Type manifest, DslJson dslJson) {
			if (manifest instanceof Class<?>) {
				return analyzeEncoding(manifest, Object.class, (Class<?>) manifest, dslJson);
			}
			if (manifest instanceof ParameterizedType) {
				final ParameterizedType pt = (ParameterizedType) manifest;
				if (pt.getActualTypeArguments().length == 1 && pt.getRawType() instanceof Class<?>) {
					return analyzeEncoding(manifest, pt.getActualTypeArguments()[0], (Class<?>) pt.getRawType(), dslJson);
				}
			}
			return null;
		}
	};

	private static CollectionDecoder analyzeDecoding(final Type manifest, final Type element, final Class<?> collection, final DslJson json) {
		if (!Collection.class.isAssignableFrom(collection)) return null;
		final Callable newInstance;
		if (!collection.isInterface()) {
			try {
				collection.newInstance();
			} catch (Exception ex) {
				return null;
			}
			newInstance = new Callable() {
				@Override
				public Object call() throws Exception {
					return collection.newInstance();
				}
			};
		} else if (Set.class.isAssignableFrom(collection)) {
			newInstance = new Callable() {
				@Override
				public Object call() {
					return new LinkedHashSet<Object>(4);
				}
			};
		} else if (List.class.isAssignableFrom(collection) || Collection.class == collection) {
			newInstance = new Callable() {
				@Override
				public Object call() {
					return new ArrayList<Object>(4);
				}
			};
		} else if (Queue.class.isAssignableFrom(collection)) {
			newInstance = new Callable() {
				@Override
				public Object call() {
					return new LinkedList();
				}
			};
		} else {
			return null;
		}
		final JsonReader.ReadObject<?> reader = json.tryFindReader(element);
		if (reader == null) {
			return null;
		}
		final CollectionDecoder decoder = new CollectionDecoder(manifest, newInstance, reader);
		json.registerReader(manifest, decoder);
		return decoder;
	}

	private static CollectionEncoder analyzeEncoding(final Type manifest, final Type element, final Class<?> collection, final DslJson json) {
		if (!Collection.class.isAssignableFrom(collection)) return null;
		final JsonWriter.WriteObject<?> writer = Object.class == element ? null : json.tryFindWriter(element);
		if (Object.class != element && writer == null) {
			return null;
		}
		final CollectionEncoder encoder = new CollectionEncoder(json, Settings.isKnownType(element) ? writer : null);
		json.registerWriter(manifest, encoder);
		return encoder;
	}
}
