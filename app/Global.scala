import play.api.Application
import play.api.GlobalSettings
import play.api.Mode
import java.util.Properties
import java.io.FileInputStream
import play.api.mvc._
import play.api.mvc.Results._

object Global extends GlobalSettings {

  override def onStart(app: Application) = {

    app.mode match {
      case Mode.Prod => {
        System.setProperty("dev7.doga.parser.dogaCommonDir", "/opt/_webapps/DOGACGA/common")
        System.setProperty("dev7.doga.parser.dogaDataDir", "/opt/_webapps/DOGACGA/data")
      }
      case _ => {
        // System.setProperty("http.proxyHost", "proxy.apl.co.jp")
        // System.setProperty("http.proxyPort", "8080")
        // System.setProperty("dev7.doga.parser.dogaCommonDir", "D:\\devel\\3d\\common_lowercase")
        // System.setProperty("dev7.doga.parser.dogaDataDir", "D:\\devel\\3d\\data")

        // System.setProperty("dev7.doga.parser.dogaCommonDir", "/Users/daishi_hmr/tool/dogacga/common")
        // System.setProperty("dev7.doga.parser.dogaDataDir", "/Users/daishi_hmr/tool/dogacga/data")

        System.setProperty("dev7.doga.parser.dogaCommonDir", "/Users/narasakitaishi/tool/dogacga/common_lowercase")
        System.setProperty("dev7.doga.parser.dogaDataDir", "/Users/narasakitaishi/tool/dogacga/data")
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
