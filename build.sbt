import com.lightbend.lagom.sbt.LagomImport.lagomScaladslKafkaBroker

organization in ThisBuild := "optrak"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

val clapper = "org.clapper" % "grizzled-slf4j_2.11" % "1.3.0"
val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test
val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.13.4" % Test
val scalaCheckShapeless = "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.3" % Test

val testDependencies = Seq(libraryDependencies ++= Seq(
    scalaTest,
    scalaCheck,
    lagomScaladslTestKit,
    scalaCheckShapeless
  )
)

val stdApiDependencies = Seq(libraryDependencies ++= Seq(
  lagomScaladslApi,
  clapper
))

val kafkaApiDependencies = Seq(libraryDependencies ++= Seq(
  lagomScaladslApi,
  clapper,
  lagomScaladslKafkaBroker
))


val stdImplDependencies = Seq(
  libraryDependencies ++= Seq(
    lagomScaladslPersistenceCassandra,
    lagomScaladslKafkaBroker,
    clapper,
    macwire
))

lazy val `model-api` = (project in file("model-api"))
  .settings(kafkaApiDependencies :_*)
  .dependsOn(`datamodel`)

lazy val `model-impl` = (project in file("model-impl"))
  .enablePlugins(LagomScala)
  .settings(stdImplDependencies :_*)
  .settings(testDependencies :_*)
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`datamodel`)
  .dependsOn(`utils`)
  .dependsOn(`model-api`)

lazy val `product-api` = (project in file("product-api"))
  .settings(stdApiDependencies :_*)
  .dependsOn(`datamodel`)

lazy val `product-impl` = (project in file("product-impl"))
  .enablePlugins(LagomScala)
  .settings(stdImplDependencies :_*)
  .settings(testDependencies :_*)
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`datamodel`)
  .dependsOn(`utils`)
  .dependsOn(`product-api`)


lazy val `model-reader-api` = (project in file("model-reader-api"))
  .settings(kafkaApiDependencies :_*)
  .dependsOn(`datamodel`)
  .dependsOn(`model-api`)

lazy val `model-reader-impl` = (project in file("model-reader-impl"))
  .enablePlugins(LagomScala)
  .settings(stdImplDependencies :_*)
  .settings(testDependencies :_*)
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`datamodel`)
  .dependsOn(`utils`)
  .dependsOn(`model-api`)
  .dependsOn(`model-reader-api`)
  .dependsOn(`model-impl` % "test")


lazy val `tenant-api` = (project in file("tenant-api"))
  .settings(kafkaApiDependencies :_*)
  .dependsOn(`datamodel`)
  .dependsOn(`utils`)

lazy val `tenant-impl` = (project in file("tenant-impl"))
  .enablePlugins(LagomScala)
  .settings(stdImplDependencies :_*)
  .settings(testDependencies :_*)
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`datamodel`)
  .dependsOn(`utils`)
  .dependsOn(`tenant-api`)


lazy val `datamodel` = (project in file("datamodel"))
  .enablePlugins(LagomScala)
  .settings(lagomForkedTestSettings: _*)

lazy val `utils` = (project in file("utils"))
  .enablePlugins(LagomScala)
  .settings(lagomForkedTestSettings: _*)
  .settings(testDependencies :_*)
  .settings(stdImplDependencies :_*)

lazy val `integration` = (project in file("integration"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      macwire
    )

  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`model-impl` % "test")
  .dependsOn(`model-reader-impl` % "test")

