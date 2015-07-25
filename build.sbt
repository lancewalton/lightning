import sbt.{std, Keys}

val commonSettings = Seq(
  organization := "lancewalton",
  scalaVersion := "2.11.7",
  scalaBinaryVersion := "2.11",
  externalResolvers += "Tim Tennant's repo" at "http://dl.bintray.com/timt/repo/"
)

lazy val root =
  project
    .in( file(".") )
    .aggregate(model, evaluator, graphviz)
   
lazy val model =
  project
    .in( file("model") )
    .settings(commonSettings: _*)
    .settings(Seq(
      name := "lightning-model",
      libraryDependencies ++= Seq(
        "org.scalaz"                   %% "scalaz-core"                    % "7.1.3",
        "io.argonaut"                  %% "argonaut"                       % "6.1")
    ): _*)

lazy val evaluator =
  project
    .in( file("evaluator") )
    .dependsOn(model)
    .settings(commonSettings: _*)
    .settings(Seq(
      name := "lightning-evaluator",
      libraryDependencies ++= Seq(
        "io.shaka"                     %% "naive-http"                     % "73")
    ): _*)

lazy val graphviz =
  project
    .in( file("graphviz") )
    .dependsOn(model)
    .settings(commonSettings: _*)
    .settings(Seq(
      name := "lightning-graphviz"
    ): _*)
