package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

public class MultipleCtors {
	private int x;
	private int y;

	public MultipleCtors(int s) {
		this.x = s;
		this.y = s;
	}

	@CompiledJson
	public MultipleCtors(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int x() { return x; }
	public void x(int v) { x = v; }
	public int getY() { return y; }
	public void setY(int v) { y = v; }
}
