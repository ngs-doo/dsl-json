package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

public class InvalidBuilderWithArgument {
	public final int x;

	private InvalidBuilderWithArgument(int x) {
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

		@CompiledJson
		public InvalidBuilderWithArgument build(int x) {
			return new InvalidBuilderWithArgument(x);
		}
	}
}