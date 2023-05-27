package dsl_json.java.sql;

import com.dslplatform.json.*;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class TimestampDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerReader(java.sql.Timestamp.class, new JsonReader.ReadObject<Timestamp>() {
			@Nullable
			@Override
			public Timestamp read(JsonReader reader) throws IOException {
				return reader.wasNull() ? null : java.sql.Timestamp.from(JavaTimeConverter.deserializeDateTime(reader).toInstant());
			}
		});
		json.registerWriter(java.sql.Timestamp.class, new JsonWriter.WriteObject<java.sql.Timestamp>() {
			@Override
			public void write(JsonWriter writer, @Nullable java.sql.Timestamp value) {
				if (value == null) writer.writeNull();
				else JavaTimeConverter.serialize(OffsetDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault()), writer);
			}
		});
	}
}