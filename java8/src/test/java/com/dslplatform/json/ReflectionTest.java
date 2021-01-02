package com.dslplatform.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.json.bind.annotation.JsonbCreator;

import org.junit.Assert;
import org.junit.Test;

import com.dslplatform.json.runtime.Settings;
import com.dslplatform.json.runtime.TypeDefinition;

public class ReflectionTest {

	public enum MyEnum {
		A,
		B;
	}

	public static class SimpleClass {
		private String y1;
		public int x;

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

	public static class WithGenericSuperclass extends Generic<String> {

	}

	public static class Generic<T> {
		public T property;
	}

	public static class PassThroughTypeParameterClass<PassThroughType> extends PassThroughTypeParameterChangesNameClass<PassThroughType, String> {

		private PassThroughType passThroughTypeProperty;

		public PassThroughType getPassThroughTypeProperty() {
			return passThroughTypeProperty;
		}

		public void setPassThroughTypeProperty(PassThroughType passThroughTypeProperty) {
			this.passThroughTypeProperty = passThroughTypeProperty;
		}
	}

	public static class GenericLevel2<T> extends Generic<T> {

	}

	public static class GenericLevel3<T> extends GenericLevel2<T> {

	}

	public static class Level1PropertyClass {
		public String s;
	}

	public static class Level2PropertyClass extends Level1PropertyClass {

	}

	public static class Level3PropertyClass extends Level2PropertyClass{

	}

	public static abstract class PassThroughTypeParameterChangesNameClass<PassThroughTypeChangedName, PassThroughType extends String> {

		private PassThroughType[] property;
		private PassThroughTypeChangedName passThroughWithChangedTypeNameProperty;

		public PassThroughTypeChangedName getPassThroughWithChangedTypeNameProperty() {
			return passThroughWithChangedTypeNameProperty;
		}

		public void setPassThroughWithChangedTypeNameProperty(PassThroughTypeChangedName passThroughWithChangedTypeNameProperty) {
			this.passThroughWithChangedTypeNameProperty = passThroughWithChangedTypeNameProperty;
		}

		public PassThroughType[] getProperty() {
			return property;
		}

		public void setProperty(PassThroughType[] property) {
			this.property = property;
		}
	}

	public static class ClassWithTypeParameterHavingGenericBound<T extends Generic<?>> {
		private T propertyWithGenericBound;

		public T getPropertyWithGenericBound() {
			return propertyWithGenericBound;
		}

		public void setPropertyWithGenericBound(T propertyWithGenericBound) {
			this.propertyWithGenericBound = propertyWithGenericBound;
		}
	}

	public static class ClassExtendingClassWithBoundTypeParameter extends ClassWithBoundTypeParameter<String> {

	}

	public static class ClassWithBoundTypeParameter<T extends String> {

		private T[] property;

		public T[] getProperty() {
			return property;
		}

		public void setProperty(T[] property) {
			this.property = property;
		}
	}

	public static class Immutable {
		public final int x;
		public final String s;

		public Immutable(int x, String s) {
			this.x = x;
			this.s = s;
		}
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

	public static class MyBind {
		private int i;
		private String s;

		public int i() {
			return i;
		}

		public void i(int value) {
			i = value;
		}

		public String s() {
			return s;
		}

		public void s(String value) {
			s = value;
		}
	}

	public static class ColProp {
		public List<MyBind> binds;
	}

	public static class Info {
		public static class Item {
			public String id;
			public int index;
		}

		public List<Item> info;
	}

	public static class MyEnumLists {
		private List<MyEnum> enums2;
		public List<MyEnum> enums1;

		public void setEnums2(List<MyEnum> value) {
			enums2 = value;
		}

		public List<MyEnum> getEnums2() {
			return enums2;
		}
	}

	public static class ObjCol {
		public Collection collection;
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

	public static class SkipMe {
		public int x;
		public String s;
	}

	public static class SkipMeImmutable {
		private int x;
		private String s;

		public SkipMeImmutable(String s, int x) {
			this.x = x;
			this.s = s;
		}

		public int x() {
			return x;
		}

		public String s() {
			return s;
		}
	}

	public static class ImmutableWithIsBean {
		private boolean isActive;
		private String name;

		public ImmutableWithIsBean(String name, boolean isActive) {
			this.name = name;
			this.isActive = isActive;
		}

		public boolean isActive() {
			return isActive;
		}

		public String getName() {
			return name;
		}
	}

	public static class ImmutableWithoutIsBean {
		private boolean active;
		private String name;

		public ImmutableWithoutIsBean(String name, boolean active) {
			this.name = name;
			this.active = active;
		}

		public boolean isActive() {
			return active;
		}

		public String getName() {
			return name;
		}
	}

