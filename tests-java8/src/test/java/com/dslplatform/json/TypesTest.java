package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TypesTest {

	@CompiledJson
	public static class All {
		public Boolean b;
	}

	private final DslJson<Object> dslJsonFull = new DslJson<>(Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());
	private final DslJson<Object> dslJsonMinimal = new DslJson<>(Settings.withRuntime().allowArrayFormat(true).skipDefaultValues(true).includeServiceLoader());

	private final DslJson<Object>[] dslJsons = new DslJson[]{dslJsonFull, dslJsonMinimal};

	@Test
	public void compare() throws IOException {
		for (DslJson<Object> dslJson : dslJsons) {
			All a = new All();
			a.b = true;
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			dslJson.serialize(a, os);
			All res = dslJson.deserialize(All.class, os.toByteArray(), os.size());
			Assert.assertEquals(a.b, res.b);
		}
	}
}
