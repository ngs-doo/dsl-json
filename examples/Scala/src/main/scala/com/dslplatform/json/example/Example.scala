package com.dslplatform.json.example

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

//import implicit conversion for DSL-JSON Scala pimps
import com.dslplatform.json.DslJson

//when defaults are defined properties can be omitted from JSON - and default value will be used for them
//types without Option[_] can't be null in input JSON
case class Report(title: String, users: Seq[User] = Nil)

//Primitives in container are correctly analyzed and decoded
case class User(name: String, age: Option[Int], metadata: Map[String, Int] = Map.empty)

object Example extends App {

  //This configuration will not support unknown types (eg AnyRef,...) or Java8 specific types
  //To allow support for unknown types use new DslJson[Any](Settings.withRuntime())
  val dslJson = new DslJson[Any]()

  val os = new ByteArrayOutputStream()
  val report = Report(
    "DSL-JSON serialization",
    Seq(
      User("username1", Some(55)),
      User("username2", Some(-123), Map("abc" -> 123)),
      User("username3", Some(0), Map("x" -> -1, "y" -> 1))
    )
  )
  //when using encode instead of serialize, types will be analyzed before conversion starts
  dslJson.encode(report, os)

  val is = new ByteArrayInputStream(os.toByteArray)
  //by using decode TypeTags will be used to create accurate type representation
  //otherwise some types - eg classes nested in objects are not analyzed correctly due to missing metadata
  val result = dslJson.decode[Report](is)

  println(os)
  println(result == report)
}
