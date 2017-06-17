package org.mellowtech.gapi.drive

/**
  * @author msvens
  * @since 2017-06-14
  */
object ExportFormats {

  type DocsConvert = (String,Map[String,String])

  val Documents: DocsConvert = (DriveService.GDOCUMENT, Map(
    "text/html" -> "HTML",
    "application/zip" -> "HTML (zipped)",
    "text/plain" -> "Plain text",
    "application/rtf" -> "Rich text",
    "application/vnd.oasis.opendocument.text" -> "Open Office document",
    "application/pdf" -> "PDF",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "MS Word document",
    "application/epub+zip" -> "EPUB"
  ))



}
