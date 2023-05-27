package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson
public class CustomCtorPropertyNameGetter {
	private String query;

	public CustomCtorPropertyNameGetter(@JsonAttribute(name = "Query") String query) {
		this.query = query;
	}

	public String getQuery() { return query; }
}
