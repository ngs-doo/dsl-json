package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

public class OnNonStaticMethod {
	private int x;
	private String y;

	private OnNonStaticMethod(int x, String y) {
		this.x = x;
		this.y = y;
	}

	public int x() { return x; }
	public void x(int v) { x = v; }
	public String getY() { return y; }
	public void setY(String v) { y = v; }

	@CompiledJson
	public OnNonStaticMethod create(String y, int x) {
		return new OnNonStaticMethod(x, y);
	}
}
