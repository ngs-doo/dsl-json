package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonEnumDefaultValue;

@CompiledJson
public enum EnumWithInvalidFieldDefaultConstant {

    FIRST("First"),

    SECOND("Second");

    @JsonEnumDefaultValue
    private final String value;

    EnumWithInvalidFieldDefaultConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
