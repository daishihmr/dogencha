package models

import akka.actor._
import akka.pattern.ask
import akka.util._
import akka.util.duration._
import play.libs.Akka
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import play.api.Play.current

object Actors {

}

object Lobby {

  implicit val timeout = Timeout(1 second)

  lazy val default = Akka.system.actorOf(Props[Lobby])

}

class Lobby extends Actor {

  var waiting: Option[ActorRef] = None

  def receive = {

    case Join(newcomer) => {
      waiting.map { host =>

        host ! Match(sender)
        sender ! Match(host)
        waiting = None

      }.getOrElse {
        waiting = Some(newcomer)
      }
    }

  }
}

object Client {

  implicit val timeout = Timeout(1 second)

  def start(userName: String) = {
    val client = Akka.system.actorOf(Props(new Client(userName)))
    val future = client ? Start(userName)
    future.asPromise.map {
      case StartSuccess(out) => {
        val in = Iteratee.foreach[JsValue] { message =>
          client ! Input(message)
        }

        (in, out)
      }
    }
  }
}

class Client(userName: String) extends Actor {

  val out = Enumerator.imperative[JsValue]()

  def receive = {
    case Start(userName) => {
      println("client start!")

      sender ! StartSuccess(out)
    }

    case Input(message) => {
      val name = (message \ "name").as[String]
      println("receive message.name = " + name)
      
      val json = JsObject(Seq("body" -> JsString("hello " + name)))
      out.push(json)
    }

  }
}

case class Start(userName: String)
case class StartSuccess(out: PushEnumerator[JsValue])
case class Input(message: JsValue)
case class Quit(userName: String)

case class Join(client: ActorRef)
case class Match(client: ActorRef)
