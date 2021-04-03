package com.dslplatform.json.processor;

import com.dslplatform.json.Nullable;

import java.util.Map;

public interface NamingStrategy {
	@Nullable
	Map<String, String> prepareNames(Map<String, AttributeInfo> attributes);
}
