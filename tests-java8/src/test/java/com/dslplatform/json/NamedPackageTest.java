package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class NamedPackageTest {

	private final DslJson<Object> dslJson = new DslJson<>();

	@Test
	public void canUsePackagePrivateObject() throws IOException {
		PackagePrivateModel v = new PackagePrivateModel();
		v.x = 11;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(v, os);
		Assert.assertEquals("{\"x\":11}", os.toString());
		PackagePrivateModel res = dslJson.deserialize(PackagePrivateModel.class, os.toByteArray(), os.size());
		Assert.assertEquals(v.x, res.x);
	}

	@Test
	public void canUsePackagePrivateNestedObject() throws IOException {
		PackagePrivateModel.Something v = new PackagePrivateModel.Something();
		v.y = 12;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(v, os);
		Assert.assertEquals("{\"y\":12}", os.toString());
		PackagePrivateModel.Something res = dslJson.deserialize(PackagePrivateModel.Something.class, os.toByteArray(), os.size());
		Assert.assertEquals(v.y, res.y);
	}
}
