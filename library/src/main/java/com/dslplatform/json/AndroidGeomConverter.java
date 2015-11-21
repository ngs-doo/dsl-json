package com.dslplatform.json;

import android.graphics.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public abstract class AndroidGeomConverter {

	static final JsonReader.ReadObject<PointF> LocationReader = new JsonReader.ReadObject<PointF>() {
		@Override
		public PointF read(JsonReader reader) throws IOException {
			return deserializeLocation(reader);
		}
	};
	static final JsonWriter.WriteObject<PointF> LocationWriter = new JsonWriter.WriteObject<PointF>() {
		@Override
		public void write(JsonWriter writer, PointF value) {
			serializeLocationNullable(value, writer);
		}
	};
	static final JsonReader.ReadObject<Point> PointReader = new JsonReader.ReadObject<Point>() {
		@Override
		public Point read(JsonReader reader) throws IOException {
			return deserializePoint(reader);
		}
	};
	static final JsonWriter.WriteObject<Point> PointWriter = new JsonWriter.WriteObject<Point>() {
		@Override
		public void write(JsonWriter writer, Point value) {
			serializePointNullable(value, writer);
		}
	};
	static final JsonReader.ReadObject<Rect> RectangleReader = new JsonReader.ReadObject<Rect>() {
		@Override
		public Rect read(JsonReader reader) throws IOException {
			return deserializeRectangle(reader);
		}
	};
	static final JsonWriter.WriteObject<Rect> RectangleWriter = new JsonWriter.WriteObject<Rect>() {
		@Override
		public void write(JsonWriter writer, Rect value) {
			serializeRectangleNullable(value, writer);
		}
	};
	static final JsonReader.ReadObject<Bitmap> ImageReader = new JsonReader.ReadObject<Bitmap>() {
		@Override
		public Bitmap read(JsonReader reader) throws IOException {
			return deserializeImage(reader);
		}
	};
	static final JsonWriter.WriteObject<Bitmap> ImageWriter = new JsonWriter.WriteObject<Bitmap>() {
		@Override
		public void write(JsonWriter writer, Bitmap value) {
			serialize(value, writer);
		}
	};

	public static void serializeLocationNullable(final PointF value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serializeLocation(value, sw);
		}
	}

	public static void serializeLocation(final PointF value, final JsonWriter sw) {
		sw.writeAscii("{\"X\":");
		NumberConverter.serialize(value.x, sw);
		sw.writeAscii(",\"Y\":");
		NumberConverter.serialize(value.y, sw);
		sw.writeByte(JsonWriter.OBJECT_END);
	}

	public static PointF deserializeLocation(final JsonReader reader) throws IOException {
		if (reader.last() != '{') throw new IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char)reader.last());
		byte nextToken = reader.getNextToken();
		if (nextToken == '}') return new PointF();
		float x = 0;
		float y = 0;
		String name = StringConverter.deserialize(reader);
		nextToken = reader.getNextToken();
		if (nextToken != ':') throw new IOException("Expecting ':' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
		reader.getNextToken();
		float value = NumberConverter.deserializeFloat(reader);
		if ("X".equalsIgnoreCase(name)) {
			x = value;
		} else if ("Y".equalsIgnoreCase(name)) {
			y = value;
		}
		while ((nextToken = reader.getNextToken()) == ',') {
			reader.getNextToken();
			name = StringConverter.deserialize(reader);
			nextToken = reader.getNextToken();
			if (nextToken != ':') throw new IOException("Expecting ':' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
			reader.getNextToken();
			value = NumberConverter.deserializeFloat(reader);
			if ("X".equalsIgnoreCase(name)) {
				x = value;
			} else if ("Y".equalsIgnoreCase(name)) {
				y = value;
			}
		}
		if (nextToken != '}') throw new IOException("Expecting '}' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
		return new PointF(x, y);
	}

	public static ArrayList<PointF> deserializeLocationCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(LocationReader);
	}

	public static void deserializeLocationCollection(final JsonReader reader, final Collection<PointF> res) throws IOException {
		reader.deserializeCollection(LocationReader, res);
	}

	public static ArrayList<PointF> deserializeLocationNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(LocationReader);
	}

	public static void deserializeLocationNullableCollection(final JsonReader reader, final Collection<PointF> res) throws IOException {
		reader.deserializeNullableCollection(LocationReader, res);
	}

	public static void serializePointNullable(final Point value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serializePoint(value, sw);
		}
	}

	public static void serializePoint(final Point value, final JsonWriter sw) {
		sw.writeAscii("{\"X\":");
		NumberConverter.serialize(value.x, sw);
		sw.writeAscii(",\"Y\":");
		NumberConverter.serialize(value.y, sw);
		sw.writeByte(JsonWriter.OBJECT_END);
	}

	public static Point deserializePoint(final JsonReader reader) throws IOException {
		if (reader.last() != '{') throw new IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char)reader.last());
		byte nextToken = reader.getNextToken();
		if (nextToken == '}') return new Point();
		int x = 0;
		int y = 0;
		String name = StringConverter.deserialize(reader);
		nextToken = reader.getNextToken();
		if (nextToken != ':') throw new IOException("Expecting ':' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
		reader.getNextToken();
		int value = NumberConverter.deserializeInt(reader);
		if ("X".equalsIgnoreCase(name)) {
			x = value;
		} else if ("Y".equalsIgnoreCase(name)) {
			y = value;
		}
		while ((nextToken = reader.getNextToken()) == ',') {
			reader.getNextToken();
			name = StringConverter.deserialize(reader);
			nextToken = reader.getNextToken();
			if (nextToken != ':') throw new IOException("Expecting ':' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
			reader.getNextToken();
			value = NumberConverter.deserializeInt(reader);
			if ("X".equalsIgnoreCase(name)) {
				x = value;
			} else if ("Y".equalsIgnoreCase(name)) {
				y = value;
			}
		}
		if (nextToken != '}') throw new IOException("Expecting '}' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
		return new Point(x, y);

	}

	public static ArrayList<Point> deserializePointCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(PointReader);
	}

	public static void deserializePointCollection(final JsonReader reader, final Collection<Point> res) throws IOException {
		reader.deserializeCollection(PointReader, res);
	}

	public static ArrayList<Point> deserializePointNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(PointReader);
	}

	public static void deserializePointNullableCollection(final JsonReader reader, final Collection<Point> res) throws IOException {
		reader.deserializeNullableCollection(PointReader, res);
	}

	public static void serializeRectangleNullable(final Rect value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serializeRectangle(value, sw);
		}
	}

	public static void serializeRectangle(final Rect value, final JsonWriter sw) {
		sw.writeAscii("{\"X\":");
		NumberConverter.serialize(value.left, sw);
		sw.writeAscii(",\"Y\":");
		NumberConverter.serialize(value.top, sw);
		sw.writeAscii(",\"Width\":");
		NumberConverter.serialize(value.width(), sw);
		sw.writeAscii(",\"Height\":");
		NumberConverter.serialize(value.height(), sw);
		sw.writeByte(JsonWriter.OBJECT_END);
	}

	public static Rect deserializeRectangle(final JsonReader reader) throws IOException {
		if (reader.last() != '{') throw new IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char)reader.last());
		byte nextToken = reader.getNextToken();
		if (nextToken == '}') return new Rect();
		int x = 0;
		int y = 0;
		int width = 0;
		int height = 0;
		String name = StringConverter.deserialize(reader);
		nextToken = reader.getNextToken();
		if (nextToken != ':') throw new IOException("Expecting ':' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
		reader.getNextToken();
		int value = NumberConverter.deserializeInt(reader);
		if ("X".equalsIgnoreCase(name)) {
			x = value;
		} else if ("Y".equalsIgnoreCase(name)) {
			y = value;
		} else if ("Width".equalsIgnoreCase(name)) {
			width = value;
		} else if ("Height".equalsIgnoreCase(name)) {
			height = value;
		}
		while ((nextToken = reader.getNextToken()) == ',') {
			reader.getNextToken();
			name = StringConverter.deserialize(reader);
			nextToken = reader.getNextToken();
			if (nextToken != ':') throw new IOException("Expecting ':' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
			reader.getNextToken();
			value = NumberConverter.deserializeInt(reader);
			if ("X".equalsIgnoreCase(name)) {
				x = value;
			} else if ("Y".equalsIgnoreCase(name)) {
				y = value;
			} else if ("Width".equalsIgnoreCase(name)) {
				width = value;
			} else if ("Height".equalsIgnoreCase(name)) {
				height = value;
			}
		}
		if (nextToken != '}') throw new IOException("Expecting '}' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
		return new Rect(x, y, x + width, y + height);
	}

	public static ArrayList<Rect> deserializeRectangleCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(RectangleReader);
	}

	public static void deserializeRectangleCollection(final JsonReader reader, final Collection<Rect> res) throws IOException {
		reader.deserializeCollection(RectangleReader, res);
	}

	public static ArrayList<Rect> deserializeRectangleNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(RectangleReader);
	}

	public static void deserializeRectangleNullableCollection(final JsonReader reader, final Collection<Rect> res) throws IOException {
		reader.deserializeNullableCollection(RectangleReader, res);
	}

	public static void serialize(final Bitmap value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			final ByteArrayOutputStream stream = new ByteArrayOutputStream(value.getByteCount());
			value.compress(Bitmap.CompressFormat.PNG, 100, stream);
			BinaryConverter.serialize(stream.toByteArray(), sw);
		}
	}

	public static Bitmap deserializeImage(final JsonReader reader) throws IOException {
		final byte[] content = BinaryConverter.deserialize(reader);
		return BitmapFactory.decodeByteArray(content, 0, content.length);
	}

	public static ArrayList<Bitmap> deserializeImageCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(ImageReader);
	}

	public static void deserializeImageCollection(final JsonReader reader, final Collection<Bitmap> res) throws IOException {
		reader.deserializeCollection(ImageReader, res);
	}

	public static ArrayList<Bitmap> deserializeImageNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(ImageReader);
	}

	public static void deserializeImageNullableCollection(final JsonReader reader, final Collection<Bitmap> res) throws IOException {
		reader.deserializeNullableCollection(ImageReader, res);
	}
}
