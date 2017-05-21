package org.mellowtech.gapi.server

import java.net.URLEncoder
import java.util.concurrent.atomic.AtomicBoolean

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import org.json4s.{DefaultFormats, native}
import org.mellowtech.gapi.config.GApiConfig
import org.mellowtech.gapi.model.TokenResponse
import org.mellowtech.gapi.store.{TokenDAO}


import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

/**
  * @author msvens
  * @since 2017-05-12
  */

trait GoogleAuthenticated{
  def onAuthenticated(tr: TokenResponse): Unit
  def redirect: Option[String] = None
}

trait DefaultAuthenticated extends GoogleAuthenticated{
  import scala.concurrent.duration._

  def tokenDAO: TokenDAO
  implicit def ec: ExecutionContext

  override def onAuthenticated(tr: TokenResponse): Unit = {
    val t = TokenDAO.toToken(tr)
    val r = Await.result(tokenDAO.put(t), 1 second)
    r match {
      case x if x > 0 => authenticated.set(true)
      case _ => authenticated.set(false)
    }

  }

  override val redirect: Option[String] = Some("/")

  val authenticated: AtomicBoolean = new AtomicBoolean(false)


}

class GoogleRouter(val callback: GoogleAuthenticated)(implicit val actorSystem: ActorSystem, implicit val executor: ExecutionContext) extends GApiConfig{

  implicit val log: LoggingAdapter = Logging(actorSystem, getClass)

  private def authUrl(state: Option[String]): String = {
    val rUriEncode = URLEncoder.encode(redirectUri, "UTF-8")
    val scopesEncode = URLEncoder.encode(scopes, "UTF-8")
    val aUrl = oauthEndPoint + "?client_id=" + clientId + "&redirect_uri=" + rUriEncode + "&response_type=code" +
      "&scope=" + scopesEncode + "&access_type=" + accessType
    state match {
      case Some(s) => aUrl + "&state=" + URLEncoder.encode(s, "UTF-8")
      case None => aUrl
    }
  }

  val authPath: String = "auth"


  def route(implicit m: Materializer): Route = {

    import Directives._

    import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
    implicit val serialization = native.Serialization
    implicit val formats = DefaultFormats

    pathPrefix(authPath) {
      pathEndOrSingleSlash {
        get{
          log.debug("redirect to google oauth")
          redirect(authUrl(Some("some state")), StatusCodes.SeeOther)
        }
      }
    } ~
    pathPrefix("authCallback") {
      pathEndOrSingleSlash {
        get{
          parameter("code") (code => {
            log.debug(s"got code from google...http request to $tokenEndPoint")
            //val uri = "https://www.googleapis.com/oauth2/v4/token"
            val params: Map[String,String] = Map(
              "code" -> code,
              "client_id" -> clientId,
              "client_secret" -> clientSecret,
              "redirect_uri" -> redirectUri,
              "grant_type" -> "authorization_code"
            )
            val formData = FormData(params).toEntity
            val request = HttpRequest(method = HttpMethods.POST, uri = tokenEndPoint, entity = formData)
            val f = for {
              r <- Http().singleRequest(request)
              tr <- Unmarshal(r.entity).to[TokenResponse]
              //updated <- tokenDAO.get.put(toToken(tr)) if tokenDAO.isDefined
            } yield{
              callback.onAuthenticated(tr)
              tr
            }

            onComplete(f) {
              case Success(r) => {
                log.debug("successful authentication")
                callback.redirect match {
                  case None => complete("success")
                  case Some(r) => redirect(r, StatusCodes.PermanentRedirect)
                }
                //complete("success")
              }
              case Failure(e) => {
                log.error(e, "could not authenticate")
                complete(StatusCodes.InternalServerError)}
            }
          })
        }
      }
    }
  }



}
