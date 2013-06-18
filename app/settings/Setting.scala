package settings
import play.api.Mode
import play.api.Play.current
import models.User

object Setting {

  private val port = "9000"

  lazy val loginCallbackUrl = current.mode match {
    case Mode.Prod => "http://doga.dev7.jp/callback"
    case _ => "http://localhost:" + port + "/callback"
  }

  lazy val staticServerName = current.mode match {
    case Mode.Prod => "static.dev7.jp"
    case _ => "static.dev7.jp"
    // case _ => "localhost:" + port + "/assets/file"
  }

  lazy val dataDir = current.mode match {
    case Mode.Prod => "/home/webapp/doga.data"
    case _ => "uploaded"
  }

  def userDir(implicit user: Option[User]) = user.map { u =>
    dataDir + "/" + u.name
  }.getOrElse(dataDir + "/guest")

  lazy val cacheDir = current.mode match {
    case Mode.Prod => "/home/webapp/doga.cache"
    case _ => "cache"
  }

}
