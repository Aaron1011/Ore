package form

import models.project.{Channel, Project}
import ore.Colors.Color

/**
  * Represents submitted [[Channel]] data.
  */
trait TChannelData {

  /** The [[Channel]] [[Color]] **/
  val color: Color = Channel.Colors.find(_.hex.equalsIgnoreCase(channelColorHex)).get

  /** Channel name **/
  def channelName: String

  /** Channel color hex **/
  protected def channelColorHex: String

  /**
    * Attempts to add this ChannelData as a [[Channel]] to the specified
    * [[Project]].
    *
    * @param project  Project to add Channel to
    * @return         Either the new channel or an error message
    */
  def addTo(project: Project): Either[String, Channel] = {
    val channels = project.channels.values
    if (channels.size >= Project.MaxChannels) {
      Left("A project may only have up to five channels.")
    } else {
      channels.find(_.name.equalsIgnoreCase(this.channelName)) match {
        case Some(channel) => Left("A channel with that name already exists.")
        case None => channels.find(_.color.equals(this.color)) match {
          case Some(channel) => Left("A channel with that color already exists.")
          case None => Right(project.addChannel(this.channelName, this.color))
        }
      }
    }
  }

  /**
    * Attempts to save this ChannelData to the specified [[Channel]] name in
    * the specified [[Project]].
    *
    * @param oldName  Channel name to save to
    * @param project  Project of channel
    * @return         Error, if any
    */
  def saveTo(oldName: String)(implicit project: Project): Option[String] = {
    val channels = project.channels.values
    val channel = channels.find(_.name.equalsIgnoreCase(oldName)).get
    val colorChan = channels.find(_.color.equals(this.color))
    val colorTaken = colorChan.isDefined && !colorChan.get.equals(channel)
    if (colorTaken) {
      Some("A channel with that color already exists.")
    } else {
      val nameChan = channels.find(_.name.equalsIgnoreCase(this.channelName))
      val nameTaken = nameChan.isDefined && !nameChan.get.equals(channel)
      if (nameTaken) {
        Some("A channel with that name already exists.")
      } else {
        channel.name = this.channelName
        channel.color = this.color
        None
      }
    }
  }

}
