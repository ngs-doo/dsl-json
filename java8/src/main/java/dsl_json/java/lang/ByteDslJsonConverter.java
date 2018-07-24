package dsl_json.java.lang;

import com.dslplatform.json.*;

import java.io.IOException;

public class ByteDslJsonConverter implements Configuration {
	private static final JsonWriter.WriteObject<Byte> ByteWriter = new JsonWriter.WriteObject<Byte>() {
		@Override
		public void write(JsonWriter writer, @Nullable Byte value) {
			if (value == null) writer.writeNull();
			else NumberConverter.serialize(value, writer);
		}
	};
	private static final JsonReader.ReadObject<Byte> ByteReader = new JsonReader.ReadObject<Byte>() {
		@Override
		public Byte read(JsonReader reader) throws IOException {
			return (byte)NumberConverter.deserializeInt(reader);
		}
	};
	private static final JsonReader.ReadObject<Byte> NullableByteReader = new JsonReader.ReadObject<Byte>() {
		@Nullable
		@Override
		public Byte read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : (byte)NumberConverter.deserializeInt(reader);
		}
	};

	@Override
	public void configure(DslJson json) {
		json.registerWriter(byte.class, ByteWriter);
		json.registerReader(byte.class, ByteReader);
		json.registerWriter(Byte.class, ByteWriter);
		json.registerReader(Byte.class, NullableByteReader);
	}
}