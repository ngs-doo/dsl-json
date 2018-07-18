package dsl_json.java.util;

import com.dslplatform.json.*;

import java.io.IOException;
import java.util.OptionalDouble;

public class OptionalDoubleDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerWriter(OptionalDouble.class, new JsonWriter.WriteObject<OptionalDouble>() {
			@Override
			public void write(JsonWriter writer, OptionalDouble value) {
				if (value != null && value.isPresent()) NumberConverter.serialize(value.getAsDouble(), writer);
				else writer.writeNull();
			}
		});
		json.registerReader(OptionalDouble.class, new JsonReader.ReadObject<OptionalDouble>() {
			@Override
			public OptionalDouble read(JsonReader reader) throws IOException {
				return reader.wasNull() ? OptionalDouble.empty() : OptionalDouble.of(NumberConverter.deserializeDouble(reader));
			}
		});
		json.registerDefault(OptionalDouble.class, OptionalDouble.empty());
	}
}