package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
public class MultipleAnnotations {
	public final int x;
	public final String s;
	public final String e;
	public final long l;

	@CompiledJson(formats = CompiledJson.Format.ARRAY)
	public MultipleAnnotations(String e, int x, String s, long l) {
		this.x = x;
		this.s = s;
		this.e = e;
		this.l = l;
	}
}
