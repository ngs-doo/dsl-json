package dsl_json.java.sql;

import com.dslplatform.json.*;

import java.io.IOException;
import java.sql.Date;

public class DateDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerReader(java.sql.Date.class, new JsonReader.ReadObject<java.sql.Date>() {
			@Override
			public Date read(JsonReader reader) throws IOException {
				return reader.wasNull() ? null : java.sql.Date.valueOf(JavaTimeConverter.deserializeLocalDate(reader));
			}
		});
		json.registerWriter(java.sql.Date.class, new JsonWriter.WriteObject<java.sql.Date>() {
			@Override
			public void write(JsonWriter writer, java.sql.Date value) {
				if (value == null) writer.writeNull();
				else JavaTimeConverter.serialize(value.toLocalDate(), writer);
			}
		});
	}
}