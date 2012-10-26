package util
import anorm.MayErr.eitherToError
import anorm.Column
import anorm.MetaDataItem
import anorm.TypeDoesNotMatch

object DbUtil {

  implicit val columnForBlob: Column[java.sql.Blob] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case blob: java.sql.Blob => Right(blob)
      case _ => Left(TypeDoesNotMatch("Cannot convert"))
    }
  }

}