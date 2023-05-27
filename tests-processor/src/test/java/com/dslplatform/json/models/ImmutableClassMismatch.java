package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
public class ImmutableClassMismatch {
	public final int x1;
	public final String s;
	public final String e;
	public final long l;

	public ImmutableClassMismatch(String e, int x, String s, long l) {
		this.x1 = x;
		this.s = s;
		this.e = e;
		this.l = l;
	}
}
