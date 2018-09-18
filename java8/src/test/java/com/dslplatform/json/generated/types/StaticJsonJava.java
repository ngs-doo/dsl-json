package com.dslplatform.json.generated.types;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.PrettifyStream;
import org.junit.Assert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class StaticJsonJava {
	private static final JsonSerialization json = new JsonSerialization();

	public static JsonSerialization getSerialization() {
		return json;
	}

	public static class Bytes {
		public byte[] content;
		public int length;
	}

	public static class JsonSerialization extends DslJson<Object> {

		private final PrettifyStream ps = new PrettifyStream();
		private final ByteArrayOutputStream psOut = new ByteArrayOutputStream();

		public JsonSerialization() {
			super(new Settings<>().includeServiceLoader());
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

		public <TResult> TResult deserialize(
				final Class<TResult> manifest,
				final byte[] body,
				final int size) throws IOException {
			TResult res1 = super.deserialize(manifest, body, size);
			psOut.reset();
			ps.process(new ByteArrayInputStream(body), psOut);
			super.deserialize(manifest, psOut.toByteArray(), psOut.size());
			return res1;
		}
	}
}
