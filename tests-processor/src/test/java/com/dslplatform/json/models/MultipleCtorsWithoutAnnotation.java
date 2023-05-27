package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.Collections;
import java.util.Map;

@CompiledJson
public class MultipleCtorsWithoutAnnotation {
	private final String id;
	@JsonAttribute(nullable = false)
	public final String getId() {
		return id;
	}

	private final String query;
	public final String getQuery() {
		return query;
	}

	private final Map<String, String> map;
	@JsonAttribute(nullable = false)
	public final Map<String, String> getMap() {
		return map;
	}

	public MultipleCtorsWithoutAnnotation(String id, String query, Map<String, String> map) {
		this.id = id;
		this.query = query;
		this.map = map;
	}

	public MultipleCtorsWithoutAnnotation() {
		this.id = "";
		this.query = null;
		this.map = Collections.EMPTY_MAP;
	}
}
