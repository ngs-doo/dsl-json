package com.dslplatform.json.models;

import com.dslplatform.json.*;
import com.dslplatform.json.processor.AttributeInfo;
import com.dslplatform.json.processor.NamingStrategy;

import java.util.LinkedHashMap;
import java.util.Map;

@CompiledJson(namingStrategy = NamingPojo.NS.class)
public class NamingPojo {
	public String s;
	public int i;

	public static class NS implements NamingStrategy {

		@Override
		public Map<String, String> prepareNames(Map<String, AttributeInfo> attributes) {
			LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
			for (AttributeInfo p : attributes.values()) {
				String name = p.alias != null ? p.alias : p.id;
				result.put(p.id, Character.toLowerCase(name.charAt(0)) + name.substring(1));
			}
			return result;
		}
	}
}
