package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

import java.math.BigDecimal;

@CompiledJson
public class InversedArguments {
	public String x;
	public int y;
	public BigDecimal z;

	public InversedArguments(int y, BigDecimal z, String x) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
}
