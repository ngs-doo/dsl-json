package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@CompiledJson
public class ValidType {
	private int prop;
	private int u;
	private int uri;
	public String simpleField;
	public List<String> listField;

	@JsonProperty("test")
	public int getProp() {
		return prop;
	}

	public void setProp(int value) {
		prop = value;
	}

	public int getU() {
		return u;
	}

	public void setU(int value) {
		u = value;
	}

	public int getURI() {
		return uri;
	}

	public void setURI(int value) {
		uri = value;
	}
}
