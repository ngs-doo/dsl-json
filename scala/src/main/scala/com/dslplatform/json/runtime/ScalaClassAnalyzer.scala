package com.dslplatform.json
package runtime

import java.io.IOException
import java.lang.reflect.{Constructor, Method, Modifier, ParameterizedType, Type => JavaType}

import scala.collection.mutable
import scala.reflect.runtime.universe
import scala.util.Try

object ScalaClassAnalyzer {

  val Reader: DslJson.ConverterFactory[JsonReader.ReadObject[_]] = (manifest: JavaType, dslJson: DslJson[_]) => {
    manifest match {
      case cl: Class[_] => analyze(manifest, cl, dslJson, reading = true) match {
        case Some(Left(fd)) => fd
        case Some(Right(id)) => id
        case _ => null
      }
      case pt: ParameterizedType if pt.getActualTypeArguments.length == 1 =>
        pt.getRawType match {
            //TODO: support for generics
          case rc: Class[_] => null//analyze(manifest, rc, dslJson).orNull
          case _ => null
        }
      case _ => null
    }
  }

  val Binder: DslJson.ConverterFactory[JsonReader.BindObject[_]] = (manifest: JavaType, dslJson: DslJson[_]) => {
    manifest match {
      case cl: Class[_] => analyze(manifest, cl, dslJson, reading = true) match {
        case Some(Left(fd)) => fd
        case _ => null
      }
      case pt: ParameterizedType if pt.getActualTypeArguments.length == 1 =>
        pt.getRawType match {
          //TODO: support for generics
          case rc: Class[_] => null//analyze(manifest, rc, dslJson).orNull
          case _ => null
        }
      case _ => null
    }
  }

  val Writer: DslJson.ConverterFactory[JsonWriter.WriteObject[_]] = (manifest: JavaType, dslJson: DslJson[_]) => {
    manifest match {
      case cl: Class[_] => analyze(manifest, cl, dslJson, reading = false) match {
        case Some(Left(fd)) => fd
        case Some(Right(id)) => id
        case _ => null
      }
      case pt: ParameterizedType if pt.getActualTypeArguments.length == 1 =>
        pt.getRawType match {
            //TODO: support for generics
          case rc: Class[_] => null//analyze(manifest, rc, dslJson).orNull
          case _ => null
        }
      case _ => null
    }
  }

  private class LazyImmutableDescription(json: DslJson[_], manifest: JavaType) extends JsonWriter.WriteObject[AnyRef] with JsonReader.ReadObject[AnyRef] {
    private var encoder: Option[JsonWriter.WriteObject[AnyRef]] = None
    private var decoder: Option[JsonReader.ReadObject[AnyRef]] = None
    var resolved: Option[ImmutableDescription[AnyRef]] = None

    private def checkSignatureNotFound() = {
      var i = 0
      while (i < 50) {
        try {
          Thread.sleep(100)
        } catch {
          case e: InterruptedException => throw new SerializationException(e)
        }
        if (resolved.nonEmpty) {
          encoder = Some(resolved.get.asInstanceOf[JsonWriter.WriteObject[AnyRef]])
          decoder = Some(resolved.get.asInstanceOf[JsonReader.ReadObject[AnyRef]])
          i = 50
        }
        i += 1
      }
      resolved.isEmpty
    }

    override def read(reader: JsonReader[_]): AnyRef = {
      if (decoder.isEmpty) {
        if (checkSignatureNotFound()) {
          val tmp = json.tryFindReader(manifest).asInstanceOf[JsonReader.ReadObject[AnyRef]]
          if (tmp == null || (tmp eq this)) throw new SerializationException(s"Unable to find reader for $manifest")
          decoder = Some(tmp)
        }
      }
      decoder.get.read(reader)
    }

    override def write(writer: JsonWriter, value: AnyRef): Unit = {
      if (encoder.isEmpty) {
        if (checkSignatureNotFound()) {
          val tmp = json.tryFindWriter(manifest).asInstanceOf[JsonWriter.WriteObject[AnyRef]]
          if (tmp == null || (tmp eq this)) throw new SerializationException(s"Unable to find writer for $manifest")
          encoder = Some(tmp)
        }
      }
      encoder.get.write(writer, value)
    }
  }

  private class LazyObjectDescription(json: DslJson[_], manifest: JavaType) extends JsonWriter.WriteObject[AnyRef] with JsonReader.ReadObject[AnyRef] with JsonReader.BindObject[AnyRef] {
    private var encoder: Option[JsonWriter.WriteObject[AnyRef]] = None
    private var decoder: Option[JsonReader.ReadObject[AnyRef]] = None
    private var binder: Option[JsonReader.BindObject[AnyRef]] = None
    var resolved: Option[ObjectFormatDescription[AnyRef, AnyRef]] = None

