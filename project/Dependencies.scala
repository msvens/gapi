import sbt._
import Keys._

object Dependencies {

  val json4sVersion = "3.5.2"


  val typesafeConf = "com.typesafe" % "config" % "1.3.1"


  //logging
  val slf4j = "org.slf4j" % "slf4j-api" % "1.7.25"
  val logback ="ch.qos.logback" % "logback-classic" % "1.2.3"

  val commonAPIs = Seq(typesafeConf,slf4j)

  //for testing
  val junit = "junit" % "junit" % "4.12" % "test"
  val scalatest = "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  val pegdown = "org.pegdown" % "pegdown" % "1.4.2" % "test"
  val testDeps = Seq(junit,scalatest,pegdown)

  //google related

  //val json4s = "org.json4s" %% "json4s-native" % json4sVersion
  //val json4sext = "org.json4s" %% "json4s-ext" % json4sVersion
  //val upickle = "com.lihaoyi" %% "upickle" % "0.4.4"
  //val jsonDeps = Seq(upickle)

  //google apis
  val driveAPI = "com.google.apis" % "google-api-services-drive" % "v3-rev72-1.22.0"
  val httpClientJackson = "com.google.http-client" % "google-http-client-jackson2" % "1.22.0"
  val oathClientJetty = "com.google.oauth-client" % "google-oauth-client-jetty" % "1.22.0"

  val googleAPIs = Seq(driveAPI,httpClientJackson,oathClientJetty)

  //akka-http apis

  //val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % "2.4.17"
  //val akkaHttp = "de.heikoseeberger" %% "akka-http-upickle" % "1.17.0"
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.0.9"
  val akkaHttpJson = "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.9"

  val akkaAPIs = Seq(akkaHttp, akkaHttpJson)



  //slick
  val slick = "com.typesafe.slick" %% "slick" % "3.2.1"
  //val slickSlf4j = "org.slf4j" % "slf4j-nop" % "1.6.4"
  val slickHikari = "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1"

  //val slickDeps = Seq(slick,slickSlf4j,slickHikari)
  val slickDeps = Seq(slick, slickHikari)

}
