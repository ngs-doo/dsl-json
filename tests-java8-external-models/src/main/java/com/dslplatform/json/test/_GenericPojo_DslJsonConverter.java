package com.dslplatform.json.test;


import com.dslplatform.json.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class _GenericPojo_DslJsonConverter implements Configuration {

	private static final JsonReader.ReadObject FAKE_READER = reader -> {
		throw new IllegalStateException("This is fake reader for " + GenericPojo.class);
	};

	private static final JsonWriter.WriteObject FAKE_WRITER = (writer, value) -> {
		throw new IllegalStateException("This is fake writer for " + GenericPojo.class);
	};

	@Override
	public void configure(@NonNull DslJson json) {
		json.registerReaderFactory((manifest, dslJson) -> isOurClass(manifest, dslJson) ? FAKE_READER : null);
		json.registerWriterFactory((manifest, dslJson) -> isOurClass(manifest, dslJson) ? FAKE_WRITER : null);
	}

	private boolean isOurClass(Type manifest, DslJson<?> dslJson) {
		if (manifest instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) manifest;
			Type arg = pt.getActualTypeArguments()[0];
			return GenericPojo.class.equals(pt.getRawType())
					&& dslJson.tryFindReader(arg) != null && dslJson.tryFindWriter(arg) != null;
		} else if (GenericPojo.class.equals(manifest)) {
			return dslJson.tryFindReader(Object.class) != null && dslJson.tryFindWriter(Object.class) != null;
		}
		return false;
	}
}
