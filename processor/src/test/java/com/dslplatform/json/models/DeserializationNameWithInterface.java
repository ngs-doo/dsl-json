package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson(name = "MyCustom.Name")
public class DeserializationNameWithInterface implements DeserializationInterface {
	public DeserializationInterface i;
}
