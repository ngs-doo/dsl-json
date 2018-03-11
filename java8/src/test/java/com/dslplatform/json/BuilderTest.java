package com.dslplatform.json;

import com.dslplatform.json.runtime.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class BuilderTest {

	private final DslJson<Object> json = new DslJson<Object>(Settings.withRuntime());

	public BuilderTest() {
		BeanDescription<Immutable.Builder, Immutable> description = new BeanDescription<Immutable.Builder, Immutable>(
				Immutable.class,
				Immutable.Builder::new,
				Immutable.Builder::build,
				new JsonWriter.WriteObject[] {
						Settings.<Immutable, Integer>createEncoder(c -> c.x, "x", json, int.class),
						Settings.<Immutable, Long>createEncoder(c -> c.y, "y", json, long.class)
				},
				new DecodePropertyInfo[] {
						Settings.<Immutable.Builder, Integer>createDecoder(Immutable.Builder::x, "x", json, int.class),
						Settings.<Immutable.Builder, Long>createDecoder(Immutable.Builder::y, "y", json, long.class)
				},
				Immutable.class.getTypeName(),
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
