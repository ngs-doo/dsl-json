package com.dslplatform.json.models;

import com.dslplatform.json.*;

@CompiledJson
public class VoidOnBuilderReturn {

	private String s;

	@NonNull
	public String getS() { return s; }

	private VoidOnBuilderReturn() {
	}

	public static class Builder {

		private String s;

		public void setS(String s) {
			this.s = s;
		}

		@CompiledJson
		public VoidOnBuilderReturn build() {
			VoidOnBuilderReturn res = new VoidOnBuilderReturn();
			res.s = s;
			return res;
		}
	}

	public static Builder builder() {
		return new Builder();
	}
}
