package com.dslplatform.json.processor;

import java.util.HashMap;
import java.util.Map;

final class CachedTypeSupport implements TypeSupport {
	private final Map<String, Boolean> cache = new HashMap<>();
	private final TypeSupport delegate;

	CachedTypeSupport(TypeSupport delegate) {
		this.delegate = delegate;
	}

	@Override
	public final boolean isSupported(String type) {
		Boolean result = cache.get(type);
		if (result == null) {
			result = delegate.isSupported(type);
			cache.put(type, result);
		}
		return result;
	}
}
