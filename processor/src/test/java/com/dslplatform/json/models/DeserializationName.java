package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson(deserializeName = "MyCustom.Name")
public class DeserializationName {
	public long y;
	private int o;
	public int getO() { return o ; }
	public void setO(int value) { o = value; }
}
