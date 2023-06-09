val scala3Version = "3.2.2"

lazy val root = project
  .in(file("."))
  .settings(
    name         := "influencer2",
    version      := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    scalacOptions ++= Seq("-source:future", "-Werror", "-Wunused:all", "-deprecation"),
    libraryDependencies ++= Seq(
      "dev.zio"              %% "zio"                       % "2.0.13",
      "dev.zio"              %% "zio-http"                  % "0.0.5",
      "dev.zio"              %% "zio-json"                  % "0.5.0",
      "dev.zio"              %% "zio-logging"               % "2.1.12",
      "dev.zio"              %% "zio-logging-slf4j2"        % "2.1.12",
      "dev.zio"              %% "zio-logging-slf4j2-bridge" % "2.1.12",
      "dev.zio"              %% "zio-test"                  % "2.0.13" % Test,
      "dev.zio"              %% "zio-test-sbt"              % "2.0.13" % Test,
      "io.github.kirill5k"   %% "mongo4cats-zio"            % "0.6.11",
      "io.github.kirill5k"   %% "mongo4cats-zio-json"       % "0.6.11",
      "org.mindrot"           % "jbcrypt"                   % "0.4",
      "com.github.jwt-scala" %% "jwt-core"                  % "9.2.0"
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    wartremoverErrors ++=
      Warts.unsafe.filterNot(Set(Wart.Any, Wart.TripleQuestionMark, Wart.DefaultArguments).contains),
    run / fork         := true,
    run / connectInput := true
  )
