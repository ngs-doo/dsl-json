package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson(deserializeAs = AbstractTypeIntoConcreteType.Concrete.class)
public abstract class AbstractTypeIntoConcreteType {
	public long y;
	public static class Concrete extends AbstractTypeIntoConcreteType {
		public String z;
	}
}
