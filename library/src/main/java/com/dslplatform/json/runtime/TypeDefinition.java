package com.dslplatform.json.runtime;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class TypeDefinition<T> {

	public final Type type;

	public TypeDefinition() {
		type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}
}