package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson(baseReaders = {BaseReader.class, BaseReader.NestedBaseReader.class})
public class ValidBaseReader implements BaseReader, BaseReader.NestedBaseReader {
	public int number;
}
