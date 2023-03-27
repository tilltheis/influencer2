val scala3Version = "3.2.2"

lazy val root = project
  .in(file("."))
  .settings(
    name         := "influencer2",
    version      := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    scalacOptions ++= Seq("-Werror", "-Wunused:all"),
    libraryDependencies ++= Seq(
      "dev.zio"            %% "zio"                       % "2.0.10",
      "dev.zio"            %% "zio-http"                  % "0.0.5",
      "dev.zio"            %% "zio-json"                  % "0.5.0",
      "dev.zio"            %% "zio-logging"               % "2.1.11",
      "dev.zio"            %% "zio-logging-slf4j2"        % "2.1.11",
      "dev.zio"            %% "zio-logging-slf4j2-bridge" % "2.1.11",
      "dev.zio"            %% "zio-test"                  % "2.0.10" % Test,
      "dev.zio"            %% "zio-test-sbt"              % "2.0.10" % Test,
      "dev.zio"            %% "zio-test-magnolia"         % "2.0.10" % Test,
      "io.github.kirill5k" %% "mongo4cats-zio"            % "0.6.10",
      "io.github.kirill5k" %% "mongo4cats-zio-json"       % "0.6.10",
      "io.github.kirill5k" %% "mongo4cats-zio-embedded"   % "0.6.10" % Test,
      "org.mindrot"         % "jbcrypt"                   % "0.4"
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    wartremoverErrors ++= Warts.unsafe.filterNot(Set(Wart.Any, Wart.TripleQuestionMark, Wart.OptionPartial).contains)
  )
