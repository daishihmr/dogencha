package controllers

import java.io.{ OutputStreamWriter, FileOutputStream, File }
import java.util.Date
import org.slf4j.LoggerFactory
import com.google.common.io.Files
import anorm.{ toParameterValue, sqlToSimple, SQL }
import jp.dev7.enchant.doga.converter.l3c.L3cConverter
import jp.dev7.enchant.doga.converter.l3p.L3pConverter
import play.api.Play.current
import play.api.db.DB
import play.api.mvc.{ Controller, Action }
import settings.Setting
import models.Model

object Jsons extends Controller {
  val log = LoggerFactory.getLogger("application")

  def data(dataName: String) = dataName match {
    case dn if dn.endsWith(".l3p") => convertAndJson(dn, new L3pConverter)
    case dn if dn.endsWith(".l3p.js") => convertAndJson(dn.substring(0, dn.length - 3), new L3pConverter)
    case dn if dn.endsWith(".l3c") => convertAndJson(dn, new L3cConverter)
    case dn if dn.endsWith(".l3c.js") => convertAndJson(dn.substring(0, dn.length - 3), new L3cConverter)
    case _ => Action(NotFound)
  }

  type CONVERTER = {
    def convertAndWriteJson(file: File, out: Appendable): Unit
  }
  def convertAndJson(dataName: String, converter: CONVERTER) = Action { request =>
    val dataFile = new File(Setting.dataDir, dataName)
    val cacheFile = new File(Setting.cacheDir, dataName + ".js")

    log.debug("data file = " + dataFile)
    if (dataFile.exists()) {
      DB.withConnection { implicit connection =>
        SQL("update model set last_accessed_at={now} where root_file={dataName}")
          .on("now" -> new Date, "dataName" -> dataName)
          .execute()
      }

      def doConvert() = {
        val writer = new OutputStreamWriter(new FileOutputStream(cacheFile), "UTF-8")
        try {
          log.info("json convert [" + dataFile + "] started...")
          converter.convertAndWriteJson(dataFile, writer)
          log.info("json convert [" + dataFile + "] success.")
          writer.flush()
        } finally {
          writer.close()
        }
      }

      if (!cacheFile.exists()) {
        log.debug("no cache")
        Files.createParentDirs(cacheFile)

        doConvert()
      } else if (dataFile.lastModified() > cacheFile.lastModified()) {
        log.debug("cache is old")
        doConvert()
      } else {
        log.debug("cache hit")
      }

      Ok.sendFile(cacheFile).as("application/json")
    } else {
      log.debug("model data is not found")
      NotFound
    }
  }

}
