package com.dslplatform.json

import java.lang.reflect.{GenericArrayType, ParameterizedType, Type => JavaType}

import scala.collection.concurrent.TrieMap
import scala.reflect.runtime.universe._
import scala.runtime.Nothing$
import scala.util.Success

private[json] object TypeAnalysis {

  private val genericsCache = new TrieMap[String, GenericType]
  private val typeTagCache = new TrieMap[Type, JavaType]
  typeTagCache.put(typeOf[Nothing], classOf[Nothing$])
  typeTagCache.put(typeOf[Any], classOf[AnyRef])
  typeTagCache.put(typeOf[Option[Nothing]], classOf[Option[AnyRef]])
  typeTagCache.put(typeOf[None.type], classOf[Option[AnyRef]])

  private class GenericType(
    val name: String,
    val raw: JavaType,
    val arguments: Array[JavaType]) extends ParameterizedType {
    private val argObjects = arguments.map(_.asInstanceOf[AnyRef])

    override def hashCode: Int = {
      java.util.Arrays.hashCode(argObjects) ^ raw.hashCode
    }

    override def equals(other: Any): Boolean = {
      other match {
        case pt: ParameterizedType =>
          raw == pt.getRawType && java.util.Arrays.equals(argObjects, pt.getActualTypeArguments.map(_.asInstanceOf[AnyRef]))
        case _ =>
          false
      }
    }

    def getActualTypeArguments: Array[JavaType] = arguments

    def getRawType: JavaType = raw

    def getOwnerType: JavaType = null

    override def toString: String = name
  }

  def makeGenericType(
    container: Class[_],
    arguments: List[JavaType]): ParameterizedType = {
    val sb = new StringBuilder
    sb.append(container.getTypeName)
    sb.append("<")
    sb.append(arguments.head.getTypeName)
    for (arg <- arguments.tail) {
      sb.append(", ")
      sb.append(arg.getTypeName)
    }
    sb.append(">")
    val name = sb.toString
    genericsCache.getOrElseUpdate(
      name, {
        new GenericType(name, container, arguments.toArray)
      })
  }

  private class GenArrType(genType: JavaType) extends GenericArrayType {
    lazy private val typeName = genType.getTypeName + "[]"

    override def getGenericComponentType = genType

    override def getTypeName: String = typeName

    override def toString: String = typeName
  }

  def findType(tpe: Type): Option[JavaType] = {
    typeTagCache.get(tpe) match {
      case found@Some(_) => found
      case _ =>
        findUnknownType(tpe, scala.reflect.runtime.currentMirror) match {
          case found@Some(jt) =>
            typeTagCache.put(tpe, jt)
            found
          case _ =>
            None
        }
    }
  }

  private def findUnknownType(tpe: Type, mirror: Mirror): Option[JavaType] = {
    tpe.dealias match {
      case TypeRef(_, sym, args) if args.isEmpty =>
        util.Try(mirror.runtimeClass(sym.asClass)).toOption
      case TypeRef(_, sym, args) if sym.fullName == "scala.Array" && args.lengthCompare(1) == 0 =>
        findType(args.head) match {
          case Some(typeArg) => Some(new GenArrType(typeArg))
          case _ => None
        }
      case TypeRef(_, sym, args) =>
        util.Try(mirror.runtimeClass(sym.asClass)) match {
          case Success(symClass) =>
            val typeArgs = args.flatMap(findType)
            if (typeArgs.lengthCompare(args.size) == 0) {
              Some(makeGenericType(symClass, typeArgs))
            } else None
          case _ => None
        }
      case _ =>
        None
    }
  }
}