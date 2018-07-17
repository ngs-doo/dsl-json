package com.dslplatform.json
package runtime

import java.io.IOException
import java.lang.reflect.{Constructor, Modifier, ParameterizedType, Type => JavaType}
import java.util.function

import scala.reflect.runtime.universe
import scala.util.{Success, Try}

object ScalaTupleAnalyzer {
  val Reader: DslJson.ConverterFactory[JsonReader.ReadObject[_]] = (manifest: JavaType, dslJson: DslJson[_]) => {
    manifest match {
      case pt: ParameterizedType =>
        pt.getRawType match {
          case tuple: Class[_] => analyze(pt, tuple, dslJson).orNull
          case _ => null
        }
      case _ => null
    }
  }

  val Writer: DslJson.ConverterFactory[JsonWriter.WriteObject[_]] = (manifest: JavaType, dslJson: DslJson[_]) => {
    manifest match {
      case pt: ParameterizedType =>
        pt.getRawType match {
          case tuple: Class[_] => analyze(pt, tuple, dslJson).orNull
          case _ => null
        }
      case _ => null
    }
  }

  private def analyze(
    manifest: ParameterizedType,
    raw: Class[_],
    json: DslJson[_]
  ): Option[ArrayFormatDescription[Array[AnyRef], Product]] = {
    if (!classOf[Product].isAssignableFrom(raw) || !raw.getName.startsWith("scala.Tuple")) None
    else {
      val ctors = raw.getDeclaredConstructors.filter(it => (it.getModifiers & Modifier.PUBLIC) == 1)
      val mirror = scala.reflect.runtime.currentMirror
      Try(mirror.staticClass(s"scala.Tuple${manifest.getActualTypeArguments.length}")) match {
        case Success(sc) =>
          sc.info.members.find(_.name.toString == "<init>") match {
            case Some(init) if init.info.paramLists.size == 1 && ctors.length == 1 =>
              val ctor = ctors.head.asInstanceOf[Constructor[Product]]
              analyzeTuple(manifest, json, ctor, init.info.paramLists.head)
            case _ =>
              None
          }
        case _ =>
          None
      }
    }
  }

  private case class TypeInfo(rawType: JavaType, isUnknown: Boolean, index: Int)

  private def analyzeTuple(
    manifest: ParameterizedType,
    json: DslJson[_],
    ctor: Constructor[Product],
    params: List[universe.Symbol]
  ): Option[ArrayFormatDescription[Array[AnyRef], Product]] = {
    val arguments = manifest.getActualTypeArguments.zipWithIndex.map { case (rt, i) =>
      TypeInfo(rt, Generics.isUnknownType(rt), i)
    }
    val writeProps = arguments.map { ti =>
      Settings.createArrayEncoder[Product, Any](
        new Settings.Function[Product, Any] {
          override def apply(p: Product): Any = p.productElement(ti.index)
        },
        json,
        if (ti.isUnknown) null else ti.rawType).asInstanceOf[JsonWriter.WriteObject[_]]
    }
    val bindProps = arguments.flatMap { ti =>
      if (ti.isUnknown || json.tryFindWriter(ti.rawType) != null && json.tryFindReader(ti.rawType) != null) {
        val isNullable = ti.rawType.getTypeName.startsWith("scala.Option<") || ti.rawType.getTypeName == "scala.Option"
        val decoder = Settings.createArrayDecoder[Array[AnyRef], AnyRef](
          new Settings.BiConsumer[Array[AnyRef], AnyRef] {
            override def accept(
              arr: Array[AnyRef],
              u: AnyRef): Unit = arr(ti.index) = u
          },
          json,
          ti.rawType)
        Some(
          if (isNullable) decoder else new JsonReader.BindObject[Array[AnyRef]] {
            override def bind(reader: JsonReader[_], args: Array[AnyRef]): Array[AnyRef] = {
              if (reader.wasNull()) throw new IOException(s"Tuple property ${ti.index + 1} of $manifest is not allowed to be null.")
              decoder.bind(reader, args)
            }
          })
      } else None
    }.map(_.asInstanceOf[JsonReader.BindObject[_]])
    if (params.size == writeProps.length && params.size == bindProps.length) {
      val converter = new ArrayFormatDescription[Array[AnyRef], Product](
        manifest,
        () => new Array[AnyRef](params.size),
        new Settings.Function[Array[AnyRef], Product] {
          override def apply(args: Array[AnyRef]): Product = ctor.newInstance(args: _*)
        },
        writeProps,
        bindProps)
      json.registerWriter(manifest, converter)
      json.registerReader(manifest, converter)
      Some(converter)
    } else {
      None
    }
  }
}