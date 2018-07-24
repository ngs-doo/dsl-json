package com.dslplatform.json;

interface TypeLookup {
	@Nullable
	<T> JsonReader.ReadObject<T> tryFindReader(Class<T> manifest);
	@Nullable
	<T> JsonReader.BindObject<T> tryFindBinder(Class<T> manifest);
}
