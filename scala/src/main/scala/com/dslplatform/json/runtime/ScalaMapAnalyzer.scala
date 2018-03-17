package com.dslplatform.json
package runtime

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

import scala.collection.mutable


object ScalaMapAnalyzer {
  private val stringReader: JsonReader.ReadObject[Any] = (reader: JsonReader[_]) => reader.readString()

  val Reader: DslJson.ConverterFactory[JsonReader.ReadObject[_]] = (manifest: Type, dslJson: DslJson[_]) => {
    manifest match {
      case cl: Class[_] =>
        analyzeDecoder(manifest, classOf[Any], classOf[Any], cl, dslJson).orNull
      case pt: ParameterizedType if pt.getActualTypeArguments.length == 2 && pt.getRawType.isInstanceOf[Class[_]] =>
        analyzeDecoder(manifest, pt.getActualTypeArguments.head, pt.getActualTypeArguments.last, pt.getRawType.asInstanceOf[Class[_]], dslJson).orNull
      case _ =>
        null
    }
  }

  val Writer: DslJson.ConverterFactory[JsonWriter.WriteObject[_]] = (manifest: Type, dslJson: DslJson[_]) => {
    manifest match {
      case cl: Class[_] =>
        analyzeEncoder(manifest, classOf[Any], classOf[Any], cl, dslJson).orNull
      case pt: ParameterizedType if pt.getActualTypeArguments.length == 2 && pt.getRawType.isInstanceOf[Class[_]] =>
        analyzeEncoder(manifest, pt.getActualTypeArguments.head, pt.getActualTypeArguments.last, pt.getRawType.asInstanceOf[Class[_]], dslJson).orNull
      case _ =>
        null
    }
  }

  private def analyzeDecoder(
    manifest: Type,
    key: Type,
    value: Type,
    map: Class[_],
    json: DslJson[_]): Option[JsonReader.ReadObject[_]] = {
    if (!classOf[scala.collection.Map[_, _]].isAssignableFrom(map)) None
    else {
      val keyReader = Option(json.tryFindReader(key))
      val valueReader = Option(json.tryFindReader(value))
      if (keyReader.isEmpty || valueReader.isEmpty) None
      else {
        if (classOf[Map[_, _]].isAssignableFrom(map)) {
          val decoder = new ScalaMapImmutableDecoder[Any, Any](
            manifest,
            if (classOf[AnyRef] eq key) stringReader else keyReader.get.asInstanceOf[JsonReader.ReadObject[Any]],
            valueReader.get.asInstanceOf[JsonReader.ReadObject[Any]])
          json.registerReader(manifest, decoder)
          Some(decoder)
        } else {
          finalizeConversion(map) match {
            case Some(fin) =>
              val decoder = new ScalaMapMutableDecoder[Any, Any](
                manifest,
                if (classOf[AnyRef] eq key) stringReader else keyReader.get.asInstanceOf[JsonReader.ReadObject[Any]],
                valueReader.get.asInstanceOf[JsonReader.ReadObject[Any]],
                fin)
              json.registerReader(manifest, decoder)
              Some(decoder)
            case _ =>
              None
          }
        }
      }
    }
  }

  private def finalizeConversion(manifest: Class[_]): Option[mutable.LinkedHashMap[Any, Any] => scala.collection.Map[Any, Any]] = {
    if (classOf[mutable.LinkedHashMap[_, _]].isAssignableFrom(manifest)) {
      Some(identity)
    } else if (classOf[mutable.HashMap[_, _]].isAssignableFrom(manifest)) {
      Some(identity)
    } else if (classOf[mutable.Map[_, _]] eq manifest) {
      Some(identity)
    } else {
      None
    }
  }

  private def analyzeEncoder(
    manifest: Type,
    key: Type,
    value: Type,
    map: Class[_],
    json: DslJson[_]): Option[JsonWriter.WriteObject[_]] = {
    if (!classOf[scala.collection.Map[_, _]].isAssignableFrom(map)) None
    else {
      val keyWriter = if (classOf[AnyRef] eq key) None else Option(json.tryFindWriter(key))
      val valueWriter = if (classOf[AnyRef] eq value) None else Option(json.tryFindWriter(value))
      if ((classOf[AnyRef] ne key) && keyWriter.isEmpty || (classOf[AnyRef] ne value) && valueWriter.isEmpty) None
      else {
        //TODO: temp hack to encode some keys as strings even if they are numbers
        val checkForConversionToString = key match {
          case cl: Class[_] =>
            classOf[Number].isAssignableFrom(cl) ||
            classOf[Numeric[_]].isAssignableFrom(cl) ||
            cl == classOf[Int] || cl == classOf[Long] ||
            cl == classOf[Double] || cl == classOf[Float] ||
            cl == classOf[Byte] || cl == classOf[Short]
          case _ =>
            false
        }
        val encoder = new ScalaMapEncoder[Any, Any](
          json,
          checkForConversionToString,
          if (classOf[AnyRef] eq key) None else keyWriter.map(_.asInstanceOf[JsonWriter.WriteObject[Any]]),
          if (classOf[AnyRef] eq value) None else valueWriter.map(_.asInstanceOf[JsonWriter.WriteObject[Any]]))
        json.registerWriter(manifest, encoder)
        Some(encoder)
      }
    }
  }
}
