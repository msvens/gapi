package org.mellowtech.gapi.drive.model

/**
  * @author msvens
  * @since 2017-05-09
  */
object TestModels extends App{

  import com.google.api.services.drive.model.File.ImageMediaMetadata.{Location => DLocation}
  import DriveConverters._

  val l = new DLocation();
  l.setAltitude(1.0)
  l.setLatitude(2.0)
  l.setLongitude(3.0)

  val conv = l.asScala

  println(conv)

}
