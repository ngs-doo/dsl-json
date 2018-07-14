package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.runtime.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class BuilderTest {

	private final DslJson<Object> json = new DslJson<Object>(Settings.withRuntime());

	public BuilderTest() {
		ObjectFormatDescription<Immutable.Builder, Immutable> description = new ObjectFormatDescription<Immutable.Builder, Immutable>(
				Immutable.class,
				new InstanceFactory<Immutable.Builder>() {
					@Override
					public Immutable.Builder create() {
						return new Immutable.Builder();
					}
				},
				new Function<Immutable.Builder, Immutable>() {
					@Override
					public Immutable apply(Immutable.Builder builder) {
						return builder.build();
					}
				},
				new JsonWriter.WriteObject[] {
						Settings.createEncoder(new Function<Immutable, Integer>() {
							@Override
							public Integer apply(Immutable c) {
								return c.x;
							}
						}, "x", json, int.class),
						Settings.createEncoder(new Function<Immutable, Long>() {
							@Override
							public Long apply(Immutable c) {
								return c.y;
							}
						}, "y", json, long.class)
				},
				new DecodePropertyInfo[] {
						Settings.createDecoder(new BiConsumer<Immutable.Builder, Integer>() {
							@Override
							public void accept(Immutable.Builder builder, Integer v) {
								builder.x(v);
							}
						}, "x", json, int.class),
						Settings.createDecoder(new BiConsumer<Immutable.Builder, Long>() {
							@Override
							public void accept(Immutable.Builder builder, Long v) {
								builder.y(v);
							}
						}, "y", json, long.class)
				},
				json,
				true
		);
		json.registerReader(Immutable.class, description);
		json.registerWriter(Immutable.class, description);
	}

	static class Immutable {
		public final int x;
		public final long y;
		public Immutable(int x, long y) {
			this.x = x;
			this.y = y;
		}
		static class Builder {
			private int x;
			private long y;
			public Builder x(int v) {
				x = v;
				return this;
			}
			public Builder y(long v) {
				y = v;
				return this;
			}
			public Immutable build() {
				return new Immutable(x, y);
			}
		}
	}

	@Test
	public void testResolution() throws IOException {
		Immutable wo = new Immutable(100, 200);
		JsonWriter writer = json.newWriter();
		json.serialize(writer, wo);
		Assert.assertEquals("{\"x\":100,\"y\":200}", writer.toString());
		ByteArrayInputStream is = new ByteArrayInputStream(writer.getByteBuffer(), 0, writer.size());
		Immutable wo2 = json.deserialize(Immutable.class, is);
		Assert.assertEquals(wo.y, wo2.y);
		Assert.assertEquals(wo.x, wo2.x);
	}

	@Test
	public void testEmptyResolution() throws IOException {
		byte[] bytes = "{}".getBytes("UTF-8");
		Immutable wo2 = json.deserialize(Immutable.class, bytes, bytes.length);
		Assert.assertEquals(0, wo2.x);
		Assert.assertEquals(0L, wo2.y);
	}
}
