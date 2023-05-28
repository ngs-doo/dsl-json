package com.dslplatform.json;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

public abstract class NetConverter {

	private static final JsonReader.ReadObject<URI> URI_READER = new JsonReader.ReadObject<URI>() {
		@Nullable
		@Override
		public URI read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : deserializeUri(reader);
		}
	};
	private static final JsonReader.ReadObject<InetAddress> ADDRESS_READER = new JsonReader.ReadObject<InetAddress>() {
		@Nullable
		@Override
		public InetAddress read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : deserializeIp(reader);
		}
	};

	static <T> void registerDefault(DslJson<T> json) {
		json.registerReader(URI.class, URI_READER);
		json.registerWriter(URI.class, (writer, value) -> serializeNullable(value, writer));
		json.registerReader(InetAddress.class, ADDRESS_READER);
		json.registerWriter(InetAddress.class, (writer, value) -> serializeNullable(value, writer));
	}

	public static void serializeNullable(@Nullable final URI value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serialize(value, sw);
		}
	}

	public static void serialize(final URI value, final JsonWriter sw) {
		StringConverter.serializeShort(value.toString(), sw);
	}

	public static URI deserializeUri(final JsonReader reader) throws IOException {
		return URI.create(reader.readString());
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<URI> deserializeUriCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(URI_READER);
	}

	public static void deserializeUriCollection(final JsonReader reader, final Collection<URI> res) throws IOException {
		reader.deserializeCollection(URI_READER, res);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<URI> deserializeUriNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(URI_READER);
	}

	public static void deserializeUriNullableCollection(final JsonReader reader, final Collection<URI> res) throws IOException {
		reader.deserializeNullableCollection(URI_READER, res);
	}

	public static void serializeNullable(@Nullable final InetAddress value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serialize(value, sw);
		}
	}

	public static void serialize(final InetAddress value, final JsonWriter sw) {
		sw.writeByte(JsonWriter.QUOTE);
		sw.writeAscii(value.getHostAddress());
		sw.writeByte(JsonWriter.QUOTE);
	}

	public static InetAddress deserializeIp(final JsonReader reader) throws IOException {
		return InetAddress.getByName(reader.readSimpleString());
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<InetAddress> deserializeIpCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(ADDRESS_READER);
	}

	public static void deserializeIpCollection(final JsonReader reader, final Collection<InetAddress> res) throws IOException {
		reader.deserializeCollection(ADDRESS_READER, res);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<InetAddress> deserializeIpNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(ADDRESS_READER);
	}

	public static void deserializeIpNullableCollection(final JsonReader reader, final Collection<InetAddress> res) throws IOException {
		reader.deserializeNullableCollection(ADDRESS_READER, res);
	}
}
