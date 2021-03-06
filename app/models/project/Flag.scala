package models.project

import java.sql.Timestamp

import db.OrePostgresDriver.api._
import db.orm.dao.ModelDAO
import db.orm.model.ModelKeys._
import db.orm.model.{Model, UserOwner}
import db.query.Queries
import db.query.Queries.now
import ore.permission.scope.ProjectScope
import ore.project.FlagReasons.FlagReason

/**
  * Represents a flag on a Project that requires staff attention.
  *
  * @param id           Unique ID
  * @param createdAt    Timestamp instant of creation
  * @param projectId    Project ID
  * @param userId       Reporter ID
  * @param reason       Reason for flag
  * @param _isResolved  True if has been reviewed and resolved by staff member
  */
case class Flag(override val  id: Option[Int],
                override val  createdAt: Option[Timestamp],
                override val  projectId: Int,
                override val  userId: Int,
                val           reason: FlagReason,
                private var   _isResolved: Boolean = false)
                extends       Model
                with          UserOwner
                with          ProjectScope { self =>

  override type M <: Flag { type M = self.M }

  def this(projectId: Int, userId: Int, reason: FlagReason) = {
    this(id=None, createdAt=None, projectId=projectId, userId=userId, reason=reason)
  }

  /**
    * Returns true if this Flag has been reviewed and marked as resolved by a
    * staff member.
    *
    * @return True if resolved
    */
  def isResolved: Boolean = this._isResolved

  /**
    * Sets whether this Flag has been marked as resolved.
    *
    * @param resolved True if resolved
    */
  def setResolved(resolved: Boolean) = assertDefined {
    this._isResolved = resolved
    update(IsResolved)
  }

  override def copyWith(id: Option[Int], theTime: Option[Timestamp]): Flag = this.copy(id = id, createdAt = theTime)

  // Table bindings

  bind[Boolean](IsResolved, _._isResolved, isResolved => {
    Seq(Queries.Projects.Flags.setBoolean(this, _.isResolved, isResolved))
  })

}

object Flag extends ModelDAO[Flag] {

  /**
    * Returns all Flags that are unresolved.
    *
    * @return All unresolved flags
    */
  def unresolved: Seq[Flag] = now(Queries.Projects.Flags.filter(!_.isResolved)).get

  override def withId(id: Int): Option[Flag] = now(Queries.Projects.Flags.get(id)).get

}
