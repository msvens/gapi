package org.mellowtech.gapi.server

import javax.swing.CellRendererPane

import com.google.api.client.json.JsonFactory
import com.google.api.client.auth.oauth2.{BearerToken, ClientParametersAuthentication, Credential}
import com.google.api.client.http.{GenericUrl, HttpTransport}
import org.mellowtech.gapi.config.GApiConfig
import org.mellowtech.gapi.model.{Converters, TokenResponse}
import org.mellowtech.gapi.store.{CredentialListener, Token, TokenDAO}

/**
  * @author Martin Svensson
  * @since 2017-05-14
  */
object GoogleHelper extends GApiConfig{

  def credential(tr: TokenResponse, factory: Option[JsonFactory], transport: Option[HttpTransport],
                 listener: Option[CredentialListener]): Credential =
    credential(tr.access_token, tr.expires_in, tr.refresh_token, factory, transport, listener)
  /*tr.refresh_token match {
    case Some(_) => {
      new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
        .setTransport(transport.get)
      .setJsonFactory(factory.get)
      .setTokenServerUrl(new GenericUrl(tokenEndPoint))
      .build().setFromTokenResponse(Converters.toJava(tr))
    }
    case None => new Credential(BearerToken.authorizationHeaderAccessMethod()).setFromTokenResponse(Converters.toJava(tr))
  }*/

  def credential(t: Token, factory: Option[JsonFactory] = None, transport: Option[HttpTransport] = None,
                 listener: Option[CredentialListener] = None): Credential =
    credential(t.access_token, TokenDAO.expiresInSecs(t.expires_in), t.refresh_token, factory, transport, listener)

  def credential(access_token: String, expires_in: Long, refresh_token: Option[String],
                 factory: Option[JsonFactory], transport: Option[HttpTransport], listener: Option[CredentialListener]): Credential = refresh_token match {
    case None => new Credential(BearerToken.authorizationHeaderAccessMethod())
      .setAccessToken(access_token)
      .setExpiresInSeconds(expires_in)
    case Some(r) => {
      val builder = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
        .setTransport(transport.get)
        .setJsonFactory(factory.get)
        .setTokenServerUrl(new GenericUrl(tokenEndPoint))
        .setClientAuthentication(new ClientParametersAuthentication(clientId,clientSecret))
        if(listener.isDefined) builder.addRefreshListener(listener.get)
        builder.build().setAccessToken(access_token).setRefreshToken(r).setExpiresInSeconds(expires_in)
    }
  }


}
