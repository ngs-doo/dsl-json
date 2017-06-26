package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

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

	@CompiledJson
	class HasCustomName implements InterfaceTypeWithoutSignature {
		private int i;

		@Override
		@JsonAttribute(name = "xyz")
		public int getI() {
			return i;
		}

		@Override
		public void setI(int value) {
			i = value;
		}
	}
}
