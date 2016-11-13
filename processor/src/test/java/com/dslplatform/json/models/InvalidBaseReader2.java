package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson(baseReaders = {NonPublicInterfejs.class})
public class InvalidBaseReader2 implements NonPublicInterfejs {
	public String string;
}
