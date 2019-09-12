package com.dslplatform.json

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.nio.charset.StandardCharsets

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class ClassWithObjectTest extends Specification with ScalaCheck {

  private lazy implicit val dslJson = new DslJson[Any]()

  "simple" >> {
    "example 1" >> {
      val os = new ByteArrayOutputStream()
      val m1 = PrivateCtor(1, "abc")
      dslJson.encode(m1, os)
      val m2 = dslJson.decode[PrivateCtor](new ByteArrayInputStream(os.toByteArray))
      m1.s === m2.s
      m1.i === m2.i
    }
    "example 1 decode with default" >> {
      val m = dslJson.decode[PrivateCtor]("""{"i":5}""".getBytes(StandardCharsets.UTF_8))
      m.s === "x"
      m.i === 5
    }
  }
}

class PrivateCtor private(var _i: Int, var _s: String) {
  def i: Int = _i
  def i_=(v: Int): Unit = { _i = v }
  def s: String = _s
  def s_=(v: String): Unit = { _s = v }
}
object PrivateCtor {
  def apply(i: Int, s: String = "x"): PrivateCtor = {
    new PrivateCtor(i, s)
  }
}
