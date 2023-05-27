package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
public class ArrayFormatWithoutIndexSingle {
	private int o;
	public int getO() { return o ; }
	public void setO(int value) { o = value; }
}
