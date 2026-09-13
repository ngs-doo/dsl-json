package com.dslplatform.json

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class MandatoryPropertiesTest extends Specification with ScalaCheck {

  private lazy implicit val dslJson = new DslJson[Any]()

  "case class with more than 64 mandatory properties" >> {
    "deserialize when all properties are present" >> {
      val input = buildJson(70, Nil).getBytes("UTF-8")
      dslJson.decode[Mandatory70](input) === Mandatory70(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69)
    }
    "report missing property" >> {
      val input = buildJson(70, List(35)).getBytes("UTF-8")
      dslJson.decode[Mandatory70](input) must throwA {
        ParsingException.create("Mandatory property (f35) not found at position: 601, following: `67,\"f68\":68,\"f69\":69`, before: `}`", true)
      }
    }
    "report all missing properties on empty object" >> {
      dslJson.decode[Mandatory70]("{}".getBytes("UTF-8")) must throwA {
        ParsingException.create("Mandatory properties (f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18, f19, f20, f21, f22, f23, f24, f25, f26, f27, f28, f29, f30, f31, f32, f33, f34, f35, f36, f37, f38, f39, f40, f41, f42, f43, f44, f45, f46, f47, f48, f49, f50, f51, f52, f53, f54, f55, f56, f57, f58, f59, f60, f61, f62, f63, f64, f65, f66, f67, f68, f69) not found at position: 1, following: `{`, before: `}`", true)
      }
    }
  }

  "case class with exactly 64 mandatory properties" >> {
    "deserialize when all properties are present" >> {
      val input = buildJson(64, Nil).getBytes("UTF-8")
      dslJson.decode[Mandatory64](input) === Mandatory64(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63)
    }
    "report missing property" >> {
      val input = buildJson(64, List(10)).getBytes("UTF-8")
      dslJson.decode[Mandatory70](input) must throwA {
        ParsingException.create("Mandatory properties (f10, f64, f65, f66, f67, f68, f69) not found at position: 547, following: `61,\"f62\":62,\"f63\":63`, before: `}`", true)
      }
    }
  }

  "case class with mandatory and default properties" >> {
    "optional property does not reset mandatory flag" >> {
      dslJson.decode[Mandatory70]("{\"b\":5}".getBytes("UTF-8")) must throwA {
        ParsingException.create("Mandatory properties (f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18, f19, f20, f21, f22, f23, f24, f25, f26, f27, f28, f29, f30, f31, f32, f33, f34, f35, f36, f37, f38, f39, f40, f41, f42, f43, f44, f45, f46, f47, f48, f49, f50, f51, f52, f53, f54, f55, f56, f57, f58, f59, f60, f61, f62, f63, f64, f65, f66, f67, f68, f69) not found at position: 6, following: `{\"b\":5`, before: `}`", true)
      }
    }
    "deserialize with all properties" >> {
      val input = "{\"a\":1,\"b\":5}".getBytes("UTF-8")
      dslJson.decode[WithDefaults](input) === WithDefaults(1, 5)
    }
    "use default for omitted optional property" >> {
      val input = "{\"a\":1}".getBytes("UTF-8")
      dslJson.decode[WithDefaults](input) === WithDefaults(1, 2)
    }
  }

  private def buildJson(count: Int, missing: List[Int]): String = {
    val sb = new StringBuilder("{")
    for (i <- 0 until count if !missing.contains(i)) {
      if (sb.length() > 1) sb.append(',')
      sb.append("\"f").append(i).append("\":").append(i)
    }
    sb.append('}').toString
  }
}

case class Mandatory64(f0: Int, f1: Int, f2: Int, f3: Int, f4: Int, f5: Int, f6: Int, f7: Int, f8: Int, f9: Int, f10: Int, f11: Int, f12: Int, f13: Int, f14: Int, f15: Int, f16: Int, f17: Int, f18: Int, f19: Int, f20: Int, f21: Int, f22: Int, f23: Int, f24: Int, f25: Int, f26: Int, f27: Int, f28: Int, f29: Int, f30: Int, f31: Int, f32: Int, f33: Int, f34: Int, f35: Int, f36: Int, f37: Int, f38: Int, f39: Int, f40: Int, f41: Int, f42: Int, f43: Int, f44: Int, f45: Int, f46: Int, f47: Int, f48: Int, f49: Int, f50: Int, f51: Int, f52: Int, f53: Int, f54: Int, f55: Int, f56: Int, f57: Int, f58: Int, f59: Int, f60: Int, f61: Int, f62: Int, f63: Int)

case class WithDefaults(a: Int, b: Int = 2)

case class Mandatory70(f0: Int, f1: Int, f2: Int, f3: Int, f4: Int, f5: Int, f6: Int, f7: Int, f8: Int, f9: Int, f10: Int, f11: Int, f12: Int, f13: Int, f14: Int, f15: Int, f16: Int, f17: Int, f18: Int, f19: Int, f20: Int, f21: Int, f22: Int, f23: Int, f24: Int, f25: Int, f26: Int, f27: Int, f28: Int, f29: Int, f30: Int, f31: Int, f32: Int, f33: Int, f34: Int, f35: Int, f36: Int, f37: Int, f38: Int, f39: Int, f40: Int, f41: Int, f42: Int, f43: Int, f44: Int, f45: Int, f46: Int, f47: Int, f48: Int, f49: Int, f50: Int, f51: Int, f52: Int, f53: Int, f54: Int, f55: Int, f56: Int, f57: Int, f58: Int, f59: Int, f60: Int, f61: Int, f62: Int, f63: Int, f64: Int, f65: Int, f66: Int, f67: Int, f68: Int, f69: Int)
