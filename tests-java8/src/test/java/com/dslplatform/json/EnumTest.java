package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class EnumTest {

	@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
	public enum MyEnum1 {
		ABC,
		DEF,
		GHI;
	}

	public enum MyEnum2 {
		XX1,
		YY,
		ZZ2;
	}

	@CompiledJson
	public enum DuplicateHash {
		n3307663,
		n519524;
	}

	@CompiledJson
	public static class SingleNonImmutable {
		public MyEnum1 e1;
		private MyEnum2 e2;
		@JsonAttribute(nullable = false)
		public MyEnum1 e3;

		public MyEnum2 e2() {
			return e2;
		}

		public void e2(MyEnum2 v) {
			this.e2 = v;
		}

		public Map<MyEnum1, Integer> map1;
		public List<MyEnum2> list2;
	}

	@CompiledJson
	public static class SingleImmutable {
		public final MyEnum1 e1;
		public final MyEnum2 e2;
		@JsonAttribute(nullable = false)
		public MyEnum1 e3;
		public final Map<MyEnum1, Integer> map1;
		private List<MyEnum2> list2;

		public List<MyEnum2> list2() {
			return list2;
		}

		public SingleImmutable(MyEnum1 e1, MyEnum2 e2, MyEnum1 e3, Map<MyEnum1, Integer> map1, List<MyEnum2> list2) {
			this.e1 = e1;
			this.e2 = e2;
			this.e3 = e3;
			this.map1 = map1;
			this.list2 = list2;
		}
	}

	private final DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime().includeServiceLoader());

	@Test
	public void objectRoundtrip() throws IOException {
		SingleNonImmutable sni = new SingleNonImmutable();
		sni.e1 = MyEnum1.DEF;
		sni.e2(MyEnum2.ZZ2);
		sni.e3 = MyEnum1.GHI;
		sni.map1 = new LinkedHashMap<>();
		sni.map1.put(MyEnum1.ABC, 2);
		sni.map1.put(MyEnum1.GHI, 5);
		sni.list2 = Arrays.asList(MyEnum2.ZZ2, MyEnum2.YY);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(sni, os);
		SingleNonImmutable res = dslJson.deserialize(SingleNonImmutable.class, os.toByteArray(), os.size());
		Assert.assertEquals(sni.e1, res.e1);
		Assert.assertEquals(sni.e2, res.e2);
		Assert.assertEquals(sni.e3, res.e3);
		Assert.assertEquals(sni.map1, res.map1);
		Assert.assertEquals(sni.list2, res.list2);
	}

	@Test
	public void immutableRoundtrip() throws IOException {
		Map map1 = new LinkedHashMap<>();
		map1.put(MyEnum1.ABC, 2);
		map1.put(MyEnum1.GHI, 5);
		SingleImmutable si = new SingleImmutable(
				MyEnum1.DEF,
				MyEnum2.ZZ2,
				MyEnum1.GHI,
				map1,
				Arrays.asList(MyEnum2.ZZ2, MyEnum2.YY));
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(si, os);
		SingleImmutable res = dslJson.deserialize(SingleImmutable.class, os.toByteArray(), os.size());
		Assert.assertEquals(si.e1, res.e1);
		Assert.assertEquals(si.e2, res.e2);
		Assert.assertEquals(si.e3, res.e3);
		Assert.assertEquals(si.map1, res.map1);
		Assert.assertEquals(si.list2, res.list2);
	}

	@Test
	public void errorOnUnknown() throws IOException {
		byte[] json = "{\"e2\":\"A\"}".getBytes("UTF-8");
		try {
			dslJson.deserialize(SingleNonImmutable.class, json, json.length);
			Assert.fail("Exception expected");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e.getMessage().contains("No enum constant com.dslplatform.json.EnumTest.MyEnum2.A"));
		}
	}

	@Test
	public void defaultOnUnknown() throws IOException {
		byte[] json = "{\"e1\":\"A\"}".getBytes("UTF-8");
		SingleNonImmutable v = dslJson.deserialize(SingleNonImmutable.class, json, json.length);
		Assert.assertEquals(MyEnum1.ABC, v.e1);
	}
}