package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson(discriminator = "abc\t")
public interface InterfaceWithBadDiscriminator {
	int getI();

	void setI(int value);

	@CompiledJson
	class ImplementsTypeWithConfiguration implements InterfaceWithBadDiscriminator {
		public String x;
		private int i;

		@Override
		public int getI() {
			return i;
		}

		@Override
		public void setI(int value) {
			i = value;
		}
	}
}
