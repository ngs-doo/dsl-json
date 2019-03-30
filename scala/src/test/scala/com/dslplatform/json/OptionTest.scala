package com.dslplatform.json

import java.io.ByteArrayOutputStream

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class OptionTest extends Specification with ScalaCheck {

  private lazy implicit val dslJson = new DslJson[Any]()

  "direct" >> {
    "string serialize" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(Option("test"), os)
      "\"test\"" === os.toString("UTF-8")
    }
    "int serialize" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(Some(1), os)
      "1" === os.toString("UTF-8")
    }
    "long serialize" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(Option(1L), os)
      "1" === os.toString("UTF-8")
    }
    "string deserialize" >> {
      val input = "\"test\"".getBytes("UTF-8")
      val value = dslJson.decode[String](input)
      "test" === value
    }
    "int deserialize" >> {
      val input = "1".getBytes("UTF-8")
      val value = dslJson.decode[Int](input)
      1 === value
      "java.lang.Integer" === value.asInstanceOf[Any].getClass.getTypeName
    }
    "long deserialize" >> {
      val input = "1".getBytes("UTF-8")
      val value = dslJson.decode[Long](input)
      1L === value
      "java.lang.Long" === value.asInstanceOf[Any].getClass.getTypeName
    }
  }

  "case class" >> {
    "serialize nones" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(WithOption(None, None, None), os)
      "{\"str\":null,\"i\":null,\"l\":null}" === os.toString("UTF-8")
    }
    "serialize somes" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(WithOption(Some("test"), Some(Int.MinValue), Some(2L)), os)
      "{\"str\":\"test\",\"i\":-2147483648,\"l\":2}" === os.toString("UTF-8")
    }
    "deserialize nulls" >> {
      val wo = dslJson.decode[WithOption]("{\"str\":null,\"i\":null,\"l\":null}".getBytes("UTF-8"))
      wo === WithOption(i = None, l = None)
    }
    "can omit default" >> {
      val wo = dslJson.decode[WithOption]("{\"i\":null,\"l\":null}".getBytes("UTF-8"))
      wo === WithOption(i = None, l = None)
    }
    "missing property will throw an error" >> {
      dslJson.decode[WithOption]("{\"l\":null}".getBytes("UTF-8")) must throwA {
        ParsingException.create("Mandatory property (i) not found at position: 9, following: `{\"l\":null`, before: `}`", true)
      }
    }
  }
}

case class WithOption(str: Option[String] = None, i: Option[Int], l: Option[Long])