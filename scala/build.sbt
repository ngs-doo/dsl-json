lazy val root = (project in file(".")
  settings (commonSettings ++ publishSettings)
  settings(
    version := "1.9.10",
    libraryDependencies ++= Seq(
      "com.dslplatform" % "dsl-json-java8" % "1.9.10",
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.specs2" %% "specs2-scalacheck" % "4.5.1" % Test,
      "javax.json.bind" % "javax.json.bind-api" % "1.0" % Test
    ),
    name := "DSL-JSON Scala"
  )
)

ThisBuild / useCoursier := false
resolvers += Resolver.mavenLocal

// ### COMMON SETTINGS ###

lazy val commonSettings = Defaults.coreDefaultSettings ++ Seq(
  organization := "com.dslplatform",
  name := baseDirectory.value.getName,
  scalaVersion := crossScalaVersions.value.head,
  crossScalaVersions := Seq("2.11.12", "2.12.16", "2.13.8"),
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:_",
    "-target:jvm-1.8",
    "-unchecked",
    "-Xlint:_",
    "-Xverify",
    "-Yrangepos",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused"
  ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 11)) => Seq(
      "-Yclosure-elim",
      "-Yno-adapted-args",
      "-Yrepl-sync",
      "-Yconst-opt",
      "-Ydead-code",
      "-Ywarn-unused-import",
      "-Xfuture",
      "-Xexperimental",
      "-Yinline",
      "-Yinline-warnings:false"
    )
    case Some((2, 12)) => Seq(
      "-Yno-adapted-args",
      "-Yrepl-sync",
      "-Ywarn-unused-import",
      "-Xfuture",
      "-Xexperimental"
    )
    case _ => Seq(
      "-opt:_"
    )
  })
)

// ### PUBLISH SETTINGS ###

val publishSettings = Seq(
  scalacOptions in(Compile, doc) ++= Seq(
    "-no-link-warnings",
    "-sourcepath", baseDirectory.value.toString,
    "-doc-source-url", if (isSnapshot.value) {
      s"""https://github.com/ngs-doo/dsl-json/tree/master/scala/${name.value}\u20AC{FILE_PATH}.scala"""
    } else {
      s"""https://github.com/ngs-doo/dsl-json/blob/${version.value}/scala/${name.value}\u20AC{FILE_PATH}.scala"""
    }
  ),

  packageOptions := Seq(Package.ManifestAttributes(
    ("Implementation-Vendor", "New Generation Software Ltd.")
  )),

  publishTo := Some(if (isSnapshot.value) Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging),
  publishArtifact in Test := false,
  publishMavenStyle := true,
  pomIncludeRepository := { _ => false },
  useGpg := true,

  credentials ++= {
    val creds = Path.userHome / ".config" / "dsl-json" / "nexus.config"
    if (creds.exists) Some(Credentials(creds)) else None
  }.toSeq,

  pomExtra :=
    <inceptionYear>2013</inceptionYear>
      <url>https://github.com/ngs-doo/dsl-json</url>
      <licenses>
        <license>
          <name>BSD 3-clause "New" or "Revised" License</name>
          <url>https://spdx.org/licenses/BSD-3-Clause.html</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:ngs-doo/dsl-json.git</url>
        <connection>scm:git:git@github.com:ngs-doo/dsl-json.git</connection>
      </scm>
      <developers>
        <developer>
          <id>zapov</id>
          <name>Rikard Paveli&#263;
          </name>
          <url>https://github.com/zapov</url>
        </developer>
      </developers>
)
