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
    .settings(commonSettings: _*)
    .settings(Seq(
      name := "lightning-model",
      libraryDependencies ++= Seq(
        scalazCore,
        argonaut)
    ): _*)

lazy val evaluator =
  project
    .dependsOn(model)
    .settings(commonSettings: _*)
    .settings(Seq(
      name := "lightning-evaluator",
      libraryDependencies += naiveHttpClient
    ): _*)

lazy val graphviz =
  project
    .dependsOn(model)
    .settings(commonSettings: _*)
    .settings(Seq(
      name := "lightning-graphviz"
    ): _*)

lazy val configuration =
  project
    .dependsOn(model, evaluator)
    .settings(commonSettings: _*)
    .settings(Seq(
      name := "lightning-configuration",
      libraryDependencies ++= Seq(
        scalazCore,
        typesafeConfig,
        shapelessScalaz)
    ): _*)

lazy val scalazCore        = "org.scalaz"                   %% "scalaz-core"                    % "7.1.3"
lazy val argonaut          = "io.argonaut"                  %% "argonaut"                       % "6.1"
lazy val naiveHttpClient   = "io.shaka"                     %% "naive-http"                     % "73"
lazy val typesafeConfig    = "com.typesafe"                  % "config"                         % "1.3.0"
lazy val shapelessScalaz   = "org.typelevel"                 %% "shapeless-scalaz"          % "0.4"         

