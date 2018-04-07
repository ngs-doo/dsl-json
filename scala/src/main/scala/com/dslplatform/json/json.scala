package com.dslplatform

import scala.reflect.runtime.universe._

package object json {

  implicit def prepareDecoder[T: TypeTag](implicit json: DslJson[_]): JsonReader.ReadObject[T] = {
    val pimp = new DslJsonScala(json)
    pimp.decoder[T]
  }

  implicit def prepareEncoder[T: TypeTag](implicit json: DslJson[_]): JsonWriter.WriteObject[T] = {
    val pimp = new DslJsonScala(json)
    pimp.encoder[T]
  }

  implicit def dslJsonToScala(json: DslJson[_]): DslJsonScala = new DslJsonScala(json)
}