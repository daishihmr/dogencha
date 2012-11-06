import play.api.Application
import play.api.GlobalSettings
import play.api.Mode
import java.util.Properties
import java.io.FileInputStream
import play.api.mvc._
import play.api.mvc.Results._
import play.api.db.DB
import play.api.Configuration
import twitter4j.TwitterAPIConfiguration
import twitter4j.Twitter

object Global extends GlobalSettings {

  override def onStart(app: Application) = {

    app.mode match {
      case Mode.Prod => {
        System.setProperty("dev7.doga.parser.dogaCommonDir", "/opt/_webapps/DOGACGA/common")
        System.setProperty("dev7.doga.parser.dogaDataDir", "/opt/_webapps/DOGACGA/data")
      }
      case _ => {
        List("http.proxyHost", "http.proxyPort",
          "dev7.doga.parser.dogaCommonDir", "dev7.doga.parser.dogaDataDir") foreach { key =>
            app.configuration.getString(key) map { value =>
              System.setProperty(key, value)
            }
          }
      }
    }

    val twitterProperties = new Properties
    twitterProperties.load(new FileInputStream("twitter.properties"))
    System.setProperty("twitter4j.oauth.consumerKey", twitterProperties.getProperty("twitter4j.oauth.consumerKey"))
    System.setProperty("twitter4j.oauth.consumerSecret", twitterProperties.getProperty("twitter4j.oauth.consumerSecret"))
  }

  //  override def onError(request: RequestHeader, ex: Throwable) = {
  //    Ok("エラーっすよ")
  //  }
  //
  //  override def onBadRequest(request: RequestHeader, error: String) = {
  //    BadRequest("BadRequest")
  //  }
  //
  //  override def onHandlerNotFound(request: RequestHeader): Result = {
  //    NotFound("NotFound")
  //  }

}
