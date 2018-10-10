package com.dslplatform.polymorphism;

import com.dslplatform.json.*;
import com.dslplatform.json.runtime.Settings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class Example {

	@CompiledJson
	public static class Model {
		public AbstractClass abstractProperty;
		public List<AbstractClass> abstractList;
		public Interface ifaceProperty;
		public List<Interface> ifaceList;
	}

	public static void main(String[] args) throws IOException {

		DslJson<Object> dslJson = new DslJson<>();

		Model instance = new Model();
		instance.abstractProperty = new AbstractClass.ImmutableImplementation(5, "DSL");
		instance.abstractList = Arrays.asList(
				new AbstractClass.ImmutableImplementation(1, "speed"),
				null,
				new AbstractClass.MutableImplementation().number(2).setName("features"));
		instance.ifaceProperty = new Interface.ImmutableImplementation(6, "JSON");
		instance.ifaceList = Arrays.asList(
				null,
				new Interface.ImmutableImplementation(1, "type safety"),
				new Interface.MutableImplementation().number(2).setName("transparency")
		);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		//To get a pretty JSON output PrettifyOutputStream wrapper can be used
		dslJson.serialize(instance, new PrettifyOutputStream(os));

		byte[] bytes = os.toByteArray();
		System.out.println(os);

		//deserialization using Stream API
		Model deser = dslJson.deserialize(Model.class, new ByteArrayInputStream(bytes));

		System.out.println(deser.abstractProperty.getName());
	}
}
