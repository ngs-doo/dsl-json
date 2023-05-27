package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

public class MultipleCtorsAndArray {
	private int x;
	private int y;

	public MultipleCtorsAndArray(int s) {
		this.x = s;
		this.y = s;
	}

	@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
	public MultipleCtorsAndArray(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@JsonAttribute(index = 1)
	public int x() { return x; }
	public void x(int v) { x = v; }
	@JsonAttribute(index = 2)
	public int getY() { return y; }
	public void setY(int v) { y = v; }
}
