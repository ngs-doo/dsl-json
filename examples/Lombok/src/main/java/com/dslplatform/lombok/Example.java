package com.dslplatform.lombok;

import com.dslplatform.json.*;
import lombok.Data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Example {

	@Data
	@CompiledJson
	public static class Employee {
		private String name;
		private int id;
	}

	public static void main(String[] args) throws IOException {

		DslJson<Object> dslJson = new DslJson<>();

		Employee employee = new Employee();
		employee.name = "name";
		employee.id = 1;

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(employee, os);
		System.out.println(os);

		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		Employee deser = dslJson.deserialize(Employee.class, is);

		System.out.println(deser.name);
	}
}
