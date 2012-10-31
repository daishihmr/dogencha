package controllers

import java.io.{ FileInputStream, File }
import java.util.zip.ZipInputStream
import java.util.Date
import org.slf4j.LoggerFactory
import com.google.common.io.Files
import anorm.SqlParser._
import anorm.SQL
import models.{ User, Model }
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.db.DB
import play.api.mvc.{ Session, Controller, Action }
import settings.Setting
import util.Controllers.param
import util.Twitters
import jp.dev7.unzip.Unzip
import play.api.mvc.SimpleResult
import play.api.mvc.AnyContent
import play.api.mvc.Result
import models.ModelEdit

object Application extends Controller {
  val log = LoggerFactory.getLogger("application")

  implicit def loginUser(implicit session: Session) = {
    Twitters.fromSession.map(t => User(t.getScreenName()))
  }
  def where(implicit session: Session) = {
    "(publication = 0" + loginUser.map(u => " or author = '%s')".format(u.name)).getOrElse(")")
  }

  val notFoundPage = NotFound(<div><h1>404</h1><p>そんなリソースないです</p></div>).as("text/html")
  val unauthorizedPage = Unauthorized(<div><h1>401</h1><a href="/login">ログイン</a>してくださいね</div>).as("text/html")
  val unauthorizedData = Unauthorized(<div><h1>401</h1>権限がないです</div>).as("text/html")

  /** トップページ表示 */
  def index = Action { implicit request =>
    val modelList = DB.withConnection { implicit connection =>
      SQL("select root_file from model where " + where +
        " order by updated_at desc limit 10").as(str("root_file").*).toList.map {
        _ match {
          case n if n.endsWith("l3p") || n.endsWith("l3c") => n
          case _ => null
        }
      }.filter(_ != null)
    }
    Ok(views.html.index(modelList))
  }

  /** Aboutページ表示 */
  def aboutPage = Action { implicit request =>
    Ok(views.html.download())
  }

  /** アップロードページ表示 */
  def uploadPage = Action { implicit request =>
    loginUser.map { user =>
      Ok(views.html.fileupload())
    }.getOrElse(unauthorizedPage)
  }

  /** ファイル一覧表示 */
  def selectFilePage = Action { implicit request =>
    loginUser.map { user =>
      Ok(views.html.selectFile(new File(Setting.dataDir, user.name)))
    }.getOrElse(unauthorizedPage)
  }

  /** ホームページ表示 */
  def homePage = Action { implicit request =>
    import Twitters._
    loginUser.map { user =>
      val modelList = DB.withConnection { implicit connection =>
        Model.selectByAuthor(user.name)
      }

      Ok(views.html.home(Twitters.fromSession.get, modelList))
    }.getOrElse(unauthorizedPage)
  }

  /** ユーザー情報ページ表示 */
  def userPage(userName: String) = TODO

  /** みんなのモデル */
  def modelsPage = Action { implicit request =>
    val modelList = DB.withConnection { implicit connection =>
      SQL("select * from model where " + where +
        " order by created_at desc")
        .as(Model.modelParser.*)
    }
    Ok(views.html.modelList(modelList, None))
  }
  /** みんなのモデル画面の検索 */
  def search(q: String) = Action { implicit request =>
    val modelList = DB.withConnection { implicit connection =>
      SQL("select * from model where " + where +
        " and (name like '%' || {keyword1} || '%' or comment like '%' || {keyword2} || '%')" +
        " order by created_at desc")
        .on("keyword1" -> q, "keyword2" -> q)
        .as(Model.modelParser.*)
    }

    Ok(views.html.modelList(modelList, Some(q)))
  }

  /** モデル単体の画面 */
  def modelPage(dataName: String) = Action { implicit request =>
    log.info("modelPage(" + dataName + ")")
    val dataFile = new File(Setting.dataDir, dataName)
    val modelOpt = DB.withConnection { implicit connection =>
      if (dataFile.exists()) {
        Model.findByRootFile(dataName match {
          case dn if dn.startsWith("/") => dn.substring(1)
          case dn => dn
        })
      } else {
        None
      }
    }
    modelOpt.map { model =>
      val visible = model.publication == 0 || loginUser.map(u => (u.name == model.author)).getOrElse(false)
      if (visible) {
        Ok(views.html.model(model))
      } else {
        unauthorizedData
      }
    }.getOrElse(notFoundPage)
  }

  /** モデル編集画面 */
  def modelEditPage(dataName: String) = Action { implicit request =>
    log.info("modelEditPage(" + dataName + ")")
    loginUser.map { user =>
      val modelOpt = DB.withConnection { implicit connection =>
        Model.findByRootFile(dataName match {
          case dn if dn.startsWith("/") => dn.substring(1)
          case _ => dataName
        })
      }
      modelOpt.map { model =>

        // update
        if (model.author == user.name) {

          Ok(views.html.modelEdit(model))

        } else {
          unauthorizedData
        }
      }.getOrElse {

        // new
        Ok(views.html.modelEdit(Model(dataName, user.name)))

      }
    }.getOrElse(unauthorizedPage)
  }
  def modelInner(dataName: String) = Action {
    Ok(views.html.modelInner(dataName))
  }

