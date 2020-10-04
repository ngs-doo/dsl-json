package com.dslplatform.json

//@CompiledJson
data class HasIgnoreWithDefault(
    val someString: String = "some-string",
    @JsonAttribute(ignore = true)val paramWithIgnoreFlag: Boolean = true) {

    @JsonAttribute(ignore = true)
    lateinit var stringFieldWithIgnoreFlag: String
    @JsonAttribute(mandatory = true)
    lateinit var stringWhichShouldBeInPlace: String
}

//@CompiledJson
data class HasIgnoreNoDefault(
    val someString: String,
    @JsonAttribute(ignore = true)val paramWithIgnoreFlag: Boolean) {

    @JsonAttribute(ignore = true)
    lateinit var stringFieldWithIgnoreFlag: String
    @JsonAttribute(mandatory = true)
    lateinit var stringWhichShouldBeInPlace: String
}