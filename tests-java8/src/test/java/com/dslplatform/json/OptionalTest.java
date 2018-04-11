package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class OptionalTest {

	@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
	public static class Composite {
		@JsonAttribute(index = 1)
		public OptionalInt oi;
		@JsonAttribute(index = 2)
		public Optional<String> os;
	}

	@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
	public static class ImmutableComposite {
		@JsonAttribute(index = 1)
		public final OptionalInt oi;
		@JsonAttribute(index = 2)
		public final Optional<String> os;

		public ImmutableComposite(OptionalInt oi, Optional<String> os) {
			this.oi = oi;
			this.os = os;
		}
	}

	private final DslJson<Object> dslJsonArray = new DslJson<>(Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());
	private final DslJson<Object> dslJsonObject = new DslJson<>(Settings.withRuntime().allowArrayFormat(false).includeServiceLoader());
	//TODO: custom defaults
	//private final DslJson<Object> dslJsonMinimal = new DslJson<>(Settings.withRuntime().allowArrayFormat(false).skipDefaultValues(true).includeServiceLoader());

	@Test
	public void objectRoundtrip() throws IOException {
		Composite c = new Composite();
		c.oi = OptionalInt.empty();
		c.os = Optional.empty();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJsonArray.serialize(c, os);
		Assert.assertEquals("[null,null]", os.toString());
		Composite res = dslJsonArray.deserialize(Composite.class, os.toByteArray(), os.size());
		Assert.assertEquals(OptionalInt.empty(), c.oi);
		Assert.assertEquals(Optional.empty(), res.os);
		os.reset();
		dslJsonObject.serialize(c, os);
		Assert.assertEquals("{\"oi\":null,\"os\":null}", os.toString());
		res = dslJsonObject.deserialize(Composite.class, os.toByteArray(), os.size());
		Assert.assertEquals(OptionalInt.empty(), c.oi);
		Assert.assertEquals(Optional.empty(), res.os);
		/*os.reset();
		dslJsonMinimal.serialize(c, os);
		Assert.assertEquals("{}", os.toString());
		res = dslJsonMinimal.deserialize(Composite.class, os.toByteArray(), os.size());
		Assert.assertEquals(OptionalInt.empty(), c.oi);
		Assert.assertEquals(Optional.empty(), res.os);*/
	}

	@Test
	public void immutableRoundtrip() throws IOException {
		ImmutableComposite c = new ImmutableComposite(
				OptionalInt.empty(),
				Optional.empty()
		);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJsonArray.serialize(c, os);
		Assert.assertEquals("[null,null]", os.toString());
		ImmutableComposite res = dslJsonArray.deserialize(ImmutableComposite.class, os.toByteArray(), os.size());
		Assert.assertEquals(OptionalInt.empty(), c.oi);
		Assert.assertEquals(Optional.empty(), res.os);
		os.reset();
		dslJsonObject.serialize(c, os);
		Assert.assertEquals("{\"oi\":null,\"os\":null}", os.toString());
		res = dslJsonObject.deserialize(ImmutableComposite.class, os.toByteArray(), os.size());
		Assert.assertEquals(OptionalInt.empty(), c.oi);
		Assert.assertEquals(Optional.empty(), res.os);
		/*os.reset();
		dslJsonMinimal.serialize(c, os);
		Assert.assertEquals("{\"oi\":null,\"os\":null}", os.toString());
		res = dslJsonMinimal.deserialize(ImmutableComposite.class, os.toByteArray(), os.size());
		Assert.assertEquals(OptionalInt.empty(), c.oi);
		Assert.assertEquals(Optional.empty(), res.os);*/
	}
}
