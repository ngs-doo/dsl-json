package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.ArrayList;
import java.util.List;

@CompiledJson
public class CollectionNoSetter {
	private int x;

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	@JsonAttribute(nullable = false)
	private final List<String> s = new ArrayList<String>();

	public List<String> getS() {
		return s;
	}
}
