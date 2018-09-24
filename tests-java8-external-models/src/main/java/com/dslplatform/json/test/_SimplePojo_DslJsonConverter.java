package com.dslplatform.json.test;


import com.dslplatform.json.*;

public class _SimplePojo_DslJsonConverter implements Configuration {

	private static final JsonReader.ReadObject FAKE_READER = reader -> {
		throw new IllegalStateException("This is fake reader for " + SimplePojo.class);
	};

	private static final JsonWriter.WriteObject FAKE_WRITER = (writer, value) -> {
		throw new IllegalStateException("This is fake writer for " + SimplePojo.class);
	};

	@Override
	public void configure(@NonNull DslJson json) {
		json.registerReader(SimplePojo.class, FAKE_READER);
		json.registerWriter(SimplePojo.class, FAKE_WRITER);
	}
}
