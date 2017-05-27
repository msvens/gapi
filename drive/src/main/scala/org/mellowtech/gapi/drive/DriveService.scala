package org.mellowtech.gapi.drive


import com.google.api.client.auth.oauth2.Credential
import com.google.api.services.drive.Drive
import org.mellowtech.gapi.config.GApiConfig
import org.mellowtech.gapi.drive.model.{About, File, FileList}
import org.mellowtech.gapi.service.GService

import scala.concurrent.{ExecutionContext, Future}

/** Scala Wrapper for the Google Drive API
  *
  * DriveService simplifies usage of the Google Drive API for scala developers. It contains methods
  * for common Drive operations as well as giving the developer the option to work directly against
  * the underlying Drive API.
  *
  * @see <a href="https://developers.google.com/apis-explorer/#p/drive/v3/">Drive API Explorer</a>
  * @see <a href="https://developers.google.com/resources/api-libraries/documentation/drive/v3/java/latest/">Drive Javadoc</a>
  *
  * @param credential
  * @param ec
  */
class DriveService(val credential: Credential)(implicit val ec: ExecutionContext, c: GApiConfig) extends GService[Drive]{

  import org.mellowtech.gapi.drive.model.DriveConverters._
  import Operators._
  import org.mellowtech.gapi.GApiImplicits._

  val drive: Drive = new Drive.Builder(httpTransport, jsonFactory,
    credential).setApplicationName(c.applicationName).build()

  val service = drive

  def aboutAll: Future[About] = {
    about(AboutFields.aboutFields: _*)
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



  val createFolder: (String,Seq[String]) => Future[File] = createFile(DriveService.GFOLDER)
  val createDocument: (String,Seq[String]) => Future[File] = createFile(DriveService.GDOCUMENT)
  val createSheet: (String,Seq[String]) => Future[File] = createFile(DriveService.GSHEET)
  val createPresentation: (String,Seq[String]) => Future[File] = createFile(DriveService.GPRESENTATION)


  def createFile(mimeType: String)(name: String, parentIds: Seq[String]): Future[File] = execA{
    val f = toGoogleFile(name, parentIds, mimeType)
    val cf = drive.files.create(f).execute()
    cf.asScala
  }


  def list(parent: File): Future[FileList] = list(parent.id.get)

  def list(parentId: String): Future[FileList] = list(Clause(parents in parentId))

  def list(q: Clause): Future[FileList] = listOf(fl => fl.setQ(q.render))

  /** list files
    *
    * Control your file listing using some common options
    * @param q
    * @param pageSize
    * @param orderBy
    * @param pageToken
    * @param fileFields
    * @return
    */
  def list(q: Option[Clause], pageSize: Option[Int], orderBy: Option[Seq[String]], pageToken: Option[String],
          fileFields: Option[Seq[String]]) = listOf(l => {
    var fields = ""
    if(q.isDefined) l.setQ(q.get.render)
    if(pageSize.isDefined) {
      fields = "nextPageToken"
      l.setPageSize(pageSize.get)
    }
    if(orderBy.isDefined) l.setOrderBy(orderBy.get.mkString(","))
    if(pageToken.isDefined) l.setPageToken(pageToken.get)
    if(fileFields.isDefined){
      if(pageSize.isDefined)
        fields += ",files("+fileFields.get.mkString(",")+")"
      else
        fields += "files("+fileFields.get.mkString(",")+")"
    }

  })

  /** list files
    *
    * This method offers the most flexibility and allows you to
    * configure the Drive.Files.List object directly. For an in-depth description of how
    * you search and list files see <a href="https://developers.google.com/drive/v3/web/search-parameters">search for files</a>
    *
    * @param f function to config the file listing
    * @return A Future to a FileList
    */
  def listOf(f: Drive#Files#List => Unit): Future[FileList] = {
    val fl = drive.files().list()
    f(fl)
    execA(fl.execute().asScala)
  }



}

object DriveService{

  val GFOLDER = "application/vnd.google-apps.folder"
  val GSHEET = "application/vnd.google-apps.spreadsheet"
  val GDOCUMENT = "application/vnd.google-apps.document"
  val GPRESENTATION = "application/vnd.google-apps.presentation"


  def apply(credential: Credential)(implicit ec: ExecutionContext, c: GApiConfig): DriveService = new DriveService(credential)

}
