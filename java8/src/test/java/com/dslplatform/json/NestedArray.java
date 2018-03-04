package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import com.dslplatform.json.runtime.TypeDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class NestedArray {

	private final DslJson<Object> json = new DslJson<Object>(Settings.withRuntime());

	public static class Example {
		public int[][] arrArrInt;
		public Long[][] arrArrLong;
		public Object[][] arrArrUnknown;
	}

	public static class Generic<T> {
		public T[] arrT;
		public T[][] arrArrT;
	}

	@Test
	public void testResolution() throws IOException {
		Example wo = new Example();
		wo.arrArrInt = new int[][] { new int[] { 1, 2 }, new int[] {}, null, new int[]{ 3 }};
		wo.arrArrLong = new Long[][] { new Long[] { 1L }, new Long[] {}, null, new Long[]{ 2L, 3L }};
		wo.arrArrUnknown = new Object[0][];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(wo, baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Example wo2 = json.deserialize(Example.class, bais);
		Assert.assertArrayEquals(wo.arrArrInt, wo2.arrArrInt);
		Assert.assertArrayEquals(wo.arrArrLong, wo2.arrArrLong);
		Assert.assertArrayEquals(wo.arrArrUnknown, wo2.arrArrUnknown);
	}

	@Test
	public void testGeneric() throws IOException {
		Generic<Long> wo = new Generic<>();
		wo.arrT = new Long[]{ 1L, null, 2L, 3L };
		wo.arrArrT = new Long[][] { new Long[] { 1L }, new Long[] {}, null, new Long[]{ 2L, 3L }};
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(wo, baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Generic<Long> wo2 = (Generic<Long>) json.deserialize(new TypeDefinition<Generic<Long>>() {}.type, bais);
		Assert.assertArrayEquals(wo.arrT, wo2.arrT);
		Assert.assertArrayEquals(wo.arrArrT, wo2.arrArrT);
	}

}
