package com.dslplatform.json

import java.io.ByteArrayOutputStream

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class CaseClassTest extends Specification with ScalaCheck {

  private lazy val dslJson = new DslJson[Any]()

  "encoding" >> {
    "example 1" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(Example1(Some("x"), Some(2), Some(-4)), os)
      "{\"str\":\"x\",\"i\":2,\"l\":-4}" === os.toString("UTF-8")
    }
    "example 2" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(Example2(Some("x"), Some("2"), Some("-4")), os)
      "{\"str1\":\"x\",\"str2\":\"2\",\"str3\":\"-4\"}" === os.toString("UTF-8")
    }
    "example 3" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(Example3(Some("x"), None), os)
      "{\"str\":\"x\",\"self\":null}" === os.toString("UTF-8")
    }
    "example 4" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(Examples.Example4("x", 2, Some(4L)), os)
      "{\"str\":\"x\",\"i\":2,\"l\":4}" === os.toString("UTF-8")
    }
    "example 5" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(Examples.Example5(Some("x"), None, None), os)
      "{\"str1\":\"x\",\"str2\":null,\"str3\":null}" === os.toString("UTF-8")
    }
    "example 6" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(Examples.Example6("x", Some(Examples.Example6("", None))), os)
      "{\"str\":\"x\",\"self\":{\"str\":\"\",\"self\":null}}" === os.toString("UTF-8")
    }
  }
  "decoding" >> {
    "example 1" >> {
      val input = "{\"str\":\"x\",\"i\":2,\"l\":-4}".getBytes("UTF-8")
      val res = dslJson.decode[Example1](input)
      Example1(Some("x"), Some(2), Some(-4)) === res
    }
    "example 2" >> {
      val input = "{\"str1\":\"x\",\"str2\":\"2\",\"str3\":\"-4\"}".getBytes("UTF-8")
      val res = dslJson.decode[Example2](input)
      Example2(Some("x"), Some("2"), Some("-4")) === res
    }
    "example 3" >> {
      val input = "{\"str\":\"x\",\"self\":null}".getBytes("UTF-8")
      val res = dslJson.decode[Example3](input)
      Example3(Some("x"), None) === res
    }
    "example 4" >> {
      val input = "{\"str\":\"x\",\"i\":2,\"l\":4}".getBytes("UTF-8")
      val res = dslJson.decode[Examples.Example4](input)
      Examples.Example4("x", 2, Some(4L)) === res
    }
    "example 5" >> {
      val input = "{\"str1\":\"x\",\"str2\":null,\"str3\":null}".getBytes("UTF-8")
      val res = dslJson.decode[Examples.Example5](input)
      Examples.Example5(Some("x"), None, None) === res
    }
    "example 6" >> {
      val input = "{\"str\":\"x\",\"self\":{\"str\":\"\",\"self\":null}}".getBytes("UTF-8")
      val res = dslJson.decode[Examples.Example6](input)
      Examples.Example6("x", Some(Examples.Example6("", None))) === res
    }
  }
}

case class Example1(str: Option[String] = None, i: Option[Int], var l: Option[Long])
case class Example2(str1: Option[String], str2: Option[String], str3: Option[String])
case class Example3(str: Option[String], self: Option[Example3])

object Examples {
  //TODO: nested classes have their primitives in containers erased ;(
  case class Example4(str: String = "", i: Int = 0, l: Option[Long])
  case class Example5(str1: Option[String] = None, str2: Option[String] = Some(""), str3: Option[String] = Some("x"))
  case class Example6(str: String = "", self: Option[Example6])

}
