package influencer2

import influencer2.http.AppRouter
import zio.{IO, ZIO}
import zio.http.model.{HeaderNames, HeaderValues, Status}
import zio.http.{!!, Body, Path, Request, Response, URL}
import zio.json.DecoderOps
import zio.json.ast.Json
import zio.test.*

object UserAppIntegrationSpec extends ZIOSpecDefault:
  case class TestResponse(response: Response, jsonBody: Json):
    export response.status

  object TestResponse:
    def fromResponse(response: Response): IO[Any, TestResponse] =
      for
        stringBody <- response.body.asString
        jsonBody   <- parseJson(stringBody)
      yield TestResponse(response, jsonBody)

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

  override def spec: Spec[Any, Any] = suite(getClass.getSimpleName)(
    suite("PUT /users/$username")(
      test("creates new user if username is available") {
        for
          response             <- runPutRequest("""{ "password": "secret" }""", !! / "users" / "test-user")
          expectedResponseJson <- parseJson("""{ "username": "test-user" }""")
        yield assertTrue(response.status == Status.Created && response.jsonBody == expectedResponseJson)
      },
      test("returns existing user if new user is same as existing user") {
        for
          request              <- ZIO.succeed(putRequest("""{ "password": "secret" }""", !! / "users" / "test-user"))
          _                    <- runRequest(request)
          response             <- runRequest(request)
          expectedResponseJson <- parseJson("""{ "username": "test-user" }""")
        yield assertTrue(response.status == Status.Created && response.jsonBody == expectedResponseJson)
      },
      test("reports conflict if new user is different from existing user") {
        for
          _                    <- runPutRequest("""{ "password": "secret" }""", !! / "users" / "test-user")
          response             <- runPutRequest("""{ "password": "different-secret" }""", !! / "users" / "test-user")
          expectedResponseJson <- parseJson("""{ "message": "username already taken" }""")
        yield assertTrue(response.status == Status.Conflict && response.jsonBody == expectedResponseJson)
      }
    ),
    suite("GET /users/$username")(
      test("returns user if username exists") {
        for
          _                    <- runPutRequest("""{ "password": "secret" }""", !! / "users" / "test-user")
          response             <- runGetRequest(!! / "users" / "test-user")
          expectedResponseJson <- parseJson("""{ "username": "test-user" }""")
        yield assertTrue(response.status == Status.Ok && response.jsonBody == expectedResponseJson)
      },
      test("returns not found if username does not exist") {
        for
          response             <- runGetRequest(!! / "users" / "test-user")
          expectedResponseJson <- parseJson("""{ "message": "user not found" }""")
        yield assertTrue(response.status == Status.NotFound && response.jsonBody == expectedResponseJson)
      }
    )
  ).provide(TestRouter.layer)