    private def checkSignatureNotFound() = {
      var i = 0
      while (i < 50) {
        try {
          Thread.sleep(100)
        } catch {
          case e: InterruptedException => throw new SerializationException(e)
        }
        if (resolved.nonEmpty) {
          encoder = Some(resolved.get.asInstanceOf[JsonWriter.WriteObject[AnyRef]])
          decoder = Some(resolved.get.asInstanceOf[JsonReader.ReadObject[AnyRef]])
          binder = Some(resolved.get.asInstanceOf[JsonReader.BindObject[AnyRef]])
          i = 50
        }
        i += 1
      }
      resolved.isEmpty
    }

    override def read(reader: JsonReader[_]): AnyRef = {
      if (decoder.isEmpty) {
        if (checkSignatureNotFound()) {
          val tmp = json.tryFindReader(manifest).asInstanceOf[JsonReader.ReadObject[AnyRef]]
          if (tmp == null || (tmp eq this)) throw new SerializationException(s"Unable to find reader for $manifest")
          decoder = Some(tmp)
        }
      }
      decoder.get.read(reader)
    }

    override def bind(reader: JsonReader[_], instance: AnyRef): AnyRef = {
      if (binder.isEmpty) {
        if (checkSignatureNotFound()) {
          val tmp = json.tryFindBinder(manifest).asInstanceOf[JsonReader.BindObject[AnyRef]]
          if (tmp == null || (tmp eq this)) throw new SerializationException(s"Unable to find binder for $manifest")
          binder = Some(tmp)
        }
      }
      binder.get.bind(reader, instance)
    }

    override def write(writer: JsonWriter, value: AnyRef): Unit = {
      if (encoder.isEmpty) {
        if (checkSignatureNotFound()) {
          val tmp = json.tryFindWriter(manifest).asInstanceOf[JsonWriter.WriteObject[AnyRef]]
          if (tmp == null || (tmp eq this)) throw new SerializationException(s"Unable to find writer for $manifest")
          encoder = Some(tmp)
        }
      }
      encoder.get.write(writer, value)
    }
  }

  private case class TypeInfo(
    name: String,
    rawType: JavaType,
    isUnknown: Boolean,
    concreteType: JavaType,
    index: Int,
    getDefault: Option[() => AnyRef])

  private def analyze(
    manifest: JavaType,
    raw: Class[_],
    json: DslJson[_],
    reading: Boolean
  ) = {
    if (isSupported(manifest, raw)) {
      val sc = scala.reflect.runtime.currentMirror.staticClass(manifest.getTypeName)
      analyzeType(manifest, raw, json, reading, sc.info)
    } else {
      None
    }
  }

  def isSupported(manifest: JavaType, raw: Class[_]): Boolean = {
    !(classOf[scala.collection.Traversable[_]].isAssignableFrom(raw) ||
      classOf[AnyRef] == manifest ||
      (raw.getModifiers & Modifier.ABSTRACT) != 0 ||
      raw.isInterface ||
      (raw.getDeclaringClass != null && (raw.getModifiers & Modifier.STATIC) == 0) ||
      (raw.getModifiers & Modifier.PUBLIC) == 0)
  }

  def analyzeType(
    manifest: JavaType,
    raw: Class[_],
    json: DslJson[_],
    reading: Boolean,
    tpe: universe.TypeApi
  ): Option[Either[ObjectFormatDescription[AnyRef, AnyRef], ImmutableDescription[AnyRef]]] = {
    val ctors = raw.getDeclaredConstructors.filter(it => (it.getModifiers & Modifier.PUBLIC) == 1)
    tpe.members.find(_.name.toString == "<init>") match {
      case Some(init) if init.info.paramLists.size == 1 && ctors.exists(_.getParameterCount == 0) =>
        val methods = tpe.members.flatMap { it =>
          if (it.isPublic && it.isMethod) {
            val eqName = it.name.toString + "_$eq"
            val setter = tpe.members.find(m => m.isPublic && m.name.toString == eqName)
            setter.map { s => it -> s }
          } else None
        }.toMap
        if (methods.nonEmpty) analyzeEmptyCtor(manifest, raw, json, methods, reading)
        else None
      case Some(init) if init.info.paramLists.size == 1 && ctors.length == 1 =>
        analyzeClassWithCtor(manifest, raw, json, ctors, tpe, init.info.paramLists.head, reading)
      case _ =>
        None
    }
  }

