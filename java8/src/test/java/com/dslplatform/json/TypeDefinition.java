package com.dslplatform.json;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

abstract class TypeDefinition<T> {

	public final Type type;

	public TypeDefinition() {
		type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

	T deserialize(DslJson json, InputStream is) throws IOException {
		return (T)json.deserialize(type, is);
	}
}