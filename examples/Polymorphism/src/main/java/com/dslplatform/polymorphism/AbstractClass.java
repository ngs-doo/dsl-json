package com.dslplatform.polymorphism;

import com.dslplatform.json.*;

//custom discriminator value can be specified. default value is $type
@CompiledJson(deserializeDiscriminator = "@type")
public abstract class AbstractClass {

	abstract int number();

	abstract String getName();

	//since deserializeName is not specified $type:ClassName will be encoded in JSON so it can be correctly deserialized
	@CompiledJson
	public static class ImmutableImplementation extends AbstractClass {
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

	//since custom deserialize name is specified $type:mutable will be encoded in JSON so it can be correctly deserialized
	@CompiledJson(deserializeName = "mutable")
	public static class MutableImplementation extends AbstractClass {
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
