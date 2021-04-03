package com.dslplatform.json.processor;

import java.util.*;

public class MinifiedNaming implements NamingStrategy {

	private static String buildShortName(String name, Set<String> names, Map<Character, Integer> counters) {
		String shortName = name.substring(0, 1);
		Character first = name.charAt(0);
		if (!names.contains(shortName)) {
			names.add(shortName);
			counters.put(first, 0);
			return shortName;
		}
		Integer next = counters.get(first);
		if (next == null) {
			next = 0;
		}
		do {
			shortName = first.toString() + next;
			next++;
		} while (names.contains(shortName));
		counters.put(first, next);
		names.add(shortName);
		return shortName;
	}

	public Map<String, String> prepareNames(Map<String, AttributeInfo> attributes) {
		Map<String, String> result = new LinkedHashMap<String, String>();
		Map<Character, Integer> counters = new HashMap<Character, Integer>();
		Set<String> processed = new HashSet<String>();
		Set<String> names = new HashSet<String>();
		for (AttributeInfo p : attributes.values()) {
			if (p.alias != null) {
				result.put(p.id, p.alias);
				processed.add(p.id);
				names.add(p.alias);
			}
		}
		for (AttributeInfo p : attributes.values()) {
			if (processed.contains(p.id)) {
				continue;
			}
			String shortName = buildShortName(p.id, names, counters);
			result.put(p.id, shortName);
		}
		return result;
	}
}
