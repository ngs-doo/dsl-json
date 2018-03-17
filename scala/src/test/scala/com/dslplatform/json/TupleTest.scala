package com.dslplatform.json

import java.io.ByteArrayOutputStream

import com.dslplatform.json.runtime.Settings
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

import scala.util.Try

class TupleTest extends Specification with ScalaCheck {

  private lazy val dslJson = new DslJson[Any]()
  private lazy val dslJsonUnknown = new DslJson[Any](Settings.withRuntime().includeServiceLoader())

  "encoding" >> {
    "simple tuple" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(("a", 1), os)
      "[\"a\",1]" === os.toString("UTF-8")
    }
    "with option nothing tuple" >> {
      val os = new ByteArrayOutputStream()
      dslJsonUnknown.encode(("a", 1, Option.empty, 0.1), os)
      "[\"a\",1,null,0.1]" === os.toString("UTF-8")
    }
    "with none tuple" >> {
      val os = new ByteArrayOutputStream()
      dslJsonUnknown.encode(("a", 1, None, 0.1), os)
      "[\"a\",1,null,0.1]" === os.toString("UTF-8")
    }
  }
  "decoding" >> {
    "simple tuple" >> {
      val input = "[\"a\",1]".getBytes("UTF-8")
      val tuple = dslJson.decode[(String, Int)](input)
      ("a", 1) === tuple
    }
    "with option object tuple" >> {
      val input = "[\"a\",1,null,0.1]".getBytes("UTF-8")
      val tuple = dslJson.decode[(String, Int, Option[String], Double)](input)
      ("a", 1, None, 0.1) === tuple
    }
    "with option object tuple" >> {
      val input = "[\"a\",1,null,0.1]".getBytes("UTF-8")
      val tuple = dslJson.decode[(String, Int, Option[String], Double)](input)
      ("a", 1, None, 0.1) === tuple
    }
    "null check tuple" >> {
      val input = "[\"a\",null,0.1]".getBytes("UTF-8")
      val tryDecode = Try{dslJson.decode[(String, Int, Double)](input)}
      tryDecode.isFailure === true
      tryDecode.failed.get.getMessage === "Tuple property 2 of scala.Tuple3<java.lang.String, int, double> is not allowed to be null."
    }
  }
}
