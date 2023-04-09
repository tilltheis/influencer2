package influencer2

import influencer2.HttpTestHelpers.*
import zio.http.!!
import zio.http.model.Status
import zio.test.*
import zio.{IO, ZIO, http}

object UserAppIntegrationSpec extends ZIOSpecDefault:
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
