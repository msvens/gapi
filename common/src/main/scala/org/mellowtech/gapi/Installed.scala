package org.mellowtech.gapi

import java.nio.file.{Path, Paths}
import java.util

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets}
import com.google.api.client.util.store.{DataStoreFactory, FileDataStoreFactory}
import org.mellowtech.gapi.GApiImplicits.{httpTransport, jsonFactory}
import org.mellowtech.gapi.config.GApiConfig

/**
  * @author msvens
  * @since 2017-07-09
  */
object Installed {

  val redirectUris: util.ArrayList[String] = {
    val l = new util.ArrayList[String]()
    l.add("urn:ietf:wg:oauth:2.0:oob")
    l.add("http://localhost")
    l
  }

  def storePath(c: GApiConfig): Path = Paths.get(System.getProperty("user.home"), ".store", c.applicationName)

  def storePath(p: String): Path = Paths.get(p)

  def fileStore(p: Path): DataStoreFactory = {
    System.out.println(p)
    new FileDataStoreFactory(p.toFile)
  }

  def fileStore(c: GApiConfig): DataStoreFactory = fileStore(storePath(c))

  def secrets(conf: GApiConfig): GoogleClientSecrets = {
    val secrets = new GoogleClientSecrets
    val installed = new GoogleClientSecrets.Details
    installed.setClientId(conf.clientIdInstalled.get)
    installed.setClientSecret(conf.clientSecretInstalled.get)
    installed.setAuthUri(conf.authUri.get)
    installed.setTokenUri(conf.tokenUri.get)
    installed.setRedirectUris(redirectUris)
    secrets.setInstalled(installed)
  }

  def flow(conf: GApiConfig, secrets: GoogleClientSecrets, store: DataStoreFactory): GoogleAuthorizationCodeFlow = {
    import scala.collection.JavaConverters._
    new GoogleAuthorizationCodeFlow.Builder(
      httpTransport, jsonFactory, secrets, conf.scopes.asJava
    ).setDataStoreFactory(store).build()
  }

  def flow(conf: GApiConfig): GoogleAuthorizationCodeFlow = flow(conf, secrets(conf), fileStore(conf))

  def credential(flow: GoogleAuthorizationCodeFlow): Credential =
    new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user")

  def credential(conf: GApiConfig): Credential = credential(flow(conf))



}
