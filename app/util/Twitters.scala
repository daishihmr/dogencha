package util

import java.util.Date
import play.api.mvc.Flash
import play.api.mvc.Session
import twitter4j.auth.AccessToken
import twitter4j.auth.RequestToken
import twitter4j.TwitterFactory
import twitter4j.Twitter
import twitter4j.TwitterException

object Twitters {
  implicit def wrapTwitter(twitter: Twitter): TwitterWraper = new TwitterWraper(twitter)

  val twitterMap = scala.collection.mutable.Map[String, (Date, Twitter)]()

  def create = new TwitterFactory().getInstance()

  def fromSession(implicit session: Session) = {
    val accessToken = session.get("accessToken")

    if (accessToken.isEmpty) {
      None
    } else {
      val result = twitterMap.get(accessToken.get)
      result.map { t =>
        if (java.lang.System.currentTimeMillis - t._1.getTime < 1000*60*2) {
          t._2
        } else {
          twitterMap.remove(accessToken.get)
          null
        }
      }
    }
  }

  def requestTokenFromFlash(implicit flash: Flash) = {
    val requestToken = flash.get("requestToken")
    val requestTokenSecret = flash.get("requestTokenSecret")

    if (requestToken.isEmpty || requestTokenSecret.isEmpty) {
      None
    } else {
      Some(new RequestToken(requestToken.get, requestTokenSecret.get))
    }
  }

}

class TwitterWraper(twitter: Twitter) {
  private def accessToTwitter(block: => String) = {
    try {
      block
    } catch {
      case e: TwitterException => "Twitterエラー"
    }
  }

  private lazy val user = twitter.showUser(twitter.getScreenName())

  lazy val img = accessToTwitter {
    twitter.showUser(twitter.getScreenName()).getMiniProfileImageURL()
  }
  lazy val name = accessToTwitter {
    user.getName()
  }
  lazy val description = accessToTwitter {
    user.getDescription()
  }
}

