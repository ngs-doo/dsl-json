name := "DSL-JSON Scala example"
version := "2.1.0"
organization := "com.dslplatform.json.example"
scalaVersion := "2.13.18"

ThisBuild / useCoursier := false
resolvers += Resolver.mavenLocal

libraryDependencies += "com.dslplatform" %% "dsl-json-scala" % "2.1.0"
libraryDependencies += "javax.json.bind" % "javax.json.bind-api" % "1.0"
