package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonObjectTest {

	@CompiledJson
	public static class ObjectModel {
		public JsonObjectReference jsonObject;
	}

	@CompiledJson
	public static class ListModel {
		public List<JsonObjectReference> jsonObjects;
	}

	@CompiledJson
	public static class CombinedModel {
		public JsonObjectReference jsonObject;
		public List<JsonObjectReference> jsonObjects;
		public Map<Integer, JsonObjectReference> mapObjects;
	}

	public static class JsonObjectReference implements JsonObject {
		private final String x;
		public JsonObjectReference(String x) {
			this.x = x;
		}
		public void serialize(JsonWriter writer, boolean minimal) {
			writer.writeAscii("{\"x\":");
			writer.writeString(x);
			writer.writeAscii("}");
		}

		public static final JsonReader.ReadJsonObject<JsonObjectReference> JSON_READER = reader -> {
			reader.fillName();
			reader.getNextToken();
			String x1 = reader.readString();
			reader.getNextToken();
			return new JsonObjectReference(x1);
		};

		@Override
		public boolean equals(Object obj) {
			return ((JsonObjectReference)obj).x.equals(this.x);
		}
	}

	private final DslJson<Object> dslJson = new DslJson<>(Settings.basicSetup());

	@Test
	public void simpleTest() throws IOException {
		ObjectModel m = new ObjectModel();
		m.jsonObject = new JsonObjectReference("test");
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(m, os);
		Assert.assertEquals("{\"jsonObject\":{\"x\":\"test\"}}", os.toString());
		ObjectModel res = dslJson.deserialize(ObjectModel.class, os.toByteArray(), os.size());
		Assert.assertEquals(m.jsonObject, res.jsonObject);
	}

	@Test
	public void collectionTest() throws IOException {
		ListModel m = new ListModel();
		m.jsonObjects = Arrays.asList(new JsonObjectReference("test"), null, new JsonObjectReference("xxx"));
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(m, os);
		Assert.assertEquals("{\"jsonObjects\":[{\"x\":\"test\"},null,{\"x\":\"xxx\"}]}", os.toString());
		ListModel res = dslJson.deserialize(ListModel.class, os.toByteArray(), os.size());
		Assert.assertEquals(m.jsonObjects, res.jsonObjects);
	}

	@Test
	public void complexTest() throws IOException {
		CombinedModel m = new CombinedModel();
		m.jsonObject = new JsonObjectReference("test");
		m.jsonObjects = Arrays.asList(new JsonObjectReference("abc"), null, new JsonObjectReference("xxx"));
		m.mapObjects = new LinkedHashMap<Integer, JsonObjectReference>() {{ put(1, null); put(2, new JsonObjectReference("xXx")); }};
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(m, os);
		CombinedModel res = dslJson.deserialize(CombinedModel.class, os.toByteArray(), os.size());
		Assert.assertEquals(m.jsonObject, res.jsonObject);
		Assert.assertEquals(m.jsonObjects, res.jsonObjects);
		Assert.assertEquals(m.mapObjects, res.mapObjects);
	}
}
