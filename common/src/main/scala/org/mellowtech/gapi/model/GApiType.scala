package org.mellowtech.gapi.model

import org.mellowtech.gapi.service.GApiException

trait GApiType

case class TokenResponse(access_token: String,
                         token_type: String,
                         expires_in: Long,
                         refresh_token: Option[String]) extends GApiType

case class ErrorInfo(domain: Option[String] = None,
                    location: Option[String] = None,
                    locationType: Option[String] = None,
                    message: Option[String] = None,
                    reason: Option[String] = None) extends GApiType

case class JsonError(code: Option[Int] = None,
                     errors: Option[Seq[ErrorInfo]] = None,
                     message: Option[String] = None) extends GApiType

object Converters{

  import com.google.api.client.googleapis.json.GoogleJsonError
  import com.google.api.client.googleapis.json.GoogleJsonResponseException
  import com.google.api.client.googleapis.json.GoogleJsonError.{ErrorInfo => JErrorInfo}
  import com.google.api.client.auth.oauth2.{TokenResponse => JTokenResponse}
  import scala.collection.JavaConverters._

  private def toErrorInfo(ei: JErrorInfo): ErrorInfo = {
    ErrorInfo(
      domain = Option(ei.getDomain),
      location = Option(ei.getLocation),
      locationType = Option(ei.getLocationType),
      message = Option(ei.getMessage),
      reason = Option(ei.getMessage))
  }

  def toJava(tr: TokenResponse): JTokenResponse = {
    val jt = new JTokenResponse()
    jt.setAccessToken(tr.access_token)
    jt.setTokenType(tr.token_type)
    jt.setExpiresInSeconds(tr.expires_in)
    if(tr.refresh_token.isDefined) jt.setRefreshToken(tr.refresh_token.get)
    jt
  }

  def toJsonError(e: GoogleJsonError): JsonError = {
    val ei = if(e.getErrors == null) None else
      Some(e.getErrors.asScala.map(toErrorInfo(_)))
    JsonError(
      code = Option(e.getCode),
      errors = ei,
      message = Option(e.getMessage)

    )
  }

  def toGApiException(e: Throwable): GApiException = e match {
    case e: GoogleJsonResponseException => {
      if(e.getDetails == null) new GApiException(None) else new GApiException(Some(toJsonError(e.getDetails)))
    }
    case e: GApiException => e
    case e: Throwable => new GApiException(None)
  }


}
