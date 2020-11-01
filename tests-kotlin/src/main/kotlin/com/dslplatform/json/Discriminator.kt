package com.dslplatform.json

@CompiledJson(discriminator = "@type")
interface BaseDiscriminator

@CompiledJson(name = "one")
data class One(val value: String): BaseDiscriminator

@CompiledJson(name = "two")
data class Two(val value: Long): BaseDiscriminator