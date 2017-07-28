package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public class VariousTest {

	class CollectionWriter implements JsonWriter.WriteObject<Collection> {
		private final DslJson dslJson;

		public CollectionWriter(DslJson dslJson) {
			this.dslJson = dslJson;
		}

		@Override
		public void write(JsonWriter writer, Collection value) {
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
}
