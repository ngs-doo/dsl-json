package com.dslplatform.json
package runtime

import java.lang.reflect.{ParameterizedType, Type}

object OptionAnalyzer {

	val Reader: DslJson.ConverterFactory[JsonReader.ReadObject[_]] = (manifest: Type, dslJson: DslJson[_]) => {
    manifest match {
      case pt: ParameterizedType if pt.getActualTypeArguments.length == 1 =>
        pt.getRawType match {
          case inner: Class[_] => analyzeDecoding(manifest, pt.getActualTypeArguments()(0), inner, dslJson).orNull
          case _ => null
        }
      case cl: Class[_] =>
        analyzeDecoding(manifest, classOf[AnyRef], cl, dslJson).orNull
      case _ => null
    }
  }

	val Writer: DslJson.ConverterFactory[JsonWriter.WriteObject[_]] = (manifest: Type, dslJson: DslJson[_]) => {
    manifest match {
      case pt: ParameterizedType if pt.getActualTypeArguments.length == 1 =>
        pt.getRawType match {
          case inner: Class[_] => analyzeEncoding(manifest, pt.getActualTypeArguments()(0), inner, dslJson).orNull
          case _ => null
        }
      case cl: Class[_] =>
        analyzeEncoding(manifest, classOf[AnyRef], cl, dslJson).orNull
      case _ => null
    }
	}

	private def analyzeDecoding(manifest: Type, content: Type, raw: Class[_], json: DslJson[_]): Option[OptionDecoder[_]] = {
    if (!classOf[Option[_]].isAssignableFrom(raw)) {
      None
    } else {
      content match {
        case opt: Class[_] if classOf[Option[_]].isAssignableFrom(opt) =>
          analyzeDecoding(content, classOf[AnyRef], classOf[Option[_]], json) match {
            case Some(nested) =>
              val outer = new OptionDecoder(nested)
              json.registerReader(manifest, outer)
              Some(outer)
            case _ => None
          }
        case _ =>
          Option(json.tryFindReader(content)) match {
            case Some(reader) =>
              val decoder = new OptionDecoder(reader)
              json.registerReader(manifest, decoder)
              Some(decoder)
            case _ => None
          }
      }
    }
  }

	private def analyzeEncoding(manifest: Type, content: Type, raw: Class[_], json: DslJson[_]): Option[JsonWriter.WriteObject[_]] = {
    if (!classOf[Option[_]].isAssignableFrom(raw)) {
      None
    } else {
      content match {
        case opt: Class[_] if classOf[Option[_]].isAssignableFrom(opt) =>
          analyzeEncoding(content, classOf[AnyRef], classOf[Option[_]], json) match {
            case Some(nested) =>
              json.registerWriter(manifest, nested)
              Some(nested)
            case _ => None
          }
        case _ =>
          val writer = if (classOf[AnyRef] eq content) None else Option(json.tryFindWriter(content))
          if ((classOf[AnyRef] ne content) && writer.isEmpty) {
            None
          } else if (writer.isEmpty || (content eq classOf[AnyRef])) {
            val encoder = new OptionUnknownEncoder(json)
            json.registerWriter(manifest, encoder)
            Some(encoder)
          } else {
            val encoder = new OptionKnownEncoder(json, writer.get)
            json.registerWriter(manifest, encoder)
            Some(encoder)
          }
      }
    }
  }

	private final class OptionDecoder[T](decoder: JsonReader.ReadObject[T]) extends JsonReader.ReadObject[Option[T]] {
		require (decoder ne null, "decoder can't be null")

		override def read(reader: JsonReader[_]): Option[T] = {
			if (reader.wasNull) None
			else Option(decoder.read(reader))
		}
	}

  private final class OptionKnownEncoder[T](json: DslJson[_], encoder: JsonWriter.WriteObject[T]) extends JsonWriter.WriteObject[Option[T]] {
    require(json ne null, "json can't be null")
    require(encoder ne null, "encoder can't be null")

    override def write(writer: JsonWriter, value: Option[T]): Unit = {
      if (value.isEmpty) writer.writeNull()
      else encoder.write(writer, value.get)
    }
  }

  private final class OptionUnknownEncoder[T](json: DslJson[_]) extends JsonWriter.WriteObject[Option[T]] {
    require(json ne null, "json can't be null")

    override def write(writer: JsonWriter, value: Option[T]): Unit = {
      if (value == null || value.isEmpty) writer.writeNull()
      else {
        val unpacked = value.get
        if (unpacked == null) writer.writeNull()
        else {
          val jw = json.tryFindWriter(unpacked.getClass).asInstanceOf[JsonWriter.WriteObject[T]]
          if (jw == null) throw new SerializationException(s"Unable to find writer for ${unpacked.getClass}")
          jw.write(writer, unpacked)
        }
      }
    }
  }
}
