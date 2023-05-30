package com.dslplatform.maven;

import com.dslplatform.json.JsonValue;

import java.math.BigDecimal;

public enum SpecialNumber {
	ONE(BigDecimal.ONE),
	PI(BigDecimal.valueOf(3.14159)),
	E(BigDecimal.valueOf(2.71828)),
	ZERO(BigDecimal.ZERO);

	private final BigDecimal value;

	SpecialNumber(BigDecimal value) {
		this.value = value;
	}

	//enum can be saved as some specific value instead by it's name
	@JsonValue
	public BigDecimal getValue() {
		return value;
	}
}
