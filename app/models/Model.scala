package models

import java.sql.Connection
import java.util.Date

import anorm.SqlParser._
import anorm.{ toParameterValue, sqlToSimple, SQL }

object Model {
  val tupleParser =
    long("id") ~
      str("root_file") ~
      get[Option[String]]("name") ~
      str("author") ~
      date("created_at") ~
      date("updated_at") ~
      date("last_accessed_at") ~
      get[Option[String]]("comment") ~
      int("publication") map (flatten)
  val modelParser = tupleParser.map(result => Model(result._1, result._2, result._3, result._4, result._5, result._6, result._7, result._8, result._9))

  def apply(rootFile: String, author: String): Model = Model.apply(-1, rootFile, None, author, null, null, null, None, 0)

  def create(rootFile: String, name: String, author: String, comment: Option[String], publication: Int)(implicit connection: Connection) = {
    findByRootFile(rootFile).map { found =>
      None
    }.getOrElse {
      val now = new Date
      val ins = SQL("""
		insert into model (
          root_file,
          name,
          author,
          created_at,
          updated_at,
          last_accessed_at,
          comment,
          publication
		) values (
          {root_file},
          {name},
          {author},
          {created_at},
          {updated_at},
          {last_accessed_at},
          {comment},
          {publication}
		)""").on(
        "root_file" -> rootFile,
        "name" -> name,
        "author" -> author,
        "created_at" -> now,
        "updated_at" -> now,
        "last_accessed_at" -> now,
        "comment" -> comment,
        "publication" -> publication)
      ins.executeInsert().flatMap { id =>
        findById(id)
      }
    }
  }
  def findById(id: Long)(implicit connection: Connection) = {
    SQL("select * from model where id = {id}")
      .on("id" -> id)
      .singleOpt(modelParser)
  }
  def findByRootFile(rootFile: String)(implicit connection: Connection) = {
    SQL("select * from model where root_file = {root_file}")
      .on("root_file" -> rootFile)
      .singleOpt(modelParser)
  }
  def selectByAuthor(author: String)(implicit connection: Connection) = {
    SQL("select * from model where author = {author} order by created_at desc")
      .on("author" -> author)
      .as(modelParser.*)
  }
  def selectByKeywords(keyword: String)(implicit connection: Connection) = {
    SQL("select * from model where name like '%' || {keyword} || '%' order by created_at desc")
      .on("keyword" -> keyword)
      .as(modelParser.*)
  }
  def selectAll()(implicit connection: Connection) = {
    SQL("select * from model order by created_at desc")
      .as(modelParser.*)
  }
  def save(model: Model)(implicit connection: Connection) = {
    val now = new Date
    val upd = SQL("""
		update model set
		  root_file = {root_file},
          name = {name},
          author = {author},
		  created_at = {created_at}, 
		  updated_at = {updated_at}, 
		  last_accessed_at = {last_accessed_at},
          comment = {comment},
          publication = {publication}
		where id = {id}""").on(
      "id" -> model.id,
      "root_file" -> model.rootFile,
      "name" -> model.name,
      "author" -> model.author,
      "created_at" -> model.createdAt,
      "updated_at" -> now,
      "last_accessed_at" -> model.lastAccessedAt,
      "comment" -> model.comment,
      "publication" -> model.publication)

    if (upd.execute) {
      findById(model.id)
    } else {
      None
    }
  }
  def delete(model: Model)(implicit connection: Connection) = {
    SQL("delete from model where id={id}")
      .on("id" -> model.id)
      .execute()
  }

}

case class Model(id: Long, rootFile: String, name: Option[String], author: String, createdAt: Date, updatedAt: Date, lastAccessedAt: Date, comment: Option[String], publication: Int) {
  def jsonFileName = new java.io.File(rootFile).getName() + ".js"
  def edit = ModelEdit(id, rootFile, name.getOrElse(""), comment, publication)
  def thumbnail = rootFile + ".bmp"
}

case class ModelEdit(id: Long, rootFile: String, name: String, comment: Option[String], publication: Int) {
  def save(user: User)(implicit connection: Connection) = {
    val now = new Date
    Model.findById(id).map { model =>
      val m = Model(id, rootFile, Some(name), user.name, model.createdAt, now, now, comment, publication)
      Model.save(m)
    }.getOrElse {
      Model.create(rootFile, name, user.name, comment, publication)
    }
  }
}

case class User(name: String)
