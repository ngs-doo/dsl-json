package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

public class OnStaticMethod {
	private int x;
	private String y;

	private OnStaticMethod(int x, String y) {
		this.x = x;
		this.y = y;
	}

	public int x() { return x; }
	public void x(int v) { x = v; }
	public String getY() { return y; }
	public void setY(String v) { y = v; }

	@CompiledJson
	public static OnStaticMethod create(String y, int x) {
		return new OnStaticMethod(x, y);
	}
}
