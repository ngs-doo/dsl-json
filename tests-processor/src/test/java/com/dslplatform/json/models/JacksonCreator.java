package com.dslplatform.json.models;

import com.fasterxml.jackson.annotation.JsonCreator;

public class JacksonCreator {
	private int x;
	private int y;

	public JacksonCreator(int s) {
		this.x = s;
		this.y = s;
	}

	@JsonCreator
	public JacksonCreator(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int x() { return x; }
	public void x(int v) { x = v; }
	public int getY() { return y; }
	public void setY(int v) { y = v; }
}
