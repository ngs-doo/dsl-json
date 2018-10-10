package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson(deserializeDiscriminator = "abc")
public interface InterfaceWithConflictingDiscriminator {

	@CompiledJson
	class ImplementsTypeWithConfiguration implements InterfaceWithConflictingDiscriminator {
		public String x;
		private int abc;

		public int getAbc() {
			return abc;
		}

		public void setAbc(int value) {
			abc = value;
		}
	}
}
