package org.mellowtech.gapi.drive


import java.io.{ByteArrayOutputStream, InputStream, OutputStream}
import java.nio.charset.Charset
import java.nio.file.Path

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.http.{ByteArrayContent, FileContent, InputStreamContent}
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.{About, File, FileList}
import org.mellowtech.gapi.config.GApiConfig
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
  * @param credential
  * @param ec
  */
class DriveService(val credential: Credential)(implicit val ec: ExecutionContext, c: GApiConfig) extends GService[Drive] {

  import AboutField._
  import FileListField.FileListField
  import Operators._
  import org.mellowtech.gapi.GApiImplicits._

  val drive: Drive = new Drive.Builder(httpTransport, jsonFactory,
    credential).setApplicationName(c.applicationName).build()

  val service = drive
  val createFolder: (String, Seq[String]) => Future[File] = create(DriveService.GFOLDER)
  val createDocument: (String, Seq[String]) => Future[File] = create(DriveService.GDOCUMENT)
  val createSheet: (String, Seq[String]) => Future[File] = create(DriveService.GSHEET)
  val createPresentation: (String, Seq[String]) => Future[File] = create(DriveService.GPRESENTATION)

  def aboutAll: Future[About] = {
    about(AboutField.values.toSeq: _*)
  }

  def about(fields: AboutField*): Future[About] = {
    execA[About] {
      val exec = drive.about.get.setFields(fields.mkString(","))
      exec.execute()
    }
  }

  def file(id: String, all: Boolean = true): Future[File] = execA[File] {
    val fields: String = all match {
      case true => {
        FileField.allFields
      }
      case false => ""
    }
    drive.files().get(id).setFields(fields).execute()
  }

  def root: Future[File] = execA[File] {
    val f = drive.files().get("root").execute()
    f
  }

  def create(mimeType: String)(name: String, parentIds: Seq[String]): Future[File] = execA {
    val f = DriveService.toGoogleFile(name, parentIds, mimeType)
    val cf = drive.files.create(f).execute()
    cf
  }


  def list(parent: File): Future[FileList] = list(parent.getId)

  def list(parentId: String): Future[FileList] = list(Clause(parents in parentId))

  def list(q: Clause): Future[FileList] = listOf(fl => fl.setQ(q.render))

  /** list files
    *
    * Control your file listing using some common options
    *
    * @param q
    * @param pageSize
    * @param orderBy
    * @param pageToken
    * @param fileFields
    * @return
    */
  def list(q: Option[Clause], pageSize: Option[Int], orderBy: Option[Seq[String]], pageToken: Option[String],
           fileFields: Option[Seq[FileListField]]) = listOf(l => {
    var fields = ""
    if (q.isDefined) l.setQ(q.get.render)
    if (pageSize.isDefined) {
      //println("nextPageToken = " + FileListField.nextPageToken.toString)
      //fields = FileListField.nextPageToken.toString //"nextPageToken"
      fields = "nextPageToken"
      l.setPageSize(pageSize.get)
    }
    if (orderBy.isDefined) l.setOrderBy(orderBy.get.mkString(","))
    if (pageToken.isDefined) l.setPageToken(pageToken.get)
    if (fileFields.isDefined) {
      if (pageSize.isDefined)
        fields += ",files(" + fileFields.get.mkString(",") + ")"
      else
        fields += "files(" + fileFields.get.mkString(",") + ")"
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
    execA(fl.execute())
  }

  def export(to: OutputStream, mimeType: String, id: String): Future[Unit] = execU {
    drive.files().export(id, mimeType).executeMediaAndDownloadTo(to)
  }

  def download(id: String, codec: String = "UTF-8"): Future[String] = execA {
    val barr = new ByteArrayOutputStream
    val d = drive.files().get(id)
    d.executeMediaAndDownloadTo(barr)
    new String(barr.toByteArray, Charset.forName(codec))
  }

  def download(to: OutputStream, id: String, range: Some[(Int, Int)]): Future[Unit] = execU {
    val d = drive.files().get(id)
    if (range.isDefined)
      d.getRequestHeaders.setRange("bytes=" + range.get._1 + "-" + range.get._2)
    d.executeMediaAndDownloadTo(to)
  }

  def upload[T](content: T, name: String, mimeType: String, parentId: Option[String] = None,
                convertTo: Option[String] = None): Future[File] = {

    import com.google.api.services.drive.model.{File => GFile}

    val mc = content match {
      case x: Path => new FileContent(mimeType, x.toFile)
      case x: java.io.File => new FileContent(mimeType, x)
      case x: Array[Byte] => new ByteArrayContent(mimeType, x)
      case x: InputStream => new InputStreamContent(mimeType, x)
      case x => {
        val b = x.toString.getBytes("UTF-8")
        new ByteArrayContent(mimeType, b)
      }
    }
    val file = new GFile
    file.setName(name)
    if (parentId.isDefined)
      file.setParents(java.util.Collections.singletonList(parentId.get))
    if (convertTo.isDefined)
      file.setMimeType(convertTo.get)
    val create = drive.files().create(file, mc).setFields("id")
    execA(create.execute())
  }

}

object DriveService {

  val GFOLDER = "application/vnd.google-apps.folder"
  val GSHEET = "application/vnd.google-apps.spreadsheet"
  val GDOCUMENT = "application/vnd.google-apps.document"
  val GPRESENTATION = "application/vnd.google-apps.presentation"

  def isFolder(f: File): Boolean = Option(f.getMimeType) match {
    case Some(m) => m.equals(GFOLDER)
    case None => false
  }

  import scala.collection.JavaConverters._

  def toGoogleFile(name: String, parentIds: Seq[String], mimeType: String): File = {
    val f = new File()
    f.setName(name)
    f.setParents(parentIds.asJava)
    f.setMimeType(mimeType)
    f
  }

  def apply(credential: Credential)(implicit ec: ExecutionContext, c: GApiConfig): DriveService = new DriveService(credential)

}
