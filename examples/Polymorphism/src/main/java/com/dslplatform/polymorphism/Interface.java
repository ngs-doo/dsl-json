package com.dslplatform.polymorphism;

import com.dslplatform.json.CompiledJson;

//when discriminator is not defined, $type will be used
public interface Interface {

	int number();

	String getName();

	//when deserialize name is not specified full class name will be used
	@CompiledJson
	class ImmutableImplementation implements Interface {
		private final int number;
		private final String name;

		public int number() {
			return number;
		}

		public String getName() {
			return name;
		}

		public ImmutableImplementation(int number, String name) {
			this.number = number;
			this.name = name;
		}
	}

	//custom deserialize name can be specified
	@CompiledJson(name = "mutable")
	class MutableImplementation implements Interface {
		private int number;
		private String name;

		public int number() {
			return number;
		}
		public MutableImplementation number(int value) {
			this.number = value;
			return this;
		}

		public String getName() {
			return name;
		}
		public MutableImplementation setName(String value) {
			this.name = value;
			return this;
		}
	}
}

