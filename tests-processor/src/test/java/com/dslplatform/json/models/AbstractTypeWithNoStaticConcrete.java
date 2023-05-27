package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson(deserializeAs = AbstractTypeWithNoStaticConcrete.Concrete.class)
public abstract class AbstractTypeWithNoStaticConcrete {
	public long y;
	public class Concrete extends AbstractTypeWithNoStaticConcrete {
		public String z;
	}
}
