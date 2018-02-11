package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.ObjectConverter;

public abstract class Settings {
	private static final DslJson.ConverterFactory<JsonReader.ReadObject> UNKNOWN_READER =
			(manifest, dslJson) -> Object.class == manifest ? ObjectConverter::deserializeObject : null;

	public static <T> DslJson.Settings<T> withRuntime() {
		return new DslJson.Settings()
				.resolveReader(UNKNOWN_READER)
				.resolveReader(OptionalAnalyzer.READER)
				.resolveWriter(OptionalAnalyzer.WRITER)
				.resolveReader(CollectionAnalyzer.READER)
				.resolveWriter(CollectionAnalyzer.WRITER)
				.resolveReader(ArrayAnalyzer.READER)
				.resolveWriter(ArrayAnalyzer.WRITER)
				.resolveReader(MapAnalyzer.READER)
				.resolveWriter(MapAnalyzer.WRITER)
				.resolveWriter(ImmutableAnalyzer.CONVERTER)
				.resolveReader(ImmutableAnalyzer.CONVERTER)
				.resolveWriter(EnumAnalyzer.CONVERTER)
				.resolveReader(EnumAnalyzer.CONVERTER)
				.resolveWriter(BeanAnalyzer.CONVERTER)
				.resolveBinder(BeanAnalyzer.CONVERTER)
				.resolveReader(BeanAnalyzer.CONVERTER);
	}
}
