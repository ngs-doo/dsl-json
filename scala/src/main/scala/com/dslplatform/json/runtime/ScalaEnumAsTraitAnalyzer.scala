package com.dslplatform.json
package runtime

import java.lang.reflect.{Type => JavaType}
import java.nio.charset.StandardCharsets
import java.util

object ScalaEnumAsTraitAnalyzer {
  val Reader: DslJson.ConverterFactory[JsonReader.ReadObject[_]] = (manifest: JavaType, dslJson: DslJson[_]) => {
    manifest match {
      case cl: Class[_] if cl.isInterface => analyze(cl,  dslJson).orNull
      case _ => null
    }
  }

  val Writer: DslJson.ConverterFactory[JsonWriter.WriteObject[_]] = (manifest: JavaType, dslJson: DslJson[_]) => {
    manifest match {
      case cl: Class[_] if cl.isInterface => analyze(cl, dslJson).orNull
      case _ => null
    }
  }

  private def analyze[T](
    manifest: Class[T],
    json: DslJson[_]
  ): Option[ScalaTraitDescription[_]] = {
    val mirror = scala.reflect.runtime.currentMirror
    val sc = mirror.staticClass(manifest.getTypeName)
    if (!sc.isTrait || !sc.isSealed || sc.knownDirectSubclasses.isEmpty) None
    else {
      val values = sc.knownDirectSubclasses.toIndexedSeq
      val emptyCtorObjects = values.flatMap { it =>
        if (it.isStatic && it.isModuleClass && it.isPublic) {
          val module = mirror.staticModule(it.fullName)
          Some(it.name.toString -> mirror.reflectModule(module).instance.asInstanceOf[T])
        } else None
      }
      if (values.size == emptyCtorObjects.size) {
        val map = new util.HashMap[T, Array[Byte]]()
        val decoders = new Array[DecodePropertyInfo[T]](values.size)
        emptyCtorObjects.foreach { case (n, i) =>
          decoders(map.size) = new DecodePropertyInfo[T](n, false, false, map.size, false, i);
          map.put(i, s""""$n"""".getBytes(StandardCharsets.UTF_8))
        }
        Some(new ScalaTraitDescription[T](manifest, map, decoders))
      } else None
    }
  }

  private class ScalaTraitDescription[T](
    signature: Class[T],
    encoders: util.HashMap[T, Array[Byte]],
    decoders: Array[DecodePropertyInfo[T]]
  ) extends JsonWriter.WriteObject[T] with JsonReader.ReadObject[T] {

    override def write(writer: JsonWriter, value: T): Unit = {
      val bytes = encoders.get(value)
      if (bytes eq null) throw new SerializationException(s"Invalid value: $value provided for $signature")
      writer.writeAscii(bytes)
    }

    override def read(reader: JsonReader[_]): T = {
      if (reader.last != '"') {
        throw reader.newParseError("Expecting '\"' for enum start")
      }
      val hash = reader.calcHash()
      var i = 0
      var found: DecodePropertyInfo[T] = null
      while (i < decoders.length) {
        val ri = decoders(i)
        if (ri.hash == hash && (!ri.exactName || reader.wasLastName(ri.nameBytes))) {
          found = ri
          i = decoders.length
        }
        i += 1
      }
      if (found != null) found.value
      else throw reader.newParseError(s"Invalid value: '${reader.getLastName}' for $signature")
    }
  }
}