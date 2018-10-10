package com.dslplatform.array;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

public class PersonBuilder {

	public final String firstName, lastName;
	@JsonAttribute(name = "years")
	public final int age;

	private PersonBuilder(String firstName, String lastName, int age) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private String firstName, lastName;
		private int age;

		private Builder() {}

		public Builder firstName(String firstName) {
			this.firstName = firstName;
			return this;
		}

		public Builder lastName(String lastName) {
			this.lastName = lastName;
			return this;
		}

		public Builder age(int age) {
			this.age = age;
			return this;
		}

		//both array and object formats can be used at once
		//the order of format definition defined the preference
		//to be able to use the array format, DslJson must be configured with allowArrayFormat(true)
		@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
		public PersonBuilder build() {
			return new PersonBuilder(firstName, lastName, age);
		}
	}
}
