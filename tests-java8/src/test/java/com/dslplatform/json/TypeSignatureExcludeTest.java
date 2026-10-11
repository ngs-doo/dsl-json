package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TypeSignatureExcludeTest {

	@CompiledJson
	public interface DefaultIface {
		int y();
		void y(int y);
	}

	@CompiledJson(typeSignature = CompiledJson.TypeSignature.EXCLUDE)
	public interface ExcludedIface {
		int y();
		void y(int y);
	}

	@CompiledJson(typeSignature = CompiledJson.TypeSignature.EXCLUDE, deserializeAs = IsExcludedTarget.class)
	public interface ExcludedWithTarget {
		int y();
		void y(int y);
	}

	@CompiledJson
	public static class IsDefault implements DefaultIface {
		private int y;

		public int y() {
			return y;
		}

		public void y(int y) {
			this.y = y;
		}

		public IsDefault(int y) {
			this.y = y;
		}
	}

	@CompiledJson
	public static class IsExcluded implements ExcludedIface {
		private int y;

		public int y() {
			return y;
		}

		public void y(int y) {
			this.y = y;
		}

		public IsExcluded(int y) {
			this.y = y;
		}
	}

	@CompiledJson
	public static class IsExcludedTarget implements ExcludedWithTarget {
		private int y;

		public int y() {
			return y;
		}

		public void y(int y) {
			this.y = y;
		}

		public IsExcludedTarget(int y) {
			this.y = y;
		}
	}

	@CompiledJson
	public static class Holder {
		public DefaultIface d;
		public ExcludedIface e;
	}

	@CompiledJson
	public static class HolderWithTarget {
		public ExcludedWithTarget t;
	}

	@CompiledJson
	public interface ImplExcludedIface {
		int y();
		void y(int y);
	}

	@CompiledJson(typeSignature = CompiledJson.TypeSignature.EXCLUDE)
	public static class ImplExcluded implements ImplExcludedIface {
		private int y;

		public int y() {
			return y;
		}

		public void y(int y) {
			this.y = y;
		}

		public ImplExcluded(int y) {
			this.y = y;
		}
	}

	@CompiledJson(typeSignature = CompiledJson.TypeSignature.EXCLUDE)
	public static abstract class AbstractExcludedBase implements ImplExcludedIface {
	}

	@CompiledJson
	public static class AbstractExcludedImpl extends AbstractExcludedBase {
		private int y;

		public int y() {
			return y;
		}

		public void y(int y) {
			this.y = y;
		}

		public AbstractExcludedImpl(int y) {
			this.y = y;
		}
	}

	@CompiledJson
	public static class HolderImplExcluded {
		public ImplExcludedIface i;
	}

	@CompiledJson
	public static class HolderAbstractExcluded {
		public ImplExcludedIface i;
		public AbstractExcludedBase a;
	}

	@CompiledJson
	public interface NestedOuterIface {
		int y();
		void y(int y);
	}

	@CompiledJson(typeSignature = CompiledJson.TypeSignature.EXCLUDE)
	public interface NestedInnerIface extends NestedOuterIface {
	}

	@CompiledJson
	public static class NestedImpl implements NestedInnerIface {
		private int y;

		public int y() {
			return y;
		}

		public void y(int y) {
			this.y = y;
		}

		public NestedImpl(int y) {
			this.y = y;
		}
	}

	@CompiledJson
	public static class HolderNestedOuter {
		public NestedOuterIface o;
	}

	@CompiledJson
	public static class HolderNestedInner {
		public NestedInnerIface i;
	}

	private final DslJson<Object> dslJson = new DslJson<>();

	@Test
	public void defaultIncludesTypeSignature() throws IOException {
		Holder h = new Holder();
		h.d = new IsDefault(-123);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(h, os);
		Assert.assertEquals("{\"e\":null,\"d\":{\"$type\":\"com.dslplatform.json.TypeSignatureExcludeTest.IsDefault\",\"y\":-123}}", os.toString());
	}

	@Test
	public void excludeOmitsTypeSignature() throws IOException {
		Holder h = new Holder();
		h.e = new IsExcluded(-123);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(h, os);
		Assert.assertEquals("{\"e\":{\"y\":-123},\"d\":null}", os.toString());
	}

	@Test
	public void excludeWithDeserializeRoundtrip() throws IOException {
		HolderWithTarget h = new HolderWithTarget();
		h.t = new IsExcludedTarget(42);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(h, os);
		Assert.assertEquals("{\"t\":{\"y\":42}}", os.toString());
		HolderWithTarget res = dslJson.deserialize(HolderWithTarget.class, os.toByteArray(), os.size());
		Assert.assertEquals(42, res.t.y());
		Assert.assertEquals(IsExcludedTarget.class, res.t.getClass());
	}

	@Test
	public void excludeOnImplementationDoesNotAffectInterfaceSerialization() throws IOException {
		HolderImplExcluded h = new HolderImplExcluded();
		h.i = new ImplExcluded(-123);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(h, os);
		Assert.assertEquals("{\"i\":{\"$type\":\"com.dslplatform.json.TypeSignatureExcludeTest.ImplExcluded\",\"y\":-123}}", os.toString());
		HolderImplExcluded res = dslJson.deserialize(HolderImplExcluded.class, os.toByteArray(), os.size());
		Assert.assertEquals(-123, res.i.y());
		Assert.assertEquals(ImplExcluded.class, res.i.getClass());
	}

	@Test
	public void abstractClassWithExclude() throws IOException {
		HolderAbstractExcluded h = new HolderAbstractExcluded();
		AbstractExcludedImpl impl = new AbstractExcludedImpl(7);
		h.i = impl;
		h.a = impl;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(h, os);
		Assert.assertEquals("{\"i\":{\"$type\":\"com.dslplatform.json.TypeSignatureExcludeTest.AbstractExcludedImpl\",\"y\":7},\"a\":{\"y\":7}}", os.toString());
	}

	@Test
	public void excludeWithoutDeserializeAsIsWriteOnly() throws IOException {
		Holder h = new Holder();
		h.e = new IsExcluded(-123);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(h, os);
		Assert.assertEquals("{\"e\":{\"y\":-123},\"d\":null}", os.toString());
		try {
			dslJson.deserialize(Holder.class, os.toByteArray(), os.size());
			Assert.fail("Expected ConfigurationException when deserializing write-only mixin");
		} catch (ConfigurationException e) {
			Assert.assertTrue(e.getMessage().contains("Unable to find reader for"));
		}
	}

	@Test
	public void excludeOnSubMixinStillAllowsParentTypeSignature() throws IOException {
		NestedImpl impl = new NestedImpl(7);
		HolderNestedOuter ho = new HolderNestedOuter();
		ho.o = impl;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(ho, os);
		Assert.assertEquals("{\"o\":{\"$type\":\"com.dslplatform.json.TypeSignatureExcludeTest.NestedImpl\",\"y\":7}}", os.toString());

		HolderNestedInner hi = new HolderNestedInner();
		hi.i = impl;
		os = new ByteArrayOutputStream();
		dslJson.serialize(hi, os);
		Assert.assertEquals("{\"i\":{\"y\":7}}", os.toString());
	}

	@Test
	public void excludeOnSubMixinRoundtripsThroughParent() throws IOException {
		NestedImpl impl = new NestedImpl(9);
		HolderNestedOuter ho = new HolderNestedOuter();
		ho.o = impl;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(ho, os);
		HolderNestedOuter res = dslJson.deserialize(HolderNestedOuter.class, os.toByteArray(), os.size());
		Assert.assertEquals(NestedImpl.class, res.o.getClass());
		Assert.assertEquals(9, res.o.y());
	}

	@Test
	public void excludeOnSubMixinWithoutDeserializeAsIsWriteOnly() throws IOException {
		HolderNestedInner hi = new HolderNestedInner();
		hi.i = new NestedImpl(3);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(hi, os);
		Assert.assertEquals("{\"i\":{\"y\":3}}", os.toString());
		try {
			dslJson.deserialize(HolderNestedInner.class, os.toByteArray(), os.size());
			Assert.fail("Exception expected");
		} catch (ConfigurationException e) {
			Assert.assertTrue(e.getMessage().contains("Unable to find reader for interface com.dslplatform.json.TypeSignatureExcludeTest$NestedInnerIface."));
		}
	}
}
