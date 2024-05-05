name         := "zio-pekko-cluster"
organization := "nl.gn0s1s"
startYear    := Some(2023)
homepage     := Some(url("https://github.com/philippus/zio-pekko-cluster"))
licenses     := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

developers := List(
  Developer(
    id = "philippus",
    name = "Philippus Baalman",
    email = "",
    url = url("https://github.com/philippus")
  )
)

crossScalaVersions := List("2.13.14")
scalaVersion       := crossScalaVersions.value.last

Test / parallelExecution := false
Test / fork              := true
run / fork               := true

val zioVersion = "1.0.18"

libraryDependencies ++= Seq(
  "dev.zio"          %% "zio"                    % zioVersion,
  "dev.zio"          %% "zio-streams"            % zioVersion,
  "org.apache.pekko" %% "pekko-cluster-tools"    % "1.0.2",
  "org.apache.pekko" %% "pekko-cluster-sharding" % "1.0.2",
  "dev.zio"          %% "zio-test"               % zioVersion     % "test",
  "dev.zio"          %% "zio-test-sbt"           % zioVersion     % "test",
  "io.netty"          % "netty"                  % "3.10.6.Final" % "test",
  compilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.3" cross CrossVersion.full),
  compilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-explaintypes",
  "-Yrangepos",
  "-feature",
  "-language:higherKinds",
  "-language:existentials",
  "-unchecked",
  "-Xlint:_,-type-parameter-shadow",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-value-discard"
)
