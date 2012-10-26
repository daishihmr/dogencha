package settings
import play.api.Mode
import play.api.Play.current
import models.User

object Setting {

  def loginCallbackUrl = current.mode match {
    case Mode.Prod => "http://doga.dev7.jp/callback"
    case _ => "http://localhost:9000/callback"
  }

  def staticServerName = "static.dev7.jp"

  def dataDir = current.mode match {
    case Mode.Prod => "/home/webapp/doga.data"
    case _ => "../uploaded"
  }

  def userDir(implicit user: Option[User]) = user.map { u =>
    dataDir + "/" + u.name
  }.getOrElse(dataDir + "/guest")

  def cacheDir = current.mode match {
    case Mode.Prod => "/home/webapp/doga.cache"
    case _ => "../cache"
  }

}
