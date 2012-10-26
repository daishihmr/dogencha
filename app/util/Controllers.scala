package util

import java.io.File
import play.api.mvc.RequestHeader
import settings.Setting

object Controllers {

  def param(name: String)(implicit request: RequestHeader) = {
    request.queryString.get(name).map {
      p => p.headOption
    }.getOrElse {
      None
    }
  }

  def dataPath(file: File) = {
    val absDataDir = new File(Setting.dataDir).getAbsolutePath
    file.getAbsolutePath.replace(absDataDir, "").replace(File.separatorChar, '/') match {
      case n if n.startsWith("/") => n.substring(1)
      case n => n
    }
  }

}