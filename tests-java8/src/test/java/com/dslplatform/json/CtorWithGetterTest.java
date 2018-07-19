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

	@CompiledJson
	public static class ChangeName {
		public final int i;

		public ChangeName(@JsonAttribute(name = "i2") int i) {
			this.i = i;
		}
	}

	@CompiledJson
	public static class Response {
		private final String queryResult;

		public Response(@JsonAttribute(name = "QueryResult") String queryResult) {
			this.queryResult = queryResult;
		}

		public String getQueryResult() {
			return queryResult;
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

	@Test
	public void customCtorName() throws IOException {
		ChangeName c = new ChangeName(505);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(c, os);
		Assert.assertEquals("{\"i2\":505}", os.toString("UTF-8"));
		ChangeName res = dslJson.deserialize(ChangeName.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.i, res.i);
	}

	@Test
	public void casingOnArgument() throws IOException {
		Response r = new Response("505");
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(r, os);
		Assert.assertEquals("{\"QueryResult\":\"505\"}", os.toString("UTF-8"));
		Response res = dslJson.deserialize(Response.class, os.toByteArray(), os.size());
		Assert.assertEquals(r.getQueryResult(), res.getQueryResult());
	}
}
