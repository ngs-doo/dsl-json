package com.dslplatform.maven;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

public class Example {

	@CompiledJson
	public static class Model {
		public String string;
		public List<Integer> integers;
		public UUID[] uuids;
		public Set<BigDecimal> decimals;
		public Vector<Long> longs;
		public int number;
		public List<Nested> nested;
		public Abstract abs;//abstract classes or interfaces can be used

		//explicitly referenced classes don't require @CompiledJson annotation
		public static class Nested {
			public long x;
			public double y;
			public float z;
		}
		public static abstract class Abstract {
			public int x;
		}
		//since this class is not explicitly referenced, but it's an extension of the abstract class used as a property
		//it needs to be decorated with annotation
		@CompiledJson
		public static class Concrete extends Abstract {
			public long y;
		}
	}

	public static void main(String[] args) throws IOException {

		//ServiceLoader.load will load Model since it will be registered into META-INF/services during annotation processing
		DslJson<Object> dslJson = new DslJson<Object>();
		//writer should be reused. For per thread reuse use ThreadLocal pattern
		JsonWriter writer = new JsonWriter();

		Model instance = new Model();
		instance.string = "Hello World!";
		instance.number = 42;
		Model.Concrete concrete = new Model.Concrete();
		concrete.x = 11;
		concrete.y = 23;
		instance.abs = concrete;

		dslJson.serialize(writer, instance);

		//resulting buffer with JSON
		byte[] buffer = writer.getByteBuffer();
		//end of buffer
		int size = writer.size();

		//deserialization using byte[] API
		Model deser = dslJson.deserialize(Model.class, buffer, size);

		System.out.println(deser.string);
	}
}
