package com.dslplatform.json;

import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;

public class ResultSetConverter implements JsonWriter.WriteObject<ResultSet> {

	private final DslJson dslJson;
	private final boolean writeNames;
	private final boolean writeTypes;
	private final ZoneId zone;

	public ResultSetConverter(DslJson dslJson) {
		this(dslJson, true, true, ZoneId.systemDefault());
	}

	public ResultSetConverter(DslJson dslJson, boolean writeNames, boolean writeTypes, ZoneId zone) {
		this.dslJson = dslJson;
		this.writeNames = writeNames;
		this.writeTypes = writeTypes;
		this.zone = zone;
	}

	public void write(JsonWriter writer, @Nullable ResultSet rs) {
		if (rs == null) writer.writeNull();
		else {
			try {
				final ResultSetMetaData metadata = rs.getMetaData();
				final int columns = metadata.getColumnCount();
				if (columns == 0) throw new ConfigurationException("No columns found in ResultSet");
				final Writer[] writers = new Writer[columns];
				for (int i = 0; i < writers.length; i++) {
					final Writer wrt = writers[i] = createWriter(metadata, i + 1);
					if (wrt == null) throw new ConfigurationException("Unable to find Writer for column " + (i + 1) + "(" + metadata.getColumnName(i + 1) + ") of type :" +  getColumnType(metadata.getColumnType(i + 1)));
				}
				serialize(rs, writer, writers);
			} catch (SQLException e) {
				throw new SerializationException(e);
			}
		}
	}

	private static byte[] NextRow = "],[".getBytes(StandardCharsets.UTF_8);
	private static byte[] DoubleEnd = "]]".getBytes(StandardCharsets.UTF_8);

	private void serialize(
			final ResultSet rs,
			final JsonWriter buffer,
			final Writer[] writers) throws SQLException {
		final ResultSetMetaData metadata = rs.getMetaData();
		if (metadata.getColumnCount() != writers.length) {
			throw new ConfigurationException("Result set metadata column count mismatch. Expecting " + metadata.getColumnCount() + " writers");
		}
		buffer.writeByte(JsonWriter.ARRAY_START);
		if (writeNames) {
			buffer.writeByte(JsonWriter.ARRAY_START);
			if (writers.length > 0) {
				buffer.writeString(metadata.getColumnLabel(1));
				for (int i = 1; i < writers.length; i++) {
					buffer.writeByte(JsonWriter.COMMA);
					buffer.writeString(metadata.getColumnLabel(i + 1));
				}
			}
			buffer.writeByte(JsonWriter.ARRAY_END);
		}
		if (writeTypes) {
			if (writeNames) {
				buffer.writeByte(JsonWriter.COMMA);
			}
			buffer.writeByte(JsonWriter.ARRAY_START);
			if (writers.length > 0) {
				buffer.writeString(getColumnType(metadata.getColumnType(1)));
				for (int i = 1; i < writers.length; i++) {
					buffer.writeByte(JsonWriter.COMMA);
					buffer.writeString(getColumnType(metadata.getColumnType(i + 1)));
				}
			}
			buffer.writeByte(JsonWriter.ARRAY_END);
		}
		if (rs.next()) {
			if (writeNames || writeTypes) {
				buffer.writeByte(JsonWriter.COMMA);
			}
			buffer.writeByte(JsonWriter.ARRAY_START);
			writers[0].write(rs, buffer);
			for (int i = 1; i < writers.length; i++) {
				buffer.writeByte(JsonWriter.COMMA);
				writers[i].write(rs, buffer);
			}
		} else {
			buffer.writeByte(JsonWriter.ARRAY_END);
			return;
		}
		while (rs.next()) {
			buffer.writeAscii(NextRow);
			writers[0].write(rs, buffer);
			for (int i = 1; i < writers.length; i++) {
				buffer.writeByte(JsonWriter.COMMA);
				writers[i].write(rs, buffer);
			}
		}
		buffer.writeAscii(DoubleEnd);
	}

