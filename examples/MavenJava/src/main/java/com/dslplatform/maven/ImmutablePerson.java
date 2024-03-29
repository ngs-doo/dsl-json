package com.dslplatform.maven;

import com.dslplatform.json.*;

import java.io.IOException;

public class ImmutablePerson {

	public final String firstName;
	public final String lastName;
	public final int age;

	//when there are multiple constructors, @CompiledJson annotation can be used to specify appropriate constructor
	//objects can be encoded/decoded as key:value pairs or as object format (without keys)
	//array format must be allowed in DslJson before it will be used for encoding
	//when non-empty ctor is used, array index on properties are not mandatory
	@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
	public ImmutablePerson(String firstName, String lastName, int age) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
	}
}
