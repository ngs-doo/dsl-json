package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;
import com.dslplatform.json.runtime.MapAnalyzer;

import java.util.Map;

@CompiledJson
public class MapRuntimeCtor {
	private final Map<String, Object> map;

	public final Map<String, Object> getMap() {
		return map;
	}

	public MapRuntimeCtor(
			@JsonAttribute(converter = MapAnalyzer.Runtime.class) Map<String, Object> map) {
		this.map = map;
	}
}