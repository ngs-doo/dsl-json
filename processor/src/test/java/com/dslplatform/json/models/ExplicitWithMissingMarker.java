package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.UUID;

@CompiledJson(objectFormatPolicy = CompiledJson.ObjectFormatPolicy.EXPLICIT)
public class ExplicitWithMissingMarker {

	public final UUID identity;

	@JsonAttribute
	public final int level;

	public ExplicitWithMissingMarker(UUID identity, int level) {
		this.identity = identity;
		this.level = level;
	}
}