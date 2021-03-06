package org.mellowtech.gapi.model

import org.mellowtech.gapi.service.GApiException

import scala.collection.immutable.ListMap


trait GApiType

case class TokenResponse(access_token: String,
                         token_type: String,
                         expires_in: Int,
                         refresh_token: Option[String] = None) extends GApiType


case class ErrorInfo(domain: Option[String] = None,
                    location: Option[String] = None,
                    locationType: Option[String] = None,
                    message: Option[String] = None,
                    reason: Option[String] = None) extends GApiType

case class JsonError(code: Option[Int] = None,
                     errors: Option[Seq[ErrorInfo]] = None,
                     message: Option[String] = None) extends GApiType

object CaseClassConverter {


  import scala.reflect.runtime.universe._

  /**
    * Returns a map from formal parameter names to types, containing one
    * mapping for each constructor argument.  The resulting map (a ListMap)
    * preserves the order of the primary constructor's parameter list.
    */
  def caseClassParamsOf[T: TypeTag]: ListMap[String, Type] = {
    val tpe = typeOf[T]
    val constructorSymbol = tpe.decl(nme.CONSTRUCTOR)
    val defaultConstructor =
      if (constructorSymbol.isMethod) constructorSymbol.asMethod
      else {
        val ctors = constructorSymbol.asTerm.alternatives
        ctors.map { _.asMethod }.find { _.isPrimaryConstructor }.get
      }

    ListMap[String, Type]() ++ defaultConstructor.paramLists.reduceLeft(_ ++ _).map {
      sym => sym.name.toString -> tpe.member(sym.name).asMethod.returnType
    }
  }

}

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
    jt.setExpiresInSeconds(tr.expires_in.toLong)
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
