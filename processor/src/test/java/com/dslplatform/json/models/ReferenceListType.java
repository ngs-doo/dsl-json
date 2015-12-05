package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

import java.util.List;

@CompiledJson
public class ReferenceListType {
	private List<ValidType> list;

	public List<ValidType> getList() {
		return list;
	}

	public void setList(List<ValidType> value) {
		list = value;
	}
}
