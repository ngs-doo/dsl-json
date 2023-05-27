package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@CompiledJson
public class CtorWithLooseSignature {
	private Set<BigDecimal> x;
	public Set<BigDecimal> getX() { return x; }

	public CtorWithLooseSignature(Set<? extends BigDecimal> x) {
		this.x = new HashSet<BigDecimal>();
		this.x.addAll(x);
	}
}
