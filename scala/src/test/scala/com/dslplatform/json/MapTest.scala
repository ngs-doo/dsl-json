package com.dslplatform.json

import java.io.ByteArrayOutputStream

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

import scala.collection.mutable

class MapTest extends Specification with ScalaCheck {

  private lazy val dslJson = new DslJson[Any]()

  "encoding" >> {
    "string value serialize" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(Map("a" -> "test"), os)
      "{\"a\":\"test\"}" === os.toString("UTF-8")
    }
    "int value serialize" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(Map("key" -> 1), os)
      "{\"key\":1}" === os.toString("UTF-8")
    }
    "option value opt long serialize" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(Some(Map("aa" -> Option(1L))), os)
      "{\"aa\":1}" === os.toString("UTF-8")
    }
    "int key serialize" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(Map(1 -> "test"), os)
      "{\"1\":\"test\"}" === os.toString("UTF-8")
    }
    "mutable map serialize" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(mutable.HashMap() ++ Map("key" -> 1), os)
      "{\"key\":1}" === os.toString("UTF-8")
    }
  }
  "decoding" >> {
    "double key deserialize" >> {
      val input = "{\"2\":\"test\"}".getBytes("UTF-8")
      val value = dslJson.decode[Map[Double, String]](input)
      Map(2.asInstanceOf[Double] -> "test") === value
      "java.lang.Double" === value.keys.head.asInstanceOf[Any].getClass.getTypeName
    }
    "string value deserialize" >> {
      val input = "{\"aa\":\"test\"}".getBytes("UTF-8")
      val value = dslJson.decode[Map[String, String]](input)
      Map("aa" -> "test") === value
    }
    "int value deserialize" >> {
      val input = "{\"a\":1}".getBytes("UTF-8")
      val value = dslJson.decode[Map[String, Int]](input)
      Map("a" -> 1) === value
      "java.lang.Integer" === value.values.head.asInstanceOf[Any].getClass.getTypeName
    }
    "opt value opt long deserialize" >> {
      val input = "{\"key\":1}".getBytes("UTF-8")
      val value = dslJson.decode[Option[Map[String, Option[Long]]]](input)
      Some(Map("key" -> Some(1L))) === value
      "java.lang.Long" === value.get.values.head.get.asInstanceOf[Any].getClass.getTypeName
    }
    "mutable map deserialize" >> {
      val input = "{\"a\":1}".getBytes("UTF-8")
      val value = dslJson.decode[mutable.Map[String, Int]](input)
      mutable.Map("a" -> 1) === value
      "java.lang.Integer" === value.values.head.asInstanceOf[Any].getClass.getTypeName
    }
  }
}
