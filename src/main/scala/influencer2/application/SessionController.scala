package influencer2.application

import influencer2.application.SessionController.UseSecureCookies
import influencer2.application.AppJsonCodec.given
import influencer2.application.Controller.withJsonRequest
import influencer2.application.JwtSignatureCookieName
import influencer2.domain.UserService
import zio.http.model.{Cookie, Status}
import zio.http.{Request, Response}
import zio.json.{DecoderOps, EncoderOps, JsonDecoder}
import zio.{UIO, URLayer, ZIO, ZLayer}

class SessionController(jwtCodec: JwtCodec, userService: UserService):
  def handleCreateSession(request: Request): UIO[Response] =
    withJsonRequest[LoginRequest](request) { login =>
      userService.verifyCredentials(login.username, login.password).either.flatMap {
        case Left(_) =>
          ZIO.succeed(Response.json(MessageResponse("invalid credentials").toJson).setStatus(Status.Unauthorized))
        case Right(user) =>
          for
            now <- zio.Clock.currentDateTime
            expiresAt        = now.plusDays(30)
            expiresInSeconds = expiresAt.toEpochSecond - now.toEpochSecond
            sessionUser      = SessionUser.fromUser(user)
            (header, payload, signature) <-
              jwtCodec.encodeJwtIntoHeaderPayloadSignature(sessionUser.toJson, expiresAt.toInstant)
            loginResponse = LoginResponse(s"$header.$payload")
          yield
            def makeCookie(name: String, content: String, isHttpOnly: Boolean) = Cookie(
              name,
              content,
              isSecure = UseSecureCookies,
              isHttpOnly = true,
              maxAge = Some(expiresInSeconds)
            )

            Response
              .json(loginResponse.toJson)
              .addCookie(makeCookie(JwtSignatureCookieName, signature, isHttpOnly = true))
      }
    }
  end handleCreateSession

  val handleDeleteSession: UIO[Response] =
    // ideally, this would also blacklist the not yet expired tokens
    ZIO.succeed(
      Response
        .json(MessageResponse("logged out").toJson)
        .setStatus(Status.Ok)
        .addCookie(Cookie.clear(JwtSignatureCookieName).withHttpOnly)
    )

  def extractSessionUser(request: Request): UIO[Option[SessionUser]] =
    (for
      headerPayload     <- ZIO.from(request.bearerToken)
      (header, payload) <- ZIO.succeed(headerPayload.split('.')).collect(()) { case Array(x, y) => (x, y) }
      signature         <- ZIO.from(request.cookieValue(JwtSignatureCookieName))
      claim             <- jwtCodec.decodeJwtFromHeaderPayloadSignature(header, payload, signature.toString)
      sessionUser       <- ZIO.from(claim.fromJson[SessionUser])
    yield sessionUser).option

end SessionController

object SessionController:
  private val UseSecureCookies = false // should come from config or be set by reverse proxy

  val layer: URLayer[JwtCodec & UserService, SessionController] = ZLayer {
    for
      jwtCodec    <- ZIO.service[JwtCodec]
      userService <- ZIO.service[UserService]
    yield SessionController(jwtCodec, userService)
  }
