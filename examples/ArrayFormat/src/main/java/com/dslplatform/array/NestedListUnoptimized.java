package com.dslplatform.array;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CompiledJson(formats = CompiledJson.Format.ARRAY)
public class NestedListUnoptimized {

	private List<Integer> numbers;

	public List<Integer> getNumbers() {
		return numbers;
	}
	@JsonAttribute(index = 0)
	public int getX() {
		return numbers.get(0);
	}
	@JsonAttribute(index = 1)
	public int getY() {
		return numbers.get(1);
	}
	@JsonAttribute(index = 2)
	public int getZ() {
		return numbers.get(2);
	}

	public NestedListUnoptimized(int x, int y, int z) {
		numbers = Arrays.asList(x, y, z);
	}
}
