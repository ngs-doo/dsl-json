package com.dslplatform.json
package runtime

import java.lang.reflect.{ParameterizedType, Type}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

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
          collectionConversion(collection) match {
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

  private case class CollectionConversion(
    emptyInstance: () => scala.collection.Iterable[Any],
    fromBuffer: mutable.ArrayBuffer[Any] => scala.collection.Iterable[Any]
  )

  private def collectionConversion(collection: Class[_]): Option[CollectionConversion] = {
    if (classOf[List[_]].isAssignableFrom(collection)) {
      Some(CollectionConversion(() => Nil, _.toList))
    } else if (classOf[Vector[_]].isAssignableFrom(collection)) {
      val empty = Vector.empty
      Some(CollectionConversion(() => empty, _.toVector))
    } else if (classOf[Set[_]].isAssignableFrom(collection)) {
      val empty = Set.empty
      Some(CollectionConversion(() => empty, _.toSet))
    } else if (classOf[mutable.ArrayBuffer[_]].isAssignableFrom(collection)) {
      Some(CollectionConversion(() => new ArrayBuffer(0), identity))
    } else if (classOf[scala.collection.immutable.IndexedSeq[_]].isAssignableFrom(collection)) {
      val empty = scala.collection.immutable.IndexedSeq.empty
      Some(CollectionConversion(() => empty, _.toIndexedSeq))
    } else if (classOf[mutable.Set[_]].isAssignableFrom(collection)) {
      Some(CollectionConversion(() => new mutable.HashSet(), ab => mutable.Set(ab:_*)))
    } else if (classOf[mutable.Stack[_]].isAssignableFrom(collection)) {
      Some(CollectionConversion(() => new mutable.Stack(), ab => mutable.Stack(ab:_*)))
    } else if (classOf[mutable.Queue[_]].isAssignableFrom(collection)) {
      Some(CollectionConversion(() => new mutable.Queue(), ab => mutable.Queue(ab:_*)))
    } else if (classOf[IndexedSeq[_]].isAssignableFrom(collection)) {
      val empty = IndexedSeq.empty
      Some(CollectionConversion(() => empty, identity))
    } else if (classOf[Seq[_]].isAssignableFrom(collection)) {
      val empty = Seq.empty
      Some(CollectionConversion(() => empty, identity))
    } else {
      None
    }
  }

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
