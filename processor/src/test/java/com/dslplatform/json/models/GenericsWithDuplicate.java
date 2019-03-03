package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson(discriminator = "type")
public abstract class GenericsWithDuplicate<T> {
	private T prop;
	public abstract String getType();
	public T getProp() {
		return prop;
	}
	public void setProp(T prop) {
		this.prop = prop;
	}

	@CompiledJson(discriminator = "boolValue", name = "second")
	public static class SecondChild extends GenericsWithDuplicate<Long> {
		private Boolean boolValue;
		public Boolean getBoolValue() {
			return boolValue;
		}
		public void setBoolValue(Boolean boolValue) {
			this.boolValue = boolValue;
		}
		@Override
		public String getType() {
			return "second";
		}
	}
}