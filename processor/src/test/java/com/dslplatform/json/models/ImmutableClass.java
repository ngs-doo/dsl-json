package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
public class ImmutableClass {
	@JsonAttribute(index = 1)
	public final int x;
	@JsonAttribute(index = 2)
	public final String s;
	@JsonAttribute(index = 3)
	public final String e;
	@JsonAttribute(index = 4)
	public final long l;

	public ImmutableClass(String e, int x, String s, long l) {
		this.x = x;
		this.s = s;
		this.e = e;
		this.l = l;
	}
}
