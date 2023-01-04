package com.dslplatform.json

@CompiledJson
data class WithIs(
    val isLocked: Boolean,
    val id: String? = null,
    val token: String? = null,
)