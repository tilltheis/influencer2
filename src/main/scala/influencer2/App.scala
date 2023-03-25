package influencer2

import zio.http.model.Method
import zio.http.{Http, Response, Server}
import zio.{ZIO, ZIOApp, ZIOAppDefault}

import zio.http.*

object App extends ZIOAppDefault {
  private val httpApp = Http.collect {
    case Method.GET -> !! / path => Response.text(s"hello $path")
  }

  override def run: ZIO[Any, Any, Any] = for {
    _ <- zio.Console.printLine("hello world")
    _ <- Server.serve(httpApp).provide(Server.default)
  } yield ()
}
