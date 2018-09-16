package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class BuilderTest {

	public static class Composite1 {
		@JsonAttribute(index = 1)
		public final int i;
		@JsonAttribute(index = 2, name = "ss")
		public final String s;

		private Composite1(int i, String s) {
			this.i = i;
			this.s = s;
		}
		public static Builder builder() {
			return new Builder();
		}
		public static class Builder {
			private int i;
			private String s;
			private Builder() {}
			public Builder i(int i) {
				this.i = i;
				return this;
			}
			public Builder s(String s) {
				this.s = s;
				return this;
			}
			@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
			public Composite1 build() {
				return new Composite1(i, s);
			}
		}
	}

	@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
	public static class Composite2 {
		@JsonAttribute(index = 1)
		public final int i;
		@JsonAttribute(index = 2)
		public final String s;

		private Composite2(int i, String s) {
			this.i = i;
			this.s = s;
		}
		public static Builder builder() {
			return new Builder();
		}
		public static class Builder {
			private int i;
			private String s;
			private Builder() {}
			public Builder i(int i) {
				this.i = i;
				return this;
			}
			public Builder s(String s) {
				this.s = s;
				return this;
			}
			public Composite2 build() {
				return new Composite2(i, s);
			}
		}
	}

	public static class Composite3 {
		@JsonAttribute(index = 1)
		public final int i;
		@JsonAttribute(index = 2)
		public final String s;

		private Composite3(int i, String s) {
			this.i = i;
			this.s = s;
		}
		public static class Builder {
			private int i;
			private String s;
			public Builder() {}
			public Builder i(int i) {
				this.i = i;
				return this;
			}
			public Builder s(String s) {
				this.s = s;
				return this;
			}
			@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
			public Composite3 build() {
				return new Composite3(i, s);
			}
		}
	}

	public static abstract class CompositeAbstract {
		@JsonAttribute(index = 1, name = "_i")
		public abstract int getI();
		@JsonAttribute(index = 2)
		public abstract String getS();

		public static Builder builder() {
			return new BuilderImpl();
		}
		public abstract static class Builder {
			public abstract Builder i(int i);
			public abstract Builder s(String s);
			@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
			public abstract CompositeAbstract build();
		}
		private static class BuilderImpl extends Builder {
			private int i;
			private String s;
			private BuilderImpl() {}
			public Builder i(int i) {
				this.i = i;
				return this;
			}
			public Builder s(String s) {
				this.s = s;
				return this;
			}
			public CompositeAbstract build() {
				return new CompositeAbstractImpl(i, s);
			}
		}
		private static class CompositeAbstractImpl extends CompositeAbstract {
			private int i;
			private String s;
			private CompositeAbstractImpl(int i, String s) {
				this.i = i;
				this.s = s;
			}
			public int getI() {
				return i;
			}
			public String getS() {
				return s;
			}
		}
	}

	private final DslJson<Object> dslJsonArray = new DslJson<>(Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());
	private final DslJson<Object> dslJsonObject = new DslJson<>(Settings.withRuntime().allowArrayFormat(false).includeServiceLoader());

	@Test
	public void roundtripBuild() throws IOException {
		Composite1 c = Composite1.builder().i(5).s("abc").build();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJsonArray.serialize(c, os);
		Assert.assertEquals("[5,\"abc\"]", os.toString());
		Composite1 res = dslJsonArray.deserialize(Composite1.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.i, res.i);
		Assert.assertEquals(c.s, res.s);
		os.reset();
		dslJsonObject.serialize(c, os);
		Assert.assertEquals("{\"i\":5,\"ss\":\"abc\"}", os.toString());
		res = dslJsonObject.deserialize(Composite1.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.i, res.i);
		Assert.assertEquals(c.s, res.s);
		os.reset();
	}

	@Test
	public void roundtripCtor() throws IOException {
		Composite2 c = Composite2.builder().i(5).s("abc").build();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJsonArray.serialize(c, os);
		Assert.assertEquals("[5,\"abc\"]", os.toString());
		Composite2 res = dslJsonArray.deserialize(Composite2.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.i, res.i);
		Assert.assertEquals(c.s, res.s);
		os.reset();
		dslJsonObject.serialize(c, os);
		Assert.assertEquals("{\"i\":5,\"s\":\"abc\"}", os.toString());
		res = dslJsonObject.deserialize(Composite2.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.i, res.i);
		Assert.assertEquals(c.s, res.s);
		os.reset();
	}

	@Test
	public void roundtripNoFactory() throws IOException {
		Composite3 c = new Composite3.Builder().i(5).s("abc").build();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJsonArray.serialize(c, os);
		Assert.assertEquals("[5,\"abc\"]", os.toString());
		Composite3 res = dslJsonArray.deserialize(Composite3.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.i, res.i);
		Assert.assertEquals(c.s, res.s);
		os.reset();
		dslJsonObject.serialize(c, os);
		Assert.assertEquals("{\"i\":5,\"s\":\"abc\"}", os.toString());
		res = dslJsonObject.deserialize(Composite3.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.i, res.i);
		Assert.assertEquals(c.s, res.s);
		os.reset();
	}

	@Test
	public void roundtripAbstracts() throws IOException {
		CompositeAbstract c = CompositeAbstract.builder().i(5).s("abc").build();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJsonArray.serialize(c, os);
		Assert.assertEquals("[5,\"abc\"]", os.toString());
		CompositeAbstract res = dslJsonArray.deserialize(CompositeAbstract.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.getI(), res.getI());
		Assert.assertEquals(c.getS(), res.getS());
		os.reset();
		dslJsonObject.serialize(c, os);
		Assert.assertEquals("{\"_i\":5,\"s\":\"abc\"}", os.toString());
		res = dslJsonObject.deserialize(CompositeAbstract.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.getI(), res.getI());
		Assert.assertEquals(c.getS(), res.getS());
		os.reset();
	}
}