	public interface Writer {
		void write(ResultSet rs, JsonWriter buffer) throws SQLException;
	}

	private JsonWriter.WriteObject<BigDecimal> DecimalConverter;

	@Nullable
	public Writer createWriter(
			final ResultSetMetaData metaData,
			final int index) throws SQLException {
		switch (metaData.getColumnType(index)) {
			case Types.ARRAY:
				return (rs, buffer) -> {
					final Array value = rs.getArray(index);
					if (value == null) buffer.writeNull();
					else {
						try {
							final Object instance = value.getArray();
							if (instance == null) buffer.writeNull();
							else {
								final Class<?> manifest = instance.getClass();
								if (!dslJson.serialize(buffer, manifest, instance)) {
									throw new SerializationException("Unable to serialize result set for column " + index);
								}
							}
						} finally {
							value.free();
						}
					}
				};
			case Types.BIGINT:
				return (rs, buffer) -> {
					final long value = rs.getLong(index);
					if (rs.wasNull()) buffer.writeNull();
					else NumberConverter.serialize(value, buffer);
				};
			case Types.VARBINARY:
			case Types.BINARY:
			case Types.LONGVARBINARY:
			case Types.BLOB:
				return (rs, buffer) -> {
					final byte[] value = rs.getBytes(index);
					if (value == null) buffer.writeNull();
					else buffer.writeBinary(value);
				};
			case Types.BIT:
			case Types.BOOLEAN:
				return (rs, buffer) -> {
					final boolean value = rs.getBoolean(index);
					if (rs.wasNull()) buffer.writeNull();
					else BoolConverter.serialize(value, buffer);
				};
			case Types.DATALINK:
				return (rs, buffer) -> {
					final URL url = rs.getURL(index);
					if (url == null) buffer.writeNull();
					else buffer.writeString(url.toExternalForm()); //TODO: optimize
				};
			case Types.DATE:
				return (rs, buffer) -> {
					java.sql.Date date = rs.getDate(index);
					if (date == null) buffer.writeNull();
					else JavaTimeConverter.serialize(date.toLocalDate(), buffer);
				};
			case Types.DECIMAL:
			case Types.NUMERIC:
				if (DecimalConverter == null) {
					DecimalConverter = dslJson.tryFindWriter(BigDecimal.class);
				}
				return (rs, buffer) -> DecimalConverter.write(buffer, rs.getBigDecimal(index));
			case Types.DOUBLE:
				return (rs, buffer) -> {
					final double value = rs.getDouble(index);
					if (rs.wasNull()) buffer.writeNull();
					else NumberConverter.serialize(value, buffer);
				};
			case Types.REAL:
			case Types.FLOAT:
				return (rs, buffer) -> {
					final float value = rs.getFloat(index);
					if (rs.wasNull()) buffer.writeNull();
					else NumberConverter.serialize(value, buffer);
				};
			case Types.TINYINT:
			case Types.SMALLINT:
				return (rs, buffer) -> {
					final int value = rs.getShort(index);
					if (rs.wasNull()) buffer.writeNull();
					else NumberConverter.serialize(value, buffer);
				};
			case Types.INTEGER:
				return (rs, buffer) -> {
					final int value = rs.getInt(index);
					if (rs.wasNull()) buffer.writeNull();
					else NumberConverter.serialize(value, buffer);
				};
			case Types.NULL:
				return (rs, buffer) -> buffer.writeNull();
			case Types.NCLOB:
			case Types.LONGNVARCHAR:
			case Types.NCHAR:
			case Types.NVARCHAR:
				return (rs, buffer) -> StringConverter.serializeNullable(rs.getNString(index), buffer);
			case Types.CLOB:
			case Types.LONGVARCHAR:
			case Types.CHAR:
			case Types.VARCHAR:
				return (rs, buffer) -> StringConverter.serializeNullable(rs.getString(index), buffer);
			case Types.DISTINCT:
			case Types.JAVA_OBJECT:
			case Types.OTHER:
			case Types.STRUCT:
				return (rs, buffer) -> {
					final Object value = rs.getObject(index);
					buffer.serializeObject(value);
				};
			case Types.REF:
				return (rs, buffer) -> {
					final Ref value = rs.getRef(index);
					if (value == null) buffer.writeNull();
					else {
						final Object instance = value.getObject();
						if (instance == null) buffer.writeNull();
						else buffer.serializeObject(instance);
					}
				};
			case Types.REF_CURSOR:
				return null;
			case Types.ROWID:
				return (rs, buffer) -> {
					final RowId value = rs.getRowId(index);
					if (value == null) buffer.writeNull();
					else StringConverter.serialize(value.toString(), buffer);
				};
			case Types.SQLXML:
				return (rs, buffer) -> {
					final SQLXML xml = rs.getSQLXML(index);
					if (xml == null) buffer.writeNull();
					else {
						try {
							StringConverter.serialize(xml.getString(), buffer);
						} finally {
							xml.free();
						}
					}
				};
			case Types.TIME:
				return (rs, buffer) -> {
					final Time time = rs.getTime(index);
					if (time == null) buffer.writeNull();
					else JavaTimeConverter.serialize(time.toLocalTime(), buffer);
				};
			case Types.TIME_WITH_TIMEZONE:
				return (rs, buffer) -> {
					final Time time = rs.getTime(index);
					if (time == null) buffer.writeNull();
					else JavaTimeConverter.serialize(OffsetTime.ofInstant(time.toInstant(), zone), buffer);
				};
			case Types.TIMESTAMP:
				return (rs, buffer) -> {
					Timestamp timestamp = rs.getTimestamp(index);
					if (timestamp == null) buffer.writeNull();
					else JavaTimeConverter.serialize(timestamp.toLocalDateTime(), buffer);
				};
			case Types.TIMESTAMP_WITH_TIMEZONE:
				return (rs, buffer) -> {
					Timestamp timestamp = rs.getTimestamp(index);
					if (timestamp == null) buffer.writeNull();
					else JavaTimeConverter.serialize(OffsetDateTime.ofInstant(timestamp.toInstant(), zone), buffer);
				};
			default:
				return (rs, buffer) -> {
					final Object value = rs.getObject(index);
					buffer.serializeObject(value);
				};
		}
	}

