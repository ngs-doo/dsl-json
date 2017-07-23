package com.dslplatform.json.generated.types;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.generated.ocd.javaasserts.*;
import org.junit.Assert;
import org.w3c.dom.Element;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class StaticJson {
	private static final JsonSerialization json = new JsonSerialization();

	public static JsonSerialization getSerialization() {
		return json;
	}

	public static class Bytes {
		public byte[] content;
		public int length;
	}

	public static class JsonSerialization extends DslJson<Object> {

		private final byte[] buffer = new byte[64];

		public JsonSerialization() {
			super(new Settings<Object>().withJavaConverters(true).includeServiceLoader());
		}

		private ByteArrayOutputStream stream = new ByteArrayOutputStream();

		public Bytes serialize(Object instance) throws IOException {
			stream.reset();
			super.serialize(instance, stream);
			Bytes b = new Bytes();
			b.content = stream.toByteArray();
			b.length = b.content.length;
			return b;
		}

		public <TResult> TResult deserialize(
				final Class<TResult> manifest,
				final byte[] body,
				final int size) throws IOException {
			TResult result1 = super.deserialize(manifest, body, size);
			TResult result2 = super.deserialize(manifest, new ByteArrayInputStream(body, 0, size), buffer);
			if (manifest.isArray()) {
				if (manifest.getComponentType() == double.class) {
					DoubleAsserts.assertNullableArrayOfOneEquals((double[]) result1, (double[]) result2);
				} else if (manifest.getComponentType() == float.class) {
					FloatAsserts.assertNullableArrayOfOneEquals((float[])result1, (float[])result2);
				} else if (manifest.getComponentType() == int.class) {
					IntegerAsserts.assertNullableArrayOfOneEquals((int[])result1, (int[])result2);
				} else if (manifest.getComponentType() == long.class) {
					LongAsserts.assertNullableArrayOfOneEquals((long[])result1, (long[])result2);
				} else if (manifest.getComponentType() == boolean.class) {
					BooleanAsserts.assertNullableArrayOfOneEquals((boolean[])result1, (boolean[])result2);
				} else if (manifest.getComponentType() == byte.class) {
					BinaryAsserts.assertNullableEquals((byte[])result1, (byte[])result2);
				} else if (manifest.getComponentType() == BufferedImage.class) {
					ImageAsserts.assertNullableArrayOfNullableEquals((BufferedImage[])result1, (BufferedImage[])result2);
				} else if (manifest.getComponentType() == Element.class) {
					XmlAsserts.assertNullableArrayOfNullableEquals((Element[])result1, (Element[])result2);
				} else {
					Assert.assertArrayEquals((Object[]) result1, (Object[]) result2);
				}
			} else if (manifest == BufferedImage.class) {
				ImageAsserts.assertNullableEquals((BufferedImage)result1, (BufferedImage)result2);
			} else if (manifest == Element.class) {
				XmlAsserts.assertNullableEquals((Element)result1, (Element)result2);
			} else {
				Assert.assertEquals(result1, result2);
			}
			return result1;
		}
	}
}
