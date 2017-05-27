package org.mellowtech.gapi.example.server


import org.mellowtech.gapi.drive.model.FileList

import scalatags.Text.TypedTag
import scalatags.Text.all._

object Pages {

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
      head(

      ),
      body(
        h1("Try Drive Functions"),
        ul(
          li(a(href:="/google/drive/about","About")),
          li(a(href:="/google/drive/list", "List Files"))
        )
      )
    )

  def listFiles(fl: FileList, id: String) =
    html(
      head(),
      body(
        h1("Listing Files"),
        ul(
          for(f <- fl.files.get) yield {
            li(f.name.getOrElse("no name")+" ("+f.mimeType.getOrElse("no type")+")")
          }
        ),
        if(fl.nextPageToken.isDefined)
          a(href:="/google/drive/list?next="+fl.nextPageToken.get+"&parent="+id, "Next 10 hits")
        else
          a(href:="/google/drive/list", "Start over")

      )
    )
}
