import sbt._
import Keys._

object Dependencies {

  //Version numbers
  val json4sVersion = "3.5.2"
  val akkaHttpVersion = "10.0.9"
  val akkaVersion = "2.4.19"
  val slickVersion = "3.2.1"
  val googleDriveAPIVersion = "v3-rev80-1.22.0"
  val googleApiVersion = "1.22.0"
  val googleHttpVersion = "1.22.0"
  val googleOauthVersion = "1.22.0"
  val slf4jVersion = "1.7.25"
  val logbackVersion = "1.2.3"
  val typesafeConfigVersion = "1.3.1"
  val scalaReflectVersion = "2.12.2"
  val scalatestVersion = "3.0.3"
  val postgresqlVersion = "42.1.1"
  val scalatagsVersion = "0.6.5"
  val pegdownVersion = "1.6.0"


  //artifacts

  val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
  val akkaHttpJson = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
  val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
  val googleApi = "com.google.api-client" % "google-api-client" % googleApiVersion
  val googleDriveApi = "com.google.apis" % "google-api-services-drive" % googleDriveAPIVersion
  val googleOauthClient = "com.google.oauth-client" % "google-oauth-client-jetty" % googleApiVersion
  val slf4j = "org.slf4j" % "slf4j-api" % slf4jVersion
  val logback ="ch.qos.logback" % "logback-classic" % logbackVersion
  val pegdown = "org.pegdown" % "pegdown" % pegdownVersion % "test"
  val postgresql = "org.postgresql" % "postgresql" % postgresqlVersion
  val scalaReflect = "org.scala-lang" % "scala-reflect" % scalaReflectVersion
  val scalatags = "com.lihaoyi" %% "scalatags" % scalatagsVersion
  val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion % "test"
  val slickCore = "com.typesafe.slick" %% "slick" % slickVersion
  val slickHikari = "com.typesafe.slick" %% "slick-hikaricp" % slickVersion
  val typesafeConf = "com.typesafe" % "config" % typesafeConfigVersion

  //project dependencies
  val akkaDeps = Seq(scalatest, pegdown, akkaHttp,akkaHttpJson)
  val commonDeps = Seq(scalatest,pegdown,googleApi,googleOauthClient,typesafeConf,scalaReflect,slickCore,slickHikari)
  val driveDeps = Seq(scalatest,pegdown,googleDriveApi)
  val serverexampleDeps = Seq(scalatest,pegdown,postgresql,akkaHttp,akkaSlf4j,logback,scalatags)
  val localexampleDeps = Seq(scalatest,pegdown,postgresql,logback)


  //val httpClientJackson = "com.google.http-client" % "google-http-client-jackson2" % googleHttpVersion

}
