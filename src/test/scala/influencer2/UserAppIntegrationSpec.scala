package influencer2

import influencer2.HttpTestHelpers.{createTestUser, parseJson}
import influencer2.TestRequest.{get, put, post}
import zio.{Random, ZIO}
import zio.http.!!
import zio.http.model.Status
import zio.json.ast.Json
import zio.test.{Spec, TestRandom, ZIOSpecDefault, assertTrue, suite, test}

object UserAppIntegrationSpec extends ZIOSpecDefault:
  override def spec: Spec[Any, Any] = suite(getClass.getSimpleName)(
    suite("PUT /users/$username")(
      test("creates new user if username is available") {
        for
          id       <- Random.nextUUID
          _        <- TestRandom.feedUUIDs(id)
          response <- put(!! / "users" / "test-user", """{ "password": "secret" }""").run
          expectedResponseJson = Json.Obj(
            "id"            -> Json.Str(id.toString),
            "username"      -> Json.Str("test-user"),
            "postCount"     -> Json.Num(0),
            "followerCount" -> Json.Num(0),
            "followeeCount" -> Json.Num(0)
          )
        yield assertTrue(response.status == Status.Created && response.jsonBody == expectedResponseJson)
      },
      test("returns existing user if new user is same as existing user") {
        for
          id <- Random.nextUUID
          _  <- TestRandom.feedUUIDs(id)
          request = put(!! / "users" / "test-user", """{ "password": "secret" }""")
          _        <- request.run
          response <- request.run
          expectedResponseJson = Json.Obj(
            "id"            -> Json.Str(id.toString),
            "username"      -> Json.Str("test-user"),
            "postCount"     -> Json.Num(0),
            "followerCount" -> Json.Num(0),
            "followeeCount" -> Json.Num(0)
          )
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
          (id, _)  <- createTestUser("test-user")
          response <- get(!! / "users" / "test-user").run
          expectedResponseJson = Json.Obj(
            "id"            -> Json.Str(id.toString),
            "username"      -> Json.Str("test-user"),
            "postCount"     -> Json.Num(0),
            "followerCount" -> Json.Num(0),
            "followeeCount" -> Json.Num(0)
          )
        yield assertTrue(response.status == Status.Ok && response.jsonBody == expectedResponseJson)
      },
      test("returns user with correct post count if username exists") {
        for
          (id, auth) <- createTestUser("test-user")
          _ <- post(!! / "posts", """{ "imageUrl": "https://example.org", "message": "test" }""").authed(auth).run
          response <- get(!! / "users" / "test-user").run
          expectedResponseJson = Json.Obj(
            "id"            -> Json.Str(id.toString),
            "username"      -> Json.Str("test-user"),
            "postCount"     -> Json.Num(1),
            "followerCount" -> Json.Num(0),
            "followeeCount" -> Json.Num(0)
          )
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
