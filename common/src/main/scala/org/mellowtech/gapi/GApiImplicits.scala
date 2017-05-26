package org.mellowtech.gapi

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory

/**
  * @author msvens
  * @since 2017-05-25
  */
object GApiImplicits {

  implicit val httpTransport: HttpTransport = GoogleNetHttpTransport.newTrustedTransport()
  implicit val jsonFactory = JacksonFactory.getDefaultInstance


}
