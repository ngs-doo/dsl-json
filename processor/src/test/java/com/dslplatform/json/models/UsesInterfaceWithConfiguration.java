package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.List;

@CompiledJson
public class UsesInterfaceWithConfiguration {
	private InterfaceType if1;

	public InterfaceType getIf1() {
		return if1;
	}

	public UsesInterfaceWithConfiguration setIf1(InterfaceType value) {
		if1 = value;
		return this;
	}

	private List<InterfaceType> if2;

	@JsonAttribute(typeSignature = CompiledJson.TypeSignature.EXCLUDE)
	public List<InterfaceType> getIf2() {
		return if2;
	}

	public void setIf2(List<InterfaceType> value) {
		if2 = value;
	}

	private InterfaceTypeWithoutSignature if3;

	public InterfaceTypeWithoutSignature getIf3() {
		return if3;
	}

	public UsesInterfaceWithConfiguration setIf3(InterfaceTypeWithoutSignature value) {
		this.if3 = value;
		return this;
	}

	@JsonAttribute(typeSignature = CompiledJson.TypeSignature.DEFAULT)
	public InterfaceTypeWithoutSignature if4;
}
