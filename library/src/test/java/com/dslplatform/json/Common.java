package com.dslplatform.json;

import org.junit.Assert;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public abstract class Common {
	private static byte[] buffer = new byte[64];

	public static <T> T deserialize(DslJson<Object> json, Class<T> target, byte[] bytes, int size) throws IOException {
		T t1 = json.deserialize(target, bytes, size);
		T t2 = json.deserialize(target, new ByteArrayInputStream(bytes, 0, size), buffer);
		Assert.assertEquals(t1, t2);
		return t1;
	}
}
