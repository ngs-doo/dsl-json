package com.dslplatform.reflection;

public class ImmutablePerson {

	public final String firstName;
	public final String lastName;
	public final int age;

	public ImmutablePerson(String firstName, String lastName, int age) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
	}
}
