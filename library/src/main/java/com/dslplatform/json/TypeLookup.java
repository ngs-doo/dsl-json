package com.dslplatform.json;

interface TypeLookup {
	<T> JsonReader.ReadObject<T> tryFindReader(Class<T> manifest);
	<T> JsonReader.BindObject<T> tryFindBinder(Class<T> manifest);
}
