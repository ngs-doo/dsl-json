package com.dslplatform.json
package runtime

import java.lang.annotation.Annotation
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
      case pt: ParameterizedType =>
        pt.getRawType match {
          case rc: Class[_] =>
            analyze(manifest, rc, dslJson, reading = true) match {
            case Some(Left(fd)) => fd
            case Some(Right(id)) => id
            case _ => null
          }
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
      case pt: ParameterizedType =>
        pt.getRawType match {
          case rc: Class[_] => analyze(manifest, rc, dslJson, reading = false) match {
            case Some(Left(fd)) => fd
            case _ => null
          }
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
      case pt: ParameterizedType =>
        pt.getRawType match {
          case rc: Class[_] => analyze(manifest, rc, dslJson, reading = false) match {
            case Some(Left(fd)) => fd
            case Some(Right(id)) => id
            case _ => null
          }
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
          case e: InterruptedException => throw new ConfigurationException(e)
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
          if (tmp == null || (tmp eq this)) throw new ConfigurationException(s"Unable to find reader for $manifest")
          decoder = Some(tmp)
        }
      }
      decoder.get.read(reader)
    }

    override def write(writer: JsonWriter, value: AnyRef): Unit = {
      if (encoder.isEmpty) {
        if (checkSignatureNotFound()) {
          val tmp = json.tryFindWriter(manifest).asInstanceOf[JsonWriter.WriteObject[AnyRef]]
          if (tmp == null || (tmp eq this)) throw new ConfigurationException(s"Unable to find writer for $manifest")
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
          case e: InterruptedException => throw new ConfigurationException(e)
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
          if (tmp == null || (tmp eq this)) throw new ConfigurationException(s"Unable to find reader for $manifest")
          decoder = Some(tmp)
        }
      }
      decoder.get.read(reader)
    }

    override def bind(reader: JsonReader[_], instance: AnyRef): AnyRef = {
      if (binder.isEmpty) {
        if (checkSignatureNotFound()) {
          val tmp = json.tryFindBinder(manifest).asInstanceOf[JsonReader.BindObject[AnyRef]]
          if (tmp == null || (tmp eq this)) throw new ConfigurationException(s"Unable to find binder for $manifest")
          binder = Some(tmp)
        }
      }
      binder.get.bind(reader, instance)
    }

    override def write(writer: JsonWriter, value: AnyRef): Unit = {
      if (encoder.isEmpty) {
        if (checkSignatureNotFound()) {
          val tmp = json.tryFindWriter(manifest).asInstanceOf[JsonWriter.WriteObject[AnyRef]]
          if (tmp == null || (tmp eq this)) throw new ConfigurationException(s"Unable to find writer for $manifest")
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
      val sc = scala.reflect.runtime.currentMirror.staticClass(raw.getTypeName)
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
    val emptyCtor = raw.getDeclaredConstructors.find(c => c.getParameterCount == 0 && (c.getModifiers & Modifier.PUBLIC) == 1)
    val matchedCtor = Option(ImmutableAnalyzer.findBestCtor(raw, json)).orElse(emptyCtor)
    val markers = json.getRegisteredCreatorMarkers
    val lookup = {
      val res = new mutable.HashMap[String, Class[_ <: Annotation]]
      val iter = markers.entrySet().iterator()
      while (iter.hasNext) {
        val kv = iter.next()
        res.put(kv.getKey.getName, kv.getKey)
      }
      res
    }
    val ctors = tpe.members.filter(_.isConstructor).toIndexedSeq
    val annotation = matchedCtor.flatMap(ct => lookup.values.find(a => ct.getAnnotation(a) != null)).map(_.getName)
    val className = raw.getName.replace('$', '.')
    val mainCtor = ctors.filter(it => it.isPublic && it.owner.fullName == className).lastOption
    val annotatedCtor = annotation.flatMap { ann =>
      ctors.find(it => it.info.paramLists.size == 1 && it.annotations.exists(a => a.toString == ann && (it.isPublic || markers.get(lookup(ann)))))
    }
    lazy val setters = tpe.members.flatMap { it =>
      if (it.isPublic && it.isMethod) {
        val eqName = it.name.toString + "_$eq"
        val setter = tpe.members.find(m => m.isPublic && m.name.toString == eqName)
        setter.map { s => it -> s }
      } else None
    }.toMap
    lazy val mainCtorProps = mainCtor.map(_.info.paramLists.head).getOrElse(Nil).toIndexedSeq
    lazy val annotatedFactory = tpe.companion.members.find(it => !markers.isEmpty && it.annotations.exists(a => lookup.contains(a.toString) && (it.isPublic || markers.get(lookup(a.toString)))))
    lazy val module = scala.reflect.runtime.currentMirror.staticModule(raw.getName)
    lazy val instance = scala.reflect.runtime.currentMirror.reflectModule(module).instance.asInstanceOf[AnyRef]
    lazy val objectClass = instance.getClass
    if (annotatedCtor.isDefined) {
      val arguments = annotatedCtor.get.info.paramLists.head.toIndexedSeq
      analyzeClassWithCtor(manifest, raw, json, matchedCtor.get.asInstanceOf[Constructor[AnyRef]], tpe, arguments, reading)
    } else if (annotatedFactory.isDefined) {
      val factory = annotatedFactory.get
      val factoryName = factory.name.toString
      val method = objectClass.getDeclaredMethods
        .find(it => it.getName == factoryName && it.getReturnType == raw && lookup.values.exists(a => it.getAnnotation(a) != null))
        .getOrElse(throw new ConfigurationException(s"Unable to find factory: $factory in: $raw"))
      if ((method.getModifiers & Modifier.PUBLIC) != Modifier.PUBLIC) {
        try {
          method.setAccessible(true)
        } catch {
          case ex: Exception =>
            throw new ConfigurationException("Unable to promote access for private factory " + method + ". Please check environment setup, or set marker on public method", ex)
        }
      }
      analyzeClassWithFactory(manifest, raw, json, tpe, factory.asMethod, method, instance, factory.info.paramLists.head, reading)
    } else if (emptyCtor.isDefined && setters.nonEmpty) {
      analyzeEmptyCtor(manifest, raw, json, setters, reading)
    } else if (mainCtor.isDefined && matchedCtor.isDefined && matchedCtor.get.getParameterCount == mainCtorProps.size) { //TODO: better matching
      analyzeClassWithCtor(manifest, raw, json, matchedCtor.get.asInstanceOf[Constructor[AnyRef]], tpe, mainCtorProps, reading)
    } else if (emptyCtor.isDefined) {
      analyzeEmptyCtor(manifest, raw, json, setters, reading)
    } else {
      val ctorParams = matchedCtor.map(_.getParameterCount).getOrElse(-1)
      tpe.companion.members.find(it => it.isPublic && it.name.toString == "apply") match {
        case Some(init) if init.info.paramLists.size == 1 && ctorParams == init.info.paramLists.head.size =>
          val applies = objectClass.getMethods.filter(it => it.getName == "apply" && it.getReturnType == raw && it.getParameterCount == ctorParams)
          if (applies.length == 1) {
            analyzeClassWithFactory(manifest, raw, json, tpe, init.asMethod, applies(0), instance, init.info.paramLists.head, reading)
          } else {
            None
          }
        case _ =>
          None
      }
    }
  }

  private def analyzeClassWithCtor(
    manifest: JavaType,
    raw: Class[_],
    json: DslJson[_],
    ctor: Constructor[AnyRef],
    tpe: universe.TypeApi,
    params: scala.collection.Seq[universe.Symbol],
    reading: Boolean
  ) = {
    import scala.collection.JavaConverters._
    val genericMappings = Generics.analyze(manifest, raw)
    val genericsByName = genericMappings.entrySet().asScala.map(kv => kv.getKey.getTypeName -> kv.getValue).toMap
    val defaults = tpe.companion.members.filter(_.name.toString.startsWith("$lessinit$greater$default$"))
    val types = params.map(_.typeSignature).toSet
    val sameTypes = tpe.members.filter { it =>
      it.isPublic && !it.name.toString.contains("$") &&
        (types.contains(it.typeSignature) || types.contains(it.typeSignature.resultType))
    }
    val names = Option(ImmutableAnalyzer.extractNames(ctor))
    val arguments = params.zipWithIndex.flatMap { case (p, i) =>
      val defMethod = defaults.find(_.name.toString.endsWith("$" + (i + 1))).flatMap { d =>
        val name = d.name.toString
        raw.getDeclaredMethods.find(_.getName == name).map { m =>
          () => m.invoke(null)
        }
      }
      val pType = p.typeSignature
      val pName = if (p.name.toString.contains("$")) names.map(it => it(i)) else Some(p.name.toString)
      Try(TypeAnalysis.convertType(pType, genericsByName)).toOption.flatMap { rt =>
        val concreteType = Generics.makeConcrete(rt, genericMappings)
        if (json.context != null && ObjectAnalyzer.matchesContext(concreteType, json)) {
          Some(TypeInfo("", rt, false, concreteType, i, None))
        } else {
          val isUnknown = Generics.isUnknownType(rt)
          val matchingTypeAndName = {
            if (pName.isEmpty) None
            else sameTypes.find(it => pName.contains(it.name.toString.trim) && (it.typeSignature == pType || it.typeSignature.resultType == pType))
          }
          lazy val matchingTypeOnly = {
            if (sameTypes.size != params.size) None
            else sameTypes.find(it => it.typeSignature == pType || it.typeSignature.resultType == pType)
          }
          val name = matchingTypeAndName.orElse(matchingTypeOnly).map(_.name.toString.trim).orElse(pName)
          if (name.isEmpty || name.get.contains("$")) None
          else Some(TypeInfo(name.get, rt, isUnknown, concreteType, i, defMethod))
        }
      }
    }
    if (arguments.size == params.size) {
      val tmp = new LazyImmutableDescription(json, manifest)
      val oldWriter = json.registerWriter(manifest, tmp)
      val oldReader = json.registerReader(manifest, tmp)
      val writeProps = arguments.filter(_.name.nonEmpty).flatMap { ti =>
        raw.getMethods.find(it => it.getName == ti.name && it.getParameterCount == 0).map { m =>
          Settings.createEncoder(
            new Reflection.ReadMethod(m),
            ti.name,
            json,
            if (ti.isUnknown) null else ti.concreteType)
        }
      }.toArray
      val readProps = arguments.flatMap { ti =>
        if (ti.name.isEmpty) {
          None
        } else if (ti.isUnknown || json.tryFindWriter(ti.concreteType) != null && json.tryFindReader(ti.concreteType) != null) {
          val isNullable = ti.rawType.getTypeName.startsWith("scala.Option<")
          val writeProp = new WriteProperty(json, ti.concreteType, ctor)
          Some(new DecodePropertyInfo[JsonReader.ReadObject[_]](ti.name, false, ti.getDefault.isEmpty, ti.index, !isNullable, writeProp))
        } else None
      }.toArray
      val emptyArgs = arguments.count(_.name.isEmpty)
      if (params.size == writeProps.length + emptyArgs && (!reading || params.size == readProps.length + emptyArgs)) {
        val defArgs = setupDefaults(params, arguments, json)
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
        if (params.size == readProps.length + emptyArgs) {
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

  private def setupDefaults(
    params: scala.collection.Seq[universe.Symbol],
    arguments: scala.collection.Seq[TypeInfo],
    json: DslJson[_]
  ): Array[AnyRef] = {
    val defArgs = new Array[AnyRef](params.size)
    arguments.zipWithIndex.foreach { case (a, i) =>
      if (a.getDefault.isDefined) {
        //TODO: it would be more correct to apply this on every invocation, but lets just use stable value instead
        defArgs(i) = a.getDefault.get.apply()
      } else if (a.name.isEmpty) {
        defArgs(i) = json.context.asInstanceOf[AnyRef]
      }
    }
    defArgs
  }

  private def analyzeClassWithFactory(
    manifest: JavaType,
    raw: Class[_],
    json: DslJson[_],
    tpe: universe.TypeApi,
    init: universe.MethodSymbol,
    factoryMethod: Method,
    companion: AnyRef,
    params: List[universe.Symbol],
    reading: Boolean
  ) = {
    import scala.collection.JavaConverters._
    val genericMappings = Generics.analyze(manifest, raw)
    val genericsByName = genericMappings.entrySet().asScala.map(kv => kv.getKey.getTypeName -> kv.getValue).toMap
    val defaults = init.owner.info.members.filter(_.name.toString.startsWith("apply$default$"))
    val types = params.map(_.typeSignature).toSet
    val sameTypes = tpe.members.flatMap { it =>
      if (it.isPublic && !it.name.toString.contains("$")) {
        if (types.contains(it.typeSignature)) {
          Some(it.name.toString.trim -> it.typeSignature)
        } else if(types.contains(it.typeSignature.resultType)) {
          Some(it.name.toString.trim -> it.typeSignature.resultType)
        } else None
      } else None
    }.toMap
    val names = Some(params.map(_.name.toString))
    val arguments = params.zipWithIndex.flatMap { case (p, i) =>
      val defMethod = defaults.find(_.name.toString.endsWith("$" + (i + 1))).flatMap { d =>
        val name = d.name.toString
        companion.getClass.getDeclaredMethods.find(_.getName == name).map { m =>
          () => m.invoke(companion)
        }
      }
      val pType = p.typeSignature
      val pName = if (p.name.toString.contains("$")) names.map(it => it(i)) else Some(p.name.toString)
      Try(TypeAnalysis.convertType(pType, genericsByName)).toOption.flatMap { rt =>
        val concreteType = Generics.makeConcrete(rt, genericMappings)
        if (json.context != null && ObjectAnalyzer.matchesContext(concreteType, json)) {
          Some(TypeInfo("", rt, false, concreteType, i, None))
        } else {
          val isUnknown = Generics.isUnknownType(rt)
          val matchingTypeAndName = {
            if (pName.isEmpty) None
            else if (sameTypes.get(pName.get).contains(pType)) pName
            else None
          }
          lazy val matchingTypeOnly = {
            if (sameTypes.size != params.size) None
            else sameTypes.find { case (_, ts) => ts == pType }.map(_._1)
          }
          val name = matchingTypeAndName.orElse(matchingTypeOnly).orElse(pName)
          if (name.isEmpty || name.get.contains("$")) None
          else Some(TypeInfo(name.get, rt, isUnknown, concreteType, i, defMethod))
        }
      }
    }
    if (arguments.size == params.size) {
      val tmp = new LazyImmutableDescription(json, manifest)
      val oldWriter = json.registerWriter(manifest, tmp)
      val oldReader = json.registerReader(manifest, tmp)
      val writeProps = arguments.filter(_.name.nonEmpty).flatMap { ti =>
        raw.getMethods.find(it => it.getName == ti.name && it.getParameterCount == 0).map { m =>
          Settings.createEncoder(
            new Reflection.ReadMethod(m),
            ti.name,
            json,
            if (ti.isUnknown) null else ti.concreteType)
        }.orElse(raw.getFields.find(it => it.getName == ti.name).map { f =>
          Settings.createEncoder(
            new Reflection.ReadField(f),
            ti.name,
            json,
            if (ti.isUnknown) null else ti.concreteType)
        })
      }.toArray
      val readProps: Array[DecodePropertyInfo[JsonReader.ReadObject[_]]] = arguments.filter(_.name.nonEmpty).flatMap { ti =>
        if (ti.isUnknown || json.tryFindWriter(ti.concreteType) != null && json.tryFindReader(ti.concreteType) != null) {
          val isNullable = ti.rawType.getTypeName.startsWith("scala.Option<")
          val writeProp = new WriteProperty(json, ti.concreteType, factoryMethod)
          Some(new DecodePropertyInfo[JsonReader.ReadObject[_]](ti.name, false, ti.getDefault.isEmpty, ti.index, !isNullable, writeProp))
        } else None
      }.toArray
      val emptyArgs = arguments.count(_.name.isEmpty)
      if (params.size == writeProps.length + emptyArgs && (!reading || params.size == readProps.length + emptyArgs)) {
        val defArgs = setupDefaults(params, arguments, json)
        val converter = new ImmutableDescription[AnyRef](
          manifest,
          defArgs,
          new Settings.Function[Array[AnyRef], AnyRef] {
            override def apply(args: Array[AnyRef]): AnyRef = factoryMethod.invoke(companion, args:_*)
          },
          writeProps,
          readProps,
          !json.omitDefaults,
          true)
        tmp.resolved = Some(converter)
        json.registerWriter(manifest, converter)
        //TODO: since nested case classes have their type signatures broken allow encoding,
        //TODO: but don't allow decoding if some types are erased
        if (params.size == readProps.length + emptyArgs) {
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

  private def analyzeEmptyCtor(
    manifest: JavaType,
    raw: Class[_],
    json: DslJson[_],
    methods: Map[universe.Symbol, universe.Symbol],
    reading: Boolean
  ) = {
    import scala.collection.JavaConverters._
    val tmp = new LazyObjectDescription(json, manifest)
    val oldWriter = json.registerWriter(manifest, tmp)
    val oldReader = json.registerReader(manifest, tmp)
    val foundWrite = new mutable.LinkedHashMap[String, JsonWriter.WriteObject[_]]
    val foundRead = new mutable.LinkedHashMap[String, DecodePropertyInfo[JsonReader.BindObject[_]]]
    val genericMappings = Generics.analyze(manifest, raw)
    val genericsByName = genericMappings.entrySet().asScala.map(kv => kv.getKey.getTypeName -> kv.getValue).toMap
    val rawAny = raw.asInstanceOf[Class[AnyRef]]
    val newInstance = new InstanceFactory[AnyRef] {
      override def create(): AnyRef = rawAny.newInstance()
    }
    var index = 0
    val rawMethods = raw.getMethods
    methods.foreach { case (g, _) =>
      val gName = g.name.toString
      rawMethods.find(m => m.getParameterCount == 0 && m.getName.equals(gName)).foreach { jm =>
        Try(TypeAnalysis.convertType(g.typeSignature.resultType, genericsByName)).foreach { t =>
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

  private final class WriteProperty(json: DslJson[_], manifest: JavaType, on: AnyRef) extends JsonReader.ReadObject[Any] {
    private var decoder: Option[JsonReader.ReadObject[Any]] = None

    override def read(reader: JsonReader[_]): Any = {
      if (decoder.isEmpty) {
        Option(json.tryFindReader(manifest)) match {
          case Some(f: JsonReader.ReadObject[Any @unchecked]) => decoder = Some(f)
          case _ => throw new ConfigurationException(s"Unable to find reader for $manifest on $on")
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
    genericMappings: java.util.HashMap[JavaType, JavaType]
  ): Boolean = {
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