	public static String getColumnType(int sqlType) {
		switch (sqlType) {
			case Types.ARRAY:
				return "Array";
			case Types.BIGINT:
				return "Long";
			case Types.VARBINARY:
			case Types.BINARY:
			case Types.LONGVARBINARY:
			case Types.BLOB:
				return "Binary";
			case Types.BIT:
			case Types.BOOLEAN:
				return "Boolean";
			case Types.DATE:
				return "Date";
			case Types.DECIMAL:
			case Types.NUMERIC:
				return "Decimal";
			case Types.DOUBLE:
				return "Double";
			case Types.REAL:
			case Types.FLOAT:
				return "Float";
			case Types.TINYINT:
			case Types.SMALLINT:
				return "Short";
			case Types.INTEGER:
				return "Int";
			case Types.NULL:
				return "Null";
			case Types.NCLOB:
			case Types.LONGNVARCHAR:
			case Types.NCHAR:
			case Types.NVARCHAR:
			case Types.CLOB:
			case Types.LONGVARCHAR:
			case Types.CHAR:
			case Types.VARCHAR:
				return "String";
			case Types.JAVA_OBJECT:
			case Types.OTHER:
			case Types.STRUCT:
				return "Unknown";
			case Types.REF:
			case Types.DISTINCT:
			case Types.REF_CURSOR:
			case Types.ROWID:
				return "Unknown";
			case Types.DATALINK:
				return "Url";
			case Types.SQLXML:
				return "SQL";
			case Types.TIME:
			case Types.TIME_WITH_TIMEZONE:
				return "Time";
			case Types.TIMESTAMP:
			case Types.TIMESTAMP_WITH_TIMEZONE:
				return "Timestamp";
			default:
				return "Unknown";
		}
	}
}
