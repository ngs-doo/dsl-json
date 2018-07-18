package dsl_json.java.util;

import com.dslplatform.json.*;

import java.io.IOException;
import java.util.OptionalInt;

public class OptionalIntDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerWriter(OptionalInt.class, new JsonWriter.WriteObject<OptionalInt>() {
			@Override
			public void write(JsonWriter writer, OptionalInt value) {
				if (value != null && value.isPresent()) NumberConverter.serialize(value.getAsInt(), writer);
				else writer.writeNull();
			}
		});
		json.registerReader(OptionalInt.class, new JsonReader.ReadObject<OptionalInt>() {
			@Override
			public OptionalInt read(JsonReader reader) throws IOException {
				return reader.wasNull() ? OptionalInt.empty() : OptionalInt.of(NumberConverter.deserializeInt(reader));
			}
		});
		json.registerDefault(OptionalInt.class, OptionalInt.empty());
	}
}