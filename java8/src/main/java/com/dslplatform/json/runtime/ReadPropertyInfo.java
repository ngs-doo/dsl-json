package com.dslplatform.json.runtime;

import java.util.HashSet;

class ReadPropertyInfo<T> {
	public final String name;
	public final int hash;
	public final boolean exactName;
	public final T value;

	ReadPropertyInfo(String name, boolean exactName, T value) {
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

	static <T> ReadPropertyInfo<T>[] prepareReaders(ReadPropertyInfo<T>[] initial) {
		final ReadPropertyInfo<T>[] readers = initial.clone();
		final HashSet<Integer> hashes = new HashSet<Integer>();
		for (final ReadPropertyInfo<T> ri : readers) {
			if (!hashes.add(ri.hash)) {
				for (int j = 0; j < readers.length; j++) {
					final ReadPropertyInfo si = readers[j];
					if (si.hash == ri.hash && !si.exactName) {
						readers[j] = new ReadPropertyInfo<>(ri.name, true, ri.value);
					}
				}
			}
		}
		return readers;
	}
}