  /** ログイン処理 */
  def login = Action {
    val requestToken = Twitters.create.getOAuthRequestToken(Setting.loginCallbackUrl)

    Redirect(requestToken.getAuthenticationURL())
      .flashing(
        "requestToken" -> requestToken.getToken(),
        "requestTokenSecret" -> requestToken.getTokenSecret())
  }
  /** ログイン.Twitterからのコールバック処理 */
  def callback = Action { implicit request =>
    Twitters.requestTokenFromFlash.map { requestToken =>

      param("oauth_verifier").map { verifier =>

        val accessToken = Twitters.create.getOAuthAccessToken(requestToken, verifier)

        Redirect(routes.Application.homePage)
          .withSession(
            "accessToken" -> accessToken.getToken(),
            "accessTokenSecret" -> accessToken.getTokenSecret())

      }.getOrElse(unauthorizedPage)
    }.getOrElse(unauthorizedPage)
  }
  /** ログアウト処理 */
  def logout = Action {
    Redirect(routes.Application.index()).withNewSession
  }

  /** アップロード処理 */
  def upload = Action(parse.multipartFormData) { implicit request =>
    loginUser.map { user =>
      request.body.file("file").map { f =>
        if (!new File(Setting.userDir).exists) new File(Setting.userDir).mkdir

        val errorMessage = Files.getFileExtension(f.filename).toLowerCase match {
          case "zip" => {
            val unzip = new Unzip
            try {
              unzip.unzip(Setting.userDir, f.filename, f.ref.file, "suf", "atr", "l3p", "l3c", "bmp")
              None
            } catch {
              case e => {
                log.error("unzip error", e)
                Some("解凍に失敗しました")
              }
            }
          }
          case _ => Some("zipファイルをアップロードしてね")
        }

        errorMessage.map { msg =>
          Redirect(routes.Application.uploadPage).flashing("error" -> msg)
        } getOrElse {
          Redirect(routes.Application.selectFilePage)
        }
      }.getOrElse {
        Redirect(routes.Application.uploadPage).flashing("error" -> "ファイルがないよ")
      }
    }.getOrElse(unauthorizedPage)
  }

  /** ファイル一覧でルートファイルを決定した時 */
  def selectFile = Action { implicit request =>
    loginUser.map { user =>

      val form = Form(("dataName" -> text))
      val dataName = form.bindFromRequest.get
      log.debug("select File dataName = " + dataName)

      Redirect(routes.Application.modelEditPage(dataName))

    }.getOrElse(unauthorizedPage)
  }

  /** モデルデータの変更を保存する時 */
  def modelEdit(dataName: String) = Action { implicit request =>
    val modelForm: Form[ModelEdit] = Form.apply(
      mapping("id" -> longNumber,
        "rootFile" -> nonEmptyText,
        "name" -> nonEmptyText(maxLength = 100),
        "comment" -> optional(text(maxLength = 1000)),
        "publication" -> number)(ModelEdit.apply)(ModelEdit.unapply))

    loginUser.map { user =>
      val f = modelForm.bindFromRequest()
      if (!f.hasErrors) {
        DB.withConnection { implicit connection =>
          val exist = Model.findById(f.get.id)
          if (exist.isEmpty || exist.get.author == user.name) {
            f.get.save(user)
            Redirect(routes.Application.modelPage(dataName))
          } else {
            unauthorizedData
          }
        }
      } else {
        f.errors.foreach { e =>
          log.warn(e.key + ":" + e.message)
        }

        Redirect(routes.Application.modelEditPage(dataName)).flashing(
          "error" -> "入力にエラーあり")
      }
    }.getOrElse(unauthorizedPage)
  }

  /** モデルを削除する時 */
  def modelDelete(dataName: String) = Action { implicit request =>
    loginUser.map { user =>

      log.debug("dataName = " + dataName)
      DB.withConnection { implicit connection =>
        Model.findByRootFile(dataName)
      }.map { model =>

        if (model.author == user.name) {

          DB.withConnection { implicit connection =>
            Model.delete(model)
          }

          val cacheFile = new File(Setting.cacheDir, dataName + ".js")
          if (cacheFile.exists()) {
            cacheFile.delete()
          }

          Redirect(routes.Application.homePage)

        } else {
          unauthorizedData
        }
      }.getOrElse(notFoundPage)
    }.getOrElse(unauthorizedPage)
  }

  /** サムネイル */
  def modelThumbnail(bmpFile: String) = Action { implicit request =>
    val file = new File(Setting.dataDir, bmpFile)
    if (file.exists()) {
      Ok.sendFile(file).withHeaders(CONTENT_TYPE -> "image/x-windows-bmp")
    } else {
      NotFound
    }
  }
}

