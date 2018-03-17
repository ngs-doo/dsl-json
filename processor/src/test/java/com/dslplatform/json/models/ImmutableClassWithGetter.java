package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.List;

@CompiledJson
public class ImmutableClassWithGetter {
	private final String e;
	public final String getE() { return e; }
	private final List<Integer> list;
	public final List<Integer> getList() { return list; }

	public ImmutableClassWithGetter(String e, List<Integer> list) {
		this.e = e;
		this.list = list;
	}
}
