package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonEnumDefaultValue;

@CompiledJson
public enum EnumWithDefaultConstant {

    FIRST,

    @JsonEnumDefaultValue
    SECOND
}
