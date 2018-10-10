package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
public class ArrayFormatWithoutIndexMultiple {
	private int o, u;
	public int getO() { return o; }
	public void setO(int value) { o = value; }
	public int getU() { return u; }
	public void setU(int value) { u = value; }
}
