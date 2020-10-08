package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@CompiledJson
public class CollectionNoSetterAlternative {
	private int x;

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	@JsonProperty
	@Nonnull
	private final List<String> s = new ArrayList<String>();

	public List<String> getS() {
		return s;
	}
}
