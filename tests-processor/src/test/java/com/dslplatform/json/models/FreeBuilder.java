package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
public abstract class FreeBuilder {
	@JsonAttribute(index = 1, name = "name")
	public abstract String name();

	@JsonAttribute(index = 2, name = "id")
	public abstract int id();

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder extends Employee_Builder {
	}

	static abstract class Employee_Builder {
		private String name;
		private int id;

		public Builder name(String name) {
			this.name = name;
			return (Builder) this;
		}

		public String name() {
			return name;
		}

		public Builder id(int id) {
			this.id = id;
			return (Builder) this;
		}

		public int id() {
			return id;
		}

		public FreeBuilder build() {
			return new Employee_Builder.Value(this);
		}

		private static final class Value extends FreeBuilder {
			private final String name;
			private final int id;

			private Value(Employee_Builder builder) {
				this.name = builder.name;
				this.id = builder.id;
			}

			@Override
			public String name() {
				return name;
			}

			@Override
			public int id() {
				return id;
			}
		}
	}
}