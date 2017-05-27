package org.mellowtech.gapi.config

import com.typesafe.config.ConfigFactory

trait GApiConfig {

  //Config for web server based auth:
  def httpHost: Option[String]
  def httpPort: Option[Int]

  def oauthEndPoint: Option[String]
  def tokenEndPoint: Option[String]
  def redirectUri: Option[String]
  def accessType: Option[String]
  def authPath: Option[String]
  def authCallbackPath: Option[String]

  def clientId: String
  def clientSecret: String
  def applicationName: String
  def scopes: String

  //db for storing stuff
  def dbProfile: Option[String]
  def dbUrl: Option[String]
  def dbUser: Option[String]
  def dbPassword: Option[String]
  /*db {
    url = "jdbc:postgresql://localhost/testdb"
    user = "test"
    password = "12test34"
  }*/
}

object GApiConfig {
  def apply(): GApiConfig = new GApiConfigTypeSafeConfig {}
}

trait GApiConfigTypeSafeConfig extends GApiConfig{
  val config = ConfigFactory.load()

  private val httpConfig = config.getConfig("http")
  private val google = config.getConfig("google")
  private val slick = config.getConfig("slick")

  val httpHost = Option(httpConfig.getString("host"))
  val httpPort = Option(httpConfig.getInt("port"))


  val oauthEndPoint = Option(google.getString("oauth2EndPoint"))
  val tokenEndPoint = Option(google.getString("tokenEndPoint"))
  val clientId = google.getString("client_id")
  val clientSecret = google.getString("client_secret")
  val applicationName = google.getString("applicationName")
  val redirectUri = Option(google.getString("redirect_uri"))
  val accessType = Option(google.getString("access_type"))
  val scopes = google.getString("scopes")
  val authPath = Option(google.getString("authPath"))
  val authCallbackPath = Option(google.getString("authCallbackPath"))

  //db stuff
  val dbProfile = Option(slick.getString("profile"))
  val dbUrl = Option(slick.getString("url"))
  val dbUser = Option(slick.getString("user"))
  val dbPassword = Option(slick.getString("password"))

}
