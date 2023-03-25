val scala3Version = "3.2.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "influencer2",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.10",
      "dev.zio" %% "zio-http" % "0.0.5",
      "dev.zio" %% "zio-test" % "2.0.10" % Test,
      "dev.zio" %% "zio-test-sbt" % "2.0.10" % Test,
      "dev.zio" %% "zio-test-magnolia" % "2.0.10" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