  private def analyzeClassWithCtor(
    manifest: JavaType,
    raw: Class[_],
    json: DslJson[_],
    ctors: Array[Constructor[_]],
    tpe: universe.TypeApi,
    params: List[universe.Symbol],
    reading: Boolean
  ) = {
    val isProduct = classOf[Product].isAssignableFrom(raw)
    val genericMappings = Generics.analyze(manifest, raw)
    val defaults = tpe.companion.members.filter(_.name.toString.startsWith("$lessinit$greater$default$"))
    val types = params.map(_.typeSignature).toSet
    val sameTypes = tpe.members.filter { it =>
      !it.name.toString.contains("$") && types.contains(it.typeSignature)
    }
    lazy val names = Option(ImmutableAnalyzer.extractNames(ctors.head).orElseGet(null))
    val arguments = params.zipWithIndex.flatMap { case (p, i) =>
      val defMethod = defaults.find(_.name.toString.endsWith("$" + (i + 1))).flatMap { d =>
        val name = d.name.toString
        raw.getDeclaredMethods.find(_.getName == name).map { m =>
          () => m.invoke(null, Array():_*)
        }
      }
      val pName = if (p.name.toString.contains("$")) names.map(it => it(i)) else Some(p.name.toString)
      Try(TypeAnalysis.convertType(p.typeSignature)).toOption.flatMap { rt =>
        val concreteType = Generics.makeConcrete(rt, genericMappings)
        val isUnknown = Generics.isUnknownType(rt)
        val machingTypeAndName = {
          if (pName.isEmpty) None
          else sameTypes.find(it => it.typeSignature == p.typeSignature && it.name.toString.trim == pName.get)
        }
        lazy val machingTypeOnly = {
          if (sameTypes.size != params.size) None
          else sameTypes.find(it => it.typeSignature == p.typeSignature)
        }
        val name = machingTypeAndName.orElse(machingTypeOnly).map(_.name.toString.trim).orElse(pName)
        if (name.isEmpty || name.get.contains("$")) None
        Some(TypeInfo(name.get, rt, isUnknown, concreteType, i, defMethod))
      }
    }
    if (arguments.size == params.size) {
      val tmp = new LazyImmutableDescription(json, manifest)
      val oldWriter = json.registerWriter(manifest, tmp)
      val oldReader = json.registerReader(manifest, tmp)
      val writeProps = if (isProduct) {
        arguments.map { ti =>
          Settings.createEncoder(
            new GetProductIndex(ti.index),
            ti.name,
            json,
            if (ti.isUnknown) null else ti.concreteType).asInstanceOf[JsonWriter.WriteObject[_]]
        }.toArray
      } else {
        arguments.flatMap { ti =>
          raw.getMethods.find(it => it.getName == ti.name && it.getParameterCount == 0).map { m =>
            Settings.createEncoder(
              new Reflection.ReadMethod(m),
              ti.name,
              json,
              if (ti.isUnknown) null else ti.concreteType)
          }
        }.toArray
      }
      val ctor = ctors.head.asInstanceOf[Constructor[AnyRef]]
      val readProps = arguments.flatMap { ti =>
        if (ti.isUnknown || json.tryFindWriter(ti.concreteType) != null && json.tryFindReader(ti.concreteType) != null) {
          val isNullable = ti.rawType.getTypeName.startsWith("scala.Option<")
          Some(new DecodePropertyInfo[JsonReader.ReadObject[_]](ti.name, false, ti.getDefault.isEmpty, ti.index, !isNullable, new WriteCtor(json, ti.concreteType, ctor)))
        } else None
      }.toArray
      if (params.size == writeProps.length && (!reading || params.size == readProps.length)) {
        val defArgs = new Array[AnyRef](params.size)
        arguments.zipWithIndex.foreach { case (a, i) =>
          if (a.getDefault.isDefined) {
            //TODO: it would be more correct to apply this on every invocation, but lets just use stable value instead
            defArgs(i) = a.getDefault.get.apply()
          }
        }
        val converter = new ImmutableDescription[AnyRef](
          manifest,
          defArgs,
          new Settings.Function[Array[AnyRef], AnyRef] {
            override def apply(args: Array[AnyRef]): AnyRef = ctor.newInstance(args:_*)
          },
          writeProps,
          readProps,
          !json.omitDefaults,
          true)
        tmp.resolved = Some(converter)
        json.registerWriter(manifest, converter)
        //TODO: since nested case classes have their type signatures broken allow encoding,
        //TODO: but don't allow decoding if some types are erased
        if (params.size == readProps.length) {
          json.registerReader(manifest, converter)
        } else {
          json.registerReader(manifest, oldReader)
        }
        Some(Right(converter))
      } else {
        json.registerWriter(manifest, oldWriter)
        json.registerReader(manifest, oldReader)
        None
      }
    } else None
  }

  private class GetProductIndex(index: Int) extends Settings.Function[Product, Any] {
    override def apply(t: Product): Any = t.productElement(index)
  }

