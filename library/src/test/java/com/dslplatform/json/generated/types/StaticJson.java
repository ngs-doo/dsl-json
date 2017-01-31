package com.dslplatform.json.generated.types;

import com.dslplatform.json.Configuration;
import com.dslplatform.json.DslJson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ServiceLoader;

public class StaticJson {
	private static final JsonSerialization json = new JsonSerialization();

	public static JsonSerialization getSerialization() {
		return json;
	}

	public static class Bytes {
		public byte[] content;
		public int length;
	}

	public static class JsonSerialization extends DslJson<Object> {
		public JsonSerialization() {
			super(new Settings<Object>().withJavaConverters(true).includeServiceLoader());
		}
		private ByteArrayOutputStream stream = new ByteArrayOutputStream();

		public Bytes serialize(Object instance) throws IOException {
			stream.reset();
			super.serialize(instance, stream);
			Bytes b = new Bytes();
			b.content = stream.toByteArray();
			b.length = b.content.length;
			return b;
		}
	}
}
