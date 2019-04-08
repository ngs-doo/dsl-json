package com.dslplatform.json
package runtime

import java.lang.reflect.{ParameterizedType, Type}

import scala.collection.mutable

object ScalaCollectionAnalyzer {
  val Reader: DslJson.ConverterFactory[JsonReader.ReadObject[_]] = (manifest: Type, dslJson: DslJson[_]) => {
    manifest match {
      case cl: Class[_] =>
        analyzeDecoding(manifest, classOf[AnyRef], cl, dslJson).orNull
      case pt: ParameterizedType if pt.getActualTypeArguments.length == 1 =>
        pt.getRawType match {
          case rc: Class[_] =>
            analyzeDecoding(manifest, pt.getActualTypeArguments.head, rc, dslJson).orNull
          case _ =>
            null
        }
      case _ => null
    }
  }
  val Writer: DslJson.ConverterFactory[JsonWriter.WriteObject[_]] = (manifest: Type, dslJson: DslJson[_]) => {
    manifest match {
      case cl: Class[_] =>
        analyzeEncoding(manifest, classOf[AnyRef], cl, dslJson).orNull
      case pt: ParameterizedType if pt.getActualTypeArguments.length == 1 =>
        pt.getRawType match {
          case rc: Class[_] =>
            analyzeEncoding(manifest, pt.getActualTypeArguments.head, rc, dslJson).orNull
          case _ =>
            null
        }
      case _ =>
        null
    }
  }

  private def analyzeDecoding(
    manifest: Type,
    element: Type,
    collection: Class[_],
    json: DslJson[_]): Option[ArrayBufferDecoder[_]] = {

    if (!classOf[scala.collection.Iterable[_]].isAssignableFrom(collection)) None
    else {
      Option(json.tryFindReader(element)) match {
        case Some(reader: JsonReader.ReadObject[Any @unchecked]) =>
          ScalaConversionMapping.collectionConversion(collection) match {
            case Some(conversion) =>
              val decoder = new ArrayBufferDecoder[Any](manifest, reader, conversion.emptyInstance, conversion.fromBuffer)
              json.registerReader(manifest, decoder)
              Some(decoder)
            case _ =>
              None
          }
        case _ => None
      }
    }
  }

  case class CollectionConversion(
    emptyInstance: () => scala.collection.Iterable[Any],
    fromBuffer: mutable.ArrayBuffer[Any] => scala.collection.Iterable[Any]
  )

  private def analyzeEncoding(manifest: Type, element: Type, collection: Class[_], json: DslJson[_]) = {
    if (!classOf[scala.collection.Iterable[_]].isAssignableFrom(collection)) None
    else {
      if (classOf[AnyRef] == element) {
        val encoder = new IterableEncoder[Any](json, None)
        json.registerWriter(manifest, encoder)
        Some(encoder)
      } else {
        Option(json.tryFindWriter(element)) match {
          case Some(writer: JsonWriter.WriteObject[Any @unchecked]) =>
            val encoder = new IterableEncoder[Any](json, Option(writer))
            json.registerWriter(manifest, encoder)
            Some(encoder)
          case _ =>
            None
        }
      }
    }
  }
}
