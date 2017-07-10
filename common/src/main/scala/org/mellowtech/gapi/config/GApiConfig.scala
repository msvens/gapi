package org.mellowtech.gapi.config

import com.typesafe.config.ConfigFactory

trait GApiConfig {

  //Config for web server based auth:
  def httpHost: Option[String]
  def httpPort: Option[Int]

  def authUri: Option[String]
  def tokenUri: Option[String]
  def redirectUri: Option[String]
  def accessType: Option[String]
  def authPath: Option[String]
  def authCallbackPath: Option[String]

  def clientIdInstalled: Option[String]
  def clientSecretInstalled: Option[String]

  def clientId: String
  def clientSecret: String
  def applicationName: String
  def scopes: Seq[String]

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

  import scala.collection.JavaConverters._


  val config = ConfigFactory.load()

  private val httpConfig = config.getConfig("http")
  private val google = config.getConfig("google")
  private val slick = config.getConfig("slick")

  val httpHost = Option(httpConfig.getString("host"))
  val httpPort = Option(httpConfig.getInt("port"))


  val authUri = Option(google.getString("authUri"))
  val tokenUri = Option(google.getString("tokenUri"))
  val clientId = google.getString("client_id")
  val clientSecret = google.getString("client_secret")
  val applicationName = google.getString("applicationName")
  val redirectUri = Option(google.getString("redirect_uri"))
  val accessType = Option(google.getString("access_type"))

  val scopes = Option(google.getStringList("scopes")) match {
    case None => Seq()
    case Some(l) => l.asScala
  }

  val authPath = Option(google.getString("authPath"))
  val authCallbackPath = Option(google.getString("authCallbackPath"))

  //installed app
  val clientIdInstalled = Option(google.getConfig("installed")) match {
    case Some(c) => Some(c.getString("client_id"))
    case None => None
  }

  val clientSecretInstalled = Option(google.getConfig("installed")) match {
    case Some(c) => Some(c.getString("client_secret"))
    case None => None
  }

  //db stuff
  val dbProfile = Option(slick.getString("profile"))
  val dbUrl = Option(slick.getString("url"))
  val dbUser = Option(slick.getString("user"))
  val dbPassword = Option(slick.getString("password"))

}
