package influencer2.http

import influencer2.http.AppJsonCodec.given_JsonCodec_SessionUser
import influencer2.user.UserService
import zio.http.*
import zio.http.model.Method.{DELETE, GET, POST, PUT}
import zio.http.model.Status
import zio.json.DecoderOps
import zio.{UIO, URLayer, ZIO, ZLayer}

class AppRouter(userController: UserController, sessionController: SessionController, postController: PostController):
  private def allRoutes(sessionUserOption: Option[SessionUser]): UHttpApp = {
    val pf: PartialFunction[(Option[SessionUser], Request), UIO[Response]] = {
      case (_, request @ PUT -> !! / "users" / username) => userController.handleCreateUser(username, request)
      case (_, GET -> !! / "users" / username)           => userController.handleReadUser(username)

      case (_, request @ POST -> !! / "sessions") => sessionController.handleCreateSession(request)
      case (Some(_), DELETE -> !! / "sessions")   => sessionController.handleDeleteSession

      case (Some(_), request @ GET -> !! / "feeds" / username) => dummyResponse(request)

      case (Some(user), request @ POST -> !! / "posts") => postController.handleCreatePost(user, request)
      case (_, request @ GET -> !! / "posts") if request.url.queryParams.get("username").isDefined =>
        dummyResponse(request)
      case (_, GET -> !! / "posts")                                                  => postController.handleReadPosts
      case (_, request @ GET -> !! / "posts" / postId)                               => dummyResponse(request)
      case (Some(_), request @ PUT -> !! / "posts" / postId / "likes" / username)    => dummyResponse(request)
      case (Some(_), request @ DELETE -> !! / "posts" / postId / "likes" / username) => dummyResponse(request)

      case (Some(_), request @ GET -> !! / "notifications" / username) => dummyResponse(request)
    }

    Http.collectZIO[Request](pf.compose(sessionUserOption -> _))
  }

  private def withSessionUser(authenticatedRoutes: Option[SessionUser] => UHttpApp): UHttpApp =
    Http.fromHttpZIO(sessionController.extractSessionUser(_).map(authenticatedRoutes))

  val routes: UHttpApp = withSessionUser(allRoutes)

  private def dummyResponse(request: Request): UIO[Response] =
    ZIO.succeed(Response.text(s"${request.method} ${request.url.toJavaURI}"))
end AppRouter

object AppRouter:
  val layer: URLayer[UserController & SessionController & PostController, AppRouter] = ZLayer {
    for
      userController    <- ZIO.service[UserController]
      sessionController <- ZIO.service[SessionController]
      postController    <- ZIO.service[PostController]
    yield AppRouter(userController, sessionController, postController)
  }
