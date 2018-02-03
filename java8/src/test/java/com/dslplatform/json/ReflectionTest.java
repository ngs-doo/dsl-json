package com.dslplatform.json;

import com.dslplatform.json.runtime.BeanAnalyzer;
import com.dslplatform.json.runtime.ImmutableAnalyzer;
import com.dslplatform.json.runtime.OptionalAnalyzer;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

public class ReflectionTest {

	private final DslJson.Settings settings = new DslJson.Settings()
			.resolveWriter(OptionalAnalyzer.CONVERTER)
			.resolveReader(OptionalAnalyzer.CONVERTER)
			.resolveWriter(ImmutableAnalyzer.CONVERTER)
			.resolveReader(ImmutableAnalyzer.CONVERTER)
			.resolveWriter(BeanAnalyzer.CONVERTER)
			.resolveBinder(BeanAnalyzer.CONVERTER)
			.resolveReader(BeanAnalyzer.CONVERTER);
	private final DslJson<Object> json = new DslJson<Object>(settings);

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

	static abstract class TypeDefinition<T> {

		public final Type type;

		public TypeDefinition() {
			type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		}
	}

	@Test
	public void checkGeneric() throws IOException {
		Generic<String> str = new Generic<>();
		str.property = "abc";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(str, baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Generic<String> sc2 = (Generic<String>)json.deserialize(new TypeDefinition<Generic<String>>(){}.type, bais);
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
		Opt<String> im2 = (Opt<String>)json.deserialize(new TypeDefinition<Opt<String>>(){}.type, bais);
		Assert.assertEquals(im2.x, im1.x);
		Assert.assertEquals(im2.x.get(), im1.x.get());
		Assert.assertEquals(im2.s, im1.s);
		Assert.assertEquals(im2.s.get(), im1.s.get());
		Assert.assertEquals(im2.self, im1.self);
	}

}
