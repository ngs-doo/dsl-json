package com.dslplatform.json;

@CompiledJson
public class BuilderWithEmptyCtor {
	private String name;
	private int id;

	public BuilderWithEmptyCtor() {
	}

	public BuilderWithEmptyCtor(String name, int id) {
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String name;
		private int id;

		private Builder() {
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder id(int id) {
			this.id = id;
			return this;
		}

		public BuilderWithEmptyCtor build() {
			return new BuilderWithEmptyCtor(name, id);
		}
	}
}
