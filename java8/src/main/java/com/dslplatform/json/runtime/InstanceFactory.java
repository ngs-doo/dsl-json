package com.dslplatform.json.runtime;

@FunctionalInterface
public interface InstanceFactory<F> {
	F create();
}
