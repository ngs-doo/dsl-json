package com.dslplatform.array;

import com.dslplatform.json.CompiledJson;

import java.util.Arrays;
import java.util.List;

//This is optimized version of serializing fixed number of elements as a list
@CompiledJson(formats = CompiledJson.Format.ARRAY)
public class NestedListOptimized {

	private final int x, y, z;

	//since it has non-empty ctor, it will define the order of elements
	//otherwise we would have to use @JsonAttribute with an index
	public NestedListOptimized(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public int getX() { return x; }
	public int getY() { return y; }
	public int getZ() { return z; }
}
