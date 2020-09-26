package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AnnotationResolutionTest {

	@CompiledJson
	public static class AnnotationOnSetter {
		private int number;

		public int getNumber() {
			return number;
		}

		@JsonAttribute(name = "num")
		public void setNumber(int value) {
			number = value;
		}
	}

	@CompiledJson
	public static class AnnotationOnPrivateField {
		@JsonAttribute(name = "num")
		private int number;

		public int getNumber() {
			return number;
		}

		public void setNumber(int value) {
			number = value;
		}
	}

	@CompiledJson
	public static class AlternativeAnnotationOnPrivateField {
		@JsonProperty("num")
		private int number;

		public int getNumber() {
			return number;
		}

		public void setNumber(int value) {
			number = value;
		}
	}

	private final DslJson<Object> dslJson = new DslJson<>(Settings.basicSetup());

	@Test
	public void willPickUpAnnotationFromSetter() throws IOException {
		AnnotationOnSetter ann = new AnnotationOnSetter();
		ann.setNumber(505);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(ann, os);
		Assert.assertEquals("{\"num\":505}", os.toString());
		AnnotationOnSetter res = dslJson.deserialize(AnnotationOnSetter.class, os.toByteArray(), os.size());
		Assert.assertEquals(ann.getNumber(), res.getNumber());
	}

	@Test
	public void willPickUpAnnotationFromPrivateField() throws IOException {
		AnnotationOnPrivateField ann = new AnnotationOnPrivateField();
		ann.setNumber(505);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(ann, os);
		Assert.assertEquals("{\"num\":505}", os.toString());
		AnnotationOnPrivateField res = dslJson.deserialize(AnnotationOnPrivateField.class, os.toByteArray(), os.size());
		Assert.assertEquals(ann.getNumber(), res.getNumber());
	}

	@Test
	public void willPickUpAltenativeAnnotationFromPrivateField() throws IOException {
		AlternativeAnnotationOnPrivateField ann = new AlternativeAnnotationOnPrivateField();
		ann.setNumber(505);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(ann, os);
		Assert.assertEquals("{\"num\":505}", os.toString());
		AlternativeAnnotationOnPrivateField res = dslJson.deserialize(AlternativeAnnotationOnPrivateField.class, os.toByteArray(), os.size());
		Assert.assertEquals(ann.getNumber(), res.getNumber());
	}
}
