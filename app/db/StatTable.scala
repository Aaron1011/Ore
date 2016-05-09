package db

import com.github.tminglei.slickpg.InetString
import db.impl.OrePostgresDriver.api._
import models.statistic.StatEntry

/**
  * Represents a table that represents statistics on a Model.
  *
  * @param tag          Table tag
  * @param name         Table name
  * @param modelIdName  Column name of model ID field
  */
abstract class StatTable[M <: StatEntry[_]](tag: Tag,
                                            name: String,
                                            modelIdName: String) extends ModelTable[M](tag, name) {

  /** The model ID of the statistic subject */
  def modelId = column[Int](modelIdName)
  /** Client address */
  def address = column[InetString]("address")
  /** Unique browser cookie */
  def cookie = column[String]("cookie")
  /** User ID if applicable */
  def userId = column[Int]("user_id")

}
