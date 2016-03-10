package models.author

import java.sql.Timestamp

import com.google.common.base.{MoreObjects, Objects}
import models.author.Author.Unknown
import models.project.Project
import sql.Storage

import scala.concurrent.Future

/**
  * Represents an author of a Project. Authors can be either a Team or Dev.
  * Every project has a single Author designated as the "owner" and then a list
  * of additional Authors on the project. The list of additional authors
  * should include the owner. Team and Dev names must be unique to one another.
  * That is, a Dev cannot have the same name as another Dev nor may it have the
  * same name as a Team and vice-versa.
  */
abstract class Author {

  /**
    * Returns this Author's unique identifier.
    *
    * @return Unique ID
    */
  def id: Option[Int]

  /**
    * Returns the Timestamp of when this Author was created.
    *
    * @return Creation of author
    */
  def createdAt: Option[Timestamp]

  /**
    * Returns the name of this Author.
    *
    * @return Name of author
    */
  def name: String

  /**
    * Returns the Project with the specified name that this Author owns.
    *
    * @param name Name of project
    * @return Owned project, if any, None otherwise
    */
  def getProject(name: String): Future[Option[Project]] = Storage.optProject(this.name, name)

  /**
    * Returns all Projects owned by this Author.
    *
    * @return All projects owned by Author
    */
  def getProjects: Future[Seq[Project]] = Storage.getProjectsBy(this.name)

  /**
    * Returns true if this Author is registered with Ore.
    *
    * @return True if registered, false otherwise
    */
  def isRegistered: Boolean = !isInstanceOf[Unknown]

  override def toString: String = MoreObjects.toStringHelper(this).add("name", this.name).toString

  override def hashCode: Int = this.name.hashCode

  override def equals(o: Any): Boolean = o.isInstanceOf[Author] && o.asInstanceOf[Author].name.equals(this.name)

}

object Author {

  /**
    * Represents an Author not registered in the system.
    *
    * @param name Name of author
    */
  case class Unknown(id: Option[Int] = None, createdAt: Option[Timestamp] = None, override val name: String) extends Author

}
