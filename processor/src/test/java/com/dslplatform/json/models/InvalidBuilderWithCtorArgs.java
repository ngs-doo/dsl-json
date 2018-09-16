package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

public class InvalidBuilderWithCtorArgs {
	public final int x;

	private InvalidBuilderWithCtorArgs(int x) {
		this.x = x;
	}

	public static class Builder {
		private int x;

		public Builder(int x) {
		}

		public Builder x(int x) {
			this.x = x;
			return this;
		}

		@CompiledJson
		public InvalidBuilderWithCtorArgs build() {
			return new InvalidBuilderWithCtorArgs(x);
		}
	}
}