package com.dslplatform.json;

import com.dslplatform.json.runtime.DecodePropertyInfo;
import com.dslplatform.json.runtime.ObjectFormatDescription;
import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

public class MandatoryPropertiesTest {

	@SuppressWarnings("unused")
	public static class SmallModel {
		public int x;
		public int y;
		public int z;
	}

	private final DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime());

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void registerSmallModel() throws Exception {
		final DecodePropertyInfo[] decoders = new DecodePropertyInfo[3];
		final Field[] fields = {
				SmallModel.class.getField("x"),
				SmallModel.class.getField("y"),
				SmallModel.class.getField("z")
		};
		for (int i = 0; i < 3; i++) {
			final Field f = fields[i];
			decoders[i] = Settings.createDecoder(
					(Settings.BiConsumer<SmallModel, Integer>) (t, v) -> {
						try {
							f.set(t, v);
						} catch (IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					},
					f.getName(), dslJson, false, i == 0, -1, false, Integer.TYPE);
		}
		ObjectFormatDescription<SmallModel, SmallModel> description = ObjectFormatDescription.create(
				SmallModel.class,
				SmallModel::new,
				new JsonWriter.WriteObject[0], decoders, dslJson, true);
		dslJson.registerReader(SmallModel.class, description);
	}

	@Test
	public void optionalPropertiesDoNotResetMandatoryFlag() throws Exception {
		registerSmallModel();
		byte[] json = "{\"y\":1,\"z\":2}".getBytes(StandardCharsets.UTF_8);
		try {
			dslJson.deserialize(SmallModel.class, json, json.length);
			Assert.fail("Expecting mandatory property error");
		} catch (IOException ex) {
			Assert.assertTrue(ex.getMessage(), ex.getMessage().contains("Mandatory property (x) not found"));
		}
	}

	@Test
	public void smallModelDeserializesWhenAllPresent() throws Exception {
		registerSmallModel();
		byte[] json = "{\"x\":3,\"y\":1,\"z\":2}".getBytes(StandardCharsets.UTF_8);
		SmallModel model = dslJson.deserialize(SmallModel.class, json, json.length);
		Assert.assertEquals(3, model.x);
		Assert.assertEquals(1, model.y);
		Assert.assertEquals(2, model.z);
	}
}
