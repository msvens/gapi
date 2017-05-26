package org.mellowtech.gapi.config

import com.typesafe.config.ConfigFactory

trait GApiConfig {
  val config = ConfigFactory.load()
  private val httpConfig = config.getConfig("http")
  //private val databaseConfig = config.getConfig("database")
  private val google = config.getConfig("google")

  val httpHost = httpConfig.getString("host")
  val httpPort = httpConfig.getInt("port")

  //val jdbcUrl = databaseConfig.getString("url")
  //val dbUser = databaseConfig.getString("user")
  //val dbPassword = databaseConfig.getString("password")

  val oauthEndPoint = google.getString("oauth2EndPoint")
  val tokenEndPoint = google.getString("tokenEndPoint")
  val clientId = google.getString("client_id")
  val clientSecret = google.getString("client_secret")
  val applicationName = google.getString("applicationName")
  val redirectUri = google.getString("redirect_uri")
  val accessType = google.getString("access_type")
  val scopes = google.getString("scopes")
  val authPath = google.getString("authPath")
  val authCallbackPath = google.getString("authCallbackPath")
  //val redirectAfterAuth = google.getString("redirectAfterAuth")

}
