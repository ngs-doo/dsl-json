package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson
public class ImmutableClass {
	public final int x;
	public final String s;
	public final String e;
	public final long l;

	public ImmutableClass(int x, String s, String e, long l) {
		this.x = x;
		this.s = s;
		this.e = e;
		this.l = l;
	}
}
