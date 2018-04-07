package com.dslplatform.json

import java.io.{InputStream, OutputStream}
import java.lang.reflect.{GenericArrayType, ParameterizedType, Type => JavaType}

import com.dslplatform.json.runtime.ScalaClassAnalyzer

import scala.reflect.runtime.universe._

class DslJsonScala(val json: DslJson[_]) extends AnyVal {

  def encoder[T: TypeTag]: JsonWriter.WriteObject[T] = {
    val tpe = typeOf[T]
    val javaType = TypeAnalysis.convertType(tpe)
    checkConversion(tpe, javaType, json.getRegisteredEncoders)
    json.tryFindWriter(javaType).asInstanceOf[JsonWriter.WriteObject[T]]
  }

  def decoder[T: TypeTag]: JsonReader.ReadObject[T] = {
    val tpe = typeOf[T]
    val javaType = TypeAnalysis.convertType(tpe)
    checkConversion(tpe, javaType, json.getRegisteredDecoders)
    json.tryFindReader(javaType).asInstanceOf[JsonReader.ReadObject[T]]
  }

  private def checkConversion(tpe: Type, foundType: JavaType, known: java.util.Set[JavaType]): JavaType = {
    if (!known.contains(foundType)) {
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

  def encode[T](value: T, os: OutputStream)(implicit encoder: JsonWriter.WriteObject[T]): Unit = {
    require(os ne null, "os can't be null")
    val writer = json.localWriter.get()
    writer.reset(os)
    try {
      encoder.write(writer, value)
      writer.flush()
    } finally {
      writer.reset(null)
    }
  }

  def decode[T](bytes: Array[Byte])(implicit decoder: JsonReader.ReadObject[T]): T = {
    require(bytes ne null, "bytes can't be null")
    decode[T](bytes, bytes.length)
  }

  def decode[T](bytes: Array[Byte], length: Int)(implicit decoder: JsonReader.ReadObject[T]): T = {
    require(bytes ne null, "bytes can't be null")
    require(length <= bytes.length, "length must be less or equal to bytes length")
    val reader = json.localReader.get()
    reader.process(bytes, length).getNextToken()
    decoder.read(reader)
  }

  def decode[T](is: InputStream)(implicit decoder: JsonReader.ReadObject[T]): T = {
    val reader = json.localReader.get()
    reader.process(is).getNextToken()
    decoder.read(reader)
  }
}
