package com.dslplatform.json

import java.io.ByteArrayOutputStream

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class GenericsTest extends Specification with ScalaCheck {

  private lazy implicit val dslJson = new DslJson[Any]()

  "encoding" >> {
    "single with primitive" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(Single(Some(5), 2), os)
      "{\"a\":5,\"b\":2}" === os.toString("UTF-8")
    }
    "single with object" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(Single(Some("x"), "2"), os)
      "{\"a\":\"x\",\"b\":\"2\"}" === os.toString("UTF-8")
    }
    "double" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(DoubleGeneric(Some(5), "abc"), os)
      "{\"a\":5,\"b\":\"abc\"}" === os.toString("UTF-8")
    }
    "nested single with primitive" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode[GenericExamples.NestedSingle[Int]](GenericExamples.NestedSingle(Some(5), 2), os)
      "{\"a\":5,\"b\":2}" === os.toString("UTF-8")
    }
    "nested single with object" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode[GenericExamples.NestedSingle[String]](GenericExamples.NestedSingle(Some("x"), "2"), os)
      "{\"a\":\"x\",\"b\":\"2\"}" === os.toString("UTF-8")
    }
    "nested double" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode[GenericExamples.NestedDouble[Int, String]](GenericExamples.NestedDouble(Some(5), "abc"), os)
      "{\"a\":5,\"b\":\"abc\"}" === os.toString("UTF-8")
    }
  }
  "decoding" >> {
    "single with primitive" >> {
      val input = "{\"a\":5,\"b\":2}".getBytes("UTF-8")
      val res = dslJson.decode[Single[Int]](input)
      Single(Some(5), 2) === res
    }
    "single with object" >> {
      val input = "{\"a\":\"x\",\"b\":\"2\"}".getBytes("UTF-8")
      val res = dslJson.decode[Single[String]](input)
      Single(Some("x"), "2") === res
    }
    "double" >> {
      val input = "{\"a\":5,\"b\":\"abc\"}".getBytes("UTF-8")
      val res = dslJson.decode[DoubleGeneric[Int, String]](input)
      DoubleGeneric(Some(5), "abc") === res
    }
    "nested single with primitive" >> {
      val input = "{\"a\":5,\"b\":2}".getBytes("UTF-8")
      val res = dslJson.decode[GenericExamples.NestedSingle[Int]](input)
      GenericExamples.NestedSingle(Some(5), 2) === res
    }
    "nested single with object" >> {
      val input = "{\"a\":\"x\",\"b\":\"2\"}".getBytes("UTF-8")
      val res = dslJson.decode[GenericExamples.NestedSingle[String]](input)
      GenericExamples.NestedSingle(Some("x"), "2") === res
    }
    "nested double" >> {
      val input = "{\"a\":5,\"b\":\"abc\"}".getBytes("UTF-8")
      val res = dslJson.decode[GenericExamples.NestedDouble[Int, String]](input)
      GenericExamples.NestedDouble(Some(5), "abc") === res
    }
  }
}

case class Single[T](a: Option[T] = None, b: T)
case class DoubleGeneric[T1, T2](a: Option[T1] = None, b: T2)

object GenericExamples {
  case class NestedSingle[T](a: Option[T] = None, b: T)
  case class NestedDouble[T1, T2](a: Option[T1] = None, b: T2)
}
