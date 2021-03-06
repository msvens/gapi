package org.mellowtech.gapi.server

import java.net.URLEncoder
import java.util.concurrent.atomic.AtomicBoolean

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import org.mellowtech.gapi.config.GApiConfig
import org.mellowtech.gapi.model.TokenResponse
import org.mellowtech.gapi.store.{TokenDAO, TokenService}
import spray.json._

import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success, Try}


trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val tokenResponseFormat = jsonFormat4(TokenResponse)
}

/**
  * @author msvens
  * @since 2017-05-12
  */

trait GoogleAuthenticated {

  def onAuthenticated(tr: TokenResponse): Unit

  def redirect: Option[String] = None
}

trait DefaultAuthenticated extends GoogleAuthenticated {

  import scala.concurrent.duration._

  def tokenService: TokenService

  override def onAuthenticated(tr: TokenResponse): Unit = {
    val t = TokenDAO.toToken(tr)
    val r = Await.result(tokenService.put(t), 1 second)
    r match {
      case x if x > 0 => authenticated.set(true)
      case _ => authenticated.set(false)
    }
  }

  override val redirect: Option[String] = Some("/")

  val authenticated: AtomicBoolean = new AtomicBoolean(false)
}

class GoogleRouter(val callback: GoogleAuthenticated)(implicit val actorSystem: ActorSystem,
                                                      implicit val m: Materializer,
                                                      implicit val executor: ExecutionContext,
                                                      implicit val c: GApiConfig) extends JsonSupport {

  implicit val log: LoggingAdapter = Logging(actorSystem, getClass)

  import Directives._

  private def authUrl(state: Option[String]): String = {
    val rUriEncode = URLEncoder.encode(c.redirectUri.get, "UTF-8")
    val scopesEncode = URLEncoder.encode(c.scopes.mkString(" "), "UTF-8")
    val aUrl = c.authUri.get + "?client_id=" + c.clientId + "&redirect_uri=" + rUriEncode + "&response_type=code" +
      "&scope=" + scopesEncode + "&access_type=" + c.accessType.get
    state match {
      case Some(s) => aUrl + "&state=" + URLEncoder.encode(s, "UTF-8")
      case None => aUrl
    }
  }

  val authRoute: Route =
    path(c.authPath.get) {
      get{
        log.debug("redirect to google oauth")
        redirect(authUrl(Some("some state")), StatusCodes.SeeOther)
      }
    }

  val authCallbackRoute: Route =
    path(c.authCallbackPath.get) {
      get {
        parameter("code")(code => {
          log.debug(s"got code from google...http request to ${c.tokenUri.get}")
          val params: Map[String, String] = Map(
            "code" -> code,
            "client_id" -> c.clientId,
            "client_secret" -> c.clientSecret,
            "redirect_uri" -> c.redirectUri.get,
            "grant_type" -> "authorization_code"
          )
          val formData = FormData(params).toEntity
          val request = HttpRequest(method = HttpMethods.POST, uri = c.tokenUri.get, entity = formData)
          val f = for {
            r <- Http().singleRequest(request)
            tr <- Unmarshal(r.entity).to[TokenResponse]
          } yield {
            callback.onAuthenticated(tr)
            tr
          }
          onComplete(f)(onAuthCompleted(_))
        })
      }
    }

  val route: Route = authRoute ~ authCallbackRoute

  def onAuthCompleted(tr: Try[TokenResponse]): Route = tr match {
    case Success(r) =>
      log.debug("successful authentication")
      callback.redirect match {
        case None => complete("success")
        case Some(r) => redirect(r, StatusCodes.PermanentRedirect)
      }
    case Failure(e) =>
      log.error(e, "could not authenticate")
      throw e
  }

}
