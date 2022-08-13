package com.dslplatform.json.test;

import com.dslplatform.json.processor.AttributeInfo;
import com.dslplatform.json.processor.NamingStrategy;

import java.util.LinkedHashMap;
import java.util.Map;

public class CustomNamingStrategyExternal implements NamingStrategy {

	@Override
	public Map<String, String> prepareNames(Map<String, AttributeInfo> attributes) {
		LinkedHashMap<String, String> result = new LinkedHashMap<>();
		for (AttributeInfo p : attributes.values()) {
			String name = p.alias != null ? p.alias : p.id;
			result.put(p.id, Character.toLowerCase(name.charAt(0)) + name.substring(1));
		}
		return result;
	}
}
