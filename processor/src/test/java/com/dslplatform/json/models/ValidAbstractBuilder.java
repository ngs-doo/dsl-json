package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

public abstract class ValidAbstractBuilder {
	@JsonAttribute(index = 1)
	public abstract int getI();

	@JsonAttribute(index = 2)
	public abstract String getS();

	public static Builder builder() {
		return new BuilderImpl();
	}

	public abstract static class Builder {
		public abstract Builder i(int i);

		public abstract Builder s(String s);

		@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
		public abstract ValidAbstractBuilder build();
	}

	private static class BuilderImpl extends Builder {
		private int i;
		private String s;

		private BuilderImpl() {
		}

		public Builder i(int i) {
			this.i = i;
			return this;
		}

		public Builder s(String s) {
			this.s = s;
			return this;
		}

		public ValidAbstractBuilder build() {
			return new AbstractImpl(i, s);
		}
	}

	private static class AbstractImpl extends ValidAbstractBuilder {
		private int i;
		private String s;

		private AbstractImpl(int i, String s) {
			this.i = i;
			this.s = s;
		}

		public int getI() {
			return i;
		}

		public String getS() {
			return s;
		}
	}
}