	public static class UppercaseName {
		private int doc;

		public int getDOC() {
			return doc;
		}

		public void setDOC(int value) {
			doc = value;
		}
	}

	public static class CtorWithDepsFields {

		private final Service service;
		public final String s;
		public final int x;

		public CtorWithDepsFields(Service service, int x, String s) {
			this.service = service;
			this.x = x;
			this.s = s;
		}
	}

	public static class CtorWithDepsMethods {

		private final Service service;
		private final int x;
		private final String s;

		public CtorWithDepsMethods(int x, Service service, String s) {
			this.service = service;
			this.x = x;
			this.s = s;
		}

		public int getX() {
			return x;
		}

		public String getS() {
			return s;
		}
	}

	public static class MutableWithDeps {

		private final Service service;
		public int x;
		public String s;

		public MutableWithDeps(Service service) {
			this.service = service;
		}
	}

	public static class CtorWithMarker {

		private final Service service;
		public int x;
		public String s;

		public CtorWithMarker() {
			this.service = null;
		}

		@JsonbCreator
		public CtorWithMarker(Service service) {
			this.service = service;
		}
	}

	public static class FactoryWithMarker {

		@JsonbCreator
		private static FactoryWithMarker factory(Service service) {
			return new FactoryWithMarker(service);
		}

		private final Service service;
		public int x;
		public String s;

		private FactoryWithMarker(Service service) {
			this.service = service;
		}

		public FactoryWithMarker() {
			this(null);
		}
	}

	public static class PrivateCtorWithDepsFields {

		@JsonbCreator
		private static PrivateCtorWithDepsFields create(int x, String s, Service service) {
			return new PrivateCtorWithDepsFields(service, x, s);
		}

		private final Service service;
		public final String s;
		public final int x;

		private PrivateCtorWithDepsFields(Service service, int x, String s) {
			this.service = service;
			this.x = x;
			this.s = s;
		}
	}

	public static class MultipleCtorWithDepsMethods {

		private final Service service;
		private final int x;
		private final String s;

		@JsonbCreator
		private MultipleCtorWithDepsMethods(int x, Service service, String s) {
			this.service = service;
			this.x = x;
			this.s = s;
		}

		public MultipleCtorWithDepsMethods(Service service) {
			this.service = service;
			this.x = 1;
			this.s = "a";
		}

		public int getX() {
			return x;
		}

		public String getS() {
			return s;
		}
	}

	class Service {
	}

	private final DslJson<Object> json = new DslJson<Object>(Settings.withRuntime().includeServiceLoader());

	private <T> T deserialize(TypeDefinition<T> td, InputStream is) throws IOException {
		return (T) json.deserialize(td.type, is);
	}

