package db

import java.sql.Timestamp

import db.OrePostgresDriver.api._
import db.orm.ModelTable
import models.project._
import models.user.{ProjectRole, User}
import ore.Colors.Color
import ore.permission.role.RoleTypes.RoleType
import ore.project.Categories.Category
import ore.project.FlagReasons.FlagReason

/*
 * Database schema definitions. Changes must be first applied as an evolutions
 * SQL script in "conf/evolutions/default", then here, then in the associated
 * model.
 */

class ProjectTable(tag: Tag) extends ModelTable[Project](tag, "projects") {

  def pluginId              =   column[String]("plugin_id")
  def name                  =   column[String]("name")
  def slug                  =   column[String]("slug")
  def ownerName             =   column[String]("owner_name")
  def ownerId               =   column[Int]("owner_id")
  def homepage              =   column[String]("homepage")
  def recommendedVersionId  =   column[Int]("recommended_version_id")
  def category              =   column[Category]("category")
  def views                 =   column[Int]("views", O.Default(0))
  def downloads             =   column[Int]("downloads", O.Default(0))
  def stars                 =   column[Int]("stars", O.Default(0))
  def issues                =   column[String]("issues")
  def source                =   column[String]("source")
  def description           =   column[String]("description")
  def topicId               =   column[Int]("topic_id")
  def postId                =   column[Int]("post_id")
  def isVisible             =   column[Boolean]("is_visible")
  def isReviewed            =   column[Boolean]("is_reviewed")

  override def * = (id.?, createdAt.?, pluginId, name, slug, ownerName, ownerId, homepage.?, recommendedVersionId.?,
                    category, views, downloads, stars, issues.?, source.?, description.?, topicId.?, postId.?,
                    isVisible, isReviewed) <> ((Project.apply _).tupled, Project.unapply)

}

class ProjectViewsTable(tag: Tag) extends Table[(Option[Int], Option[String],
                                                 Option[Int], Int)](tag, "project_views") {

  def id          =   column[Int]("id", O.PrimaryKey, O.AutoInc)
  def cookie      =   column[String]("cookie")
  def userId      =   column[Int]("user_id")
  def projectId   =   column[Int]("project_id")

  override def * = (id.?, cookie.?, userId.?, projectId)

}

class ProjectStarsTable(tag: Tag) extends Table[(Int, Int)](tag, "project_stars") {

  def userId      =   column[Int]("user_id")
  def projectId   =   column[Int]("project_id")

  override def * = (userId, projectId)

}

class PageTable(tag: Tag) extends ModelTable[Page](tag, "pages") {

  def projectId     =   column[Int]("project_id")
  def name          =   column[String]("name")
  def slug          =   column[String]("slug")
  def contents      =   column[String]("contents")
  def isDeletable   =   column[Boolean]("is_deletable")

  override def * = (id.?, createdAt.?, projectId,
                    name, slug, contents, isDeletable) <> ((Page.apply _).tupled, Page.unapply)

}

class ChannelTable(tag: Tag) extends ModelTable[Channel](tag, "channels") {

  def name        =   column[String]("name")
  def color       =   column[Color]("color")
  def projectId   =   column[Int]("project_id")

  override def * = (id.?, createdAt.?, name, color, projectId) <> ((Channel.apply _).tupled, Channel.unapply)
}

class VersionTable(tag: Tag) extends ModelTable[Version](tag, "versions") {

  def versionString   =   column[String]("version_string")
  def dependencies    =   column[List[String]]("dependencies")
  def description     =   column[String]("description")
  def assets          =   column[String]("assets")
  def downloads       =   column[Int]("downloads")
  def projectId       =   column[Int]("project_id")
  def channelId       =   column[Int]("channel_id")
  def fileSize        =   column[Long]("file_size")
  def hash            =   column[String]("hash")

  override def * = (id.?, createdAt.?, versionString, dependencies, description.?, assets.?, downloads, projectId,
                    channelId, fileSize, hash) <> ((Version.apply _).tupled, Version.unapply)
}

class VersionDownloadsTable(tag: Tag) extends Table[(Option[Int], Option[String],
                                                     Option[Int], Int)](tag, "version_downloads") {

  def id          =   column[Int]("id", O.PrimaryKey, O.AutoInc)
  def cookie      =   column[String]("cookie")
  def userId      =   column[Int]("user_id")
  def versionId   =   column[Int]("version_id")

  override def * = (id.?, cookie.?, userId.?, versionId)

}

class UserTable(tag: Tag) extends ModelTable[User](tag, "users") {

  override def id   =   column[Int]("id", O.PrimaryKey)
  def name          =   column[String]("name")
  def username      =   column[String]("username")
  def email         =   column[String]("email")
  def tagline       =   column[String]("tagline")
  def globalRoles   =   column[List[Int]]("global_roles")
  def joinDate      =   column[Timestamp]("join_date")

  override def * = (id.?, createdAt.?, name.?, username, email.?, tagline.?,
                    globalRoles, joinDate.?) <> ((User.apply _).tupled, User.unapply)

}

class ProjectRolesTable(tag: Tag) extends ModelTable[ProjectRole](tag, "user_project_roles") {

  def userId      =   column[Int]("user_id")
  def roleType    =   column[RoleType]("role_type")
  def projectId   =   column[Int]("project_id")

  override def * = (id.?, createdAt.?, userId, roleType, projectId) <> ((ProjectRole.apply _).tupled, ProjectRole.unapply)

}

class FlagTable(tag: Tag) extends ModelTable[Flag](tag, "flags") {

  def projectId = column[Int]("project_id")
  def userId = column[Int]("user_id")
  def reason = column[FlagReason]("reason")
  def isResolved = column[Boolean]("is_resolved")

  override def * = (id.?, createdAt.?, projectId, userId, reason, isResolved) <> ((Flag.apply _).tupled, Flag.unapply)

}
