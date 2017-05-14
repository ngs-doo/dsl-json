package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson(deserializeAs = InterfaceIntoNonRelated.Concrete.class)
public interface InterfaceIntoNonRelated {
	long getY();
	class Concrete {
		public String z;
	}
}
