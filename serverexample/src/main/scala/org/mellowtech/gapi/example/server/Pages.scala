package org.mellowtech.gapi.example.server


import org.mellowtech.gapi.drive.model.FileList

import scalatags.Text.TypedTag
import scalatags.Text.all._

object Pages {

  val driveListing =
    html(
      head(

      ),
      body(
        h1("Try Drive Functions"),
        ul(
          li(a(href:="about","About")),
          li(a(href:="list", "List Files"))
        )
      )
    )

  def listFiles(fl: FileList) =
    html(
      head(),
      body(
        h1("Listing files in your root"),
        ul(
          for(f <- fl.files.get) yield {
            li(f.name.getOrElse("no name")+" ("+f.mimeType.getOrElse("no type")+")")
          }
        )

      )
    )
}