	private <T> T deserialize(TypeDefinition<T> td, byte[] bytes) throws IOException {
		return (T) json.deserialize(td.type, bytes, bytes.length);
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
	public void checkSimpleMinimal() throws IOException {
		DslJson<Object> jsonMin = new DslJson<Object>(Settings.withRuntime().skipDefaultValues(true).includeServiceLoader());
		SimpleClass sc = new SimpleClass();
		sc.x = 12;
		sc.setY("abc");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		jsonMin.serialize(sc, baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		SimpleClass sc2 = jsonMin.deserialize(SimpleClass.class, bais);
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

	@Test
	public void checkGeneric() throws IOException {
		Generic<String> str = new Generic<>();
		str.property = "abc";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(str, baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Generic<String> sc2 = deserialize(new TypeDefinition<Generic<String>>() {
		}, bais);
		Assert.assertEquals(str.property, sc2.property);
	}

	@Test
	public void checkGenericSuperclass() throws IOException {
		byte[] bytes = "{\"property\":\"abc\"}".getBytes("UTF-8");
		WithGenericSuperclass deser = json.deserialize(WithGenericSuperclass.class, bytes, bytes.length);
		Assert.assertEquals("abc", deser.property);
	}


	@Test
	public void checkBoundTypeParameter() throws IOException {
		byte[] bytes = "{\"property\":[\"abc\"]}".getBytes("UTF-8");
		ClassExtendingClassWithBoundTypeParameter deser = json.deserialize(ClassExtendingClassWithBoundTypeParameter.class, bytes, bytes.length);
		Assert.assertArrayEquals(new String[]{"abc"}, deser.getProperty());
	}

	@Test
	public void checkPassingActualTypeWhenTypeParameterNameChanges() throws IOException {
		byte[] bytes = "{\"property\":[\"abc\"],\"passThroughTypeProperty\": true, \"passThroughWithChangedTypeNameProperty\": true }".getBytes("UTF-8");
		PassThroughTypeParameterClass<Boolean> deser = deserialize(new TypeDefinition<PassThroughTypeParameterClass<Boolean>>(){},bytes);
		Assert.assertArrayEquals(new String[]{"abc"}, deser.getProperty());
		Assert.assertTrue(deser.getPassThroughWithChangedTypeNameProperty());
		Assert.assertTrue(deser.getPassThroughTypeProperty());
	}

	@Test
	public void checkActualTypeMappingOfGenericBound() throws IOException {
		byte[] bytes = "{\"propertyWithGenericBound\":{\"property\":\"abc\"}}".getBytes("UTF-8");
		ClassWithTypeParameterHavingGenericBound<GenericLevel2<String>> deser = deserialize(new TypeDefinition<ClassWithTypeParameterHavingGenericBound<GenericLevel2<String>>>(){}, bytes);
		Assert.assertEquals("abc", deser.getPropertyWithGenericBound().property);
	}

	@Test
	public void checkPassingActualTypeThroughDeepClassHierarchy() throws IOException {
		byte[] bytes = "{\"property\":{\"s\":\"abc\"}}".getBytes("UTF-8");
		GenericLevel3<Level3PropertyClass> deser = deserialize(new TypeDefinition<GenericLevel3<Level3PropertyClass>>(){}, bytes);
		Assert.assertEquals("abc", deser.property.s);
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

	@Test
	public void checkSameTypeImmutable() throws IOException {
		SameTypeImmutable im1 = new SameTypeImmutable(1, "b", "a", 2L);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(im1, baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		SameTypeImmutable im2 = json.deserialize(SameTypeImmutable.class, bais);
		Assert.assertEquals(im2.x, im1.x);
		Assert.assertEquals(im2.s, im1.s);
		Assert.assertEquals(im2.e, im1.e);
		Assert.assertEquals(im2.l, im1.l);
	}

	@Test
	public void checkOptional() throws IOException {
		Opt<String> im1 = new Opt<>(Optional.of(5), Optional.of("abc"), Optional.empty());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(im1, baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Opt<String> im2 = deserialize(new TypeDefinition<Opt<String>>() {
		}, bais);
		Assert.assertEquals(im2.x, im1.x);
		Assert.assertEquals(im2.x.get(), im1.x.get());
		Assert.assertEquals(im2.s, im1.s);
		Assert.assertEquals(im2.s.get(), im1.s.get());
		Assert.assertEquals(im2.self, im1.self);
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

	@Test
	public void readSingleCollectionProperty() throws IOException {
		String input = "{\"info\":[{\"id\":\"37874220710194827570\",\"index\":359465317}]}";
		byte[] bytes = input.getBytes();
		Info value = json.deserialize(Info.class, bytes, bytes.length);
		Assert.assertEquals(1, value.info.size());
		Assert.assertEquals("37874220710194827570", value.info.get(0).id);
		Assert.assertEquals(359465317, value.info.get(0).index);
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

		Map<MyEnum, Object> ts = deserialize(new TypeDefinition<Map<MyEnum, Object>>() {
		}, bytes);
		Assert.assertEquals(map, ts);
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

	@Test
	public void primitiveDefaults() throws IOException {
		byte[] bytes = "{}".getBytes();
		ImmutableDefaults def = json.deserialize(ImmutableDefaults.class, bytes, bytes.length);
		Assert.assertEquals(0.0, def.d, 0);
		Assert.assertEquals(0.0, def.f, 0);
		Assert.assertEquals(0, def.i);
		Assert.assertEquals(0, def.s);
		Assert.assertEquals(0L, def.l);
		Assert.assertEquals((byte) 0, def.b);
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
		Assert.assertEquals(4L, ((Concrete) me2.list1.get(1)).y);
		Assert.assertEquals(me1.list2.size(), me2.list2.size());
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

	@Test
	public void uppercaseName() throws IOException {
		UppercaseName val = new UppercaseName();
		val.setDOC(505);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(val, baos);
		Assert.assertEquals("{\"DOC\":505}", baos.toString("UTF-8"));
		byte[] bytes = baos.toByteArray();
		UppercaseName deser = json.deserialize(UppercaseName.class, bytes, bytes.length);
		Assert.assertEquals(val.getDOC(), deser.getDOC());
	}

	@Test
	public void canInjectDependencyWithImmutableFields() throws IOException {
		Service s = new Service();
		DslJson<Service> json = new DslJson<Service>(Settings.<Service>withRuntime().withContext(s).includeServiceLoader());
		byte[] bytes = "{\"x\":5,\"s\":\"x\"}".getBytes("UTF-8");
		CtorWithDepsFields deser = json.deserialize(CtorWithDepsFields.class, bytes, bytes.length);
		Assert.assertEquals(5, deser.x);
		Assert.assertEquals("x", deser.s);
		Assert.assertEquals(s, deser.service);
	}

	@Test
	public void canInjectDependencyWithImmutableMethods() throws IOException {
		Service s = new Service();
		DslJson<Service> json = new DslJson<Service>(Settings.<Service>withRuntime().withContext(s).includeServiceLoader());
		byte[] bytes = "{\"x\":5,\"s\":\"x\"}".getBytes("UTF-8");
		CtorWithDepsMethods deser = json.deserialize(CtorWithDepsMethods.class, bytes, bytes.length);
		Assert.assertEquals(5, deser.x);
		Assert.assertEquals("x", deser.s);
		Assert.assertEquals(s, deser.service);
	}

	@Test
	public void canInjectDependencyWithMutableFields() throws IOException {
		Service s = new Service();
		DslJson<Object> json = new DslJson<Object>(Settings.withRuntime().withContext(s).includeServiceLoader());
		byte[] bytes = "{\"x\":5,\"s\":\"x\"}".getBytes("UTF-8");
		MutableWithDeps deser = json.deserialize(MutableWithDeps.class, bytes, bytes.length);
		Assert.assertEquals(5, deser.x);
		Assert.assertEquals("x", deser.s);
		Assert.assertEquals(s, deser.service);
	}

	@Test
	public void willUseMarkedCtor() throws IOException {
		Service s = new Service();
		DslJson<Object> json = new DslJson<Object>(Settings.withRuntime().withContext(s).creatorMarker(JsonbCreator.class, true).includeServiceLoader());
		byte[] bytes = "{\"x\":5,\"s\":\"x\"}".getBytes("UTF-8");
		CtorWithMarker deser = json.deserialize(CtorWithMarker.class, bytes, bytes.length);
		Assert.assertEquals(5, deser.x);
		Assert.assertEquals("x", deser.s);
		Assert.assertEquals(s, deser.service);
	}

	@Test
	public void willUseMarkedFactory() throws IOException {
		Service s = new Service();
		DslJson<Object> json = new DslJson<Object>(Settings.withRuntime().withContext(s).creatorMarker(JsonbCreator.class, true).includeServiceLoader());
		byte[] bytes = "{\"x\":5,\"s\":\"x\"}".getBytes("UTF-8");
		FactoryWithMarker deser = json.deserialize(FactoryWithMarker.class, bytes, bytes.length);
		Assert.assertEquals(5, deser.x);
		Assert.assertEquals("x", deser.s);
		Assert.assertEquals(s, deser.service);
	}

	@Test
	public void canInjectDependencyWithImmutableAndFactory() throws IOException {
		Service s = new Service();
		DslJson<Object> json = new DslJson<Object>(Settings.withRuntime().withContext(s).creatorMarker(JsonbCreator.class, true).includeServiceLoader());
		byte[] bytes = "{\"x\":5,\"s\":\"x\"}".getBytes("UTF-8");
		PrivateCtorWithDepsFields deser = json.deserialize(PrivateCtorWithDepsFields.class, bytes, bytes.length);
		Assert.assertEquals(5, deser.x);
		Assert.assertEquals("x", deser.s);
		Assert.assertEquals(s, deser.service);
	}

	@Test
	public void willPickCorrectCtorWithImmutables() throws IOException {
		Service s = new Service();
		DslJson<Object> json = new DslJson<Object>(Settings.withRuntime().withContext(s).creatorMarker(JsonbCreator.class, true).includeServiceLoader());
		byte[] bytes = "{\"x\":5,\"s\":\"x\"}".getBytes("UTF-8");
		MultipleCtorWithDepsMethods deser = json.deserialize(MultipleCtorWithDepsMethods.class, bytes, bytes.length);
		Assert.assertEquals(5, deser.x);
		Assert.assertEquals("x", deser.s);
		Assert.assertEquals(s, deser.service);
	}

	@Test
	public void immutableWithIsBean() throws IOException {
		ImmutableWithIsBean inst = new ImmutableWithIsBean("abc", true);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		json.serialize(inst, os);
		ImmutableWithIsBean deser = json.deserialize(ImmutableWithIsBean.class, os.toByteArray(), os.size());
		Assert.assertEquals("abc", deser.getName());
		Assert.assertEquals(true, deser.isActive);
	}

	@Test
	public void immutableWithoutIsBean() throws IOException {
		ImmutableWithoutIsBean inst = new ImmutableWithoutIsBean("abc", true);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		json.serialize(inst, os);
		ImmutableWithoutIsBean deser = json.deserialize(ImmutableWithoutIsBean.class, os.toByteArray(), os.size());
		Assert.assertEquals("abc", deser.getName());
		Assert.assertEquals(true, deser.active);
	}
}
