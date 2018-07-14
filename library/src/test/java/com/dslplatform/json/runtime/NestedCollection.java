package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.runtime.Settings;
import com.dslplatform.json.runtime.TypeDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class NestedCollection {

	private final DslJson<Object> json = new DslJson<Object>(Settings.withRuntime());

	public static class Example {
		public Collection<Collection<Long>> colColLong;
		public Collection<Collection> colColUnknown;
		public Collection<Collection<Example>> colColSelf;
	}

	public static class Generic<T> {
		public Collection<T> colT;
		public Collection<Collection<T>> colColT;
	}

	@Test
	public void testResolution() throws IOException {
		Example wo = new Example();
		wo.colColLong = Arrays.asList(Arrays.asList(1L, 2L), new ArrayList<Long>(), (Collection<Long>) null, Collections.singletonList(3L));
		wo.colColUnknown = Arrays.asList(Arrays.asList(true, "one"), new ArrayList(),(Collection) null, Collections.singletonList("3"));
		wo.colColSelf = Arrays.asList((Collection<Example>) null, new ArrayList<Example>());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(wo, baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Example wo2 = json.deserialize(Example.class, bais);
		Assert.assertEquals(wo.colColLong, wo2.colColLong);
		Assert.assertEquals(wo.colColUnknown, wo2.colColUnknown);
		Assert.assertEquals(wo.colColSelf, wo2.colColSelf);
	}

	@Test
	public void testGeneric() throws IOException {
		Generic<Long> wo = new Generic<Long>();
		wo.colT = Arrays.asList(1L, 2L);
		wo.colColT = Arrays.asList(Arrays.asList(1L, 2L), new ArrayList<Long>(), (Collection<Long>) null, Collections.singletonList(3L));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.serialize(wo, baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Generic<Long> wo2 = (Generic<Long>) json.deserialize(new TypeDefinition<Generic<Long>>() {}.type, bais);
		Assert.assertEquals(wo.colT, wo2.colT);
		Assert.assertEquals(wo.colColT, wo2.colColT);
	}
}
