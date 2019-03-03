package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CompiledJson
public class GenericWithVariousTypesAndCtor<A, B, C> {
	@JsonAttribute(nullable = false)
	public C[] c1;
	@JsonAttribute(nullable = false)
	public B[][] b2;

	public GenericWithVariousTypesAndCtor(C[] c1, B[][] b2) {
		this.c1 = c1;
		this.b2 = b2;
	}
}
