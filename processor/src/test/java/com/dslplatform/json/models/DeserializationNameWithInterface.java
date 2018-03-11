package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson(deserializeName = "MyCustom.Name")
public class DeserializationNameWithInterface implements DeserializationInterface {
	public DeserializationInterface i;
}
