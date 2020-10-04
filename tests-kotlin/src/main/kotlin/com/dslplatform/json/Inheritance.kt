package com.dslplatform.json

@CompiledJson
abstract class BaseA(val t: String)

@CompiledJson
class TopLevelB(val r: String) : BaseA("hello")