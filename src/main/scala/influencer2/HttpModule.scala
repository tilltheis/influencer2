package influencer2

import zio.{Cause, RLayer, Trace, UIO, ZIO, ZLayer}
import zio.http.{Handler, HttpAppMiddleware, Request, RequestHandlerMiddleware, Response, Server}

case class HttpModule(server: Server)

object HttpModule:
  val layer: RLayer[UserModule, HttpModule] =
    val zio = for
      userModule <- ZIO.service[UserModule]
      server     <- ZIO.service[Server]
      httpApp = loggingHttpApp(AppRouter(userModule.userService))
      _ <- server.install(httpApp, Some(errorCallback))
      _ <- ZIO.log(s"HTTP server listening on port ${server.port}")
      _ <- ZIO.addFinalizer(ZIO.log("HTTP server shut down"))
    yield HttpModule(server)
    Server.default >>> ZLayer.scoped(zio)

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
