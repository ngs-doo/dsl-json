package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson
public class MultipleCtorsWithoutAnnotation {
	private final String id;
	public final String getId() {
		return id;
	}

	private final String query;
	public final String getQuery() {
		return query;
	}

	public MultipleCtorsWithoutAnnotation(String id, String query) {
		this.id = id;
		this.query = query;
	}

	public MultipleCtorsWithoutAnnotation() {
		this.id = "";
		this.query = null;
	}
}
