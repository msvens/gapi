package org.mellowtech.gapi.example.server

import java.util.concurrent.atomic.AtomicBoolean

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directives, ExceptionHandler, Route}
import akka.stream.{ActorMaterializer, Materializer}
import com.google.api.client.auth.oauth2.Credential
import org.mellowtech.gapi.config.GApiConfig
import org.mellowtech.gapi.drive.DriveService
import org.mellowtech.gapi.server.{DefaultAuthenticated, GoogleHelper, GoogleRouter}
import org.mellowtech.gapi.service.GApiException
import org.mellowtech.gapi.store.{CredentialListener, DbService, TokenDAO}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class ServerCallback(val tokenDAO: TokenDAO)(implicit val ec: ExecutionContext) extends DefaultAuthenticated with CredentialListener {

  val hasDrive: AtomicBoolean = new AtomicBoolean(false)
  var gdrive: Option[DriveService] = None


}

object ServerApp extends GApiConfig {

  import Directives._
  import org.mellowtech.gapi.GApiImplicits._

  implicit val actorSystem = ActorSystem()
  implicit val executor: ExecutionContext = actorSystem.dispatcher
  implicit val log: LoggingAdapter = Logging(actorSystem, getClass)
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val dbService = new DbService
  val tokenDAO = new TokenDAO(dbService)
  val serverCallback = new ServerCallback(tokenDAO)
  val gAuth = new GoogleRouter(serverCallback)
  val gApiExceptionHandler = ExceptionHandler {
    case x: GApiException =>
      extractUri { uri =>
        log.error(s"Request to $uri could not be handled normally")
        complete(HttpResponse(StatusCodes.InternalServerError, entity = "" + x.jsonError.getOrElse("no json error")))
      }
    case z =>
      log.error(z, "exception")
      complete(StatusCodes.InternalServerError)
  }

  def main(args: Array[String]): Unit = {

    initGoogleServices
    Http().bindAndHandle(route ~ gAuth.route, httpHost, httpPort)
  }

  def initGoogleServices: Unit = {
    //First try to create a drive-service if we already have credentials
    val f: Future[Option[Credential]] = for {
      opt <- tokenDAO.getDefault
    } yield opt match {
      case Some(t) => Some(GoogleHelper.credential(t, Some(jsonFactory), Some(httpTransport), Some(serverCallback)))
      case None => None
    }
    val cred = Await.result(f, 1 seconds)
    cred match {
      case Some(c) => {
        serverCallback.gdrive = Some(DriveService(c))
        serverCallback.hasDrive.set(true)
      }
      case None => serverCallback.gdrive = None
    }
  }

  def route(implicit m: Materializer): Route = handleExceptions(gApiExceptionHandler) {

    validate(serverCallback.hasDrive.get(), "no drive needs auth") {

      pathEndOrSingleSlash {
        get {
          complete("root worked")
        }
      } ~
        pathPrefix("google") {
          log.debug("in the google branch")
          pathEndOrSingleSlash {
            get(toUtf(Pages.googleListing.render))
          } ~ driveRoute
        }
    }
  }

  def driveRoute(implicit m: Materializer): Route = handleExceptions(gApiExceptionHandler) {
    //import Directives._
    pathPrefix("drive") {
      pathEndOrSingleSlash {
        get(toUtf(Pages.driveListing.render))
      } ~
      path("about") {
        val gdrive = serverCallback.gdrive.get
        val fa = gdrive.aboutAll
        onSuccess(fa) {a => complete(a.user.get.displayName)}
      } ~
      path("list") {
        val gdrive = serverCallback.gdrive.get
        val fa = for {
          r <- gdrive.root
          l <- gdrive.list(r)
        } yield (l)
        onSuccess(fa) (a => toUtf(Pages.listFiles(a).render))
      } ~
      path("file") {
        parameter("name")(name => {
          val gdrive = serverCallback.gdrive.get
          val fa = gdrive.createDocument(name, Nil)
          onSuccess(fa) (a => complete("file created "+a.name))
        })
      }
    }
  }

  def toUtf(t: String): Route = {
    complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, t))
  }


}
