package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

import java.math.BigDecimal;

@CompiledJson
public class MissingArguments {
	public String x;
	public int y;
	public BigDecimal z;

	public MissingArguments(BigDecimal z, String x) {
		this.x = x;
		this.y = 0;
		this.z = z;
	}
}
