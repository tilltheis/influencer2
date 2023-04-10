package influencer2

import influencer2.HttpTestHelpers.parseJson
import influencer2.TestRequest.{get, put}
import zio.{Random, ZIO}
import zio.http.!!
import zio.http.model.Status
import zio.test.{Spec, TestRandom, ZIOSpecDefault, assertTrue, suite, test}

object UserAppIntegrationSpec extends ZIOSpecDefault:
  override def spec: Spec[Any, Any] = suite(getClass.getSimpleName)(
    suite("PUT /users/$username")(
      test("creates new user if username is available") {
        for
          id                   <- Random.nextUUID
          _                    <- TestRandom.feedUUIDs(id)
          response             <- put(!! / "users" / "test-user", """{ "password": "secret" }""").run
          expectedResponseJson <- parseJson(s"""{ "id": "${id.toString}", "username": "test-user" }""")
        yield assertTrue(response.status == Status.Created && response.jsonBody == expectedResponseJson)
      },
      test("returns existing user if new user is same as existing user") {
        for
          id <- Random.nextUUID
          _  <- TestRandom.feedUUIDs(id)
          request = put(!! / "users" / "test-user", """{ "password": "secret" }""")
          _                    <- request.run
          response             <- request.run
          expectedResponseJson <- parseJson(s"""{ "id": "${id.toString}", "username": "test-user" }""")
        yield assertTrue(response.status == Status.Created && response.jsonBody == expectedResponseJson)
      },
      test("reports conflict if new user is different from existing user") {
        for
          _                    <- put(!! / "users" / "test-user", """{ "password": "secret" }""").run
          response             <- put(!! / "users" / "test-user", """{ "password": "different-secret" }""").run
          expectedResponseJson <- parseJson("""{ "message": "username already taken" }""")
        yield assertTrue(response.status == Status.Conflict && response.jsonBody == expectedResponseJson)
      }
    ),
    suite("GET /users/$username")(
      test("returns user if username exists") {
        for
          id                   <- Random.nextUUID
          _                    <- TestRandom.feedUUIDs(id)
          _                    <- put(!! / "users" / "test-user", """{ "password": "secret" }""").run
          response             <- get(!! / "users" / "test-user").run
          expectedResponseJson <- parseJson(s"""{ "id": "${id.toString}", "username": "test-user" }""")
        yield assertTrue(response.status == Status.Ok && response.jsonBody == expectedResponseJson)
      },
      test("returns not found if username does not exist") {
        for
          response             <- get(!! / "users" / "test-user").run
          expectedResponseJson <- parseJson("""{ "message": "user not found" }""")
        yield assertTrue(response.status == Status.NotFound && response.jsonBody == expectedResponseJson)
      }
    )
  ).provide(TestRouter.layer)
