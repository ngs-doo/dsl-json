package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson
public class ValidBuilderAnnotationOnClass {
	public final int x;

	private ValidBuilderAnnotationOnClass(int x) {
		this.x = x;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private int x;

		private Builder() {
		}

		public Builder x(int x) {
			this.x = x;
			return this;
		}

		public ValidBuilderAnnotationOnClass build() {
			return new ValidBuilderAnnotationOnClass(x);
		}
	}
}