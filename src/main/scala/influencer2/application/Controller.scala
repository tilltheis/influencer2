package influencer2.application

import zio.http.model.Status
import zio.{UIO, ZIO}
import zio.http.{Request, Response}
import zio.json.{DecoderOps, JsonDecoder}

object Controller:
  def withJsonRequest[A: JsonDecoder](request: Request)(f: A => UIO[Response]): UIO[Response] =
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
end Controller
