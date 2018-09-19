package com.dslplatform.jsonb;

import javax.json.bind.annotation.JsonbProperty;

//builder pattern is detected even without @JsonbCreator in this class since it's referenced from other class with JsonbCreator
public class PersonBuilder {

	public final String firstName, lastName;
	@JsonbProperty(value = "years")
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

		public PersonBuilder build() {
			return new PersonBuilder(firstName, lastName, age);
		}
	}
}
