name := "DSL-JSON Scala example"
version := "2.0.1"
organization := "com.dslplatform.json.example"
scalaVersion := "2.13.11"

ThisBuild / useCoursier := false
resolvers += Resolver.mavenLocal

libraryDependencies += "com.dslplatform" %% "dsl-json-scala" % "2.0.1"
libraryDependencies += "javax.json.bind" % "javax.json.bind-api" % "1.0"
