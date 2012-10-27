package settings
import play.api.Mode
import play.api.Play.current
import models.User

object Setting {

  def loginCallbackUrl = current.mode match {
    case Mode.Prod => "http://doga.dev7.jp/callback"
    case _ => "http://localhost:9000/callback"
  }

  def staticServerName = current.mode match {
    case Mode.Prod => "static.dev7.jp"
    case _ => "localhost:8080"
  }

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
