package com.dslplatform.json

import java.io.ByteArrayOutputStream

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class ExplicitTupleTest extends Specification with ScalaCheck {

  private lazy val dslJson = new DslJson[Any]()

  "encoding" >> {
    "simple tuple" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(("a", 1), os)(dslJson.encoder)
      "[\"a\",1]" === os.toString("UTF-8")
    }
  }
  "decoding" >> {
    "simple tuple" >> {
      val input = "[\"a\",1]".getBytes("UTF-8")
      val tuple = dslJson.decode[(String, Int)](input)(dslJson.decoder)
      ("a", 1) === tuple
    }
  }
}
