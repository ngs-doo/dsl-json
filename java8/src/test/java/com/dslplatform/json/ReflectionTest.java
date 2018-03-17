package com.dslplatform.json;

import com.dslplatform.json.runtime.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ReflectionTest {

	private final DslJson<Object> json = new DslJson<Object>(Settings.withRuntime().includeServiceLoader());

	public static class SimpleClass {
		public int x;
		private String y1;
		public String getY() {
			return y1;
		}
		public void setY(String v) {
			y1 = v;
		}
	}
	public static class Referencing {
		public SimpleClass sc;
		public Referencing self;
	}

	@Test
	public void checkSimple() throws IOException {
		SimpleClass sc = new SimpleClass();
		sc.x = 12;
		sc.setY("abc");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(sc, baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		SimpleClass sc2 = json.deserialize(SimpleClass.class, bais);
		Assert.assertEquals(sc.x, sc2.x);
		Assert.assertEquals(sc.getY(), sc2.getY());
	}

	@Test
	public void selfReference() throws IOException {
		SimpleClass sc1 = new SimpleClass();
		sc1.x = 12;
		sc1.setY("abc");
		SimpleClass sc2 = new SimpleClass();
		sc2.x = 2;
		Referencing r1 = new Referencing();
		Referencing r2 = new Referencing();
		r1.sc = sc1;
		r1.self = r2;
		r2.sc = sc2;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(r1, baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Referencing r3 = json.deserialize(Referencing.class, bais);
		Assert.assertEquals(r3.sc.x, sc1.x);
		Assert.assertEquals(r3.self.sc.x, sc2.x);
	}

	public static class Generic<T> {
		public T property;
	}

	private <T> T deserialize(TypeDefinition<T> td, InputStream is) throws IOException {
		return (T)json.deserialize(td.type, is);
	}

	private <T> T deserialize(TypeDefinition<T> td, byte[] bytes) throws IOException {
		return (T)json.deserialize(td.type, bytes, bytes.length);
	}

	@Test
	public void checkGeneric() throws IOException {
		Generic<String> str = new Generic<>();
		str.property = "abc";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(str, baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Generic<String> sc2 = deserialize(new TypeDefinition<Generic<String>>(){}, bais);
		Assert.assertEquals(str.property, sc2.property);
	}

	public static class Immutable {
		public final int x;
		public final String s;

		public Immutable(int x, String s) {
			this.x = x;
			this.s = s;
		}
	}

	@Test
	public void checkImmutable() throws IOException {
		Immutable im1 = new Immutable(5, "abc");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(im1, baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Immutable im2 = json.deserialize(Immutable.class, bais);
		Assert.assertEquals(im2.x, im1.x);
		Assert.assertEquals(im2.s, im1.s);
	}

	public static class SameTypeImmutable {
		public final int x;
		public final String s;
		public final String e;
		public final long l;

		public SameTypeImmutable(int x, String s, String e, long l) {
			this.x = x;
			this.s = s;
			this.e = e;
			this.l = l;
		}
	}

	@Test
	public void checkSameTypeImmutable() throws IOException {
		SameTypeImmutable im1 = new SameTypeImmutable(1,"b", "a", 2L);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(im1, baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		SameTypeImmutable im2 = json.deserialize(SameTypeImmutable.class, bais);
		Assert.assertEquals(im2.x, im1.x);
		Assert.assertEquals(im2.s, im1.s);
		Assert.assertEquals(im2.e, im1.e);
		Assert.assertEquals(im2.l, im1.l);
	}

	public static class Opt<T> {
		public final Optional<Integer> x;
		public final Optional<T> s;
		public final Optional<Opt<T>> self;

		public Opt(Optional<Integer> x, Optional<T> s, Optional<Opt<T>> self) {
			this.x = x;
			this.s = s;
			this.self = self;
		}
	}

	@Test
	public void checkOptional() throws IOException {
		Opt<String> im1 = new Opt<>(Optional.of(5), Optional.of("abc"), Optional.empty());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(im1, baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Opt<String> im2 = deserialize(new TypeDefinition<Opt<String>>(){}, bais);
		Assert.assertEquals(im2.x, im1.x);
		Assert.assertEquals(im2.x.get(), im1.x.get());
		Assert.assertEquals(im2.s, im1.s);
		Assert.assertEquals(im2.s.get(), im1.s.get());
		Assert.assertEquals(im2.self, im1.self);
	}

	public static class MyBind {
		private int i;
		public int i() { return i; }
		public void i(int value) { i = value; }
		private String s;
		public String s() { return s; }
		public void s(String value) { s = value; }
	}

	@Test
	public void bindObject() throws IOException {
		String input = "{\"i\":12,\"s\":\"abc\"}";
		byte[] bytes = input.getBytes();
		JsonReader<Object> reader = json.newReader().process(bytes, bytes.length);
		MyBind instance = new MyBind();
		MyBind bound = reader.next(MyBind.class, instance);
		Assert.assertEquals(12, bound.i);
		Assert.assertEquals("abc", bound.s);
		Assert.assertSame(instance, bound);

		reader = json.newReader().process(bytes, bytes.length);
		JsonReader.BindObject<MyBind> binder = json.tryFindBinder(MyBind.class);
		bound = reader.next(binder, instance);
		Assert.assertEquals(12, bound.i);
		Assert.assertEquals("abc", bound.s);
		Assert.assertSame(instance, bound);

		reader = json.newReader().process(bytes, bytes.length);
		JsonReader.ReadObject<MyBind> rdr = json.tryFindReader(MyBind.class);
		instance = reader.next(rdr);
		Assert.assertEquals(12, instance.i);
		Assert.assertEquals("abc", instance.s);
	}

	@Test
	public void readCollection() throws IOException {
		String input = "[{\"i\":12,\"s\":\"abc\"},{\"i\":13,\"s\":\"def\"}]";
		byte[] bytes = input.getBytes();
		MyBind[] values = json.deserialize(MyBind[].class, bytes, bytes.length);
		Assert.assertEquals(2, values.length);
		Assert.assertEquals(12, values[0].i);
		Assert.assertEquals("abc", values[0].s);
		Assert.assertEquals(13, values[1].i);
		Assert.assertEquals("def", values[1].s);
	}

	public static class ColProp {
		public List<MyBind> binds;
	}

	@Test
	public void readCollectionProperty() throws IOException {
		String input = "{\"binds\":[{\"i\":12,\"s\":\"abc\"},{\"i\":13,\"s\":\"def\"}]}";
		byte[] bytes = input.getBytes();
		ColProp value = json.deserialize(ColProp.class, bytes, bytes.length);
		Assert.assertEquals(2, value.binds.size());
		Assert.assertEquals(12, value.binds.get(0).i);
		Assert.assertEquals("abc", value.binds.get(0).s);
		Assert.assertEquals(13, value.binds.get(1).i);
		Assert.assertEquals("def", value.binds.get(1).s);
	}

	public static class Info {
		public List<Item> info;

		public static class Item {
			public String id;
			public int index;
		}
	}

	@Test
	public void readSingleCollectionProperty() throws IOException {
		String input = "{\"info\":[{\"id\":\"37874220710194827570\",\"index\":359465317}]}";
		byte[] bytes = input.getBytes();
		Info value = json.deserialize(Info.class, bytes, bytes.length);
		Assert.assertEquals(1, value.info.size());
		Assert.assertEquals("37874220710194827570", value.info.get(0).id);
		Assert.assertEquals(359465317, value.info.get(0).index);
	}

	public enum MyEnum {
		A,
		B;
	}

	@Test
	public void canUseEnum() throws IOException {
		String input = "[\"A\",\"B\"]";
		byte[] bytes = input.getBytes();
		Enum[] enums = json.deserialize(MyEnum[].class, bytes, bytes.length);
		Assert.assertArrayEquals(new MyEnum[]{MyEnum.A, MyEnum.B}, enums);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(new MyEnum[]{MyEnum.B, null, MyEnum.A}, baos);
		Assert.assertEquals("[\"B\",null,\"A\"]", baos.toString("UTF-8"));
	}

	public static class MyEnumLists {
		public List<MyEnum> enums1;
		private List<MyEnum> enums2;
		public void setEnums2(List<MyEnum> value) {
			enums2 = value;
		}
		public List<MyEnum> getEnums2() {
			return enums2;
		}
	}

	@Test
	public void canUseEnumLists() throws IOException {
		MyEnumLists me1 = new MyEnumLists();
		me1.enums1 = Arrays.asList(MyEnum.A, MyEnum.B);
		me1.setEnums2(new ArrayList<>());
		me1.getEnums2().add(MyEnum.B);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(me1, baos);

		byte[] bytes = baos.toByteArray();
		MyEnumLists me2 = json.deserialize(MyEnumLists.class, bytes, bytes.length);
		Assert.assertEquals(me1.enums1, me2.enums1);
		Assert.assertEquals(me1.enums2, me2.enums2);
	}

	@Test
	public void enumAsMapKey() throws IOException {
		Map<MyEnum, Object> map = new HashMap<>();
		map.put(MyEnum.B, 1L);
		map.put(MyEnum.A, "abc");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(map, baos);

		byte[] bytes = baos.toByteArray();
		Map deser = json.deserialize(map.getClass(), bytes, bytes.length);
		Assert.assertEquals(2, deser.size());
		Assert.assertEquals(1L, deser.get("B"));
		Assert.assertEquals("abc", deser.get("A"));

		Map<MyEnum, Object> ts = deserialize(new TypeDefinition<Map<MyEnum, Object>>(){}, bytes);
		Assert.assertEquals(map, ts);
	}

	public static class ObjCol {
		public Collection collection;
	}

	@Test
	public void unknownCollectionElements() throws IOException {
		ObjCol col = new ObjCol();
		col.collection = Arrays.asList(1L, "abc", null, true);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(col, baos);

		byte[] bytes = baos.toByteArray();
		ObjCol deser = json.deserialize(ObjCol.class, bytes, bytes.length);
		Assert.assertEquals(col.collection, deser.collection);
	}

	public static class ImmutableDefaults {
		public final double d;
		public final float f;
		public final int i;
		public final short s;
		public final long l;
		public final byte b;
		public ImmutableDefaults(double d, float f, int i, short s, long l, byte b) {
			this.d = d;
			this.f = f;
			this.i = i;
			this.s = s;
			this.l = l;
			this.b = b;
		}
	}

	@Test
	public void primitiveDefaults() throws IOException {
		byte[] bytes = "{}".getBytes();
		ImmutableDefaults def = json.deserialize(ImmutableDefaults.class, bytes, bytes.length);
		Assert.assertEquals(0.0, def.d, 0);
		Assert.assertEquals(0.0, def.f, 0);
		Assert.assertEquals(0, def.i);
		Assert.assertEquals(0, def.s);
		Assert.assertEquals(0L, def.l);
		Assert.assertEquals((byte)0, def.b);
	}

	@Test
	public void testNestedCollection() throws IOException {
		JsonWriter jw = json.newWriter();
		json.serializeMap(
				Collections.singletonMap("x",
						Collections.singletonList(Collections.singletonList("Hello"))
				),
				jw
		);
		Assert.assertEquals("{\"x\":[[\"Hello\"]]}", jw.toString());
	}

	public static abstract class Abstract {
		public int x;
	}
	public static class Concrete extends Abstract {
		public long y;
	}

	public static class AbstractLists {
		public List<Abstract> list1;
		public List list2;
	}

	@Test
	public void canUseAbstractList() throws IOException {
		DslJson<Object> json = new DslJson<Object>(Settings.withRuntime());
		json.registerReader(Abstract.class, json.tryFindReader(Concrete.class));
		AbstractLists me1 = new AbstractLists();
		Concrete c1 = new Concrete();
		c1.y = 4L;
		c1.x = 2;
		me1.list1 = Arrays.asList(null, c1);
		me1.list2 = Arrays.asList(c1, null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(me1, baos);
		byte[] bytes = baos.toByteArray();
		AbstractLists me2 = json.deserialize(AbstractLists.class, bytes, bytes.length);
		Assert.assertEquals(me1.list1.size(), me2.list1.size());
		Assert.assertNull(me2.list1.get(0));
		Assert.assertNull(me2.list1.get(0));
		Assert.assertTrue(me2.list1.get(1) instanceof Concrete);
		Assert.assertEquals(2, me2.list1.get(1).x);
		Assert.assertTrue(me2.list1.get(1) instanceof Concrete);
		Assert.assertEquals(4L, ((Concrete)me2.list1.get(1)).y);
		Assert.assertEquals(me1.list2.size(), me2.list2.size());
	}

	public static class SkipMe {
		public int x;
		public String s;
	}

	@Test
	public void canSkipOverOnObjects() throws IOException {
		String input = "{\"x\":1,\"y\":\"abc\",\"s\":null,\"a\":5}";
		byte[] bytes = input.getBytes();
		SkipMe skip = json.deserialize(SkipMe.class, bytes, bytes.length);
		Assert.assertEquals(1, skip.x);
		Assert.assertNull(skip.s);
		input = "{\"s\":null,\"y\":\"abc\",\"x\":1,\"a\":5}";
		bytes = input.getBytes();
		skip = json.deserialize(SkipMe.class, bytes, bytes.length);
		Assert.assertEquals(1, skip.x);
		Assert.assertNull(skip.s);
		input = "{\"b\":null, \"x\":1,\"y\":\"abc\",\"s\":null,\"a\":5}";
		bytes = input.getBytes();
		skip = json.deserialize(SkipMe.class, bytes, bytes.length);
		Assert.assertEquals(1, skip.x);
		Assert.assertNull(skip.s);
		input = "{\"x\":1,\"s\":null,\"a\":5}";
		bytes = input.getBytes();
		skip = json.deserialize(SkipMe.class, bytes, bytes.length);
		Assert.assertEquals(1, skip.x);
		Assert.assertNull(skip.s);
		input = "{\"s\":null,\"x\":1,\"a\":5}";
		bytes = input.getBytes();
		skip = json.deserialize(SkipMe.class, bytes, bytes.length);
		Assert.assertEquals(1, skip.x);
		Assert.assertNull(skip.s);
		input = "{\"b\":null, \"x\":1,\"y\":\"abc\",\"s\":null}";
		bytes = input.getBytes();
		skip = json.deserialize(SkipMe.class, bytes, bytes.length);
		Assert.assertEquals(1, skip.x);
		Assert.assertNull(skip.s);
	}

	public static class SkipMeImmutable {
		private int x;
		private String s;
		public SkipMeImmutable(String s, int x) {
			this.x = x;
			this.s = s;
		}
		public int x() { return x; }
		public String s() { return s; }
	}

	@Test
	public void canSkipOverOnImmutables() throws IOException {
		String input = "{\"x\":1,\"y\":\"abc\",\"s\":null,\"a\":5}";
		byte[] bytes = input.getBytes();
		SkipMeImmutable skip = json.deserialize(SkipMeImmutable.class, bytes, bytes.length);
		Assert.assertEquals(1, skip.x);
		Assert.assertNull(skip.s);
		input = "{\"s\":null,\"y\":\"abc\",\"x\":1,\"a\":5}";
		bytes = input.getBytes();
		skip = json.deserialize(SkipMeImmutable.class, bytes, bytes.length);
		Assert.assertEquals(1, skip.x);
		Assert.assertNull(skip.s);
		input = "{\"b\":null, \"x\":1,\"y\":\"abc\",\"s\":null,\"a\":5}";
		bytes = input.getBytes();
		skip = json.deserialize(SkipMeImmutable.class, bytes, bytes.length);
		Assert.assertEquals(1, skip.x);
		Assert.assertNull(skip.s);
		input = "{\"s\":null,\"x\":1,\"a\":5}";
		bytes = input.getBytes();
		skip = json.deserialize(SkipMeImmutable.class, bytes, bytes.length);
		Assert.assertEquals(1, skip.x);
		Assert.assertNull(skip.s);
		input = "{\"b\":null, \"x\":1,\"y\":\"abc\",\"s\":null}";
		bytes = input.getBytes();
		skip = json.deserialize(SkipMeImmutable.class, bytes, bytes.length);
		Assert.assertEquals(1, skip.x);
		Assert.assertNull(skip.s);
	}
}
