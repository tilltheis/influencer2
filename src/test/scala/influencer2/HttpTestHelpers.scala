package influencer2

import influencer2.application.{AppRouter, UserResponse}
import zio.http.model.{Cookie, Header, HeaderNames, HeaderValues}
import zio.http.*
import zio.json.DecoderOps
import zio.json.ast.{Json, JsonCursor}
import zio.{IO, ZIO}

import java.util.UUID

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
  def get(path: Path, queryParams: QueryParams = QueryParams.empty): TestRequest = TestRequest(
    Request.get(URL(path, queryParams = queryParams))
  )
  def post(path: Path, jsonBody: String): TestRequest = TestRequest(
    Request
      .post(Body.fromString(jsonBody), URL(path))
      .addHeader(HeaderNames.contentType, HeaderValues.applicationJson)
  )
  def put(path: Path): TestRequest = TestRequest(Request.put(Body.empty, URL(path)))
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
      jsonBody   <- HttpTestHelpers.parseJson(stringBody).mapError(e => s"$e. String response was: $stringBody")
    yield TestResponse(response, jsonBody)

case class TestAuth(token: String, signature: String)

object HttpTestHelpers:
  def parseJson(jsonString: String): IO[String, Json] = ZIO.from(jsonString.fromJson[Json])

  def createTestUser(username: String): ZIO[AppRouter, Any, (UUID, TestAuth)] =
    for
      userResponse    <- TestRequest.put(!! / "users" / username, """{ "password": "secret" }""").run
      jsonStringId    <- ZIO.from(userResponse.jsonBody.get(JsonCursor.field("id").isString))
      id              <- ZIO.attempt(UUID.fromString(jsonStringId.value))
      sessionResponse <- TestRequest.post(!! / "sessions", s"""{ "username": "$username", "password": "secret" }""").run
      auth            <- sessionResponse.auth
    yield (id, auth)

  def createTestUserAuth(username: String): ZIO[AppRouter, Any, TestAuth] = createTestUser(username).map(_._2)
