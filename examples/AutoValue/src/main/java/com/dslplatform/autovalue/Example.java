package com.dslplatform.autovalue;

import com.dslplatform.json.*;
import com.google.auto.value.AutoValue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Example {

	@AutoValue
	@CompiledJson
	public static abstract class Employee {
		public abstract String name();
		@JsonAttribute(name = "_id")
		public abstract int id();
		public static Builder builder() {
			return new AutoValue_Example_Employee.Builder();
		}
		@AutoValue.Builder
		public abstract static class Builder {
			public abstract Builder name(String name);
			public abstract Builder id(int id);
			public abstract Employee build();
		}
	}

	public static void main(String[] args) throws IOException {

		DslJson<Object> dslJson = new DslJson<>();

		Employee employee = Employee.builder().name("name").id(1).build();

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(employee, os);
		System.out.println(os);

		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		Employee deser = dslJson.deserialize(Employee.class, is);

		System.out.println(deser.name());
	}
}
