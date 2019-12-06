package com.dslplatform.json;

import com.dslplatform.json.subclass.SubClass;
import com.dslplatform.json.subpackage.AbstractClass;
import com.dslplatform.json.superclass.SuperClass;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class NamedPackageTest {

	@CompiledJson
	public static class ConcreteClass extends AbstractClass {
		public String s;
	}

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

	@Test
	public void canUsePackageNestedInheritedObjectFromDifferentPackage() throws IOException {
		ConcreteClass cc = new ConcreteClass();
		cc.i = 5;
		cc.s = "x";
		JsonWriter writer = dslJson.newWriter();
		dslJson.serialize(writer, AbstractClass.class, cc);
		Assert.assertEquals("{\"$type\":\"com.dslplatform.json.NamedPackageTest.ConcreteClass\",\"i\":5,\"s\":\"x\"}", writer.toString());
		AbstractClass res = dslJson.deserialize(AbstractClass.class, writer.getByteBuffer(), writer.size());
		Assert.assertEquals(cc.i, res.i);
	}

	@Test
	public void canUsePackageInheritedObjectFromDifferentPackage() throws IOException {
		SubClass cc = new SubClass();
		cc.setCreator("me");
		JsonWriter writer = dslJson.newWriter();
		dslJson.serialize(writer, SuperClass.class, cc);
		Assert.assertEquals("{\"$type\":\"com.dslplatform.json.subclass.SubClass\",\"name\":null,\"created\":null,\"edited\":null,\"creator\":\"me\",\"editor\":null}", writer.toString());
		SuperClass res = dslJson.deserialize(SuperClass.class, writer.getByteBuffer(), writer.size());
		Assert.assertEquals(cc.getCreator(), res.getCreator());
	}
}
