package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson(deserializeAs = InvalidConcreteWithDeserializeAs.Something.class)
public class InvalidConcreteWithDeserializeAs {
	public long y;
	public static class Something extends InvalidConcreteWithDeserializeAs {
	}
}
