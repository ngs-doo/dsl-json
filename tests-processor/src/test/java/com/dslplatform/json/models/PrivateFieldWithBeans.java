package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson
public class PrivateFieldWithBeans {
	@JsonAttribute
	private int num;
	public int getNum() { return num; }
	public void setNum(int num) { this.num = num; }
}
