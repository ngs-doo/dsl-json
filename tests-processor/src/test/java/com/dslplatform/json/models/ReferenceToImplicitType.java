package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson
public class ReferenceToImplicitType {
	private ImplicitType prop;

	public ImplicitType getProp() {
		return prop;
	}

	public void setProp(ImplicitType value) {
		prop = value;
	}
}
