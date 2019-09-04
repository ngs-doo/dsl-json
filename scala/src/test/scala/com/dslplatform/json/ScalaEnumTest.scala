package com.dslplatform.json

import java.io.ByteArrayOutputStream

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class ScalaEnumTest extends Specification with ScalaCheck {

  private lazy implicit val dslJson = new DslJson[Any]()

  "encoding" >> {
    "example 1" >> {
      val os = new ByteArrayOutputStream()
      //TODO: without type extra information is encoded and converter fails
      dslJson.encode(Seq[Status](Status.ACTIVE, Status.INACTIVE), os)
      "[\"ACTIVE\",\"INACTIVE\"]" === os.toString("UTF-8")
    }
  }
  "decoding" >> {
    "example 1" >> {
      val input = "[\"ACTIVE\",\"INACTIVE\"]".getBytes("UTF-8")
      val res = dslJson.decode[Seq[Status]](input)
      Seq(Status.ACTIVE, Status.INACTIVE) === res
    }
  }
}

sealed trait Status

object Status {

  case object ACTIVE extends Status

  case object INACTIVE extends Status
}