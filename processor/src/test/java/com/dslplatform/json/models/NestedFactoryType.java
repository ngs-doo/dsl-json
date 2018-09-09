package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

public class NestedFactoryType implements InterfaceType {
	private int x;
	private String y;

	private NestedFactoryType(int x, String y) {
		this.x = x;
		this.y = y;
	}

	public int x() { return x; }
	public void x(int v) { x = v; }
	public String getY() { return y; }
	public void setY(String v) { y = v; }

	public static class Factory {
		@CompiledJson
		public static NestedFactoryType create(String y, int x) {
			return new NestedFactoryType(x, y);
		}
	}
}
