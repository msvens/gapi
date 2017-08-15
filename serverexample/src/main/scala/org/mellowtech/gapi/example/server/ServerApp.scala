package org.mellowtech.gapi.example.server

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.concurrent.atomic.AtomicBoolean

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Directives, ExceptionHandler, Route}
import akka.stream.scaladsl.StreamConverters
import akka.stream.{ActorMaterializer, Materializer}
import com.google.api.client.auth.oauth2.Credential
import org.mellowtech.gapi.GoogleHelper
import org.mellowtech.gapi.config.GApiConfig
import org.mellowtech.gapi.drive.{Clause, DriveService}
import org.mellowtech.gapi.server.{DefaultAuthenticated, GoogleRouter}
import org.mellowtech.gapi.service.GApiException
import org.mellowtech.gapi.store.{CredentialListener, DbService, TokenDAO, TokenService}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class ServerCallback(val tokenService: TokenService) extends DefaultAuthenticated with CredentialListener {

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
  val tokenDAO: TokenService = TokenDAO(dbService)
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
    Http().bindAndHandle(authRoute ~ gAuth.route ~ defRoute, conf.httpHost.get, conf.httpPort.get)
  }

  def drive: DriveService = serverCallback.gdrive.get

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

  def defRoute(implicit m: Materializer): Route = {
    pathEndOrSingleSlash {
      toUtf(Pages.rootPage.render)
    } ~ extractUri(uri => {
      println("redirecting from: "+uri+" to root")
      redirect("/", StatusCodes.SeeOther)
    })
  }

  def authRoute(implicit m: Materializer): Route = handleExceptions(gApiExceptionHandler) {

    validate(serverCallback.hasDrive.get(), "no drive needs auth") {
      pathEndOrSingleSlash {
        get {
          toUtf(Pages.rootPage.render)
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
    import org.mellowtech.gapi.drive.Operators._
    pathPrefix("drive") {
      pathEndOrSingleSlash {
        get(toUtf(Pages.driveListing.render))
      } ~
      path("about") {
        val gdrive = serverCallback.gdrive.get
        val fa = gdrive.aboutAll
        onSuccess(fa){a => toUtf(Pages.about(a).render)}// {a => complete(a.getUser.getDisplayName)}
      } ~
      path("upload") {
        get{
          toUtf(Pages.uploadFile.render)
        } ~
        post {
          formFields('filename,'filecontent){(fn,fc) =>
            val gdrive = serverCallback.gdrive.get
            val ff = gdrive.upload(fc, fn, "text/plain")
            onSuccess(ff)(f => redirect("/google/drive/files/"+f.getId, StatusCodes.SeeOther))
            //onSuccess(ff)(f => complete("created file: "+f.id))

          }
        }
      } ~
      path("list") {
        parameter('next.?, 'parent.?) {(next,parent) =>
          val gdrive = serverCallback.gdrive.get
          println("list next 10 hits: "+next+parent)
          val fa = parent match {
            case Some(n) => for{
              l <- gdrive.list(Some(Clause(parents in parent.get)), Some(10), None, next, None)
            } yield(Pages.listFiles(l, parent.get))
            case None => for {
              r <- gdrive.root
              l <- gdrive.list(Some(Clause(parents in r.getId)), Some(10), None, None, None)
            } yield (Pages.listFiles(l,r.getId))
          }
          onSuccess(fa)(a => toUtf(a.render))
        }
      } ~
      path("files" / Segment){id => {
        val gdrive = serverCallback.gdrive.get
        val ff = gdrive.file(id)
        onSuccess(ff)(f => toUtf(Pages.file(f).render))
      }} ~
      path("files" / Segment / "raw"){ id => {
        val gdrive = drive
        val fs = gdrive.download(id)
        onSuccess(fs)(complete(_))
      }} ~
      path("files" / Segment / "export"){ id => {
       parameter('type){tp =>
         val bout = new ByteArrayOutputStream()
         getFromFile("helloworld.txt")
         val fe = drive.export(bout,tp,id)
         getFromFile("helloworld.txt")
         onSuccess(fe){
           val arr = bout.toByteArray
           val ct: ContentType = ContentType(MediaTypes.`application/pdf`)
           complete{
             HttpEntity.Default(ct, arr.length,
               StreamConverters.fromInputStream(() => new ByteArrayInputStream(arr)))
           }

           //complete(id+" "+tp)
         }
         /*
         complete {
                    HttpEntity.Default(contentType, length,
                      StreamConverters.fromInputStream(() ⇒ url.openStream())
                        .withAttributes(ActorAttributes.dispatcher(settings.fileIODispatcher))) // TODO is this needed? It already uses `val inputStreamSource = name("inputStreamSource") and IODispatcher`
                  }
          */
         /*import akka.http.scaladsl.server.directives._
         import ContentTypeResolver.Default
         getFromFile("helloworld.txt")
         //complete(id+" " + t)
         */
       }
      }}
    }
  }

  def toUtf(t: String): Route = {
    complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, t))
  }


}
