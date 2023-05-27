package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

public class CompanionFactory {
	public final String firstName;
	public final String lastName;
	public final int age;

	private CompanionFactory(String firstName, String lastName, int age) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
	}

	public static final Companion Companion = new Companion();

	public static class Companion {
		@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
		public CompanionFactory create(String firstName, String lastName, int age) {
			return new CompanionFactory(firstName, lastName, age);
		}
	}
}