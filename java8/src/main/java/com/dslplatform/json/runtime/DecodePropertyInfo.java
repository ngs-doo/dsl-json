package com.dslplatform.json.runtime;

import com.dslplatform.json.SerializationException;

import java.util.HashSet;

public class DecodePropertyInfo<T> {
	public final String name;
	public final int hash;
	public final boolean exactName;
	public final boolean mandatory;
	public final T value;
	final long mandatoryValue;

	public DecodePropertyInfo(String name, boolean exactName, boolean mandatory, T value) {
		this(name, exactName, mandatory, 0, calcHash(name), value);
	}

	private static int calcHash(String name) {
		long hash = 0x811c9dc5;
		for (int x = 0; x < name.length(); x++) {
			hash ^= (byte) name.charAt(x);
			hash *= 0x1000193;
		}
		return  (int) hash;
	}

	private DecodePropertyInfo(String name, boolean exactName, boolean mandatory, long mandatoryValue, int hash, T value) {
		this.name = name;
		this.exactName = exactName;
		this.mandatory = mandatory;
		this.mandatoryValue = ~mandatoryValue;
		this.value = value;
		this.hash = hash;
	}

	static <T> DecodePropertyInfo<T>[] prepare(DecodePropertyInfo<T>[] initial) {
		final DecodePropertyInfo<T>[] readers = initial.clone();
		final HashSet<Integer> hashes = new HashSet<Integer>();
		int mandatoryIndex = 0;
		for (int i = 0; i < readers.length; i++) {
			DecodePropertyInfo<T> ri = readers[i];
			if (!hashes.add(ri.hash)) {
				for (int j = 0; j < readers.length; j++) {
					final DecodePropertyInfo si = readers[j];
					if (si.hash == ri.hash && !si.exactName) {
						readers[j] = new DecodePropertyInfo<>(ri.name, true, ri.mandatory, 0, ri.hash, ri.value);
					}
				}
			}
			if (ri.mandatory) {
				ri = readers[i];
				if (mandatoryIndex > 63) throw new SerializationException("Only up to 64 mandatory properties are supported");
				readers[i] = new DecodePropertyInfo<>(ri.name, ri.exactName, true, 1 << mandatoryIndex, ri.hash, ri.value);
				mandatoryIndex++;
			}
		}
		return readers;
	}
}