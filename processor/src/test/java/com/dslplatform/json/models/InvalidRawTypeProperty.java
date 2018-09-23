package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

import java.util.List;
import java.util.Map;

@CompiledJson
public class InvalidRawTypeProperty {
	public List rawList;
	public MyGeneric rawGeneric;
	public Map<String, MyGeneric> mapWithRawGeneric;

	public static class MyGeneric<T> {
		public T value;
	}
}
