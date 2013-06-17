package util

import play.api.mvc.Flash
import play.api.mvc.Session
import twitter4j.auth.AccessToken
import twitter4j.auth.RequestToken
import twitter4j.TwitterFactory
import twitter4j.Twitter
import twitter4j.TwitterException

object Twitters {
  implicit def wrapTwitter(twitter: Twitter): TwitterWraper = new TwitterWraper(twitter)

  def create = new TwitterFactory().getInstance()

  def fromSession(implicit session: Session) = {
    val accessToken = session.get("accessToken")
    val accessTokenSecret = session.get("accessTokenSecret")

    if (accessToken.isEmpty || accessTokenSecret.isEmpty) {
      None
    } else {
      Some(new TwitterFactory().getInstance(new AccessToken(
        accessToken.get, accessTokenSecret.get)))
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

