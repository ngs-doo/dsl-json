package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class RecursiveOptional {

	public static class WithOptional {
		public Optional<Optional<Integer>> optOptInt;
		public Optional<Optional> optOptUnknown;
		public Optional<Optional<Optional>> optOpt2Unknown;
	}

	@Test
	public void testResolution() throws IOException {
		DslJson<Object> json = new DslJson<Object>(Settings.withRuntime());
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
