package org.mellowtech.gapi.drive.model

import com.google.api.client.util.DateTime
import org.mellowtech.gapi.drive.DriveService
import org.mellowtech.gapi.model.GApiType

case class StorageQuota(limit: Option[Long] = None,
                        usage: Option[Long] = None,
                        usageInDrive: Option[Long] = None,
                        usageInDriveTrash: Option[Long] = None) extends GApiType


case class TeamDriveThemes(backgroundImageLink: Option[String] = None,
                           colorRgb: Option[String] = None,
                           id: Option[String] = None) extends GApiType

case class About(appInstalled: Option[Boolean] = None,
                 exportFormats: Option[scala.collection.Map[String, Seq[String]]] = None,
                 folderColorPalette: Option[Seq[String]] = None,
                 importFormats: Option[scala.collection.Map[String, Seq[String]]] = None,
                 kind: Option[String] = None,
                 maxImportSizes: Option[scala.collection.Map[String, Long]] = None,
                 maxUploadSize: Option[Long] = None,
                 storageQuota: Option[StorageQuota] = None,
                 teamDriveThemes: Option[Seq[TeamDriveThemes]] = None,
                 user: Option[User] = None) extends GApiType

case class Location(altitude: Option[Double] = None,
                    latitude: Option[Double] = None,
                    longitude: Option[Double] = None) extends GApiType

case class Capabilities(canAddChildren: Option[Boolean] = None,
                        canChangeViewersCanCopyConent: Option[Boolean] = None,
                        canComment: Option[Boolean] = None,
                        canCopy: Option[Boolean] = None,
                        canDelete: Option[Boolean] = None,
                        canDownload: Option[Boolean] = None,
                        canEdit: Option[Boolean] = None,
                        canListChildren: Option[Boolean] = None,
                        canMoveItemIntoTeamDrive: Option[Boolean] = None,
                        canMoveTeamDriveItem: Option[Boolean] = None,
                        canReadRevisions: Option[Boolean] = None,
                        canReadTeamDrive: Option[Boolean] = None,
                        canRemoveChildren: Option[Boolean] = None,
                        canRename: Option[Boolean] = None,
                        canShare: Option[Boolean] = None,
                        canThrash: Option[Boolean] = None,
                        canUntrash: Option[Boolean] = None) extends GApiType


case class ImageMediaMetadata(aperture: Option[Float] = None,
                              cameraMake: Option[String] = None,
                              cameraModel: Option[String] = None,
                              colorSpace: Option[String] = None,
                              exposureBias: Option[Float] = None,
                              exposureMode: Option[String] = None,
                              exposureTime: Option[Float] = None,
                              flashUsed: Option[Boolean] = None,
                              focalLength: Option[Float] = None,
                              height: Option[Int] = None,
                              isoSpeed: Option[Int] = None,
                              lens: Option[String] = None,
                              location: Option[Location] = None,
                              maxApertureValue: Option[Float] = None,
                              meteringMode: Option[String] = None,
                              rotation: Option[Int] = None,
                              sensor: Option[String] = None,
                              subjectDistance: Option[Int] = None,
                              whiteBalance: Option[String] = None,
                              width: Option[Int] = None) extends GApiType


case class User(displayName: Option[String] = None,
                emailAddress: Option[String] = None,
                kind: Option[String] = None,
                me: Option[Boolean] = None,
                permissionId: Option[String] = None,
                photoLink: Option[String] = None) extends GApiType

case class TeamDrivePermissionDetails(inherited: Option[Boolean] = None,
                                      inheritedFrom: Option[String] = None,
                                      role: Option[String] = None,
                                      teamDrivePermissionType: Option[String] = None) extends GApiType

case class Permission(allowFileDiscovery: Option[Boolean] = None,
                      deleted: Option[Boolean] = None,
                      displayName: Option[String] = None,
                      domain: Option[String] = None,
                      emailAddress: Option[String] = None,
                      expirationTime: Option[DateTime] = None,
                      id: Option[String] = None,
                      kind: Option[String] = None,
                      photoLink: Option[String] = None,
                      role: Option[String] = None,
                      teamDrivePermissionDetails: Option[Seq[TeamDrivePermissionDetails]] = None,
                      aType: Option[String] = None) extends GApiType

//maybe add encode/decode methods
case class Thumbnail(image: Option[String] = None,
                     mimeType: Option[String] = None) extends GApiType

case class VideoMediaMetadata(durationMillis: Option[Long] = None,
                              height: Option[Int] = None,
                              width: Option[Int] = None) extends GApiType


case class ContentHints(indexableText: Option[String] = None,
                        thumbnail: Option[Thumbnail] = None) extends GApiType

case class File(appProperties: Option[scala.collection.Map[String, String]] = None,
                capabilities: Option[Capabilities] = None,
                conentHints: Option[ContentHints] = None,
                createdTime: Option[DateTime] = None,
                description: Option[String] = None,
                explicitlyTrashed: Option[Boolean] = None,
                fileExtension: Option[String] = None,
                folderColorRbg: Option[String] = None,
                fullFileExtension: Option[String] = None,
                hasAugmentedPermissions: Option[Boolean] = None,
                hasThumbNail: Option[Boolean] = None,
                headRevisionId: Option[String] = None,
                iconLink: Option[String] = None,
                id: Option[String] = None,
                imageMediaMetadata: Option[ImageMediaMetadata] = None,
                isAppAuthorized: Option[Boolean] = None,
                kind: Option[String] = None,
                lastModifyingUser: Option[User] = None,
                md5Checksum: Option[String] = None,
                mimeType: Option[String] = None,
                modifiedByMe: Option[Boolean] = None,
                modifiedByMeTime: Option[DateTime] = None,
                modifiedTime: Option[DateTime] = None,
                name: Option[String] = None,
                originalFileName: Option[String] = None,
                ownedByMe: Option[Boolean] = None,
                owners: Option[Seq[User]] = None,
                parents: Option[Seq[String]] = None,
                permissions: Option[Seq[Permission]] = None,
                properties: Option[scala.collection.Map[String, String]] = None,
                quotaBytesUsed: Option[Long] = None,
                shared: Option[Boolean] = None,
                sharedWithMeTime: Option[DateTime] = None,
                sharingUser: Option[User] = None,
                size: Option[Long] = None,
                spaces: Option[Seq[String]] = None,
                starred: Option[Boolean] = None,
                teamDriveId: Option[String] = None,
                thumbnailLink: Option[String] = None,
                thumbnailVersion: Option[Long] = None,
                trashed: Option[Boolean] = None,
                trashedTime: Option[DateTime] = None,
                trashingUser: Option[User] = None,
                version: Option[Long] = None,
                videoMediaMetadata: Option[VideoMediaMetadata] = None,
                viewedByMe: Option[Boolean] = None,
                viewedByMeTime: Option[DateTime] = None,
                viewersCanCopyContent: Option[Boolean] = None,
                webContentLink: Option[String] = None,
                webViewLink: Option[String] = None,
                writersCanShare: Option[Boolean] = None) extends GApiType {

  def isFolder: Boolean = mimeType.getOrElse("notype").equals(DriveService.GFOLDER)
}

case class FileList(files: Option[Seq[File]] = None,
                    incompleteSearch: Option[Boolean] = None,
                    kind: Option[String] = None,
                    nextPageToken: Option[String] = None) extends GApiType
