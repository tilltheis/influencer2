package influencer2

import influencer2.http.AppRouter
import zio.{IO, ZIO}
import zio.http.model.{Cookie, Header, HeaderNames, HeaderValues}
import zio.http.{Body, Path, Request, Response, URL}
import zio.json.DecoderOps
import zio.json.ast.{Json, JsonCursor}

case class TestRequest(request: Request):
  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def authed(auth: TestAuth): TestRequest = copy(
    request
      .addHeader(HeaderNames.authorization, s"Bearer ${auth.token}")
      .withCookie(Cookie("jwt-signature", auth.signature).toRequest.encode.fold(throw _, identity))
  )

  def run(failurePrefix: String): ZIO[AppRouter, Any, TestResponse] =
    ZIO
      .service[AppRouter]
      .flatMap(_.routes.runZIO(request).orElseFail(s"$failurePrefix failed"))
      .flatMap(TestResponse.fromResponse)

  def run: ZIO[AppRouter, Any, TestResponse] = run("request")

object TestRequest:
  def get(path: Path): TestRequest = TestRequest(Request.get(URL(path)))
  def post(path: Path, jsonBody: String): TestRequest = TestRequest(
    Request
      .post(Body.fromString(jsonBody), URL(path))
      .addHeader(HeaderNames.contentType, HeaderValues.applicationJson)
  )
  def put(path: Path, jsonBody: String): TestRequest = TestRequest(
    Request
      .put(Body.fromString(jsonBody), URL(path))
      .addHeader(HeaderNames.contentType, HeaderValues.applicationJson)
  )
  def delete(path: Path): TestRequest = TestRequest(Request.delete(URL(path)))
end TestRequest

case class TestResponse(response: Response, jsonBody: Json):
  export response.status

  def setCookieDecoded(name: CharSequence): Option[Cookie[Response]] = response.setCookiesDecoded().find(_.name == name)

  def auth: IO[String, TestAuth] =
    for
      token <- ZIO
        .from(jsonBody.get(JsonCursor.field("token").isString).map(_.value))
        .mapError(e => s"missing token: $e")
      signature <- ZIO.from(setCookieDecoded("jwt-signature").map(_.content)).orElseFail("missing signature")
    yield TestAuth(token, signature)
end TestResponse

object TestResponse:
  def fromResponse(response: Response): IO[Any, TestResponse] =
    for
      stringBody <- response.body.asString
      jsonBody   <- HttpTestHelpers.parseJson(stringBody)
    yield TestResponse(response, jsonBody)

case class TestAuth(token: String, signature: String)

object HttpTestHelpers:
  val DefaultRequestFailure: String = "request failed"

  def putRequest(jsonBody: String, path: Path): Request =
    Request
      .put(Body.fromString(jsonBody), URL(path))
      .addHeader(HeaderNames.contentType, HeaderValues.applicationJson)

  def postRequest(jsonBody: String, path: Path): Request =
    Request
      .post(Body.fromString(jsonBody), URL(path))
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

  def runPostRequest(
      jsonBody: String,
      path: Path,
      orElseFailWith: String = DefaultRequestFailure
  ): ZIO[AppRouter, Any, TestResponse] =
    runRequest(postRequest(jsonBody, path), orElseFailWith)

  def runGetRequest(path: Path, orElseFailWith: String = DefaultRequestFailure): ZIO[AppRouter, Any, TestResponse] =
    runRequest(getRequest(path), orElseFailWith)

  def parseJson(jsonString: String): IO[String, Json] = ZIO.from(jsonString.fromJson[Json])
end HttpTestHelpers
