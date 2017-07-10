package org.mellowtech.gapi.cli


import java.util

import com.google.api.client.auth.oauth2.{Credential, CredentialRefreshListener}
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets}
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import org.mellowtech.gapi.GApiImplicits
import org.mellowtech.gapi.config.GApiConfig
import org.mellowtech.gapi.drive.DriveService
import org.mellowtech.gapi.service.GApiException
import org.mellowtech.gapi.store.{CredentialListener, TokenService}

import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}


/**
  * @author msvens
  * @since 2017-05-06
  */
object LocalAuth{


  def main(args: Array[String]): Unit = {

    implicit val conf: GApiConfig = GApiConfig()
    import org.mellowtech.gapi.GApiImplicits._
    implicit val ec = ExecutionContext.global

    import scala.collection.JavaConverters._

    val secrets = new GoogleClientSecrets

    val DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".store/brfapp")

    val redirectUris = new util.ArrayList[String]()
    redirectUris.add("urn:ietf:wg:oauth:2.0:oob")
    redirectUris.add("http://localhost")

    val dataStoreFactory: FileDataStoreFactory =
    new FileDataStoreFactory(DATA_STORE_DIR)


    val installed = new GoogleClientSecrets.Details

    installed.setClientId(conf.clientIdInstalled.get)
    installed.setClientSecret(conf.clientSecretInstalled.get)
    installed.setAuthUri(conf.authUri.get)
    installed.setTokenUri(conf.tokenUri.get)
    installed.setRedirectUris(redirectUris)

    secrets.setInstalled(installed)


    val flow = new GoogleAuthorizationCodeFlow.Builder(
      httpTransport, jsonFactory, secrets, conf.scopes.asJava
    ).setDataStoreFactory(dataStoreFactory).build()


    val c = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user")

    val ds = DriveService(c)

  }

  //installed.setClientId(conf.clie)




  /*
  import scala.concurrent.duration._

  implicit val ec = ExecutionContext.global

  val APPLICATION_NAME = "Mellowtech Houser/1.0"

  val DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".store/houser_store")


  private val dataStoreFactory: FileDataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR)
  implicit val httpTransport: HttpTransport = GoogleNetHttpTransport.newTrustedTransport()
  implicit val jsonFactory = JacksonFactory.getDefaultInstance


  private def authorise: Option[Credential] = {

    clientIdJsonReader match {
      case Success(r) => {
        val secrets = GoogleClientSecrets.load(jsonFactory, r)
        val flow = new GoogleAuthorizationCodeFlow.Builder(
          httpTransport, jsonFactory, secrets, scopes)
          .setDataStoreFactory(dataStoreFactory)
          .build()
        Some(new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user"))
      }
      case Failure(e) => {
        e.printStackTrace()
        None
      }
    }
  }

  try {

    val credential = authorise.get

    val service = DriveService(credential, APPLICATION_NAME)

    val cf = service.createFile("testfile-20170512", Nil, DriveService.DOC_TYPE)

    Await.ready(cf, 10 seconds).value.get match {
      case Success(cf) => println(cf)
      case Failure(e) => e match {
        case x: GApiException => println(x.jsonError)
      }
    }


    // set up the global Drive instance
    //drive = new Drive.Builder(httpTransport, JSON_FACTORY,
    //  credential).setApplicationName(APPLICATION_NAME).build()

    System.exit(0)


  } catch {
    case e: Exception => e.printStackTrace()
  }
  */



}
