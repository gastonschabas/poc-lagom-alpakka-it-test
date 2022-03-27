ThisBuild / version      := "0.0.0"
ThisBuild / scalaVersion := "2.13.8"
ThisBuild / libraryDependencySchemes +=
  "org.scala-lang.modules" %% "scala-java8-compat" % VersionScheme.Always

lazy val root = (project in file("."))
  .settings(
    name         := "POC Lagom Alpakka IT Test",
    organization := "com.gaston.schabas"
  )
  .aggregate(`poc-api`, `poc-impl`)

lazy val `poc-api` = (project in file("poc-api"))
  .settings(libraryDependencies ++= Seq(lagomScaladslApi))

lazy val `poc-impl` = (project in file("poc-impl"))
  .dependsOn(`poc-api`)
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslServer,
      lagomScaladslDevMode,
      lagomScaladslKafkaBroker,
      lagomScaladslPersistenceJdbc,
      lagomScaladslPersistence,
      lagomScaladslTestKit,
      "org.scalatest" %% "scalatest"                       % "3.2.11" % Test,
      "com.dimafeng"  %% "testcontainers-scala-scalatest"  % "0.40.3" % Test,
      "com.dimafeng"  %% "testcontainers-scala-kafka"      % "0.40.3" % Test,
      "com.dimafeng"  %% "testcontainers-scala-postgresql" % "0.40.3" % Test,
      "org.postgresql" % "postgresql"                      % "42.3.3",
      "ch.qos.logback" % "logback-classic"                 % "1.2.11",
      "com.typesafe.scala-logging" %% "scala-logging"  % "3.9.4",
      "com.softwaremill.macwire"   %% "macros"         % "2.5.6" % "provided",
      "com.typesafe.slick"         %% "slick"          % "3.3.3",
      "com.typesafe.slick"         %% "slick-hikaricp" % "3.3.3"
    )
  )
