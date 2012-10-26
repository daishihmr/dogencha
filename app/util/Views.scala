package util

object Views {

  def format(date: java.util.Date) = date match {
    case null => ""
    case _ => new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date)
  }

}