  private def analyzeEmptyCtor(
    manifest: JavaType,
    raw: Class[_],
    json: DslJson[_],
    methods: Map[universe.Symbol, universe.Symbol],
    reading: Boolean
  ) = {
    val tmp = new LazyObjectDescription(json, manifest)
    val oldWriter = json.registerWriter(manifest, tmp)
    val oldReader = json.registerReader(manifest, tmp)
    val foundWrite = new mutable.LinkedHashMap[String, JsonWriter.WriteObject[_]]
    val foundRead = new mutable.LinkedHashMap[String, DecodePropertyInfo[JsonReader.BindObject[_]]]
    val genericMappings = Generics.analyze(manifest, raw)
    val rawAny = raw.asInstanceOf[Class[AnyRef]]
    val newInstance = new InstanceFactory[AnyRef] {
      override def create(): AnyRef = rawAny.newInstance()
    }
    var index = 0
    val rawMethods = raw.getMethods
    methods.foreach { case (g, _) =>
      val gName = g.name.toString
      rawMethods.find(m => m.getParameterCount == 0 && m.getName.equals(gName)).foreach { jm =>
        Try(TypeAnalysis.convertType(g.typeSignature.resultType)).foreach { t =>
          if (analyzeMethods(jm, t, raw, json, foundWrite, foundRead, index, genericMappings)) {
            index += 1
          }
        }
      }
    }
    if (foundWrite.size == methods.size && (!reading || foundRead.size == methods.size)) {
      val writeProps = foundWrite.values.toArray
      val readProps = foundRead.values.toArray
      val converter = ObjectFormatDescription.create(rawAny, newInstance, writeProps, readProps, json, true)
      tmp.resolved = Some(converter)
      json.registerWriter(manifest, converter)
      if (foundRead.size == methods.size) {
        json.registerReader(manifest, converter)
        json.registerBinder(manifest, converter)
      } else {
        json.registerReader(manifest, oldReader)
      }
      Some(Left(converter))
    } else {
      json.registerWriter(manifest, oldWriter)
      json.registerReader(manifest, oldReader)
      None
    }
  }

  private final class WriteCtor(json: DslJson[_], manifest: JavaType, ctor: Constructor[_]) extends JsonReader.ReadObject[Any] {
    private var decoder: Option[JsonReader.ReadObject[Any]] = None

    override def read(reader: JsonReader[_]): Any = {
      if (decoder.isEmpty) {
        Option(json.tryFindReader(manifest)) match {
          case Some(f: JsonReader.ReadObject[Any @unchecked]) => decoder = Some(f)
          case _ => throw new IOException(s"Unable to find reader for $manifest on $ctor")
        }
      }
      decoder.get.read(reader)
    }
  }

  private def analyzeMethods(
    mget: Method,
    actualType: JavaType,
    manifest: Class[_],
    json: DslJson[_],
    foundWrite: mutable.LinkedHashMap[String, JsonWriter.WriteObject[_]],
    foundRead: mutable.LinkedHashMap[String, DecodePropertyInfo[JsonReader.BindObject[_]]],
    index: Int,
    genericMappings: java.util.HashMap[JavaType, JavaType]): Boolean = {
    if (mget.getParameterTypes.length != 0) false
    else {
      val name = mget.getName
      val setName = name + "_$eq"
      manifest.getMethods.find(_.getName == setName) match {
        case Some(mset) if !canRead(mget.getModifiers) || !canWrite(mset.getModifiers) =>
          false
        case Some(_) if foundRead.contains(name) && foundWrite.contains(name) =>
          false
        case Some(mset) =>
          val concreteType = Generics.makeConcrete(actualType, genericMappings)
          val isUnknown = Generics.isUnknownType(actualType)
          if (isUnknown || json.tryFindWriter(concreteType) != null) {
            foundWrite.put(
              name,
              Settings.createEncoder(
                new Reflection.ReadMethod(mget),
                name,
                json,
                if (isUnknown) null else concreteType))
            if (isUnknown || json.tryFindReader(concreteType) != null) {
              foundRead.put(
                name,
                Settings.createDecoder(
                  new Reflection.SetMethod(mset),
                  name,
                  json,
                  false,
                  false,
                  index,
                  !concreteType.getTypeName.startsWith("scala.Option<"),
                  concreteType).asInstanceOf[DecodePropertyInfo[JsonReader.BindObject[_]]]
              )
            }
            true
          } else {
            false
          }
        case _ =>
          false
      }
    }
  }

  private def canRead(modifiers: Int) =
    (modifiers & Modifier.PUBLIC) != 0 &&
      (modifiers & Modifier.TRANSIENT) == 0 &&
      (modifiers & Modifier.NATIVE) == 0 &&
      (modifiers & Modifier.STATIC) == 0

  private def canWrite(modifiers: Int) =
    (modifiers & Modifier.PUBLIC) != 0 &&
      (modifiers & Modifier.TRANSIENT) == 0 &&
      (modifiers & Modifier.NATIVE) == 0 &&
      (modifiers & Modifier.FINAL) == 0 &&
      (modifiers & Modifier.STATIC) == 0

}
