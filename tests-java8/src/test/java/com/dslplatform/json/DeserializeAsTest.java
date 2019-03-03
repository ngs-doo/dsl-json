package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DeserializeAsTest {

	@CompiledJson
	public static class HasInterface {
		public int x;
		public Iface i;
	}

	@CompiledJson(deserializeAs = IsIfaceDefault.class)
	public interface Iface {
		int y();
		void y(int y);
	}

	@CompiledJson
	public static class IsIfaceDefault implements Iface {
		private int y;

		public int y() {
			return y;
		}

		public void y(int y) {
			this.y = y;
		}

		public IsIfaceDefault(int y) {
			this.y = y;
		}
	}

	@CompiledJson(name = "custom-name")
	public static class IsIfaceCustom implements Iface {
		private int y;

		public int y() {
			return y;
		}

		public void y(int y) {
			this.y = y;
		}

		public IsIfaceCustom(int y) {
			this.y = y;
		}
	}

	private final DslJson<Object> dslJson = new DslJson<>();

	@Test
	public void roundtripDefault() throws IOException {
		HasInterface hi = new HasInterface();
		hi.x = 505;
		hi.i = new IsIfaceDefault(-123);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(hi, os);
		Assert.assertEquals("{\"i\":{\"y\":-123},\"x\":505}", os.toString());
		HasInterface res = dslJson.deserialize(HasInterface.class, os.toByteArray(), os.size());
		Assert.assertEquals(hi.x, res.x);
		Assert.assertEquals(hi.i.y(), res.i.y());
		Assert.assertEquals(hi.i.getClass(), res.i.getClass());
	}

	@Test
	public void roundtripCustom() throws IOException {
		HasInterface hi = new HasInterface();
		hi.x = 505;
		hi.i = new IsIfaceCustom(-123);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(hi, os);
		Assert.assertEquals("{\"i\":{\"y\":-123},\"x\":505}", os.toString());
		HasInterface res = dslJson.deserialize(HasInterface.class, os.toByteArray(), os.size());
		Assert.assertEquals(hi.x, res.x);
		Assert.assertEquals(hi.i.y(), res.i.y());
		Assert.assertNotEquals(hi.i.getClass(), res.i.getClass());
	}
}
