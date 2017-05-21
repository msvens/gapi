package org.mellowtech.gapi

import java.io.{FileInputStream, InputStreamReader, Reader}

import com.typesafe.config.ConfigFactory

import scala.util.{Failure, Success, Try}
/**
  * @author msvens
  * @since 2017-05-08
  */
trait Config {

  private val config = ConfigFactory.load()

  private val localauthConfig = config.getConfig("localauth")
  private val gapiConfig = config.getConfig("gapi")

  val scopes = gapiConfig.getStringList("scopes")

  val clientIdJson = localauthConfig.getString("clientIdJson")

  def clientIdJsonReader: Try[Reader] = {
    try {
      Success(new InputStreamReader(new FileInputStream(clientIdJson)))
    } catch {
      case e: Throwable => Failure(e)
    }
  }




}
