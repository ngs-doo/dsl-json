package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

public class ImmutablePerson {
	public final String firstName;
	public final String lastName;
	public final int age;

	@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
	public ImmutablePerson(String firstName, String lastName, int age) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
	}
}
