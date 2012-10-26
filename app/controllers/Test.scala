package controllers

import java.io.{ FileInputStream, File }
import scala.collection.JavaConverters.asScalaBufferConverter
import com.google.common.io.ByteStreams
import anorm.SqlParser.{ str, long, flatten, date }
import anorm.{ toParameterValue, sqlToSimple, SQL }
import play.api.Play.current
import play.api.db.DB
import play.api.libs.iteratee.Enumerator
import play.api.mvc.{ SimpleResult, ResponseHeader, Controller, Action }
import settings.Setting
import util.DbUtil.columnForBlob
import util.Guavas.{ inputStreamToInputSupplier, byteArrayToInputSupplier }
import util.Twitters
import org.slf4j.LoggerFactory

object Test extends Controller {
  val log = LoggerFactory.getLogger("application")
}
