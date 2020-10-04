package com.dslplatform.json

import java.io.ByteArrayOutputStream

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class ScalaEnumTest extends Specification with ScalaCheck {

  private lazy val settings = runtime.Settings.basicSetup[Any]().`with`(new ConfigureScala)
  private lazy implicit val dslJson = new DslJson[Any](settings)

  "encoding" >> {
    "example e1" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(Seq[Status](Status.ACTIVE, Status.INACTIVE), os)
      os.toString("UTF-8") === "[\"ACTIVE\",\"INACTIVE\"]"
    }
    "example e2" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(Seq(Status.ACTIVE, Status.INACTIVE), os)
      os.toString("UTF-8") === "[\"ACTIVE\",\"INACTIVE\"]"
    }
    "example e3" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(Array(Status.ACTIVE, Status.INACTIVE), os)
      os.toString("UTF-8") === "[\"ACTIVE\",\"INACTIVE\"]"
    }
  }
  "decoding" >> {
    "example d1" >> {
      val input = "[\"ACTIVE\",\"INACTIVE\"]".getBytes("UTF-8")
      val res = dslJson.decode[Seq[Status]](input)
      res.toIndexedSeq === IndexedSeq(Status.ACTIVE, Status.INACTIVE)
    }
    "example d2" >> {
      val input = "[\"ACTIVE\",\"INACTIVE\"]".getBytes("UTF-8")
      val res = dslJson.decode[Array[Status]](input)
      res.toIndexedSeq === IndexedSeq(Status.ACTIVE, Status.INACTIVE)
    }
  }
}

sealed trait Status

object Status {

  case object ACTIVE extends Status

  case object INACTIVE extends Status
}