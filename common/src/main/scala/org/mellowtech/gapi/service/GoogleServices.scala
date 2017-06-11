package org.mellowtech.gapi.service

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import org.mellowtech.gapi.model.{Converters, GApiType, JsonError}

import scala.concurrent.{ExecutionContext, Future}

class GApiException(val jsonError: Option[JsonError]) extends Exception

trait GService[A] {

  def credential: Credential
  def service: A

  implicit def ec: ExecutionContext

  def execA[T](e: => T): Future[T] = {
    Future(e) recover {
      case e: Throwable => throw Converters.toGApiException(e)
    }
  }

  def execU(e: => Unit): Future[Unit] = {
    Future(e) recover {
      case e: Throwable => throw Converters.toGApiException(e)
    }
  }
}



