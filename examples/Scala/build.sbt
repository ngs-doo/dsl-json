name := "DSL-JSON Scala example"
version := "1.9.7"
organization := "com.dslplatform.json.example"
scalaVersion := "2.12.12"

ThisBuild / useCoursier := false
resolvers += Resolver.mavenLocal

libraryDependencies += "com.dslplatform" %% "dsl-json-scala" % "1.9.7"
libraryDependencies += "javax.json.bind" % "javax.json.bind-api" % "1.0"
