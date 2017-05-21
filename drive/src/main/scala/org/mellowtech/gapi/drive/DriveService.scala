package org.mellowtech.gapi.drive


import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import org.mellowtech.gapi.drive.model.{About, File, FileList}
import org.mellowtech.gapi.service.GService

import scala.concurrent.{ExecutionContext, Future}

class DriveService(val credential: Credential, applicationName: String,
                   val jsonFactory: JacksonFactory,
                   val httpTransport: HttpTransport)(implicit val ec: ExecutionContext) extends GService[Drive] {

  import org.mellowtech.gapi.drive.model.DriveConverters._

  val drive: Drive = new Drive.Builder(httpTransport, jsonFactory,
    credential).setApplicationName(applicationName).build()

  val service = drive

  def aboutAll: Future[About] = {
    val f = Seq("appInstalled", "exportFormats","folderColorPalette","importFormats","kind",
      "maxImportSizes","maxUploadSize","storageQuota","teamDriveThemes","user")
    about(f:_*)
  }
  def about(fields: String*): Future[About] = {
    execA[About] {
      val exec = drive.about.get.setFields(fields.mkString(","))
      exec.execute().asScala
    }
  }

  def root: Future[File] = execA[File]{
    val f = drive.files().get("root").execute()
    f.asScala
  }

  def createFile(name: String, parentIds: Seq[String], mimeType: String): Future[File] = execA{
    val f = toGoogleFile(name, parentIds, mimeType)
    val cf = drive.files.create(f).execute()
    cf.asScala
  }

  def list(parent: File): Future[FileList] = list(parent.id.get)

  def list(parentId: String): Future[FileList] = execA{
    val f = drive.files().list().setQ(s"'$parentId' in parents").execute()
    f.asScala
  }

}

object DriveService{

  val FOLDER_TYPE = "application/vnd.google-apps.folder"
  val SHEET_TYPE = "application/vnd.google-apps.spreadsheet"
  val DOC_TYPE = "application/vnd.google-apps.document"



  def apply(credential: Credential, applicationName: String)
           (implicit jsonFactory: JacksonFactory, httpTransport: HttpTransport, ec: ExecutionContext): DriveService = {

    new DriveService(credential, applicationName, jsonFactory, httpTransport)

  }
}
