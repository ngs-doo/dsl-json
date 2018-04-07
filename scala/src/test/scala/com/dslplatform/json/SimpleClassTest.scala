package com.dslplatform.json

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class SimpleClassTest extends Specification with ScalaCheck {

  private lazy implicit val dslJson = new DslJson[Any]()

  "simple" >> {
    "example 1" >> {
      val os = new ByteArrayOutputStream()
      val m1 = new Mutable1
      m1.str = Some("x")
      m1.i = Some(2)
      m1.l = Some(-4)
      dslJson.encode(m1, os)
      val m2 = dslJson.decode[Mutable1](new ByteArrayInputStream(os.toByteArray))
      m1.str === m2.str
      m1.i === m2.i
      m1.l === m2.l
    }
    "example 2" >> {
      val os = new ByteArrayOutputStream()
      val m1 = new Mutable2
      m1.str = Some("x")
      m1.i = Some(2)
      m1.l = Some(-4)
      dslJson.encode(m1, os)
      val m2 = dslJson.decode[Mutable2](new ByteArrayInputStream(os.toByteArray))
      m1.str === m2.str
      m1.i === m2.i
      m1.l === m2.l
    }
    "example 3 encoding" >> {
      val os = new ByteArrayOutputStream()
      val m1 = new MutableExamples.Mutable3
      m1.str = Some("x")
      m1.i = Some(2)
      m1.l = Some(-4)
      dslJson.encode(m1, os)
      val output = os.toString("UTF-8")
      true === output.contains("\"str\":\"x\"")
      true === output.contains("\"i\":2")
      true === output.contains("\"l\":-4")
    }
    "example 3 decoding" >> {
      val os = new ByteArrayOutputStream()
      val m1 = new MutableExamples.Mutable3
      m1.str = Some("x")
      m1.i = Some(2)
      m1.l = Some(-4)
      dslJson.encode(m1, os)
      val m2 = dslJson.decode[MutableExamples.Mutable3](new ByteArrayInputStream(os.toByteArray))
      m1.str === m2.str
      m1.i === m2.i
      m1.l === m2.l
    }
  }
}

class Mutable1 {
  var str: Option[String] = None
  var i: Option[Int] = None
  var l: Option[Long] = None
}

class Mutable2 {
  private var _str: Option[String] = None
  def str = _str
  def str_=(v: Option[String]): Unit = { _str = v }
  private var _i: Option[Int] = None
  def i = _i
  def i_=(v: Option[Int]): Unit = { _i = v }
  private var _l: Option[Long] = None
  def l = _l
  def l_=(v: Option[Long]): Unit = { _l = v }
}

object MutableExamples {
  //TODO: does not work currently due to primitives being erased
  class Mutable3 {
    var str: Option[String] = None
    var i: Option[Int] = None
    var l: Option[Long] = None
  }
}