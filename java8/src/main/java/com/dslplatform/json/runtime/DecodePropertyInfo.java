package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.SerializationException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class DecodePropertyInfo<T> {

	private static final Charset utf8 = Charset.forName("UTF-8");

	public final String name;
	public final int hash;
	public final int weakHash;
	public final boolean exactName;
	public final boolean mandatory;
	public final int index;
	public final boolean nonNull;
	public final T value;
	final long mandatoryValue;
	final byte[] nameBytes;

	public DecodePropertyInfo(String name, boolean exactName, boolean mandatory, int index, boolean nonNull, T value) {
		this(name, exactName, mandatory, 0, index, nonNull, calcHash(name), calcWeakHash(name), value, name.getBytes(utf8));
	}

	static int calcHash(String name) {
		long hash = 0x811c9dc5;
		for (int x = 0; x < name.length(); x++) {
			hash ^= (byte) name.charAt(x);
			hash *= 0x1000193;
		}
		return (int) hash;
	}

	static int calcWeakHash(String name) {
		int hash = 0;
		for (int x = 0; x < name.length(); x++) {
			hash += (byte) name.charAt(x);
		}
		return hash;
	}

	private DecodePropertyInfo(String name, boolean exactName, boolean mandatory, long mandatoryValue, int index, boolean nonNull, int hash, int weakHash, T value, byte[] nameBytes) {
		this.name = name;
		this.exactName = exactName;
		this.mandatory = mandatory;
		this.mandatoryValue = mandatoryValue;
		this.index = index;
		this.nonNull = nonNull;
		this.hash = hash;
		this.weakHash = weakHash;
		this.value = value;
		this.nameBytes = nameBytes;
	}

	static <T> DecodePropertyInfo<T>[] prepare(DecodePropertyInfo<T>[] initial) {
		final DecodePropertyInfo<T>[] decoders = initial.clone();
		final HashSet<Integer> hashes = new HashSet<Integer>();
		int mandatoryIndex = 0;
		boolean needsSorting = false;
		for (int i = 0; i < decoders.length; i++) {
			DecodePropertyInfo<T> ri = decoders[i];
			if (!hashes.add(ri.hash)) {
				for (int j = 0; j < decoders.length; j++) {
					final DecodePropertyInfo si = decoders[j];
					if (si.hash == ri.hash && !si.exactName) {
						decoders[j] = new DecodePropertyInfo<>(ri.name, true, ri.mandatory, ~0, ri.index, ri.nonNull, ri.hash, ri.weakHash, ri.value, ri.nameBytes);
					}
				}
			}
			if (ri.mandatory) {
				ri = decoders[i];
				if (mandatoryIndex > 63) {
					throw new SerializationException("Only up to 64 mandatory properties are supported");
				}
				decoders[i] = new DecodePropertyInfo<>(ri.name, ri.exactName, true, ~(1 << mandatoryIndex), ri.index, ri.nonNull, ri.hash, ri.weakHash, ri.value, ri.nameBytes);
				mandatoryIndex++;
			}
			needsSorting = needsSorting || ri.index >= 0;
		}
		if (needsSorting) {
			Arrays.sort(decoders, (a, b) -> {
				if (b.index == -1) return -1;
				else if (a.index == -1) return 1;
				return a.index - b.index;
			});
		}
		final HashMap<String, Integer> nameOrder = new HashMap<>();
		for (int i = 0; i < decoders.length; i++) {
			final DecodePropertyInfo<T> ri = decoders[i];
			Integer index = nameOrder.get(ri.name);
			if (index == null) {
				index = nameOrder.size();
				nameOrder.put(ri.name, index);
			}
			decoders[i] = new DecodePropertyInfo<>(ri.name, ri.exactName, ri.mandatory, ri.mandatoryValue, index, ri.nonNull, ri.hash, ri.weakHash, ri.value, ri.nameBytes);
		}
		return decoders;
	}

	static long calculateMandatory(DecodePropertyInfo[] decoders) {
		long flag = 0;
		for (DecodePropertyInfo dp : decoders) {
			if (dp.mandatory) {
				flag = flag | ~dp.mandatoryValue;
			}
		}
		return flag;
	}

	static void showMandatoryError(
			final JsonReader reader,
			final long mandatoryFlag,
			final DecodePropertyInfo[] decoders) throws IOException {
		final StringBuilder sb = new StringBuilder("Mandatory ");
		sb.append(Long.bitCount(mandatoryFlag) == 1 ? "property" : "properties");
		sb.append(" (");
		for (final DecodePropertyInfo ri : decoders) {
			if (ri.mandatory && (mandatoryFlag & ~ri.mandatoryValue) != 0) {
				sb.append(ri.name).append(", ");
			}
		}
		sb.setLength(sb.length() - 2);
		sb.append(") not found ");
		sb.append(reader.positionDescription());
		throw new IOException(sb.toString());
	}
}