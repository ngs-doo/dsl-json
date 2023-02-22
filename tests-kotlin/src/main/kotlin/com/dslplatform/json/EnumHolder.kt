package com.dslplatform.json

@CompiledJson
data class EnumHolder(
    val enumField: EnumClass? = null,
    @JsonAttribute(hashMatch = false)
    val enumFieldNoHash: EnumClass,
)