package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson(typeSignature = CompiledJson.TypeSignature.EXCLUDE)
public interface InterfaceTypeWithoutSignature {
	int getI();

	void setI(int value);

	@CompiledJson
	class ImplementsTypeWithConfiguration implements InterfaceTypeWithoutSignature {
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
