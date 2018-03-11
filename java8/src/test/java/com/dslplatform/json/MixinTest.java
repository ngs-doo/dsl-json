package com.dslplatform.json;

import com.dslplatform.json.runtime.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class MixinTest {

	private final DslJson<Object> json = new DslJson<Object>(Settings.withRuntime());
	private final BeanDescription<Example, Example> beanDescription1 = BeanDescription.create(
			Example.class,
			Example::new,
			new JsonWriter.WriteObject[] {
					Settings.<Example, Integer>createEncoder(Iface::y, "y", json, int.class),
					Settings.<Example, Long>createEncoder(c -> c.x, "x", json, long.class)
			},
			new DecodePropertyInfo[] {
					Settings.<Example, Integer>createDecoder((c, v) -> c.y = v, "y", json, int.class),
					Settings.<Example, Long>createDecoder((c, v) -> c.x = v, "x", json, long.class)
			},
			true);
	private final MixinDescription<Iface> mixinDescription1 = new MixinDescription<>(
			Iface.class,
			new BeanDescription[] {beanDescription1}
	);
	private final BeanDescription<EmptyExample, EmptyExample> beanDescription2 = BeanDescription.create(
			EmptyExample.class,
			EmptyExample::new,
			new JsonWriter.WriteObject[0],
			new DecodePropertyInfo[0],
			true);
	private final MixinDescription<IEmpty> mixinDescription2 = new MixinDescription<>(
			IEmpty.class,
			new BeanDescription[] {beanDescription2}
	);
	public MixinTest() {
		json.registerReader(Example.class, beanDescription1);
		json.registerWriter(Example.class, beanDescription1);
		json.registerReader(Iface.class, mixinDescription1);
		json.registerWriter(Iface.class, mixinDescription1);
		json.registerReader(EmptyExample.class, beanDescription2);
		json.registerWriter(EmptyExample.class, beanDescription2);
		json.registerReader(IEmpty.class, mixinDescription2);
		json.registerWriter(IEmpty.class, mixinDescription2);
	}

	interface Iface {
		int y();
		void y(int value);
	}

	interface IEmpty {
	}

	static class Example implements Iface {
		long x;
		private int y;

		public int y() {
			return y;
		}
		public void y(int value) {
			y = value;
		}
	}

	static class EmptyExample implements IEmpty {
	}

	@Test
	public void testResolution() throws IOException {
		Example wo = new Example();
		wo.x = 100;
		wo.y(200);
		JsonWriter writer = json.newWriter();
		json.serialize(writer, Iface.class, wo);
		Assert.assertEquals("{\"$type\":\"com.dslplatform.json.MixinTest.Example\",\"y\":200,\"x\":100}", writer.toString());
		ByteArrayInputStream is = new ByteArrayInputStream(writer.getByteBuffer(), 0, writer.size());
		Iface wo2 = json.deserialize(Iface.class, is);
		Assert.assertEquals(wo.y, wo2.y());
		Assert.assertTrue(wo2 instanceof Example);
		Assert.assertEquals(wo.x, ((Example)wo2).x);
	}

	@Test
	public void mustHaveType1() throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream("{}".getBytes("UTF-8"));
		try {
			json.deserialize(Iface.class, is);
			Assert.fail("Expecting error");
		} catch (IOException ex) {
			Assert.assertTrue(ex.getMessage().contains("Expecting \"$type\" attribute as first element of mixin at position 2"));
		}
	}

	@Test
	public void mustHaveType2() throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream("{\"$typ\":123}".getBytes("UTF-8"));
		try {
			json.deserialize(Iface.class, is);
			Assert.fail("Expecting error");
		} catch (IOException ex) {
			Assert.assertTrue(ex.getMessage().contains("Expecting \"$type\" attribute as first element of mixin at position 2. Found: $typ"));
		}
	}

	@Test
	public void testEmptyResolution() throws IOException {
		EmptyExample wo = new EmptyExample();
		JsonWriter writer = json.newWriter();
		json.serialize(writer, IEmpty.class, wo);
		Assert.assertEquals("{\"$type\":\"com.dslplatform.json.MixinTest.EmptyExample\"}", writer.toString());
		ByteArrayInputStream is = new ByteArrayInputStream(writer.getByteBuffer(), 0, writer.size());
		IEmpty wo2 = json.deserialize(IEmpty.class, is);
		Assert.assertTrue(wo2 instanceof EmptyExample);
	}
}
