package org.mellowtech.gapi.drive


trait Fieldable {
  this: Enumeration =>

  lazy val allFields: String = values.mkString(",")



}

object TeamDriveThemesField extends Enumeration with Fieldable {
  type TeamDriveThemesField = Value
  val backgroundImageLink, colorRgb, id = Value
}

object StorageQuotaField extends Enumeration with Fieldable{
  type StorageQuotaField = Value
  val limit, usage, usageInDrive, usageInDriveTrash = Value
}

object LocationField extends Enumeration with Fieldable {
  type LocationField = Value
  val altitude, latitude, longitude = Value
}

object CapabilitiesField extends Enumeration with Fieldable {
  type CapabilitiesField = Value
  val canAddChildren, canChangeViewersCanCopyConent, canComment = Value
  val canCopy, canDelete, canDownload, canEdit, canListChildren = Value
  val canMoveItemIntoTeamDrive, canMoveTeamDriveItem, canReadRevisions, canReadTeamDrive = Value
  val canRemoveChildren, canRename, canShare, canThrash, canUntrash = Value
}

object ImageMediaMetadataField extends Enumeration with Fieldable {
  type ImageMediaMetadataField = Value
  val aperture, cameraMake, cameraModel, colorSpace, exposureBias = Value
  val exposureMode, exposureTime, flashUsed, focalLength = Value
  val height, isoSpeed, lens, location, maxApertureValue, meteringMode, rotation = Value
  val sensor, subjectDistance, whiteBalance, width = Value
}

object UserField extends Enumeration with Fieldable {
  type UserField = Value
  val displayName, emailAddress, kind, me, permissionId, photoLink = Value
}

object TeamDrivePemissionDetailsField extends Enumeration with Fieldable {
  type TeamDrivePemissionDetailsField = Value
  val inherited, inheritedFrom, role, teamDrivePermissionType = Value
}

object PermissionField extends Enumeration with Fieldable {
  type PermissionField = Value
  val allowFileDiscovery, deleted, displayName, domain = Value
  val emailAddress, expirationTime, id,  kind = Value
  val photoLink, role, teamDrivePermissionDetails, aType = Value
}

object ThumbnailField extends Enumeration with Fieldable {
  type ThumbnailField = Value
  val image, mimeType = Value
}

object VideoMediaMetadataField extends Enumeration with Fieldable {
  type VideoMediaMetadataField = Value
  val durationMillis, height, width = Value
}

object ContentHintsField extends Enumeration with Fieldable {
  type ContentHintsField = Value
  val indexableText, thumbnail = Value
}

object FileField extends Enumeration with Fieldable{
  type FileField = Value
  val appProperties, capabilities, contentHints = Value
  val createdTime, description, explicitlyTrashed = Value
  val fileExtension, folderColorRgb, fullFileExtension = Value
  val hasAugmentedPermissions, hasThumbnail, headRevisionId = Value
  val iconLink, id, imageMediaMetadata, isAppAuthorized = Value
  val kind, lastModifyingUser, md5Checksum, mimeType = Value
  val modifiedByMe, modifiedByMeTime, modifiedTime = Value
  val name, originalFilename, ownedByMe, owners = Value
  val parents, permissions, properties, quotaBytesUsed = Value
  val shared, sharedWithMeTime, sharingUser, size = Value
  val spaces, starred, teamDriveId, thumbnailLink = Value
  val thumbnailVersion, trashed, trashedTime = Value
  val trashingUser, version, videoMediaMetadata = Value
  val viewedByMe, viewedByMeTime, viewersCanCopyContent = Value
  val webContentLink, webViewLink, writersCanShare = Value
}

object FileListField extends Enumeration with Fieldable {
  type FileListField = Value
  val files, incompleteSearch, kind, nextPageToken = Value
}

object AboutField extends Enumeration with Fieldable {
  type AboutField = Value
  val appInstalled,exportFormats,folderColorPalette = Value
  val importFormats,kind,maxImportSizes,maxUploadSize = Value
  val storageQuota,teamDriveThemes,user = Value
}

