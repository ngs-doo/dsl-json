package com.dslplatform.json

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.nio.charset.StandardCharsets

import javax.json.bind.annotation.JsonbCreator
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class ClassWithObjectTest extends Specification with ScalaCheck {

  private val serv = new ServiceImpl

  "simple" >> {
    val settings = new DslJson.Settings[Service]().withContext(serv).creatorMarker(classOf[JsonbCreator], false).includeServiceLoader()
    implicit val dslJson = new DslJson[Service](settings)
    "example 1" >> {
      val os = new ByteArrayOutputStream()
      val m1 = PrivateCtor(1, "abc")
      dslJson.encode(m1, os)
      val m2 = dslJson.decode[PrivateCtor](new ByteArrayInputStream(os.toByteArray))
      m1.s === m2.s
      m1.i === m2.i
    }
    "example 1 decode with default" >> {
      val m = dslJson.decode[PrivateCtor]("""{"i":5}""".getBytes(StandardCharsets.UTF_8))
      m.s === "x"
      m.i === 5
    }
    "will respect different via annotation factory" >> {
      val os = new ByteArrayOutputStream()
      val pmf = CreatorInstance(505, serv, "abc")
      dslJson.encode(pmf, os)
      os.toString("UTF-8") === """{"s":"abc"}"""
    }
    "factories will be propagated around" >> {
      val os = new ByteArrayOutputStream()
      val pmf = CreatorInstance(505, serv, "abc")
      val report = Report("test", Nil, Some(pmf))
      dslJson.encode(report, os)
      os.toString("UTF-8") === """{"title":"test","users":[],"creator":{"s":"abc"}}"""
    }
  }
  "annotations" >> {
    val settings = new DslJson.Settings[Service]().withContext(serv).creatorMarker(classOf[JsonbCreator], true).includeServiceLoader()
    implicit val dslJson = new DslJson[Service](settings)
    "will respect same via annotation factory" >> {
      val os = new ByteArrayOutputStream()
      val pmf = PrivateMarkedFactoryWithDeps(505)
      dslJson.encode(pmf, os)
      os.toString("UTF-8") === """{"i":505,"s":"a"}"""
    }
  }
  "dependency" >> {
    val settings = new DslJson.Settings[Service]().withContext(serv).creatorMarker(classOf[JsonbCreator], true).includeServiceLoader()
    implicit val dslJson = new DslJson[Service](settings)
    "in public ctor" >> {
      val os = new ByteArrayOutputStream()
      val m1 = new CtorWithDeps(1, "abc", serv)
      dslJson.encode(m1, os)
      val m2 = dslJson.decode[CtorWithDeps](new ByteArrayInputStream(os.toByteArray))
      m1.s === m2.s
      m1.i === m2.i
      m1.service === serv
    }
    "via annotation ctor marking" >> {
      val m = dslJson.decode[MarkedCtorWithDeps]("""{"i":5,"s":"x"}""".getBytes(StandardCharsets.UTF_8))
      m.s === "x"
      m.i === 5
      m.service === serv
    }
    "via annotation public factory marking" >> {
      val m = dslJson.decode[MarkedFactoryWithDeps]("""{"i":5,"s":"x"}""".getBytes(StandardCharsets.UTF_8))
      m.s === "x"
      m.i === 5
      m.service === serv
    }
    "via annotation private factory marking" >> {
      val m = dslJson.decode[PrivateMarkedFactoryWithDeps]("""{"i":5,"s":"x"}""".getBytes(StandardCharsets.UTF_8))
      m.s === "x"
      m.i === 5
      m.service === serv
    }
  }
}

class ServiceImpl extends Service {}
trait Service {}

class PrivateCtor private(var _i: Int, var _s: String) {
  def i: Int = _i
  def i_=(v: Int): Unit = { _i = v }
  def s: String = _s
  def s_=(v: String): Unit = { _s = v }
}
object PrivateCtor {
  def apply(i: Int, s: String = "x"): PrivateCtor = {
    new PrivateCtor(i, s)
  }
}
class CtorWithDeps (var _i: Int, var _s: String, val service: Service) {
  def i: Int = _i
  def i_=(v: Int): Unit = { _i = v }
  def s: String = _s
  def s_=(v: String): Unit = { _s = v }
}
class MarkedCtorWithDeps private(private var _i: Int, private var _s: String, val service: Service) {
  def i: Int = _i
  def i_=(v: Int): Unit = { _i = v }
  def s: String = _s
  def s_=(v: String): Unit = { _s = v }
  @JsonbCreator
  private def this(service: Service, i: Int, s: String) = {
    this(i, s, service)
  }
}
class MarkedFactoryWithDeps private(var _i: Int, var _s: String, val service: Service) {
  def i: Int = _i
  def i_=(v: Int): Unit = { _i = v }
  def s: String = _s
  def s_=(v: String): Unit = { _s = v }
}
object MarkedFactoryWithDeps {
  def apply(i: Int): MarkedFactoryWithDeps = {
    new MarkedFactoryWithDeps(i, "a", new ServiceImpl)
  }
  @JsonbCreator
  def json(service: Service, i: Int, s: String): MarkedFactoryWithDeps = {
    new MarkedFactoryWithDeps(i, s, service)
  }
}
class PrivateMarkedFactoryWithDeps private(var _i: Int, var _s: String, val service: Service) {
  def i: Int = _i
  def i_=(v: Int): Unit = { _i = v }
  def s: String = _s
  def s_=(v: String): Unit = { _s = v }
}
object PrivateMarkedFactoryWithDeps {
  def apply(i: Int): PrivateMarkedFactoryWithDeps = {
    new PrivateMarkedFactoryWithDeps(i, "a", new ServiceImpl)
  }
  @JsonbCreator
  private def json(service: Service, i: Int, s: String): PrivateMarkedFactoryWithDeps = {
    new PrivateMarkedFactoryWithDeps(i, s, service)
  }
}
case class CreatorInstance(x: Int, private val service: Service, s: String) {
  @JsonbCreator
  def this(s: String, service: Service) = {
    this(505, service, s)
  }
}
case class Report(title: String, users: Seq[User] = Nil, creator: Option[CreatorInstance])
case class User(name: String, age: Option[Int], metadata: Map[String, Int] = Map.empty)
