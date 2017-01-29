organization in ThisBuild := "optrak"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test
val specs2 = "org.specs2" %% "specs2-core" % "3.8.7" % Test
/*
lazy val `model-api` = (project in file("model-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslApi
    )
  ).dependsOn(`model`)

lazy val `model-impl` = (project in file("model-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      macwire,
      specs2
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`model`)
  .dependsOn(`model-api`)
*/
lazy val `client-api` = (project in file("client-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslApi
    )
  ).dependsOn(`datamodel`)
  .dependsOn(`utils`)

lazy val `client-impl` = (project in file("client-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      macwire,
      scalaTest,
      specs2
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`datamodel`)
  .dependsOn(`utils`)
  .dependsOn(`client-api`)


lazy val `datamodel` = (project in file("datamodel"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      macwire,
      specs2
    )
  )
  .settings(lagomForkedTestSettings: _*)

lazy val `utils` = (project in file("utils"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      macwire,
      specs2
    )
  )
  .settings(lagomForkedTestSettings: _*)
