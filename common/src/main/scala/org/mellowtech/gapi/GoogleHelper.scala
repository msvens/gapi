package org.mellowtech.gapi

import com.google.api.client.auth.oauth2.{BearerToken, ClientParametersAuthentication, Credential}
import com.google.api.client.http.{GenericUrl, HttpTransport}
import com.google.api.client.json.JsonFactory
import org.mellowtech.gapi.config.GApiConfig
import org.mellowtech.gapi.model.TokenResponse
import org.mellowtech.gapi.store.{CredentialListener, Token, TokenDAO}

/**
  * @author Martin Svensson
  * @since 2017-05-14
  */
object GoogleHelper{

  def credential(tr: TokenResponse, factory: Option[JsonFactory], transport: Option[HttpTransport],
                 listener: Option[CredentialListener])(implicit c: GApiConfig): Credential =
    credential(tr.access_token, tr.expires_in, tr.refresh_token, factory, transport, listener)

  def credential(t: Token, factory: Option[JsonFactory] = None, transport: Option[HttpTransport] = None,
                 listener: Option[CredentialListener] = None)(implicit c: GApiConfig): Credential =
    credential(t.access_token, TokenDAO.expiresInSecs(t.expires_in), t.refresh_token, factory, transport, listener)

  def credential(access_token: String,
                 expires_in: Long,
                 refresh_token: Option[String],
                 factory: Option[JsonFactory],
                 transport: Option[HttpTransport],
                 listener: Option[CredentialListener])(implicit c: GApiConfig): Credential = refresh_token match {
    case None => new Credential(BearerToken.authorizationHeaderAccessMethod())
      .setAccessToken(access_token)
      .setExpiresInSeconds(expires_in)
    case Some(r) => {
      val builder = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
        .setTransport(transport.get)
        .setJsonFactory(factory.get)
        .setTokenServerUrl(new GenericUrl(c.tokenUri.get))
        .setClientAuthentication(new ClientParametersAuthentication(c.clientId,c.clientSecret))
        if(listener.isDefined) builder.addRefreshListener(listener.get)
        builder.build().setAccessToken(access_token).setRefreshToken(r).setExpiresInSeconds(expires_in)
    }
  }


}
