import sbt.Project.projectToLocalProject

val mainScala = "2.13.14"
val allScala  = Seq(mainScala)

val zioVersion   = "2.1.9"
val pekkoVersion = "1.0.3"

inThisBuild(
  List(
    organization             := "nl.gn0s1s",
    startYear                := Some(2023),
    homepage                 := Some(url("https://github.com/philippus/zio-pekko-cluster")),
    licenses                 := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalaVersion             := mainScala,
    Test / parallelExecution := false,
    Test / fork              := true,
    developers               := List(
      Developer(
        "philippus",
        "Philippus Baalman",
        "",
        url("https://github.com/philippus")
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
    .aggregate(`zio-pekko-cluster`, docs)
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
      compilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.3" cross CrossVersion.full),
      compilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
    )
  )

run / fork := true

crossScalaVersions := allScala

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

lazy val docs = project
  .in(file("zio-pekko-cluster-docs"))
  .settings(
    publish / skip                             := true,
    moduleName                                 := "zio-pekko-cluster-docs",
    projectName                                := "ZIO Pekko Cluster",
    mainModuleName                             := (`zio-pekko-cluster` / moduleName).value,
    projectStage                               := ProjectStage.ProductionReady,
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(`zio-pekko-cluster`),
    docsPublishBranch                          := "series/2.x"
  )
  .enablePlugins(WebsitePlugin)
  .dependsOn(`zio-pekko-cluster`)
