package influencer2.http

import influencer2.http.AppJsonCodec.given_JsonCodec_SessionUser
import influencer2.user.UserService
import zio.http.*
import zio.http.model.Method.{DELETE, GET, POST, PUT}
import zio.http.model.Status
import zio.json.DecoderOps
import zio.{UIO, URLayer, ZIO, ZLayer}

class AppRouter(jwtCodec: JwtCodec, appController: AppController):
  private def allRoutes(sessionUserOption: Option[SessionUser]): UHttpApp = {
    val pf: PartialFunction[(Option[SessionUser], Request), UIO[Response]] = {
      case (_, request @ PUT -> !! / "users" / username) => appController.handleCreateUser(username, request)
      case (_, GET -> !! / "users" / username)           => appController.handleReadUser(username)

      case (_, request @ POST -> !! / "sessions")         => appController.handleCreateSession(request)
      case (Some(_), request @ DELETE -> !! / "sessions") => appController.handleDeleteSession(request)

      case (Some(_), request @ GET -> !! / "feeds" / username) => dummyResponse(request)

      case (Some(_), request @ PUT -> !! / "posts" / postId) => dummyResponse(request)
      case (_, request @ GET -> !! / "posts") if request.url.queryParams.get("username").isDefined =>
        dummyResponse(request)
      case (_, request @ GET -> !! / "posts" / postId)                               => dummyResponse(request)
      case (Some(_), request @ PUT -> !! / "posts" / postId / "likes" / username)    => dummyResponse(request)
      case (Some(_), request @ DELETE -> !! / "posts" / postId / "likes" / username) => dummyResponse(request)

      case (Some(_), request @ GET -> !! / "notifications" / username) => dummyResponse(request)
    }

    Http.collectZIO[Request](pf.compose(sessionUserOption -> _))
  }

  private def withSessionUser(authenticatedRoutes: Option[SessionUser] => UHttpApp): UHttpApp =
    Http.fromHttpZIO { (request: Request) =>
      val sessionUserZio = for
        headerPayload     <- ZIO.from(request.bearerToken)
        (header, payload) <- ZIO.succeed(headerPayload.split('.')).collect(()) { case Array(x, y) => (x, y) }
        signature         <- ZIO.from(request.cookieValue(JwtSignatureCookieName))
        claim             <- jwtCodec.decodeJwtFromHeaderPayloadSignature(header, payload, signature.toString)
        sessionUser       <- ZIO.from(claim.fromJson[SessionUser])
      yield sessionUser
      sessionUserZio.option.map(authenticatedRoutes)
    }

  val routes: UHttpApp = withSessionUser(allRoutes)

  private def dummyResponse(request: Request): UIO[Response] =
    ZIO.succeed(Response.text(s"${request.method} ${request.url.toJavaURI}"))
end AppRouter

object AppRouter:
  val layer: URLayer[JwtCodec & AppController, AppRouter] = ZLayer {
    for
      jwtCodec      <- ZIO.service[JwtCodec]
      appController <- ZIO.service[AppController]
    yield AppRouter(jwtCodec, appController)
  }
