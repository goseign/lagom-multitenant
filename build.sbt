import com.lightbend.lagom.sbt.LagomImport.lagomScaladslKafkaBroker

organization in ThisBuild := "optrak"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

val latestIntegration = "latest.integration"

val myScalacOptions = Seq(
  "-feature",
  "-deprecation",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-unchecked",
  "-deprecation",
  "-Xfuture",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-unused"
)

val clapper = "org.clapper" % "grizzled-slf4j_2.11" % "1.3.0"
val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test
val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.13.4" % Test
val scalaCheckShapeless = "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.3" % Test
val csvXls = "com.optrak" %% "csv-xls" % latestIntegration
val optrakJson = "com.optrak" %% "scala-json" % latestIntegration
val optrakXml = "com.optrak" %% "scala-xml" % latestIntegration
val json4s = "org.json4s" %  "json4s-jackson_2.11" % "3.5.0"

val testDependencies = Seq(libraryDependencies ++= Seq(
    scalaTest,
    scalaCheck,
    lagomScaladslTestKit,
    scalaCheckShapeless
  )
)

val stdApiDependencies = Seq(libraryDependencies ++= Seq(
  lagomScaladslApi,
  optrakJson,
  clapper
))

val kafkaApiDependencies = Seq(libraryDependencies ++= Seq(
  lagomScaladslApi,
  clapper,
  lagomScaladslKafkaBroker
))

lazy val commonSettings = Seq(
  parallelExecution in Test := false,
  scalacOptions ++= myScalacOptions,
  scalaVersion := "2.11.8",
  organization := "optrak",
  exportJars := true,
  updateOptions := updateOptions.value.withCachedResolution(true),
  resolvers ++= Seq(
    "optrak repo" at "https://office.optrak.com/code/releases/",
    "optrak thirdparty" at "https://office.optrak.com/code/thirdparty/"
  ),
  lagomCassandraCleanOnStart in ThisBuild := true
)

val stdImplDependencies = Seq(
  libraryDependencies ++= Seq(
    optrakJson,
    lagomScaladslPersistenceCassandra,
    lagomScaladslKafkaBroker,
    clapper,
    macwire
))

lazy val `plan-api` = (project in file("plan-api"))
  .settings(kafkaApiDependencies :_*)
  .dependsOn(`datamodel`)
  .dependsOn(`lagom-util`)

lazy val `plan-impl` = (project in file("plan-impl"))
  .enablePlugins(LagomScala)
  .settings(stdImplDependencies :_*)
  .settings(testDependencies :_*)
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`datamodel`)
  .dependsOn(`lagom-util`)
  .dependsOn(`plan-api`)
  .dependsOn(`product-impl` % "test->test")
  .dependsOn(`site-impl` % "test->test")
  .dependsOn(`vehicle-impl` % "test->test")
  .dependsOn(`order-impl` % "test->test")
  .dependsOn(`lagom-testutils` % "test")

lazy val `product-api` = (project in file("product-api"))
  .settings(stdApiDependencies :_*)
  .dependsOn(`datamodel`)
  .dependsOn(`lagom-util`)

lazy val `product-impl` = (project in file("product-impl"))
  .enablePlugins(LagomScala)
  .settings(stdImplDependencies :_*)
  .settings(testDependencies :_*)
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`datamodel`)
  .dependsOn(`lagom-util`)
  .dependsOn(`product-api`)
  .dependsOn(`lagom-testutils` % "test")

lazy val `site-api` = (project in file("site-api"))
  .settings(stdApiDependencies :_*)
  .dependsOn(`datamodel`)
  .dependsOn(`lagom-util`)

lazy val `site-impl` = (project in file("site-impl"))
  .enablePlugins(LagomScala)
  .settings(stdImplDependencies :_*)
  .settings(testDependencies :_*)
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`datamodel`)
  .dependsOn(`lagom-util`)
  .dependsOn(`site-api`)
  .dependsOn(`lagom-testutils` % "test")

lazy val `order-api` = (project in file("order-api"))
  .settings(stdApiDependencies :_*)
  .dependsOn(`datamodel`)
  .dependsOn(`lagom-util`)

lazy val `order-impl` = (project in file("order-impl"))
  .enablePlugins(LagomScala)
  .settings(stdImplDependencies :_*)
  .settings(testDependencies :_*)
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`datamodel`)
  .dependsOn(`lagom-util`)
  .dependsOn(`order-api`)
  .dependsOn(`product-api`)
  .dependsOn(`lagom-testutils` % "test")
  .dependsOn(`site-api`)

lazy val `vehicle-api` = (project in file("vehicle-api"))
  .settings(stdApiDependencies :_*)
  .dependsOn(`datamodel`)
  .dependsOn(`lagom-util`)

lazy val `vehicle-impl` = (project in file("vehicle-impl"))
  .enablePlugins(LagomScala)
  .settings(stdImplDependencies :_*)
  .settings(testDependencies :_*)
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`datamodel`)
  .dependsOn(`lagom-util`)
  .dependsOn(`lagom-testutils` % "test")
  .dependsOn(`vehicle-api`)

lazy val `plan-reader-api` = (project in file("plan-reader-api"))
  .settings(kafkaApiDependencies :_*)
  .dependsOn(`datamodel`)
  .dependsOn(`plan-api`)
  .dependsOn(`lagom-util`)

lazy val `plan-reader-impl` = (project in file("plan-reader-impl"))
  .enablePlugins(LagomScala)
  .settings(stdImplDependencies :_*)
  .settings(testDependencies :_*)
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`datamodel`)
  .dependsOn(`lagom-util`)
  .dependsOn(`plan-api`)
  .dependsOn(`plan-reader-api`)
  .dependsOn(`plan-impl` % "test")
  .dependsOn(`lagom-testutils` % "test")


lazy val `tenant-api` = (project in file("tenant-api"))
  .settings(kafkaApiDependencies :_*)
  .dependsOn(`datamodel`)
  .dependsOn(`lagom-util`)

lazy val `tenant-impl` = (project in file("tenant-impl"))
  .enablePlugins(LagomScala)
  .settings(stdImplDependencies :_*)
  .settings(testDependencies :_*)
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`datamodel`)
  .dependsOn(`lagom-util`)
  .dependsOn(`tenant-api`)
  .dependsOn(`lagom-testutils` % "test")
//

lazy val `datamodel` = (project in file("datamodel"))
  .enablePlugins(LagomScala)
  .settings(lagomForkedTestSettings: _*)

lazy val `lagom-util` = (project in file("lagom-util"))
  .enablePlugins(LagomScala)
  .settings(Seq(libraryDependencies ++= Seq(
    lagomScaladslApi,
    optrakJson,
    optrakXml,
    csvXls,
    clapper
  )))
  .settings(lagomForkedTestSettings: _*)
  .settings(testDependencies :_*)

lazy val `integration` = (project in file("integration"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      macwire
    )

  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`plan-impl` % "test")
  .dependsOn(`plan-reader-impl` % "test")

lazy val `lagom-testutils` = (project in file("lagom-testutils"))
  .enablePlugins(LagomScala)
  .settings(Seq(libraryDependencies ++= Seq(
    lagomScaladslApi,
    clapper
  )))
  .settings(commonSettings: _*)
  .settings(lagomForkedTestSettings: _*)
  .settings(testDependencies :_*)
