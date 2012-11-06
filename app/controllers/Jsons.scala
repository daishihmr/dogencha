package controllers

import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStreamWriter
import java.util.Date

import org.slf4j.LoggerFactory

import com.google.common.io.ByteStreams
import com.google.common.io.Files
import com.google.common.io.InputSupplier

import anorm.SQL
import anorm.sqlToSimple
import anorm.toParameterValue
import jp.dev7.enchant.doga.converter.l3c.L3cConverter
import jp.dev7.enchant.doga.converter.l3p.L3pConverter
import play.api.Play.current
import play.api.db.DB
import play.api.libs.iteratee.Enumerator
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.ResponseHeader
import play.api.mvc.SimpleResult
import settings.Setting
import util.Controllers.param

object Jsons extends Controller {
  val log = LoggerFactory.getLogger("application")

  def countup(dataName: String) = Action {
    log.info("download " + dataName)
    DB.withConnection { implicit connection =>
      SQL("update model " +
        "set download_count = download_count + 1 " +
        "where root_file={dataName}")
        .on("dataName" -> dataName)
        .execute()
    }
    Ok
  }

  def data(dataName: String) = process(dataName, false)

  def jsonp(dataName: String) = process(dataName, true)

  def process(dataName: String, jsonp: Boolean) = dataName match {
    case dn if dn.endsWith(".l3p") => convertAndJson(jsonp, dn, new L3pConverter)
    case dn if dn.endsWith(".l3p.js") => convertAndJson(jsonp, dn.substring(0, dn.length - 3), new L3pConverter)
    case dn if dn.endsWith(".l3c") => convertAndJson(jsonp, dn, new L3cConverter)
    case dn if dn.endsWith(".l3c.js") => convertAndJson(jsonp, dn.substring(0, dn.length - 3), new L3cConverter)
    case _ => Action(NotFound)
  }

  type CONVERTER = {
    def convertAndWriteJson(file: File, out: Appendable): Unit
  }
  def convertAndJson(jsonp: Boolean, dataName: String, converter: CONVERTER) = Action { implicit request =>
    val dataFile = new File(Setting.dataDir, dataName)
    val cacheFile = new File(Setting.cacheDir, dataName + ".js")

    log.debug("data file = " + dataFile)
    if (dataFile.exists()) {
      // 最終アクセス日時を更新
      DB.withConnection { implicit connection =>
        SQL("update model " +
          "set last_accessed_at={now} " +
          "where root_file={dataName}")
          .on("now" -> new Date, "dataName" -> dataName)
          .execute()
      }

      // キャッシュファイル存在チェック
      if (!cacheFile.exists()) {
        // ない
        log.debug("no cache")
        Files.createParentDirs(cacheFile)
        cache(converter, dataFile, cacheFile)
      } else if (dataFile.lastModified() > cacheFile.lastModified()) {
        // 古い
        log.debug("cache is old")
        cache(converter, dataFile, cacheFile)
      } else {
        // ある
        log.debug("cache hit")
      }

      def send(file: File) = {
        if (jsonp) {
          implicit def iToI[I <: InputStream](i: I): InputSupplier[I] = {
            new InputSupplier[I] { def getInput = i }
          }

          param("callback") map { callback =>
            val open = new ByteArrayInputStream((callback + "(").getBytes())
            val json = new FileInputStream(file)
            val close = new ByteArrayInputStream(")".getBytes())
            val joined = ByteStreams.join(open, json, close)

            SimpleResult(
              header = ResponseHeader(OK, Map(CONTENT_TYPE -> JAVASCRIPT)),
              Enumerator.fromStream(joined.getInput()))

          } getOrElse {
            BadRequest
          }
        } else Ok.sendFile(file).as(JSON)
      }

      send(cacheFile)
    } else {
      log.debug("model data is not found")
      NotFound
    }
  }

  def cache(converter: CONVERTER, dataFile: File, cacheFile: File) = {
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

}
