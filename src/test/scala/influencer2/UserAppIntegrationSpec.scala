package influencer2

import influencer2.http.{AppController, AppRouter}
import influencer2.user.{MongoUserDao, UserService}
import zio.{IO, ZIO}
import zio.http.model.{HeaderNames, HeaderValues, Headers, MediaType, Status}
import zio.http.model.headers.values.ContentType
import zio.http.{!!, Body, Path, Request, Response, URL}
import zio.json.DecoderOps
import zio.json.ast.Json
import zio.test.*

object UserAppIntegrationSpec extends ZIOSpecDefault:
  val DefaultRequestFailure: String = "request failed"

  def jsonPutRequest(jsonBody: String, path: Path): Request = Request
    .put(Body.fromString(jsonBody), URL(path))
    .addHeader(HeaderNames.contentType, HeaderValues.applicationJson)

  def getRequest(path: Path): Request = Request.get(URL(path))

  def jsonResponse(request: Request, orElseFailWith: String = DefaultRequestFailure): ZIO[AppRouter, Any, Response] =
    ZIO.service[AppRouter].flatMap(_.routes.runZIO(request).orElseFail(orElseFailWith))

  def jsonPutResponse(
      jsonBody: String,
      path: Path,
      orElseFailWith: String = DefaultRequestFailure
  ): ZIO[AppRouter, Any, Response] =
    jsonResponse(jsonPutRequest(jsonBody, path), orElseFailWith)

  def getResponse(path: Path, orElseFailWith: String = DefaultRequestFailure): ZIO[AppRouter, Any, Response] =
    jsonResponse(getRequest(path), orElseFailWith)

  def jsonBody(response: Response): IO[Any, Json] = response.body.asString.flatMap(x => ZIO.from(x.fromJson[Json]))

  override def spec: Spec[Any, Any] = suite(getClass.getSimpleName)(
    suite("PUT /users/$username")(
      test("creates new user if username is available") {
        for
          response             <- jsonPutResponse("""{ "password": "secret" }""", !! / "users" / "test-user")
          responseJson         <- jsonBody(response)
          expectedResponseJson <- ZIO.from("""{ "username": "test-user" }""".fromJson[Json])
        yield assertTrue(response.status == Status.Created && responseJson == expectedResponseJson)
      },
      test("returns existing user if new user is same as existing user") {
        for
          request      <- ZIO.succeed(jsonPutRequest("""{ "password": "secret" }""", !! / "users" / "test-user"))
          _            <- jsonResponse(request)
          response     <- jsonResponse(request)
          responseJson <- jsonBody(response)
          expectedResponseJson <- ZIO.from("""{ "username": "test-user" }""".fromJson[Json])
        yield assertTrue(response.status == Status.Created && responseJson == expectedResponseJson)
      },
      test("reports conflict if new user is different from existing user") {
        for
          _                    <- jsonPutResponse("""{ "password": "secret" }""", !! / "users" / "test-user")
          response             <- jsonPutResponse("""{ "password": "different-secret" }""", !! / "users" / "test-user")
          responseJson         <- jsonBody(response)
          expectedResponseJson <- ZIO.from("""{ "message": "username already taken" }""".fromJson[Json])
        yield assertTrue(response.status == Status.Conflict && responseJson == expectedResponseJson)
      }
    ),
    suite("GET /users/$username")(
      test("returns user if username exists") {
        for
          _                    <- jsonPutResponse("""{ "password": "secret" }""", !! / "users" / "test-user")
          response             <- getResponse(!! / "users" / "test-user")
          responseJson         <- jsonBody(response)
          expectedResponseJson <- ZIO.from("""{ "username": "test-user" }""".fromJson[Json])
        yield assertTrue(response.status == Status.Ok && responseJson == expectedResponseJson)
      },
      test("returns not found if username does not exist") {
        for
          response             <- getResponse(!! / "users" / "test-user")
          responseJson         <- jsonBody(response)
          expectedResponseJson <- ZIO.from("""{ "message": "user not found" }""".fromJson[Json])
        yield assertTrue(response.status == Status.NotFound && responseJson == expectedResponseJson)
      }
    )
  ).provide(TestRouter.layer)
