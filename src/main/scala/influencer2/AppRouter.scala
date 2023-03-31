package influencer2

import influencer2.user.{CreateUser, UserService}
import zio.{UIO, URLayer, ZIO, ZLayer}
import zio.http.model.{Method, Status}
import zio.http.*

class AppRouter(userService: UserService):
  val routes = Http.collectZIO[Request] {
    case request @ Method.PUT -> !! / "api" / "users" / userName =>
      userService
        .createUser(CreateUser("till", "passwordHash"))
        .fold(
          _ => Response.status(Status.Conflict),
          _ => Response.text("user created").setStatus(Status.Created)
        )
    case request @ Method.DELETE -> !! / "api" / "users" / userName => dummyResponse(request)

    case request @ Method.POST -> !! / "api" / "sessions"               => dummyResponse(request)
    case request @ Method.DELETE -> !! / "api" / "sessions" / sessionId => dummyResponse(request)

    case request @ Method.GET -> !! / "api" / "feeds" / userName => dummyResponse(request)

    case request @ Method.PUT -> !! / "api" / "posts" / postId => dummyResponse(request)
    case request @ Method.GET -> !! / "api" / "posts" if request.url.queryParams.get("userName").isDefined =>
      dummyResponse(request)
    case request @ Method.GET -> !! / "api" / "posts" / postId                         => dummyResponse(request)
    case request @ Method.PUT -> !! / "api" / "posts" / postId / "likes" / userName    => dummyResponse(request)
    case request @ Method.DELETE -> !! / "api" / "posts" / postId / "likes" / userName => dummyResponse(request)

    case request @ Method.GET -> !! / "api" / "notifications" / userName => dummyResponse(request)
  }

  private def dummyResponse(request: Request): UIO[Response] =
    ZIO.succeed(Response.text(s"${request.method} ${request.url.toJavaURI}"))
end AppRouter

object AppRouter:
  val layer: URLayer[UserService, AppRouter] = ZLayer.fromFunction(AppRouter(_))
