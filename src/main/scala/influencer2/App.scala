package influencer2

import zio.http.model.Method
import zio.http.{Http, Response, Server}
import zio.{Task, UIO, ZIO, ZIOApp, ZIOAppDefault}
import zio.http.*

object App extends ZIOAppDefault:
  private val httpApp = Http.collect[Request] {
    case request @ Method.PUT -> !! / "api" / "users" / userId    => dummyResponse(request)
    case request @ Method.DELETE -> !! / "api" / "users" / userId => dummyResponse(request)

    case request @ Method.POST -> !! / "api" / "sessions"               => dummyResponse(request)
    case request @ Method.DELETE -> !! / "api" / "sessions" / sessionId => dummyResponse(request)

    case request @ Method.GET -> !! / "api" / "feeds" / userId => dummyResponse(request)

    case request @ Method.PUT -> !! / "api" / "posts" / postId => dummyResponse(request)
    case request @ Method.GET -> !! / "api" / "posts" if request.url.queryParams.get("userId").isDefined =>
      dummyResponse(request)
    case request @ Method.GET -> !! / "api" / "posts" / postId                       => dummyResponse(request)
    case request @ Method.PUT -> !! / "api" / "posts" / postId / "likes" / userId    => dummyResponse(request)
    case request @ Method.DELETE -> !! / "api" / "posts" / postId / "likes" / userId => dummyResponse(request)

    case request @ Method.GET -> !! / "api" / "notifications" / userId => dummyResponse(request)
  }

  private def dummyResponse(request: Request): Response =
    Response.text(s"${request.method} ${request.url.toJavaURI}")

  override def run: Task[Unit] = for
    _ <- zio.Console.printLine("hello world")
    _ <- Server.serve(httpApp).provide(Server.default)
  yield ()
end App
