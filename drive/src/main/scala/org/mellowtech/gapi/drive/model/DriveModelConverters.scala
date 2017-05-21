package org.mellowtech.gapi.drive.model


import com.google.api.services.drive.model.File.ContentHints.{Thumbnail => JThumbnail}
import com.google.api.services.drive.model.File.ImageMediaMetadata.{Location => JLocation}
import com.google.api.services.drive.model.File.{Capabilities => JCapabilites, ContentHints => JContentHints, ImageMediaMetadata => JImageMediaMetadata, VideoMediaMetadata => JVideoMediaMetadata}
import com.google.api.services.drive.model.Permission.{TeamDrivePermissionDetails => JTeamDrivePermissionDetails}
import com.google.api.services.drive.model.{About => JAbout, File => JFile, FileList => JFileList, Permission => JPermission, User => JUser}
import com.google.api.services.drive.model.About.{StorageQuota => JStorageQuota, TeamDriveThemes => JTeamDriveThemes}

private[model] object Decorators {

  import scala.collection.JavaConverters._


  class FileListConverter(var fl: JFileList) {
    val files: Option[Seq[File]] = if(fl.getFiles == null) None else
      Some(fl.getFiles.asScala.map(new FileConverter(_).asScala))
    def asScala: FileList = FileList(
                         files = files,
                         incompleteSearch = Option(fl.getIncompleteSearch),
                         kind = Option(fl.getKind),
                         nextPageToken = Option(fl.getNextPageToken))

  }

  class StorageQuotaConverter(val q: JStorageQuota) {
    def asScala: StorageQuota = StorageQuota(
      limit = Option(q.getLimit),
    usage = Option(q.getUsage),
    usageInDrive = Option(q.getUsageInDrive),
    usageInDriveTrash = Option(q.getUsageInDriveTrash))
  }

  class TeamDriveThemesConverter(val tdt: JTeamDriveThemes){
    def asScala: TeamDriveThemes = TeamDriveThemes(
      backgroundImageLink = Option(tdt.getBackgroundImageLink),
    colorRgb = Option(tdt.getColorRgb),
    id = Option(tdt.getId))
  }

  class AboutConverter(val a: JAbout) {

    val teamDrives = if(a.getTeamDriveThemes == null) None else
      Some(a.getTeamDriveThemes.asScala.map(e => new TeamDriveThemesConverter(e).asScala))

    //Ugly as hell and not very performant
    val mis: Option[scala.collection.Map[String,Long]] = if(a.getMaxImportSizes == null) None else
      Some(a.getMaxImportSizes.asScala.map{case (key,value) => (key,value.longValue())})

    val exp = if(a.getExportFormats == null) None else
      Some(a.getExportFormats.asScala.map{case(k,v) => (k,v.asScala)})

    val imp = if(a.getImportFormats == null) None else
      Some(a.getImportFormats.asScala.map{case(k,v) => (k,v.asScala)})

    def asScala: About = About(
      appInstalled = Option(a.getAppInstalled),
    exportFormats = exp,
    folderColorPalette = if(a.getFolderColorPalette == null) None else Some(a.getFolderColorPalette.asScala),
    importFormats = imp,
    kind = Option(a.getKind),
    maxImportSizes = mis,
    maxUploadSize = Option(a.getMaxUploadSize),
    storageQuota = if(a.getStorageQuota == null) None else Some(new StorageQuotaConverter(a.getStorageQuota).asScala),
    teamDriveThemes = teamDrives,
    user = if(a.getUser == null) None else Some(new UserConverter(a.getUser()).asScala))
  }


  class LocationConverter(val loc: JLocation) {
    def asScala: Location = {
      Location(Option(loc.getAltitude), Option(loc.getLatitude), Option(loc.getLongitude))
    }
  }

  class CapabilitiesConverter(val c: JCapabilites) {
    def asScala: Capabilities = {
      Capabilities(
        canAddChildren = Option(c.getCanAddChildren),
        canChangeViewersCanCopyConent = Option(c.getCanChangeViewersCanCopyContent),
        canComment = Option(c.getCanComment),
        canCopy = Option(c.getCanCopy),
        canDelete = Option(c.getCanDelete),
        canDownload = Option(c.getCanDownload),
        canEdit = Option(c.getCanEdit),
        canListChildren = Option(c.getCanListChildren),
        canMoveItemIntoTeamDrive = Option(c.getCanMoveItemIntoTeamDrive),
        canMoveTeamDriveItem = Option(c.getCanMoveTeamDriveItem),
        canReadRevisions = Option(c.getCanReadRevisions),
        canReadTeamDrive = Option(c.getCanReadTeamDrive),
        canRemoveChildren = Option(c.getCanRemoveChildren),
        canRename = Option(c.getCanRename),
        canShare = Option(c.getCanShare),
        canThrash = Option(c.getCanTrash),
        canUntrash = Option(c.getCanUntrash))
    }
  }

  class ImageMediaMetadataConverter(val i: JImageMediaMetadata) {
    def asScala: ImageMediaMetadata = {
      ImageMediaMetadata(
        aperture = Option(i.getAperture),
        cameraMake = Option(i.getCameraMake),
        cameraModel = Option(i.getCameraModel),
        colorSpace = Option(i.getColorSpace),
        exposureBias = Option(i.getExposureBias),
        exposureMode = Option(i.getExposureMode),
        exposureTime = Option(i.getExposureTime),
        flashUsed = Option(i.getFlashUsed),
        focalLength = Option(i.getFocalLength),
        height = Option(i.getHeight),
        isoSpeed = Option(i.getIsoSpeed),
        lens = Option(i.getLens),
        location = if (i.getLocation == null) None else Some(new LocationConverter(i.getLocation).asScala),
        maxApertureValue = Option(i.getMaxApertureValue),
        meteringMode = Option(i.getMeteringMode),
        rotation = Option(i.getRotation),
        sensor = Option(i.getSensor),
        subjectDistance = Option(i.getSubjectDistance),
        whiteBalance = Option(i.getWhiteBalance),
        width = Option(i.getWidth))
    }
  }

  class UserConverter(val u: JUser) {
    def asScala: User = User(
      displayName = Option(u.getDisplayName),
      emailAddress = Option(u.getEmailAddress),
      kind = Option(u.getKind),
      me = Option(u.getMe),
      permissionId = Option(u.getPermissionId),
      photoLink = Option(u.getPhotoLink))
  }

  class TeamDrivePermissionDetailsConverter(val pd: JTeamDrivePermissionDetails) {
    def asScala: TeamDrivePermissionDetails = TeamDrivePermissionDetails(
      inherited = Option(pd.getInherited),
      inheritedFrom = Option(pd.getInheritedFrom),
      role = Option(pd.getRole),
      teamDrivePermissionType = Option(pd.getTeamDrivePermissionType))
  }

  class PermissionConverter(val p: JPermission) {
    def asScala: Permission = {
      val permDetails: Option[Seq[TeamDrivePermissionDetails]] =
        if (p.getTeamDrivePermissionDetails == null) None
        else
          Some(p.getTeamDrivePermissionDetails.asScala.map(e => new TeamDrivePermissionDetailsConverter(e).asScala))

      Permission(
        allowFileDiscovery = Option(p.getAllowFileDiscovery),
        deleted = Option(p.getDeleted),
        displayName = Option(p.getDisplayName),
        domain = Option(p.getDomain),
        emailAddress = Option(p.getEmailAddress),
        expirationTime = Option(p.getExpirationTime),
        id = Option(p.getId),
        kind = Option(p.getKind),
        photoLink = Option(p.getPhotoLink),
        role = Option(p.getPhotoLink),
        teamDrivePermissionDetails = permDetails,
        aType = Option(p.getType))
    }
  }

  class ThumbnailConverter(val t: JThumbnail) {
    def asScala: Thumbnail = Thumbnail(
      image = Option(t.getImage),
      mimeType = Option(t.getMimeType))
  }

  class VideoMediaMetadataConverter(v: JVideoMediaMetadata) {
    def asScala: VideoMediaMetadata = VideoMediaMetadata(
      durationMillis = Option(v.getDurationMillis),
      height = Option(v.getHeight),
      width = Option(v.getWidth))
  }

  class ContentHintsConverter(c: JContentHints) {
    def asScala: ContentHints = ContentHints(
      indexableText = Option(c.getIndexableText),
      thumbnail = if (c.getThumbnail == null) None else Some(new ThumbnailConverter(c.getThumbnail).asScala))
  }

  class FileConverter(f: JFile) {
    def asScala: File = {
      val owns = if (f.getOwners == null) None else
        Some(f.getOwners.asScala.map(e => new UserConverter(e).asScala))

      val perms = if (f.getPermissions == null) None else
        Some(f.getPermissions.asScala.map(e => new PermissionConverter(e).asScala))

      File(
        appProperties = if (f.getAppProperties == null) None else Some(f.getAppProperties.asScala),
        capabilities = if (f.getCapabilities == null) None else Some(new CapabilitiesConverter(f.getCapabilities).asScala),
        conentHints = if (f.getContentHints == null) None else Some(new ContentHintsConverter(f.getContentHints).asScala),
        createdTime = Option(f.getCreatedTime),
        description = Option(f.getDescription),
        explicitlyTrashed = Option(f.getExplicitlyTrashed),
        fileExtension = Option(f.getFileExtension),
        folderColorRbg = Option(f.getFolderColorRgb),
        fullFileExtension = Option(f.getFullFileExtension),
        hasAugmentedPermissions = Option(f.getHasAugmentedPermissions),
        hasThumbNail = Option(f.getHasThumbnail),
        headRevisionId = Option(f.getHeadRevisionId),
        iconLink = Option(f.getIconLink),
        id = Option(f.getId),
        imageMediaMetadata = if (f.getImageMediaMetadata == null) None else Some(new ImageMediaMetadataConverter(f.getImageMediaMetadata).asScala),
        isAppAuthorized = Option(f.getIsAppAuthorized),
        kind = Option(f.getKind),
        lastModifyingUser = if (f.getLastModifyingUser == null) None else Some(new UserConverter(f.getLastModifyingUser).asScala),
        md5Checksum = Option(f.getMd5Checksum),
        mimeType = Option(f.getMimeType),
        modifiedByMe = Option(f.getModifiedByMe),
        modifiedByMeTime = Option(f.getModifiedByMeTime),
        modifiedTime = Option(f.getModifiedTime),
        name = Option(f.getName),
        originalFileName = Option(f.getOriginalFilename),
        ownedByMe = Option(f.getOwnedByMe),
        owners = owns,
        parents = if (f.getParents == null) None else Some(f.getParents.asScala),
        permissions = perms,
        properties = if (f.getProperties == null) None else Some(f.getProperties.asScala),
        quotaBytesUsed = Option(f.getQuotaBytesUsed),
        shared = Option(f.getShared),
        sharedWithMeTime = Option(f.getSharedWithMeTime),
        sharingUser = if (f.getSharingUser == null) None else Some(new UserConverter(f.getSharingUser).asScala),
        size = Option(f.getSize),
        spaces = if (f.getSpaces == null) None else Some(f.getSpaces.asScala),
        starred = Option(f.getStarred),
        teamDriveId = Option(f.getTeamDriveId),
        thumbnailLink = Option(f.getThumbnailLink),
        thumbnailVersion = Option(f.getThumbnailVersion),
        trashed = Option(f.getTrashed),
        trashedTime = Option(f.getTrashedTime),
        trashingUser = if (f.getTrashingUser == null) None else Some(new UserConverter(f.getTrashingUser).asScala),
        version = Option(f.getVersion),
        videoMediaMetadata = if (f.getVideoMediaMetadata == null) None else Some(new VideoMediaMetadataConverter(f.getVideoMediaMetadata).asScala),
        viewedByMe = Option(f.getViewedByMe),
        viewedByMeTime = Option(f.getViewedByMeTime),
        viewersCanCopyContent = Option(f.getViewersCanCopyContent),
        webContentLink = Option(f.getWebContentLink),
        webViewLink = Option(f.getWebContentLink),
        writersCanShare = Option(f.getWritersCanShare))
    }
  }


}


object DriveConverters {

  import scala.collection.JavaConverters._
  import Decorators._

  implicit def aboutConverter(about: JAbout) = new AboutConverter(about)
  implicit def locationConverter(loc: JLocation) = new LocationConverter(loc)
  implicit def fileConverter(f: JFile) = new FileConverter(f)
  implicit def fileListConverter(fl: JFileList) = new FileListConverter(fl)

  def toGoogleFile(name: String, parentIds: Seq[String], mimeType: String): JFile = {
    val f = new JFile()
    f.setName(name)
    f.setParents(parentIds.asJava)
    f.setMimeType(mimeType)
    f
  }


}
