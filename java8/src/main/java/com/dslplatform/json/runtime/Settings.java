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
				.resolveWriter(OptionalAnalyzer.CONVERTER)
				.resolveReader(OptionalAnalyzer.CONVERTER)
				.resolveReader(CollectionAnalyzer.CONVERTER)
				.resolveWriter(CollectionAnalyzer.CONVERTER)
				.resolveReader(ArrayAnalyzer.CONVERTER)
				.resolveWriter(ArrayAnalyzer.CONVERTER)
				.resolveReader(MapAnalyzer.CONVERTER)
				.resolveWriter(MapAnalyzer.CONVERTER)
				.resolveWriter(ImmutableAnalyzer.CONVERTER)
				.resolveReader(ImmutableAnalyzer.CONVERTER)
				.resolveWriter(EnumAnalyzer.CONVERTER)
				.resolveReader(EnumAnalyzer.CONVERTER)
				.resolveWriter(BeanAnalyzer.CONVERTER)
				.resolveBinder(BeanAnalyzer.CONVERTER)
				.resolveReader(BeanAnalyzer.CONVERTER);
	}
}
