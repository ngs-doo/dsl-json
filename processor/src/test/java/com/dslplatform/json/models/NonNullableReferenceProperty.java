package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.UUID;

@CompiledJson
public class NonNullableReferenceProperty {
	private ValidCtor[] ref;

	@Nonnull
	public ValidCtor[] getRef() {
		return ref;
	}

	public void setRef(ValidCtor[] value) {
		ref = value;
	}

	private String prop;

	@Nonnull
	public String getProp() {
		return prop;
	}

	public void setProp(String value) {
		prop = value;
	}

	private SimpleEnum en;

	@Nonnull
	public SimpleEnum getEnum() {
		return en;
	}

	public void setEnum(SimpleEnum value) {
		en = value;
	}

	private Set<UUID> uuid;

	@JsonAttribute(nullable = false)
	public Set<UUID> getUuid() {
		return uuid;
	}

	public void setUuid(Set<UUID> value) {
		uuid = value;
	}

}
