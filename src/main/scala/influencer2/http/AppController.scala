package influencer2.http

import influencer2.http.JwtSignatureCookieName
import influencer2.http.AppController.UseSecureCookies
import influencer2.http.AppJsonCodec.given
import influencer2.user.{CreateUser, UserService}
import zio.http.model.{Cookie, Status}
import zio.http.{Request, Response}
import zio.json.{DecoderOps, EncoderOps, JsonDecoder}
import zio.{UIO, URLayer, ZIO, ZLayer}

import java.util.Base64
import javax.crypto.spec.SecretKeySpec

class AppController(jwtCodec: JwtCodec, userService: UserService):
  def handleCreateUser(request: Request): UIO[Response] =
    userService
      .createUser(CreateUser("till", "passwordHash"))
      .fold(
        _ => Response.status(Status.Conflict),
        _ => Response.text("user created").setStatus(Status.Created)
      )

  def handleCreateSession(request: Request): UIO[Response] =
    withJsonRequest[LoginRequest](request) { login =>
      userService.login(login.username, login.password).either.flatMap {
        case Left(_) =>
          ZIO.succeed(Response.json(ErrorResponse("invalid credentials").toJson).setStatus(Status.Unauthorized))
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

  def handleDeleteSession(request: Request): UIO[Response] =
    ZIO.succeed(Response.status(Status.Ok).addCookie(Cookie.clear(JwtSignatureCookieName)))

  private def withJsonRequest[A: JsonDecoder](request: Request)(f: A => UIO[Response]): UIO[Response] =
    val withJsonContentType =
      if request.hasJsonContentType then ZIO.succeed(request)
      else ZIO.fail(Status.UnsupportedMediaType -> "expected json content type")
    val withStringBody =
      withJsonContentType.flatMap(_.body.asString.orElseFail(Status.BadRequest -> "request body must be string"))
    val withEntityBody = withStringBody.map(_.fromJson[A]).flatMap {
      case Left(jsonErrorMessage) => ZIO.fail(Status.BadRequest -> jsonErrorMessage)
      case Right(entity)          => ZIO.succeed(entity)
    }
    val withResponse = withEntityBody.flatMap(f)
    withResponse.catchAll { case (status, text) => ZIO.succeed(Response.json(s"\"$text\"").setStatus(status)) }

end AppController

object AppController:
  private val UseSecureCookies = false // should come from config or be set by reverse proxy

  val layer: URLayer[JwtCodec & UserService, AppController] = ZLayer {
    for
      jwtCodec    <- ZIO.service[JwtCodec]
      userService <- ZIO.service[UserService]
    yield AppController(jwtCodec, userService)
  }
