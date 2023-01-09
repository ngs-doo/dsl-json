package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson
public class GetterAndSetterAnnotation {
	public int x;
	private String s;

	@JsonAttribute
	public String getS() {
		return s;
	}

	@JsonAttribute
	public void setS(String value) {
		s = value;
	}
}
