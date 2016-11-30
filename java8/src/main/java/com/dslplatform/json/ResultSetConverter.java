package com.dslplatform.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public abstract class ResultSetConverter {

	static final JsonWriter.WriteObject<ResultSet> Writer = (writer, value) -> {
		if (value == null) writer.writeNull();
		else {
			try {
				serialize(value, writer, null);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	};

	public static <T> void serialize(
			final ResultSet rs,
			final JsonWriter sw,
			final OutputStream stream) throws SQLException, IOException {
		if (sw == null && stream == null) throw new IOException("Either JsonWriter or OutputStream must be non-null");
		final JsonWriter buffer = sw == null ? new JsonWriter() : sw;
		final ResultSetMetaData metadata = rs.getMetaData();
		final int columns = metadata.getColumnCount();
		if (columns == 0) throw new IOException("No columns found in ResultSet");
		final Writer[] writers = new Writer[columns];
		for (int i = 0; i < writers.length; i++) {
			final Writer wrt = writers[i] = createWriter(metadata, i + 1);
			if (wrt == null) throw new IOException("Unable to find Writer for column " + i);
		}
		serialize(rs, stream, buffer, writers);
	}

	public static void serialize(
			final ResultSet rs,
			final OutputStream stream,
			final JsonWriter sw,
			final Writer[] writers) throws SQLException, IOException {
		if (stream != null) {
			sw.reset();
		}
		sw.writeByte((byte) '[');
		if (rs.next()) {
			sw.writeByte((byte) '[');
			writers[0].write(rs, sw);
			for (int i = 1; i < writers.length; i++) {
				sw.writeByte((byte) ',');
				writers[i].write(rs, sw);
			}
		} else {
			sw.writeByte((byte) ']');
			if (stream != null) {
				sw.toStream(stream);
			}
			return;
		}
		while (rs.next()) {
			sw.writeAscii("],[", 3);
			writers[0].write(rs, sw);
			for (int i = 1; i < writers.length; i++) {
				sw.writeByte((byte) ',');
				writers[i].write(rs, sw);
			}
			if (stream != null) {
				sw.toStream(stream);
				sw.reset();
			}
		}
		sw.writeAscii("]]");
		if (stream != null) {
			sw.toStream(stream);
		}
	}

	@FunctionalInterface
	public interface Writer {
		void write(ResultSet rs, JsonWriter buffer) throws SQLException;
	}

	public static <T> Writer createWriter(
			final ResultSetMetaData metaData,
			final int index) throws SQLException {
		switch (metaData.getColumnType(index)) {
			case Types.ARRAY:
				return null;
			case Types.BIGINT:
				return metaData.isNullable(index) == ResultSetMetaData.columnNoNulls
						? (rs, buffer) -> NumberConverter.serialize(rs.getLong(index), buffer)
						: (rs, buffer) -> {
					final long value = rs.getLong(index);
					if (rs.wasNull()) buffer.writeNull();
					else NumberConverter.serialize(value, buffer);
				};
			case Types.VARBINARY:
			case Types.BINARY:
			case Types.LONGVARBINARY:
			case Types.BLOB:
				return metaData.isNullable(index) == ResultSetMetaData.columnNoNulls
						? (rs, buffer) -> buffer.writeBinary(rs.getBytes(index))
						: (rs, buffer) -> {
					final byte[] value = rs.getBytes(index);
					if (value == null) buffer.writeNull();
					else buffer.writeBinary(value);
				};
			case Types.BIT:
			case Types.BOOLEAN:
				return metaData.isNullable(index) == ResultSetMetaData.columnNoNulls
						? (rs, buffer) -> BoolConverter.serialize(rs.getBoolean(index), buffer)
						: (rs, buffer) -> {
					final boolean value = rs.getBoolean(index);
					if (rs.wasNull()) buffer.writeNull();
					else BoolConverter.serialize(value, buffer);
				};
			case Types.DATALINK:
				return null;
			case Types.DATE:
				return metaData.isNullable(index) == ResultSetMetaData.columnNoNulls
						? (rs, buffer) -> JavaTimeConverter.serialize(rs.getDate(index).toLocalDate(), buffer)
						: (rs, buffer) -> {
					java.sql.Date date = rs.getDate(index);
					if (date == null) buffer.writeNull();
					else JavaTimeConverter.serialize(date.toLocalDate(), buffer);
				};
			case Types.DECIMAL:
			case Types.NUMERIC:
				return metaData.isNullable(index) == ResultSetMetaData.columnNoNulls
						? (rs, buffer) -> NumberConverter.serialize(rs.getBigDecimal(index), buffer)
						: (rs, buffer) -> NumberConverter.serializeNullable(rs.getBigDecimal(index), buffer);
			case Types.DISTINCT:
				return null;
			case Types.DOUBLE:
				return metaData.isNullable(index) == ResultSetMetaData.columnNoNulls
						? (rs, buffer) -> NumberConverter.serialize(rs.getDouble(index), buffer)
						: (rs, buffer) -> {
					final double value = rs.getDouble(index);
					if (rs.wasNull()) buffer.writeNull();
					else NumberConverter.serialize(value, buffer);
				};
			case Types.REAL:
			case Types.FLOAT:
				return metaData.isNullable(index) == ResultSetMetaData.columnNoNulls
						? (rs, buffer) -> NumberConverter.serialize(rs.getFloat(index), buffer)
						: (rs, buffer) -> {
					final float value = rs.getFloat(index);
					if (rs.wasNull()) buffer.writeNull();
					else NumberConverter.serialize(value, buffer);
				};
			case Types.TINYINT:
			case Types.SMALLINT:
				return metaData.isNullable(index) == ResultSetMetaData.columnNoNulls
						? (rs, buffer) -> NumberConverter.serialize(rs.getShort(index), buffer)
						: (rs, buffer) -> {
					final int value = rs.getShort(index);
					if (rs.wasNull()) buffer.writeNull();
					else NumberConverter.serialize(value, buffer);
				};
			case Types.INTEGER:
				return metaData.isNullable(index) == ResultSetMetaData.columnNoNulls
						? (rs, buffer) -> NumberConverter.serialize(rs.getInt(index), buffer)
						: (rs, buffer) -> {
					final int value = rs.getInt(index);
					if (rs.wasNull()) buffer.writeNull();
					else NumberConverter.serialize(value, buffer);
				};
			case Types.JAVA_OBJECT:
				return null;
			case Types.NULL:
				return (rs, buffer) -> buffer.writeNull();
			case Types.NCLOB:
			case Types.LONGNVARCHAR:
			case Types.NCHAR:
			case Types.NVARCHAR:
				return metaData.isNullable(index) == ResultSetMetaData.columnNoNulls
						? (rs, buffer) -> buffer.writeString(rs.getNString(index))
						: (rs, buffer) -> StringConverter.serializeNullable(rs.getNString(index), buffer);
			case Types.CLOB:
			case Types.LONGVARCHAR:
			case Types.CHAR:
			case Types.VARCHAR:
				return metaData.isNullable(index) == ResultSetMetaData.columnNoNulls
						? (rs, buffer) -> buffer.writeString(rs.getString(index))
						: (rs, buffer) -> StringConverter.serializeNullable(rs.getString(index), buffer);
			case Types.OTHER:
				return null;
			case Types.REF:
				return null;
			case Types.REF_CURSOR:
				return null;
			case Types.ROWID:
				return null;
			case Types.SQLXML:
				return (rs, buffer) -> {
					final SQLXML xml = rs.getSQLXML(index);
					if (xml == null) buffer.writeNull();
					else {
						StringConverter.serialize(xml.getString(), buffer);
						xml.free();
					}
				};
			case Types.STRUCT:
				return null;
			case Types.TIME:
				return null;
			case Types.TIME_WITH_TIMEZONE:
				return null;
			case Types.TIMESTAMP:
			case Types.TIMESTAMP_WITH_TIMEZONE:
				final ZoneId zone = ZoneId.systemDefault();
				return metaData.isNullable(index) == ResultSetMetaData.columnNoNulls
						? (rs, buffer) -> JavaTimeConverter.serialize(OffsetDateTime.ofInstant(rs.getTimestamp(index).toInstant(), zone), buffer)
						: (rs, buffer) -> {
					java.sql.Timestamp timestamp = rs.getTimestamp(index);
					if (timestamp == null) buffer.writeNull();
					else JavaTimeConverter.serialize(OffsetDateTime.ofInstant(timestamp.toInstant(), zone), buffer);
				};
			default:
				return null;
		}
	}
}
