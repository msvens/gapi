package org.mellowtech.gapi.example.local

import org.mellowtech.gapi.Installed
import org.mellowtech.gapi.config.GApiConfig
import org.mellowtech.gapi.drive.DriveService
import org.mellowtech.gapi.service.GApiException

import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}


/**
  * @author msvens
  * @since 2017-05-06
  */
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
