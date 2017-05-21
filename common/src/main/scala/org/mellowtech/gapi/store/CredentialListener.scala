package org.mellowtech.gapi.store

import java.time.LocalDateTime

import com.google.api.client.auth.oauth2.{Credential, CredentialRefreshListener, TokenErrorResponse => JTokenErrorResponse, TokenResponse => JTokenResponse}
import org.slf4j.LoggerFactory

import scala.concurrent.Await

/**
  * @author msvens
  * @since 2017-05-15
  */
trait CredentialListener extends CredentialRefreshListener{

  import scala.concurrent.duration._

  private val logger = LoggerFactory.getLogger(getClass)

  def tokenDAO: TokenDAO


  override def onTokenResponse(credential: Credential, tokenResponse: JTokenResponse): Unit = {
    val ld = LocalDateTime.now().plusSeconds(credential.getExpiresInSeconds)
    val f = tokenDAO.update(TokenDAO.defaultUUID, credential.getAccessToken, ld)
    val updated = Await.result(f, 1 seconds)
    logger.debug("updated token: "+1)
  }

  override def onTokenErrorResponse(credential: Credential, tokenErrorResponse: JTokenErrorResponse): Unit = {
    logger.error("token error: "+tokenErrorResponse.getError)
  }
}
