package influencer2.http

import influencer2.user.UserService
import zio.{UIO, URLayer, ZIO, ZLayer}
import zio.http.*
import zio.http.model.Method

class AppRouter(appController: AppController):
  val routes: UHttpApp = Http.collectZIO[Request] {
    case request @ Method.PUT -> !! / "api" / "users" / username    => appController.handleCreateUser(request)
    case request @ Method.DELETE -> !! / "api" / "users" / username => dummyResponse(request)

    case request @ Method.POST -> !! / "api" / "sessions"   => appController.handleCreateSession(request)
    case request @ Method.DELETE -> !! / "api" / "sessions" => dummyResponse(request)

    case request @ Method.GET -> !! / "api" / "feeds" / username => dummyResponse(request)

    case request @ Method.PUT -> !! / "api" / "posts" / postId => dummyResponse(request)
    case request @ Method.GET -> !! / "api" / "posts" if request.url.queryParams.get("username").isDefined =>
      dummyResponse(request)
    case request @ Method.GET -> !! / "api" / "posts" / postId                         => dummyResponse(request)
    case request @ Method.PUT -> !! / "api" / "posts" / postId / "likes" / username    => dummyResponse(request)
    case request @ Method.DELETE -> !! / "api" / "posts" / postId / "likes" / username => dummyResponse(request)

    case request @ Method.GET -> !! / "api" / "notifications" / username => dummyResponse(request)
  }

  private def dummyResponse(request: Request): UIO[Response] =
    ZIO.succeed(Response.text(s"${request.method} ${request.url.toJavaURI}"))
end AppRouter

object AppRouter:
  val layer: URLayer[AppController, AppRouter] = ZLayer.fromFunction(AppRouter(_))
