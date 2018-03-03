package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.List;

@CompiledJson
public class UsesAbstractTypeWithConfiguration {
	public AbstractType abs1;
	@JsonAttribute(typeSignature = CompiledJson.TypeSignature.EXCLUDE)
	public List<AbstractType> abs2;
	public AbstractTypeWithoutSignature abs3;
	@JsonAttribute(typeSignature = CompiledJson.TypeSignature.DEFAULT)
	public AbstractTypeWithoutSignature abs4;
	@JsonAttribute(typeSignature = CompiledJson.TypeSignature.EXCLUDE)
	public AbstractTypeWithoutSignature[] abs5;
}
