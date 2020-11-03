package com.dslplatform.json

import java.io.ByteArrayOutputStream

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class CollectionTest extends Specification with ScalaCheck {

  private lazy implicit val dslJson = new DslJson[Any]()

  "encoding" >> {
    "string seq serialize" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(Seq("test"), os)
      "[\"test\"]" === os.toString("UTF-8")
    }
    "int seq serialize" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(Seq(1), os)
      "[1]" === os.toString("UTF-8")
    }
    "option seq opt long serialize" >> {
      val os = new ByteArrayOutputStream()
      dslJson.encode(Some(Seq(Option(1L))), os)
      "[1]" === os.toString("UTF-8")
    }
  }
  "decoding" >> {
    "string seq deserialize" >> {
      val input = "[\"test\"]".getBytes("UTF-8")
      val value = dslJson.decode[Seq[String]](input)
      Seq("test") === value
    }
    "int seq deserialize" >> {
      val input = "[1]".getBytes("UTF-8")
      val value = dslJson.decode[Seq[Int]](input)
      Seq(1) === value
      "java.lang.Integer" === value.head.asInstanceOf[Any].getClass.getTypeName
    }
    "opt seq opt long deserialize" >> {
      val input = "[1]".getBytes("UTF-8")
      val value = dslJson.decode[Option[Seq[Option[Long]]]](input)
      Some(Seq(Some(1L))) === value
      "java.lang.Long" === value.get.head.get.asInstanceOf[Any].getClass.getTypeName
    }
    "mutable array buffer deserialize" >> {
      val input = "[\"test\"]".getBytes("UTF-8")
      val value = dslJson.decode[ArrayBuffer[String]](input)
      ArrayBuffer("test") === value
    }
    "indexed seq deserialize" >> {
      val input = "[2,3]".getBytes("UTF-8")
      val value = dslJson.decode[IndexedSeq[Long]](input)
      IndexedSeq(2L, 3L) === value
    }
    "list deserialize" >> {
      val input = "[1,2,3]".getBytes("UTF-8")
      val value = dslJson.decode[List[Int]](input)
      List(1, 2, 3) === value
    }
    "vector deserialize" >> {
      val input = "[1,2,3]".getBytes("UTF-8")
      val value = dslJson.decode[Vector[Int]](input)
      Vector(1, 2, 3) === value
    }
    "set primitive deserialize" >> {
      val input = "[1,2,3]".getBytes("UTF-8")
      val value = dslJson.decode[Set[Long]](input)
      Set(1L, 2L, 3L) === value
    }
    "set object deserialize" >> {
      val input = "[1,2,3]".getBytes("UTF-8")
      val value = dslJson.decode[Set[BigDecimal]](input)
      Set(BigDecimal(1), BigDecimal(2), BigDecimal(3)) === value
    }
    "mutable set deserialize" >> {
      val input = "[1,2,3]".getBytes("UTF-8")
      val value = dslJson.decode[mutable.Set[Long]](input)
      mutable.Set(1L, 2L, 3L) === value
    }
    "mutable stack deserialize" >> {
      val input = "[1,2,3]".getBytes("UTF-8")
      val value = dslJson.decode[mutable.Stack[Long]](input)
      mutable.Stack(1L, 2L, 3L) === value
    }
    "mutable queue deserialize" >> {
      val input = "[1,2,3]".getBytes("UTF-8")
      val value = dslJson.decode[mutable.Queue[Long]](input)
      mutable.Queue(1L, 2L, 3L) === value
    }
    "array boolean deserialize" >> {
      val input = "[true,false,true]".getBytes("UTF-8")
      val value = dslJson.decode[Array[Boolean]](input)
      Array(true, false, true) === value
    }
    "base indexed seq deserialize" >> {
      val input = "[2,3]".getBytes("UTF-8")
      val value = dslJson.decode[scala.collection.IndexedSeq[Long]](input)
      IndexedSeq(2L, 3L) === value
    }
  }
}
