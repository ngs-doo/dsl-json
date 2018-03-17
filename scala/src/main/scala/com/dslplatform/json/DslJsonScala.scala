package com.dslplatform.json

import java.io.{InputStream, OutputStream}
import java.lang.reflect.{GenericArrayType, ParameterizedType, Type => JavaType}

import com.dslplatform.json.runtime.ScalaClassAnalyzer

import scala.reflect.runtime.universe._

class DslJsonScala(val json: DslJson[_]) extends AnyVal {
  def findWriter[T: TypeTag]: Option[JsonWriter.WriteObject[T]] = {
    TypeAnalysis.findType(typeOf[T]).map(json.tryFindWriter).map(_.asInstanceOf[JsonWriter.WriteObject[T]])
  }

  def findReader[T: TypeTag]: Option[JsonReader.ReadObject[T]] = {
    TypeAnalysis.findType(typeOf[T]).map(json.tryFindReader).map(_.asInstanceOf[JsonReader.ReadObject[T]])
  }

  def findBinder[T: TypeTag]: Option[JsonReader.BindObject[T]] = {
    TypeAnalysis.findType(typeOf[T]).map(json.tryFindBinder).map(_.asInstanceOf[JsonReader.BindObject[T]])
  }

  private def findJavaType(tpe: Type): JavaType = {
    val foundType = TypeAnalysis.findType(tpe).getOrElse(throw new IllegalArgumentException(s"Unable to find Java type for $tpe"))
    if (!json.getRegisteredDecoders.contains(foundType)) {
      foundType match {
        case _: ParameterizedType =>
        case _: GenericArrayType =>
        case cl: Class[_] if ScalaClassAnalyzer.isSupported(foundType, cl) =>
          //since loading class metadata doesn't work in some cases force the analysis via compiler provided type tag
          ScalaClassAnalyzer.analyzeType(foundType, cl, json, reading = false, tpe)
        case _ =>
      }
    }
    foundType
  }

  def encode[T: TypeTag](value: T, os: OutputStream): Unit = {
    require(os ne null, "os can't be null")
    val foundType = findJavaType(typeOf[T])
    val writer = json.localWriter.get()
    writer.reset(os)
    try {
      if (!json.serialize(writer, foundType, value)) {
        throw new IllegalArgumentException(s"Unable to encode $foundType")
      }
      writer.flush()
    } finally {
      writer.reset(null)
    }
  }

  def encode[T: TypeTag](writer: JsonWriter, value: T): Unit = {
    require(writer ne null, "writer can't be null")
    val foundType = findJavaType(typeOf[T])
    if (!json.serialize(writer, foundType, value)) {
      throw new IllegalArgumentException(s"Unable to encode $foundType")
    }
  }

  def decode[T: TypeTag](bytes: Array[Byte]): T = {
    require(bytes ne null, "bytes can't be null")
    decode[T](bytes, bytes.length)
  }

  def decode[T: TypeTag](bytes: Array[Byte], length: Int): T = {
    require(bytes ne null, "bytes can't be null")
    require(length <= bytes.length, "length must be less or equal to bytes length")
    val foundType = findJavaType(typeOf[T])
    json.deserialize(foundType, bytes, length).asInstanceOf[T]
  }

  def decode[T: TypeTag](is: InputStream): T = {
    val foundType = findJavaType(typeOf[T])
    json.deserialize(foundType, is).asInstanceOf[T]
  }
}
