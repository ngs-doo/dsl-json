package com.dslplatform.androidkotlin

import com.dslplatform.json.CompiledJson


@CompiledJson(formats = arrayOf(CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT))
data class Person(val firstName: String, val lastName: String, val age: Int)