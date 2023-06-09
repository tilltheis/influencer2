package influencer2

import influencer2.application.{AppRouter, JwtCodec, PostController, SessionController, UserController}
import influencer2.domain.{PostService, UserService}
import zio.{Cause, RLayer, Trace, UIO, ULayer, ZIO, ZLayer}
import zio.http.{Handler, HttpAppMiddleware, Request, RequestHandlerMiddleware, Response, Server}

import java.util.Base64
import javax.crypto.spec.SecretKeySpec

object HttpModule:
  private val jwtCodecLayer: ULayer[JwtCodec] =
    // should come from config/secret store
    val base64Key = "XAtCdfixJz9JPJOsynaqTSkZp8TbHXDKgaFWWw72t+Q="
    val key       = new SecretKeySpec(Base64.getDecoder.decode(base64Key.getBytes("UTF-8")), "HmacSHA256")
    ZLayer.succeed(key) >>> JwtCodec.layer

  val layer: RLayer[UserService & PostService, Server] =
    val zio = for
      appRouter <- ZIO.service[AppRouter]
      server    <- ZIO.service[Server]
      httpApp = loggingHttpApp(appRouter)
      _ <- server.install(httpApp, Some(errorCallback))
      _ <- ZIO.log(s"HTTP server listening on port ${server.port}")
      _ <- ZIO.addFinalizer(ZIO.log("HTTP server shut down"))
    yield ()

    ZLayer.makeSome[UserService & PostService, Server](
      AppRouter.layer,
      jwtCodecLayer,
      UserController.layer,
      SessionController.layer,
      PostController.layer,
      Server.default,
      ZLayer.scoped(zio)
    )
  end layer

  private val correlationIdLogAnnotationMiddleware: RequestHandlerMiddleware[Nothing, Any, Nothing, Any] =
    new RequestHandlerMiddleware.Simple[Any, Nothing]:
      override def apply[Env <: Any, Err >: Nothing](
          handler: Handler[Env, Err, Request, Response]
      )(using Trace): Handler[Env, Err, Request, Response] =
        Handler.fromFunctionZIO { request =>
          ZIO.scoped {
            for
              correlationId <- ZIO.random.flatMap(_.nextUUID)
              _             <- ZIO.logAnnotateScoped("correlationId", correlationId.toString)
              response      <- handler(request)
            yield response
          }
        }

  private def loggingHttpApp(router: AppRouter) =
    val loggingHttpApp    = router.routes @@ HttpAppMiddleware.requestLogging() @@ correlationIdLogAnnotationMiddleware
    val allLoggingHttpApp = loggingHttpApp.tapErrorCauseZIO(ZIO.logErrorCause("request failed", _))
    allLoggingHttpApp.withDefaultErrorResponse

  private def errorCallback(cause: Cause[Nothing]): UIO[Unit] = ZIO.logErrorCause("unhandled error", cause)

end HttpModule
