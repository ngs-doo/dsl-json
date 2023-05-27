package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson(formats = CompiledJson.Format.ARRAY)
public class ArrayFormatDuplicateIndex {
	@JsonAttribute(index = 1)
	public long y;
	private int o;
	@JsonAttribute(index = 1)
	public int getO() { return o ; }
	public void setO(int value) { o = value; }
}
