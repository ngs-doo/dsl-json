package dsl_json.java.util;

import com.dslplatform.json.*;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class DateDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerReader(java.util.Date.class, new JsonReader.ReadObject<java.util.Date>() {
			@Nullable
			@Override
			public java.util.Date read(JsonReader reader) throws IOException {
				return reader.wasNull() ? null : java.util.Date.from(JavaTimeConverter.deserializeDateTime(reader).toInstant());
			}
		});
		json.registerWriter(java.util.Date.class, new JsonWriter.WriteObject<java.util.Date>() {
			@Override
			public void write(JsonWriter writer, @Nullable java.util.Date value) {
				if (value == null) writer.writeNull();
				else JavaTimeConverter.serialize(OffsetDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault()), writer);
			}
		});
	}
}