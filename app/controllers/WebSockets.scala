package controllers

import play.api.mvc._
import play.api.libs.iteratee._
import play.api.libs.json.JsValue
import models.Client

object WebSockets {

  /**
   * 一回「Hello」って返すだけ.
   */
  def hello = WebSocket.using[String] { request =>
    println("open")

    val out = Enumerator("Hello")

    val in = Iteratee.foreach[String] { message =>
      println(message)
    }.mapDone { _ =>
      println("Disconnected")
    }

    (in, out)
  }

  /**
   * エコー.
   */
  def echo = WebSocket.using[String] { request =>
    println("open")

    val out = Enumerator.imperative[String]()

    val in = Iteratee.foreach[String] { message =>
      println("receive " + message)
      out.push("echo " + message)
    }.mapDone { _ =>
      println("Disconnected")
      out.close()
    }

    (in, out)
  }

  def start(userName: String) = WebSocket.async[JsValue] { request =>
    Client.start(userName)
  }

}
