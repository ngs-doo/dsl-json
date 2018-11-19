package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CompiledJson(deserializeDiscriminator = "type")
public abstract class GenericsWithInheritance<T> {
	private T prop;
	public abstract String getType();
	public T getProp() {
		return prop;
	}
	public void setProp(T prop) {
		this.prop = prop;
	}

	@CompiledJson(deserializeDiscriminator = "type", deserializeName = "first")
	public static class FirstChild extends GenericsWithInheritance<Long> {
		private Boolean boolValue;
		public Boolean getBoolValue() {
			return boolValue;
		}
		public void setBoolValue(Boolean boolValue) {
			this.boolValue = boolValue;
		}
		@Override
		public String getType() {
			return "first";
		}
	}
}
