package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

public class StaticMethodWrongType implements InterfaceType {
	private int x;
	private String y;

	private StaticMethodWrongType(int x, String y) {
		this.x = x;
		this.y = y;
	}

	public int x() { return x; }
	public void x(int v) { x = v; }
	public String getY() { return y; }
	public void setY(String v) { y = v; }

	@CompiledJson
	public static InterfaceType create(String y, int x) {
		return new StaticMethodWrongType(x, y);
	}
}
