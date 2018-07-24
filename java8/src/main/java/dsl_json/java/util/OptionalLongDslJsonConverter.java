package dsl_json.java.util;

import com.dslplatform.json.*;

import java.io.IOException;
import java.util.OptionalLong;

public class OptionalLongDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerWriter(OptionalLong.class, new JsonWriter.WriteObject<OptionalLong>() {
			@Override
			public void write(JsonWriter writer, @Nullable OptionalLong value) {
				if (value != null && value.isPresent()) NumberConverter.serialize(value.getAsLong(), writer);
				else writer.writeNull();
			}
		});
		json.registerReader(OptionalLong.class, new JsonReader.ReadObject<OptionalLong>() {
			@Override
			public OptionalLong read(JsonReader reader) throws IOException {
				return reader.wasNull() ? OptionalLong.empty() : OptionalLong.of(NumberConverter.deserializeLong(reader));
			}
		});
		json.registerDefault(OptionalLong.class, OptionalLong.empty());
	}
}