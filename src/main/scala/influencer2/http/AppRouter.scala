package influencer2.http

import influencer2.http.AppJsonCodec.given_JsonCodec_SessionUser
import influencer2.user.UserService
import zio.http.*
import zio.http.model.{Method, Status}
import zio.json.DecoderOps
import zio.{UIO, URLayer, ZIO, ZLayer}

class AppRouter(jwtCodec: JwtCodec, appController: AppController):
  private def privateRoutes(sessionUser: SessionUser): UHttpApp = Http.collectZIO[Request] {
    case request @ Method.DELETE -> !! / "api" / "users" / username => dummyResponse(request)

    case request @ Method.DELETE -> !! / "api" / "sessions" => appController.handleDeleteSession(request)

    case request @ Method.GET -> !! / "api" / "feeds" / username => dummyResponse(request)

    case request @ Method.PUT -> !! / "api" / "posts" / postId                         => dummyResponse(request)
    case request @ Method.PUT -> !! / "api" / "posts" / postId / "likes" / username    => dummyResponse(request)
    case request @ Method.DELETE -> !! / "api" / "posts" / postId / "likes" / username => dummyResponse(request)

    case request @ Method.GET -> !! / "api" / "notifications" / username => dummyResponse(request)
  }

  private val publicRoutes: UHttpApp = Http.collectZIO[Request] {
    case request @ Method.PUT -> !! / "api" / "users" / username => appController.handleCreateUser(request)

    case request @ Method.POST -> !! / "api" / "sessions" => appController.handleCreateSession(request)

    case request @ Method.GET -> !! / "api" / "posts" if request.url.queryParams.get("username").isDefined =>
      dummyResponse(request)
    case request @ Method.GET -> !! / "api" / "posts" / postId => dummyResponse(request)
  }

  private def authenticate(authenticatedRoutes: SessionUser => UHttpApp): UHttpApp =
    Http.fromHttpZIO { (request: Request) =>
      val zio = for
        headerPayload     <- ZIO.from(request.bearerToken)
        (header, payload) <- ZIO.succeed(headerPayload.split('.')).collect(()) { case Array(x, y) => (x, y) }
        signature         <- ZIO.from(request.cookieValue(JwtSignatureCookieName))
        claim             <- jwtCodec.decodeJwtFromHeaderPayloadSignature(header, payload, signature.toString)
        sessionUser       <- ZIO.from(claim.fromJson[SessionUser])
      yield authenticatedRoutes(sessionUser)
      zio.orElseSucceed(Http.empty)
    }

  val routes: UHttpApp = publicRoutes ++ authenticate(privateRoutes)

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
