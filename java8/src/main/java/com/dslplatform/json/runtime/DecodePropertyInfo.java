package com.dslplatform.json.runtime;

import java.util.HashSet;

class DecodePropertyInfo<T> {
	public final String name;
	public final int hash;
	public final boolean exactName;
	public final T value;

	DecodePropertyInfo(String name, boolean exactName, T value) {
		this.name = name;
		this.exactName = exactName;
		this.value = value;
		long hash = 0x811c9dc5;
		for (int x = 0; x < name.length(); x++) {
			hash ^= (byte) name.charAt(x);
			hash *= 0x1000193;
		}
		this.hash = (int) hash;
	}

	static <T> DecodePropertyInfo<T>[] prepare(DecodePropertyInfo<T>[] initial) {
		final DecodePropertyInfo<T>[] readers = initial.clone();
		final HashSet<Integer> hashes = new HashSet<Integer>();
		for (final DecodePropertyInfo<T> ri : readers) {
			if (!hashes.add(ri.hash)) {
				for (int j = 0; j < readers.length; j++) {
					final DecodePropertyInfo si = readers[j];
					if (si.hash == ri.hash && !si.exactName) {
						readers[j] = new DecodePropertyInfo<>(ri.name, true, ri.value);
					}
				}
			}
		}
		return readers;
	}
}