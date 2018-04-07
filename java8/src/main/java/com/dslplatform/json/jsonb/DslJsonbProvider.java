package com.dslplatform.json.jsonb;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.SerializationException;
import com.dslplatform.json.runtime.Settings;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.JsonbException;
import javax.json.bind.spi.JsonbProvider;
import java.io.*;
import java.lang.reflect.Type;

public class DslJsonbProvider extends JsonbProvider {
	@Override
	public JsonbBuilder create() {
		return new DslJsonbBuilder();
	}

	private static class DslJsonbBuilder implements JsonbBuilder {

		private final DslJson.Settings settings = Settings.withRuntime().skipDefaultValues(true).includeServiceLoader();

		@Override
		public JsonbBuilder withConfig(JsonbConfig config) {
			config.getProperty("jsonb.null-values")
					.ifPresent(o -> settings.skipDefaultValues(Boolean.FALSE.equals(o)));
			return this;
		}

		@Override
		public JsonbBuilder withProvider(javax.json.spi.JsonProvider provider) {
			return this;
		}

		@Override
		public Jsonb build() {
			return new DslJsonb(settings);
		}
	}

	private static class DslJsonb implements Jsonb {

		private final DslJson<Object> dslJson;
		private final ThreadLocal<JsonWriter> localWriter;

		DslJsonb(DslJson.Settings settings) {
			dslJson = new DslJson<>(settings);
			localWriter = ThreadLocal.withInitial(dslJson::newWriter);
		}

		@Override
		public <T> T fromJson(String input, Class<T> as) throws JsonbException {
			if (input == null) throw new JsonbException("input can't be null");
			if (as == null) throw new JsonbException("as can't be null");
			try {
				byte[] bytes = input.getBytes("UTF-8");
				return dslJson.deserialize(as, bytes, bytes.length);
			} catch (IOException e) {
				throw new JsonbException(e.getMessage(), e.getCause());
			}
		}

		@Override
		public <T> T fromJson(String input, Type type) throws JsonbException {
			if (input == null) throw new JsonbException("input can't be null");
			if (type == null) throw new JsonbException("type can't be null");
			try {
				byte[] bytes = input.getBytes("UTF-8");
				return (T)dslJson.deserialize(type, bytes, bytes.length);
			} catch (IOException e) {
				throw new JsonbException(e.getMessage(), e.getCause());
			}
		}

		@Override
		public <T> T fromJson(Reader reader, Class<T> as) throws JsonbException {
			throw new JsonbException("DSL-JSON does not support Reader API");
		}

		@Override
		public <T> T fromJson(Reader reader, Type type) throws JsonbException {
			throw new JsonbException("DSL-JSON does not support Reader API");
		}

		@Override
		public <T> T fromJson(InputStream stream, Class<T> as) throws JsonbException {
			if (stream == null) throw new JsonbException("stream can't be null");
			if (as == null) throw new JsonbException("as can't be null");
			try {
				return (T)dslJson.deserialize(as, stream);
			} catch (IOException e) {
				throw new JsonbException(e.getMessage(), e.getCause());
			}
		}

		@Override
		public <T> T fromJson(InputStream stream, Type type) throws JsonbException {
			if (stream == null) throw new JsonbException("stream can't be null");
			if (type == null) throw new JsonbException("type can't be null");
			try {
				return (T)dslJson.deserialize(type, stream);
			} catch (IOException e) {
				throw new JsonbException(e.getMessage(), e.getCause());
			}
		}

		@Override
		public String toJson(Object obj) throws JsonbException {
			try {
				JsonWriter writer = localWriter.get();
				writer.reset();
				dslJson.serialize(writer, obj);
				return new String(writer.getByteBuffer(), 0, writer.size(), "UTF-8");
			} catch (IOException | SerializationException ex) {
				throw new JsonbException(ex.getMessage(), ex.getCause());
			}
		}

		@Override
		public String toJson(Object obj, Type type) throws JsonbException {
			if (type == null) throw new JsonbException("type can't be null");
			try {
				JsonWriter writer = localWriter.get();
				writer.reset();
				if (!dslJson.serialize(writer, type, obj)) {
					throw new JsonbException("Unable to serialize provided " + type);
				}
				return new String(writer.getByteBuffer(), 0, writer.size(), "UTF-8");
			} catch (IOException | SerializationException ex) {
				throw new JsonbException(ex.getMessage(), ex.getCause());
			}
		}

		@Override
		public void toJson(Object obj, Writer writer) throws JsonbException {
			if (writer == null) throw new JsonbException("writer can't be null");
			try {
				JsonWriter jw = localWriter.get();
				jw.reset();
				dslJson.serialize(jw, obj);
				writer.write(new String(jw.getByteBuffer(), 0, jw.size(), "UTF-8"));
			} catch (IOException | SerializationException ex) {
				throw new JsonbException(ex.getMessage(), ex.getCause());
			}
		}

		@Override
		public void toJson(Object obj, Type type, Writer writer) throws JsonbException {
			if (type == null) throw new JsonbException("type can't be null");
			if (writer == null) throw new JsonbException("writer can't be null");
			try {
				JsonWriter jw = localWriter.get();
				jw.reset();
				if (!dslJson.serialize(jw, type, obj)) {
					throw new JsonbException("Unable to serialize provided " + type);
				}
				writer.write(new String(jw.getByteBuffer(), 0, jw.size(), "UTF-8"));
			} catch (IOException | SerializationException ex) {
				throw new JsonbException(ex.getMessage(), ex.getCause());
			}
		}

		@Override
		public void toJson(Object obj, OutputStream stream) throws JsonbException {
			if (stream == null) throw new JsonbException("stream can't be null");
			try {
				dslJson.serialize(obj, stream);
			} catch (IOException | SerializationException ex) {
				throw new JsonbException(ex.getMessage(), ex.getCause());
			}
		}

		@Override
		public void toJson(Object obj, Type type, OutputStream stream) throws JsonbException {
			if (type == null) throw new JsonbException("type can't be null");
			if (stream == null) throw new JsonbException("stream can't be null");
			JsonWriter jw = localWriter.get();
			try {
				jw.reset(stream);
				if (!dslJson.serialize(jw, type, obj)) {
					throw new JsonbException("Unable to serialize provided " + type);
				}
				jw.flush();
			} catch (SerializationException ex) {
				throw new JsonbException(ex.getMessage(), ex.getCause());
			} finally {
				jw.reset(null);
			}
		}

		@Override
		public void close() {
		}
	}
}
