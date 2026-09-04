val mainScala = "3.9.0"
val allScala  = Seq(mainScala)

val zioVersion   = "2.1.26"
val pekkoVersion = "1.7.0"

inThisBuild(
  List(
    organization             := "nl.gn0s1s",
    startYear                := Some(2023),
    homepage                 := Some(uri("https://github.com/philippus/zio-pekko-cluster")),
    licenses                 := List("Apache-2.0" -> uri("http://www.apache.org/licenses/LICENSE-2.0")),
    scalaVersion             := mainScala,
    Test / parallelExecution := false,
    Test / fork              := true,
    developers               := List(
      Developer(
        "philippus",
        "Philippus Baalman",
        "",
        uri("https://github.com/philippus")
      )
    ),
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
  )
)

lazy val root =
  project.in(file("."))
    .aggregate(`zio-pekko-cluster`)
    .settings(
      publish / skip := true
    )

lazy val `zio-pekko-cluster` = project
  .in(file("zio-pekko-cluster"))
  .settings(
    name           := "zio-pekko-cluster",
    publish / skip := false,
    libraryDependencies ++= Seq(
      "dev.zio"          %% "zio"                    % zioVersion,
      "dev.zio"          %% "zio-streams"            % zioVersion,
      "org.apache.pekko" %% "pekko-cluster-tools"    % pekkoVersion,
      "org.apache.pekko" %% "pekko-cluster-sharding" % pekkoVersion,
      "dev.zio"          %% "zio-test"               % zioVersion % "test",
      "dev.zio"          %% "zio-test-sbt"           % zioVersion % "test",
      compilerPlugin("org.typelevel" %% "kind-projector" % "0.13.4").cross(CrossVersion.full),
      compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
    )
  )

run / fork := true

crossScalaVersions := allScala

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
