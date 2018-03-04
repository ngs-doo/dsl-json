package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.SerializationException;

import javax.sql.rowset.serial.SerialException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.function.Function;

class LazyAttributeEncoder<T, R> implements JsonWriter.WriteObject<T> {

	private static final Charset utf8 = Charset.forName("UTF-8");

	private final Function<T, R> read;
	private final byte[] quotedName;
	private final boolean alwaysSerialize;
	private JsonWriter.WriteObject<R> encoder;
	private final DslJson json;
	private final Type type;

	LazyAttributeEncoder(
			final Function<T, R> read,
			final String name,
			final DslJson json,
			final Type type) {
		if (read == null) throw new IllegalArgumentException("read can't be null");
		if (name == null || name.isEmpty()) throw new IllegalArgumentException("name can't be null");
		if (json == null) throw new IllegalArgumentException("json can't be null");
		this.read = read;
		quotedName = ("\"" + name + "\":").getBytes(utf8);
		this.alwaysSerialize = !json.omitDefaults;
		this.json = json;
		this.type = type;
	}

	@Override
	public void write(final JsonWriter writer, final T value) {
		if (encoder == null) {
			encoder = json.tryFindWriter(type);
			if (encoder == null) {
				throw new SerializationException("Unable to find writer for " + type);
			}
		}
		final R attr = read.apply(value);
		if (alwaysSerialize || attr != null) {
			writer.writeAscii(quotedName);
			encoder.write(writer, attr);
		}
	}
}
