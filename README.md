# GAPI #


[![Build Status](https://travis-ci.org/msvens/gapi.svg?branch=master)](https://travis-ci.org/msvens/gapi)
[![Maven Central](https://img.shields.io/maven-central/v/org.mellowtech/gapi_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/org.mellowtech/gapi_2.12)


## Overview

Scala wrapper for Google APIs - specifically drive. Focus is on making it simple to do
common tasks such as listing files, creating folders, etc while still offering full access
to the underlying Google APIs.

GAPI also simplifies the auth process for installed applications and akka-http based servers

### Installation

TBW

### Configuration

TBW

### Authenticate from a server

Check out the full source code in [ServerApp.scala](serverexample/src/main/scala/org/mellowtech/gapi/example/server/ServerApp.scala)

The following description assumes familiarity with [akka-http](http://doc.akka.io/docs/akka-http/current/scala/http/index.html)

First we create a an object that will serve as our example akka server.

```scala
object ServerApp {

  import Directives._
  import org.mellowtech.gapi.GApiImplicits._

  implicit val actorSystem = ActorSystem()
  implicit val executor: ExecutionContext = actorSystem.dispatcher
  implicit val log: LoggingAdapter = Logging(actorSystem, getClass)
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val conf: GApiConfig = GApiConfig()
}
```

Next we instantiate our database service. This is needed to store tokens. GApi uses slick so you need to provide a
slick configuration (see above).

```scala
val dbService = new DbService
val tokenDAO: TokenService = TokenDAO(dbService)
```

Next we setup our router for google as well as the callback to store the token in our Db. 

```scala
class ServerCallback(val tokenService: TokenService) extends DefaultAuthenticated with CredentialListener {
  val hasDrive: AtomicBoolean = new AtomicBoolean(false)
  var gdrive: Option[DriveService] = None
}

val serverCallback = new ServerCallback(tokenDAO)
val gAuth = new GoogleRouter(serverCallback)
```

Although not strictly needed it can be a good idea to setup your own exception handler for your akka routes in case 
you want to handle any GAPI Exception separately. This can be done like so

```scala
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
```

Next we need to setup an initialise the specific google service we want to use (in this case drive).

```scala
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
```

The final thing is to create a main method and bind our routes to our akka http server

```scala
  def main(args: Array[String]): Unit = {
    initGoogleServices
    Http().bindAndHandle(authRoute ~ gAuth.route ~ defRoute, conf.httpHost.get, conf.httpPort.get)
  }
```

For the actual routes please check the example source code [ServerApp.scala](serverexample/src/main/scala/org/mellowtech/gapi/example/server/ServerApp.scala)

### Authenticate from an installed application

Authentication from an installed application is simpler since we can rely on the APIs provided by google.

You can use the helper [Installed.scala](common/src/main/scala/org/mellowtech/gapi/Installed.scala) to get away
with almost all boiler plate. To create an application that lists your google drive root you can do this:

```scala
object LocalApp extends App{

    implicit val conf: GApiConfig = GApiConfig()
    implicit val ec = ExecutionContext.global

    import scala.concurrent.duration._
    import scala.collection.JavaConverters._

    val ds = DriveService(Installed.credential(conf))

    val rootFolder = for{
      f <- ds.root
      fl <- ds.list(f)
    } yield fl

  Await.ready(rootFolder, 10 seconds).value.get match {
    case Success(fl) => {
      val nl = fl.getFiles.asScala.map(_.getName)
      println(nl.mkString("\n"))
    }
    case Failure(e) => e match {
      case x: GApiException => println(x.jsonError)
    }
  }
}
```

### Use Google Drive API



## TODO

## Version History





