package org.mellowtech.gapi.example.server



import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query
import com.google.api.services.drive.model.{File, FileList}
import org.mellowtech.gapi.drive.DriveService

import scalatags.Text.all._
import scala.collection.JavaConverters._

object Pages {

  def toUri(uri: String, query: (String,String)*): String = Uri("/google/drive/list").withQuery(Query(query.toMap)).toString


  val rootPage =
    html(
      head(),
      h1("Welcome to the goole services test app"),
      ul(
        li(a(href:="/google", "Test Google Services")),
        li(a(href:="/auth", "Authenticate with Google"))
      )
    )

  def g(p: String): String = s"google $p"
  val googleListing =
    html(
      head(),
      h1("Google services"),
      ul(
        li(a(href:="google/drive", "Drive")),
        li(a(href:="google/sheets", "Sheets")),
        li(a(href:="google/plus", "Plus"))
      )
    )

  val driveListing =
    html(
      head(),
      body(
        h1("Try Drive Functions"),
        ul(
          li(a(href:="/google/drive/about","About")),
          li(a(href:="/google/drive/upload","Upload File")),
          li(a(href:="/google/drive/list", "List Files"))
        )
      )
    )

  val uploadFile =
    html(
      head(),
      body(
        h1("Fill in the form to create your new text file"),
        form(action:="/google/drive/upload", method:="post",
          "File text",br,
          input(`type`:="text",name:="filename",value:="<name file>"),br,br,
          input(`type`:="text",name:="filecontent",value:="<file content>"),br,br,
          input(`type`:="submit", value:="create")
        )
      )
    )

  def file(f: File) = {
    html(
      head(),
      body(
        h2(f.getName),
        ul(
          li("mime type: "+f.getMimeType),
          li(""+f.getCreatedTime),
          li("is folder: "+DriveService.isFolder(f)),
          if(f.getMimeType.startsWith("text/")) li(a(href:="/google/drive/files/"+f.getId+"/raw","raw view")) else (),
          if(f.getWebContentLink != null) li(a(href:=f.getWebContentLink, "web content link")) else (),
          if(f.getWebViewLink != null) li(a(href:=f.getWebViewLink, "web view link")) else ()
        )
      )
    )
  }

  def listFiles(fl: FileList, id: String) = {
    html(
      head(),
      body(
        h1("Listing Files" +
          ""),
        ul(
          for (f <- fl.getFiles.asScala) yield {
            val id = f.getId
            if (DriveService.isFolder(f)) {
              li(a(href := "/google/drive/list?parent=" + id, f.getName + " (folder)"))
            } else {
              li(a(href := "/google/drive/files/" + id, f.getName))
            }
          }
        ),
        if (fl.getNextPageToken != null) {
          a(href := toUri("/google/drive/list", ("next", fl.getNextPageToken), ("parent", id)), "Next 10 hits")
        }
        else
          a(href := "/google/drive/list", "Start over")

      )
    )
  }
}
