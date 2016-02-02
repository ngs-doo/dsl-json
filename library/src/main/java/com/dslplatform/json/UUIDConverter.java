package com.dslplatform.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public abstract class UUIDConverter {

	public static final UUID MIN_UUID = new java.util.UUID(0L, 0L);
	static final JsonReader.ReadObject<UUID> Reader = new JsonReader.ReadObject<UUID>() {
		@Override
		public UUID read(JsonReader reader) throws IOException {
			return deserialize(reader);
		}
	};
	static final JsonWriter.WriteObject<UUID> Writer = new JsonWriter.WriteObject<UUID>() {
		@Override
		public void write(JsonWriter writer, UUID value) {
			serializeNullable(value, writer);
		}
	};

	private static final char[] Lookup;
	private static final byte[] Values;

	static {
		Lookup = new char[256];
		Values = new byte['f' + 1 - '0'];
		for (int i = 0; i < 256; i++) {
			int hi = (i >> 4) & 15;
			int lo = i & 15;
			Lookup[i] = (char) (((hi < 10 ? '0' + hi : 'a' + hi - 10) << 8) + (lo < 10 ? '0' + lo : 'a' + lo - 10));
		}
		for (char c = '0'; c <= '9'; c++) {
			Values[c - '0'] = (byte) (c - '0');
		}
		for (char c = 'a'; c <= 'f'; c++) {
			Values[c - '0'] = (byte) (c - 'a' + 10);
		}
		for (char c = 'A'; c <= 'F'; c++) {
			Values[c - '0'] = (byte) (c - 'A' + 10);
		}
	}


	public static void serializeNullable(final UUID value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serialize(value, sw);
		}
	}

	public static void serialize(final UUID value, final JsonWriter sw) {
		final long hi = value.getMostSignificantBits();
		final long lo = value.getLeastSignificantBits();
		final int hi1 = (int) (hi >> 32);
		final int hi2 = (int) hi;
		final int lo1 = (int) (lo >> 32);
		final int lo2 = (int) lo;
		final byte[] buf = sw.ensureCapacity(38);
		final int pos = sw.size();
		buf[pos] = '"';
		int v = (hi1 >> 24) & 255;
		int l = Lookup[v];
		buf[pos + 1] = (byte) (l >> 8);
		buf[pos + 2] = (byte) l;
		v = (hi1 >> 16) & 255;
		l = Lookup[v];
		buf[pos + 3] = (byte) (l >> 8);
		buf[pos + 4] = (byte) l;
		v = (hi1 >> 8) & 255;
		l = Lookup[v];
		buf[pos + 5] = (byte) (l >> 8);
		buf[pos + 6] = (byte) l;
		v = hi1 & 255;
		l = Lookup[v];
		buf[pos + 7] = (byte) (l >> 8);
		buf[pos + 8] = (byte) l;
		buf[pos + 9] = '-';
		v = (hi2 >> 24) & 255;
		l = Lookup[v];
		buf[pos + 10] = (byte) (l >> 8);
		buf[pos + 11] = (byte) l;
		v = (hi2 >> 16) & 255;
		l = Lookup[v];
		buf[pos + 12] = (byte) (l >> 8);
		buf[pos + 13] = (byte) l;
		buf[pos + 14] = '-';
		v = (hi2 >> 8) & 255;
		l = Lookup[v];
		buf[pos + 15] = (byte) (l >> 8);
		buf[pos + 16] = (byte) l;
		v = hi2 & 255;
		l = Lookup[v];
		buf[pos + 17] = (byte) (l >> 8);
		buf[pos + 18] = (byte) l;
		buf[pos + 19] = '-';
		v = (lo1 >> 24) & 255;
		l = Lookup[v];
		buf[pos + 20] = (byte) (l >> 8);
		buf[pos + 21] = (byte) l;
		v = (lo1 >> 16) & 255;
		l = Lookup[v];
		buf[pos + 22] = (byte) (l >> 8);
		buf[pos + 23] = (byte) l;
		buf[pos + 24] = '-';
		v = (lo1 >> 8) & 255;
		l = Lookup[v];
		buf[pos + 25] = (byte) (l >> 8);
		buf[pos + 26] = (byte) l;
		v = lo1 & 255;
		l = Lookup[v];
		buf[pos + 27] = (byte) (l >> 8);
		buf[pos + 28] = (byte) l;
		v = (lo2 >> 24) & 255;
		l = Lookup[v];
		buf[pos + 29] = (byte) (l >> 8);
		buf[pos + 30] = (byte) l;
		v = (lo2 >> 16) & 255;
		l = Lookup[v];
		buf[pos + 31] = (byte) (l >> 8);
		buf[pos + 32] = (byte) l;
		v = (lo2 >> 8) & 255;
		l = Lookup[v];
		buf[pos + 33] = (byte) (l >> 8);
		buf[pos + 34] = (byte) l;
		v = lo2 & 255;
		l = Lookup[v];
		buf[pos + 35] = (byte) (l >> 8);
		buf[pos + 36] = (byte) l;
		buf[pos + 37] = '"';
		sw.advance(38);
	}

	public static UUID deserialize(final JsonReader reader) throws IOException {
		final char[] buf = reader.readSimpleQuote();
		final int len = reader.getCurrentIndex() - reader.getTokenStart();
		if (len == 37 && buf[8] == '-' && buf[13] == '-' && buf[18] == '-' && buf[23] == '-') {
			try {
				long hi = 0;
				for (int i = 0; i < 8; i++)
					hi = (hi << 4) + Values[buf[i] - '0'];
				for (int i = 9; i < 13; i++)
					hi = (hi << 4) + Values[buf[i] - '0'];
				for (int i = 14; i < 18; i++)
					hi = (hi << 4) + Values[buf[i] - '0'];
				long lo = 0;
				for (int i = 19; i < 23; i++)
					lo = (lo << 4) + Values[buf[i] - '0'];
				for (int i = 24; i < 36; i++)
					lo = (lo << 4) + Values[buf[i] - '0'];
				return new UUID(hi, lo);
			} catch (ArrayIndexOutOfBoundsException ex) {
				return UUID.fromString(new String(buf, 0, 36));
			}
		} else if (len == 33) {
			try {
				long hi = 0;
				for (int i = 0; i < 16; i++)
					hi = (hi << 4) + Values[buf[i] - '0'];
				long lo = 0;
				for (int i = 16; i < 32; i++)
					lo = (lo << 4) + Values[buf[i] - '0'];
				return new UUID(hi, lo);
			} catch (ArrayIndexOutOfBoundsException ex) {
				return UUID.fromString(new String(buf, 0, 32));
			}
		} else {
			return UUID.fromString(new String(buf, 0, len - 1));
		}
	}

	public static ArrayList<UUID> deserializeCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(Reader);
	}

	public static void deserializeCollection(final JsonReader reader, final Collection<UUID> res) throws IOException {
		reader.deserializeCollection(Reader, res);
	}

	public static ArrayList<UUID> deserializeNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(Reader);
	}

	public static void deserializeNullableCollection(final JsonReader reader, final Collection<UUID> res) throws IOException {
		reader.deserializeNullableCollection(Reader, res);
	}
}
