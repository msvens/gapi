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
import org.mellowtech.gapi.drive.{Clause, DriveService}
import org.mellowtech.gapi.server.{DefaultAuthenticated, GoogleHelper, GoogleRouter}
import org.mellowtech.gapi.service.GApiException
import org.mellowtech.gapi.store.{CredentialListener, DbService, TokenDAO}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class ServerCallback(val tokenDAO: TokenDAO)(implicit val ec: ExecutionContext) extends DefaultAuthenticated with CredentialListener {

  val hasDrive: AtomicBoolean = new AtomicBoolean(false)
  var gdrive: Option[DriveService] = None


}

object ServerApp{

  import Directives._
  import org.mellowtech.gapi.GApiImplicits._

  implicit val actorSystem = ActorSystem()
  implicit val executor: ExecutionContext = actorSystem.dispatcher
  implicit val log: LoggingAdapter = Logging(actorSystem, getClass)
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val conf: GApiConfig = GApiConfig()

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
    Http().bindAndHandle(route ~ gAuth.route, conf.httpHost.get, conf.httpPort.get)
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
    import org.mellowtech.gapi.drive.Operators._
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
        parameter('next.?, 'parent.?) {(next,parent) =>
          val gdrive = serverCallback.gdrive.get
          val fa = next match {
            case Some(n) => for{
              l <- gdrive.list(Some(Clause(parents in parent.get)), Some(10), None, next, None)
            } yield(Pages.listFiles(l, parent.get))
            case None => for {
              r <- gdrive.root
              l <- gdrive.list(Some(Clause(parents in r.id.get)), Some(10), None, None, None)
            } yield (Pages.listFiles(l,r.id.get))
          }
          onSuccess(fa)(a => toUtf(a.render))
        }
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
