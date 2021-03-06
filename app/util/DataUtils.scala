package util

import java.nio.file.Files

import db.OrePostgresDriver.api._
import db.query.Queries
import db.query.Queries.{now, run}
import forums.SpongeForums
import models.project.Project.PendingProject
import models.project.Version.PendingVersion
import models.project.{Channel, Project, Version}
import models.user.User
import ore.project.util.ProjectFactory
import org.apache.commons.io.FileUtils
import play.api.libs.Files.TemporaryFile
import play.api.libs.ws.WSClient
import util.C._
import util.P._

/**
  * Utility class for performing some bulk actions on the application data.
  * Typically for testing.
  */
object DataUtils {

  implicit private var ws: WSClient = null
  private val pluginPath = RootDir.resolve(OreConf.getString("test-plugin").get)

  def apply(implicit ws: WSClient) = this.ws = ws

  /**
    * Resets the application to factory defaults.
    */
  def reset() = {
    for (project <- now(Queries.Projects.collect()).get) project.delete
    now(run(Queries.Users.baseQuery.delete)).get
    FileUtils.deleteDirectory(UploadsDir.toFile)
  }

  /**
    * Fills the application with some dummy data.
    *
    * @param users Amount of users to create
    */
  def seed(users: Int, versions: Int, channels: Int) = {
    // Note: Dangerous as hell, handle with care
    SpongeForums.disable() // Disable topic creation
    this.reset()
    var pluginFile = copyPlugin
    for (i <- 0 until users) {

      println("User: " + i + '/' + users)

      // Initialize plugin
      val user = User.getOrCreate(new User(i, null, "User-" + i, null, null))
      while (!pluginFile.exists()) pluginFile = copyPlugin // /me throws up
      var plugin = ProjectFactory.initUpload(TemporaryFile(pluginFile), pluginFile.getName, user).get
      val pluginId = "pluginId." + i

      // Modify meta
      var meta = plugin.meta.get
      meta.setId(pluginId)

      // Create project
      var project = Project.fromMeta(user, meta)
      project = PendingProject(project, plugin).complete.get

      // Create channels
      var channelSeq: Seq[Channel] = Seq.empty
      for (i <- 0 until channels) {
        channelSeq :+= project.addChannel("Channel" + (i + 1).toString, Channel.Colors(i))
      }

      // Create additional versions
      for (i <- 0 until versions) {
        println("Version: " + i + '/' + versions)

        for ((channel, j) <- channelSeq.zipWithIndex) {
          // Initialize plugin
          while (!pluginFile.exists()) pluginFile = copyPlugin
          plugin = ProjectFactory.initUpload(TemporaryFile(pluginFile), pluginFile.getName, user).get

          // Modify meta
          meta = plugin.meta.get
          meta.setId(pluginId)
          meta.setVersion(i.toString)

          // Create version
          val version = Version.fromMeta(project, plugin).copy(channelId=channel.id.get)
          PendingVersion(user.username, project.slug, channel.name, version=version, plugin=plugin).complete.get
        }
      }
    }
    SpongeForums.apply // Re-enable forum hooks
  }

  private def copyPlugin = {
    val path = this.pluginPath.getParent.resolve("plugin.jar")
    if (Files.notExists(path)) {
      Files.copy(this.pluginPath, this.pluginPath.getParent.resolve("plugin.jar")).toFile
    }
    path.toFile
  }

}
