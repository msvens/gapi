package org.mellowtech.gapi.drive

/**
  * @author msvens
  * @since 2017-05-22
  */
object AboutFields {

  lazy val appInstalled = "appInstalled"
  lazy val exportFormats = "exportFormats"
  lazy val folderColorPalette = "folderColorPalette"
  lazy val importFormats = "importFormats"
  lazy val kind = "kind"
  lazy val maxImportSizes = "maxImportSizes"
  lazy val maxUploadSizes = "maxUploadSize"
  lazy val storageQuota = "storageQuota"
  lazy val teamDriveThemes = "teamDriveThemes"
  lazy val user = "user"

  //fields for order by
  lazy val createdTime = "createdTime"
  lazy val folder = "folder"
  lazy val modifiedByMeTime = "modifiedByMeTime"
  lazy val modifiedTime = "modifiedTime"
  lazy val name = "name"
  lazy val quotaBytesUsed = "quotaBytesNeeded"
  lazy val recency = "recency"
  lazy val sharedWithMeTime = "sharedWithMeTime"
  lazy val starred = "starred"
  lazy val viewedByMeTime = "viewedByMeTime"

  lazy val orderByFields = Array[String](createdTime, folder, modifiedByMeTime, modifiedTime, name,
  quotaBytesUsed, recency, sharedWithMeTime, starred, viewedByMeTime)

  lazy val aboutFields = Array[String](appInstalled, exportFormats, folderColorPalette,
    importFormats,kind,maxImportSizes,maxUploadSizes,storageQuota,teamDriveThemes,user)

  def isAboutField(f: String): Boolean = aboutFields.contains(f)

}

