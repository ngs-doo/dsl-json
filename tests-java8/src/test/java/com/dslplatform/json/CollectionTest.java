package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CollectionTest {

	@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
	public static class PublicNonNullable {
		@JsonAttribute(nullable = false)
		private final List<String> collection = new ArrayList<String>();
		public List<String> getCollection() { return collection; }
	}

	@CompiledJson
	public static class AlternativeMarkers {
		@JsonProperty
		@NonNull
		private final List<String> collection = new ArrayList<String>();
		public List<String> getCollection() { return collection; }
	}

	private final DslJson<Object> dslJsonArray = new DslJson<>(Settings.basicSetup().allowArrayFormat(true));
	private final DslJson<Object> dslJsonObject = new DslJson<>(Settings.basicSetup().allowArrayFormat(false));

	@Test
	public void workingCollectionWithBeanGetter() throws IOException {
		PublicNonNullable model = new PublicNonNullable();
		model.collection.add("test");
		model.collection.add("me");
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJsonArray.serialize(model, os);
		Assert.assertEquals("[[\"test\",\"me\"]]", os.toString());
		PublicNonNullable array = dslJsonArray.deserialize(PublicNonNullable.class, os.toByteArray(), os.size());
		Assert.assertEquals(model.collection, array.collection);
		os.reset();
		dslJsonObject.serialize(model, os);
		Assert.assertEquals("{\"collection\":[\"test\",\"me\"]}", os.toString());
		PublicNonNullable object = dslJsonObject.deserialize(PublicNonNullable.class, os.toByteArray(), os.size());
		Assert.assertEquals(model.collection, object.collection);
		os.reset();
	}

	@Test
	public void alternativeCollectionMarker() throws IOException {
		AlternativeMarkers model = new AlternativeMarkers();
		model.collection.add("test");
		model.collection.add("me");
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJsonObject.serialize(model, os);
		Assert.assertEquals("{\"collection\":[\"test\",\"me\"]}", os.toString());
		AlternativeMarkers object = dslJsonObject.deserialize(AlternativeMarkers.class, os.toByteArray(), os.size());
		Assert.assertEquals(model.collection, object.collection);
		os.reset();
	}
}
