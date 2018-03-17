package com.dslplatform

package object json {

  implicit def dslJsonToScala(json: DslJson[_]): DslJsonScala = new DslJsonScala(json)
}