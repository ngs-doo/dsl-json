package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class VariousTest {

	class CollectionWriter implements JsonWriter.WriteObject<Collection> {
		private final DslJson dslJson;

		public CollectionWriter(DslJson dslJson) {
			this.dslJson = dslJson;
		}

		@Override
		public void write(JsonWriter writer, @Nullable Collection value) {
			writer.writeByte((byte) '[');
			boolean isFirst = true;
			for (Object it : value) {
				if (isFirst) isFirst = false;
				else writer.writeByte((byte) ',');
				if (it == null) writer.writeNull();
				else dslJson.serialize(writer, it.getClass(), it);
			}
			writer.writeByte((byte) ']');
		}
	}

	@Test
	public void testNestedCollection() throws IOException {
		DslJson dsl = new DslJson<Object>();
		dsl.registerWriter(Collection.class, new CollectionWriter(dsl));
		JsonWriter jw = dsl.newWriter();
		dsl.serializeMap(
				Collections.singletonMap("x",
						Collections.singletonList(Collections.singletonList("Hello"))
				),
				jw
		);
		Assert.assertEquals("{\"x\":[[\"Hello\"]]}", jw.toString());
	}

	@Test
	public void stringIndexIssue() throws IOException {
		try {
			String json = "{ \"a\": 1, \"b\": { \"c\": { \"d\": \"e\" } } }";
			DslJson<Object> dsl = new DslJson<Object>();
			dsl.deserialize(Map.class, json.getBytes("UTF-8"), 11);
			Assert.fail("Expecting end of JSON error");
		} catch (StringIndexOutOfBoundsException e) {
			Assert.fail("Expecting end of JSON error");
		} catch (IOException e) {
			Assert.assertTrue(e.getMessage().contains("Unable to parse input at position: 11"));
		}
	}
}
