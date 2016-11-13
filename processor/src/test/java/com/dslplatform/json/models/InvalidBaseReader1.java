package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson(baseReaders = {Number.class})
public class InvalidBaseReader1 {
	public int number;
}
