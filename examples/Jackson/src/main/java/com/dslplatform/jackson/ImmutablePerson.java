package com.dslplatform.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ImmutablePerson {

	public final String firstName;
	public final String lastName;
	public final int age;

	//Jackson can be used for selecting the appropriate constructor
	@JsonCreator
	public ImmutablePerson(String firstName, String lastName, int age) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
	}
}
