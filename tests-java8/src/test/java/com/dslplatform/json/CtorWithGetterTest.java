package com.dslplatform.json;

import com.dslplatform.json.runtime.MapAnalyzer;
import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

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

	@CompiledJson
	public static class MapRuntimeCtor {
		private final Map<String, Object> map;
		public final Map<String, Object> getMap() { return map; }

		public MapRuntimeCtor(
				@JsonAttribute(converter = MapAnalyzer.Runtime.class)
				Map<String, Object> map) {
			this.map = map;
		}
	}

	@CompiledJson()
	public static final class MultipleCtors {

		private final String id;
		public final String getId() {
			return id;
		}

		private final String query;
		public final String getQuery() {
			return query;
		}

		private final Map<String, Object> variables;
		public final Map<String, Object> getVariables() {
			return variables;
		}

		public MultipleCtors(
				String id,
				String query,
				Map<String, ? extends Object> variables) {
			this.id = id;
			this.query = query;
			this.variables = new HashMap<>(variables);
		}

		public MultipleCtors() {
			this.id = "";
			this.query = null;
			this.variables = new HashMap<>();
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

	@Test
	public void jsonAttributeOnCtor() throws IOException {
		MapRuntimeCtor r = new MapRuntimeCtor(Collections.singletonMap("abc", "dfg"));
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(r, os);
		Assert.assertEquals("{\"map\":{\"abc\":\"dfg\"}}", os.toString("UTF-8"));
		MapRuntimeCtor res = dslJson.deserialize(MapRuntimeCtor.class, os.toByteArray(), os.size());
		Assert.assertEquals(r.getMap(), res.getMap());
	}

	@Test
	public void classWithMultipleCtors() throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write("{\"query\":\"mutation T($input: [DocumentInput]) {createDocument(input: $input) {name,id}}\",\"variables\":{\"input\":[{\"name\":\"doc1\"},{\"name\":\"doc2\"}]},\"operationName\":\"T\"}".getBytes("UTF-8"));
		MultipleCtors res = dslJson.deserialize(MultipleCtors.class, os.toByteArray(), os.size());
		Assert.assertEquals(1, res.getVariables().size());
		//Assert.assertEquals("", res.getId());
	}
}
