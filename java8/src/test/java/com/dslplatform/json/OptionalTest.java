package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class OptionalTest {

	public static class WithOptional {
		public Optional<Optional<Integer>> optOptInt = Optional.empty();
		public Optional<Optional> optOptUnknown = Optional.empty();
		public Optional<Optional<Optional>> optOpt2Unknown = Optional.empty();
	}

	public static class PrimitiveOptionals {
		public OptionalLong optLong = OptionalLong.empty();
		public Optional<OptionalInt> optOptInt = Optional.empty();
		public Optional<Optional<OptionalDouble>> optOptOptDouble = Optional.empty();
	}

	private final DslJson<Object> jsonFull = new DslJson<Object>(Settings.withRuntime());
	private final DslJson<Object> jsonMinimal = new DslJson<Object>(Settings.withRuntime().skipDefaultValues(true));
	private final DslJson<Object> jsonLazy = new DslJson<Object>(Settings.basicSetup());
	private final DslJson<Object>[] dslJsons = new DslJson[] { jsonFull, jsonMinimal, jsonLazy };

	@Test
	public void testRecursive() throws IOException {
		for(DslJson<Object> json : dslJsons) {
			WithOptional wo = new WithOptional();
			wo.optOptInt = Optional.of(Optional.of(12));
			wo.optOptUnknown = Optional.of(Optional.of("abc"));
			wo.optOpt2Unknown = Optional.empty();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			json.serialize(wo, baos);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			WithOptional wo2 = json.deserialize(WithOptional.class, bais);
			Assert.assertEquals(wo.optOptInt, wo2.optOptInt);
			Assert.assertEquals(wo.optOptUnknown, wo2.optOptUnknown);
			Assert.assertEquals(wo.optOpt2Unknown, wo2.optOpt2Unknown);
		}
	}

	@Test
	public void testPrimitives() throws IOException {
		for(DslJson<Object> json : dslJsons) {
			PrimitiveOptionals wo = new PrimitiveOptionals();
			wo.optLong = OptionalLong.of(-5L);
			wo.optOptInt = Optional.of(OptionalInt.of(2));
			wo.optOptOptDouble = Optional.of(Optional.of(OptionalDouble.of(5.5)));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			json.serialize(wo, baos);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			PrimitiveOptionals wo2 = json.deserialize(PrimitiveOptionals.class, bais);
			Assert.assertEquals(wo.optLong, wo2.optLong);
			Assert.assertEquals(wo.optOptInt, wo2.optOptInt);
			Assert.assertEquals(wo.optOptOptDouble, wo2.optOptOptDouble);
		}
	}

	public static class OptionalWithUnknown {
		public Object something;
	}

	@Test
	public void optionalOnUnknown() throws IOException {
		OptionalWithUnknown owu = new OptionalWithUnknown();
		owu.something = Optional.empty();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		jsonMinimal.serialize(owu, baos);
		Assert.assertEquals("{}", baos.toString());
		owu.something = 0;
		baos.reset();
		jsonMinimal.serialize(owu, baos);
		Assert.assertEquals("{\"something\":0}", baos.toString());
	}
}
