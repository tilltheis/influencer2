package influencer2

import influencer2.http.AppRouter
import zio.{IO, ZIO}
import zio.http.model.{HeaderNames, HeaderValues}
import zio.http.{Body, Path, Request, Response, URL}
import zio.json.DecoderOps
import zio.json.ast.Json

case class TestResponse(response: Response, jsonBody: Json):
  export response.status

object TestResponse:
  def fromResponse(response: Response): IO[Any, TestResponse] =
    for
      stringBody <- response.body.asString
      jsonBody   <- HttpTestHelpers.parseJson(stringBody)
    yield TestResponse(response, jsonBody)

object HttpTestHelpers:
  val DefaultRequestFailure: String = "request failed"

  def putRequest(jsonBody: String, path: Path): Request =
    Request
      .put(Body.fromString(jsonBody), URL(path))
      .addHeader(HeaderNames.contentType, HeaderValues.applicationJson)

  def getRequest(path: Path): Request = Request.get(URL(path))

  def runRequest(request: Request, orElseFailWith: String = DefaultRequestFailure): ZIO[AppRouter, Any, TestResponse] =
    ZIO
      .service[AppRouter]
      .flatMap(_.routes.runZIO(request).orElseFail(orElseFailWith))
      .flatMap(TestResponse.fromResponse)

  def runPutRequest(
      jsonBody: String,
      path: Path,
      orElseFailWith: String = DefaultRequestFailure
  ): ZIO[AppRouter, Any, TestResponse] =
    runRequest(putRequest(jsonBody, path), orElseFailWith)

  def runGetRequest(path: Path, orElseFailWith: String = DefaultRequestFailure): ZIO[AppRouter, Any, TestResponse] =
    runRequest(getRequest(path), orElseFailWith)

  def parseJson(jsonString: String): IO[String, Json] = ZIO.from(jsonString.fromJson[Json])
end HttpTestHelpers
