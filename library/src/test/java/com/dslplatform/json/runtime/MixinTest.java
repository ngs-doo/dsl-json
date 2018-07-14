package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class MixinTest {

	private final DslJson<Object> json = new DslJson<Object>(Settings.withRuntime());

	private final ObjectFormatDescription<Example, Example> objectFormatDescription1 = ObjectFormatDescription.create(
			Example.class,
			new InstanceFactory<Example>() {
				@Override
				public Example create() {
					return new Example();
				}
			},
			new JsonWriter.WriteObject[] {
					Settings.createEncoder(new Function<Example, Integer>() {
						@Override
						public Integer apply(Example example) {
							return example.y();
						}
					}, "y", json, int.class),
					Settings.createEncoder(new Function<Example, Long>() {
						@Override
						public Long apply(Example c) {
							return c.x;
						}
					}, "x", json, long.class)
			},
			(DecodePropertyInfo<JsonReader.BindObject<Example>>[]) new DecodePropertyInfo[] {
					Settings.createDecoder(new BiConsumer<Example, Integer>() {
						@Override
						public void accept(Example c, Integer v) {
							c.y = v;
						}
					}, "y", json, int.class),
					Settings.createDecoder(new BiConsumer<Example, Long>() {
						@Override
						public void accept(Example c, Long v) {
							c.x = v;
						}
					}, "x", json, long.class)
			},
			json,
			true);

	private final MixinDescription<Iface> mixinDescription1 = new MixinDescription<Iface>(
			Iface.class,
			json,
			new FormatDescription[] { new FormatDescription(Example.class, objectFormatDescription1, null, true, "Example", json)}
	);

	private final ObjectFormatDescription<EmptyExample, EmptyExample> objectFormatDescription2 = ObjectFormatDescription.create(
			EmptyExample.class,
			new InstanceFactory<EmptyExample>() {
				@Override
				public EmptyExample create() {
					return new EmptyExample();
				}
			},
			new JsonWriter.WriteObject[0],
			(DecodePropertyInfo<JsonReader.BindObject<EmptyExample>>[]) new DecodePropertyInfo[0],
			json,
			true);

	private final MixinDescription<IEmpty> mixinDescription2 = new MixinDescription<IEmpty>(
			IEmpty.class,
			json,
			new FormatDescription[] { new FormatDescription(EmptyExample.class, objectFormatDescription2, null, true, "EmptyExample", json)}
	);

	public MixinTest() {
		json.registerReader(Example.class, objectFormatDescription1);
		json.registerWriter(Example.class, objectFormatDescription1);
		json.registerReader(Iface.class, mixinDescription1);
		json.registerWriter(Iface.class, mixinDescription1);
		json.registerReader(EmptyExample.class, objectFormatDescription2);
		json.registerWriter(EmptyExample.class, objectFormatDescription2);
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
		Assert.assertEquals("{\"$type\":\"Example\",\"y\":200,\"x\":100}", writer.toString());
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
			Assert.assertTrue(ex.getMessage().contains("Expecting \"$type\" attribute as first element of mixin at position: 2"));
		}
	}

	@Test
	public void mustHaveType2() throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream("{\"$typ\":123}".getBytes("UTF-8"));
		try {
			json.deserialize(Iface.class, is);
			Assert.fail("Expecting error");
		} catch (IOException ex) {
			Assert.assertTrue(ex.getMessage().contains("Expecting \"$type\" attribute as first element of mixin at position: 2. Found: $typ"));
		}
	}

	@Test
	public void testEmptyResolution() throws IOException {
		EmptyExample wo = new EmptyExample();
		JsonWriter writer = json.newWriter();
		json.serialize(writer, IEmpty.class, wo);
		Assert.assertEquals("{\"$type\":\"EmptyExample\"}", writer.toString());
		ByteArrayInputStream is = new ByteArrayInputStream(writer.getByteBuffer(), 0, writer.size());
		IEmpty wo2 = json.deserialize(IEmpty.class, is);
		Assert.assertTrue(wo2 instanceof EmptyExample);
	}
}
