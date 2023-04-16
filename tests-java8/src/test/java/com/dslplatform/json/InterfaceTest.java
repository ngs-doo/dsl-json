package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import com.dslplatform.json.runtime.TypeDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.dslplatform.json.CompiledJson.Format.ARRAY;
import static com.dslplatform.json.CompiledJson.Format.OBJECT;

public class InterfaceTest {

	@CompiledJson
	public static class HasInterface {
		@JsonAttribute(index = 3)
		public int x;
		@JsonAttribute(index = 1)
		public Iface1 i;
		@JsonAttribute(index = 4)
		public List<Iface1> ii;
		@JsonAttribute(index = 2)
		public Iface2 c;
	}

	public interface Iface1 {
		int y();
		void y(int y);
	}

	@CompiledJson(discriminator = "@type", formats = {ARRAY, OBJECT})
	public interface Iface2 {
		int y();
		void y(int y);
	}

	@CompiledJson
	public static class IsIfaceDefault1 implements Iface1 {
		private int y;

		public int y() {
			return y;
		}

		public void y(int y) {
			this.y = y;
		}

		public IsIfaceDefault1(int y) {
			this.y = y;
		}
	}

	@CompiledJson(name = "custom-name")
	public static class IsIfaceCustom1 implements Iface1 {
		private int y;

		public int y() {
			return y;
		}

		public void y(int y) {
			this.y = y;
		}

		public IsIfaceCustom1(int y) {
			this.y = y;
		}
	}

	@CompiledJson
	public static class IsIfaceDefault2 implements Iface2 {
		private int y;

		public int y() {
			return y;
		}

		public void y(int y) {
			this.y = y;
		}

		public IsIfaceDefault2(int y) {
			this.y = y;
		}
	}

	@CompiledJson(name = "custom",formats = {ARRAY,OBJECT})
	public static class IsIfaceCustom2 implements Iface2 {
		private int y;

		public int y() {
			return y;
		}

		public void y(int y) {
			this.y = y;
		}

		public List<Iface2> list;

		public IsIfaceCustom2(int y, List<Iface2> list) {
			this.y = y;
			this.list = list;
		}
	}

	private final DslJson<Object> dslJson = new DslJson<>();

	@Test
	public void roundtripDefault() throws IOException {
		HasInterface hi = new HasInterface();
		hi.x = 505;
		hi.i = new IsIfaceDefault1(-123);
		hi.c = new IsIfaceDefault2(2);
		hi.ii = Collections.singletonList(new IsIfaceDefault1(1));
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(hi, os);
		Assert.assertEquals("{\"i\":{\"$type\":\"com.dslplatform.json.InterfaceTest.IsIfaceDefault1\",\"y\":-123}," +
				"\"c\":{\"@type\":\"com.dslplatform.json.InterfaceTest.IsIfaceDefault2\",\"y\":2}," +
				"\"x\":505,\"ii\":[{\"$type\":\"com.dslplatform.json.InterfaceTest.IsIfaceDefault1\",\"y\":1}]}", os.toString());
		HasInterface res = dslJson.deserialize(HasInterface.class, os.toByteArray(), os.size());
		Assert.assertEquals(hi.x, res.x);
		Assert.assertEquals(hi.i.y(), res.i.y());
		Assert.assertEquals(hi.i.getClass(), res.i.getClass());
		Assert.assertEquals(hi.c.y(), res.c.y());
		Assert.assertEquals(hi.c.getClass(), res.c.getClass());
		Assert.assertEquals(hi.ii.size(), res.ii.size());
		Assert.assertEquals(hi.ii.get(0).y(), res.ii.get(0).y());
		Assert.assertEquals(hi.ii.get(0).getClass(), res.ii.get(0).getClass());
	}

	@Test
	public void roundtripCustom() throws IOException {
		HasInterface hi = new HasInterface();
		hi.x = 505;
		hi.i = new IsIfaceCustom1(-123);
		hi.c = new IsIfaceCustom2(2, Arrays.asList(null, new IsIfaceCustom2(3, Collections.emptyList())));
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(hi, os);
		Assert.assertEquals("{\"i\":{\"$type\":\"custom-name\",\"y\":-123},\"c\":{\"@type\":\"custom\",\"y\":2,\"list\":[null,{\"@type\":\"custom\",\"y\":3,\"list\":[]}]},\"x\":505,\"ii\":null}", os.toString());
		HasInterface res = dslJson.deserialize(HasInterface.class, os.toByteArray(), os.size());
		Assert.assertEquals(hi.x, res.x);
		Assert.assertEquals(hi.i.y(), res.i.y());
		Assert.assertEquals(hi.i.getClass(), res.i.getClass());
		Assert.assertEquals(hi.c.y(), res.c.y());
		Assert.assertEquals(hi.c.getClass(), res.c.getClass());
	}

	@Test
	public void usingInterfaceDirectly() throws IOException {
		DslJson dslJsonRuntime = new DslJson(Settings.basicSetup());
		List<Iface2> list = Arrays.asList(null, new IsIfaceCustom2(3, Collections.emptyList()));
		JsonWriter writer = dslJsonRuntime.newWriter();
		dslJsonRuntime.serialize(writer, new TypeDefinition<List<Iface2>>(){}.type, list);
		Assert.assertEquals("[null,{\"@type\":\"custom\",\"y\":3,\"list\":[]}]", writer.toString());
		List<Iface2> res = (List) dslJson.deserialize(new TypeDefinition<List<Iface2>>(){}.type, writer.getByteBuffer(), writer.size());
		Assert.assertEquals(2, res.size());
		Assert.assertEquals(IsIfaceCustom2.class, res.get(1).getClass());
	}

	@Test
	public void withArrayFormat() throws IOException {
		DslJson dslJsonRuntime = new DslJson(Settings.basicSetup().allowArrayFormat(true));
		List<Iface2> list = Arrays.asList(null, new IsIfaceCustom2(3, Collections.emptyList()));
		JsonWriter writer = dslJsonRuntime.newWriter();
		dslJsonRuntime.serialize(writer, new TypeDefinition<List<Iface2>>(){}.type, list);
		Assert.assertEquals("[null,[\"custom\",3,[]]]", writer.toString());
		List<Iface2> res = (List) dslJson.deserialize(new TypeDefinition<List<Iface2>>(){}.type, writer.getByteBuffer(), writer.size());
		Assert.assertEquals(2, res.size());
		Assert.assertEquals(IsIfaceCustom2.class, res.get(1).getClass());
	}
}
