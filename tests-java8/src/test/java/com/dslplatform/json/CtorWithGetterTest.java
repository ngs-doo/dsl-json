package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CtorWithGetterTest {

	@CompiledJson
	public static class Example {
		private final Double d;
		public final Double getD() { return d; }
		private final List<Integer> list;
		public final List<Integer> getList() { return list; }

		public Example(Double d, List<Integer> list) {
			this.d = d;
			this.list = list;
		}
	}

	private final DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());

	@Test
	public void objectRoundtrip() throws IOException {
		Example c = new Example(Double.parseDouble("123.456"), Arrays.asList(1, 2, 3));
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(c, os);
		Example res = dslJson.deserialize(Example.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.getD(), res.getD());
		Assert.assertEquals(c.getList(), res.getList());
	}
}
