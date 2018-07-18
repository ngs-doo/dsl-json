package dsl_json.java.util;

import com.dslplatform.json.*;
import com.dslplatform.json.runtime.OptionalAnalyzer;

import java.util.Optional;

public class OptionalDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerDefault(Optional.class, Optional.empty());
		json.registerWriterFactory(OptionalAnalyzer.WRITER);
		json.registerReaderFactory(OptionalAnalyzer.READER);
		OptionalAnalyzer.WRITER.tryCreate(Optional.class, json);
		OptionalAnalyzer.READER.tryCreate(Optional.class, json);
	}
}