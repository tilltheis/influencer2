package influencer2

import influencer2.HttpTestHelpers.{createTestUser, parseJson}
import influencer2.TestRequest.{get, post, put}
import zio.{Random, ZIO}
import zio.http.!!
import zio.http.model.Status
import zio.json.ast.{Json, JsonCursor}
import zio.test.{Spec, TestClock, TestRandom, ZIOSpecDefault, assertTrue, suite, test}

import java.time.Instant

object UserAppIntegrationSpec extends ZIOSpecDefault:
  override def spec: Spec[Any, Any] = suite(getClass.getSimpleName)(
    suite("PUT /users/$username")(
      test("creates new user if username is available") {
        for
          id       <- Random.nextUUID
          _        <- TestRandom.feedUUIDs(id)
          _        <- TestClock.setTime(Instant.ofEpochSecond(123456789))
          response <- put(!! / "users" / "test-user", """{ "password": "secret" }""").run
          expectedResponseJson = Json.Obj(
            "id"            -> Json.Str(id.toString),
            "createdAt"     -> Json.Str("1973-11-29T21:33:09Z"),
            "username"      -> Json.Str("test-user"),
            "postCount"     -> Json.Num(0),
            "followerCount" -> Json.Num(0),
            "followeeCount" -> Json.Num(0),
            "followers"     -> Json.Obj(),
            "followees"     -> Json.Obj()
          )
        yield assertTrue(response.status == Status.Created && response.jsonBody == expectedResponseJson)
      },
      test("returns existing user if new user is same as existing user") {
        for
          id <- Random.nextUUID
          _  <- TestRandom.feedUUIDs(id)
          _  <- TestClock.setTime(Instant.ofEpochSecond(123456789))
          request = put(!! / "users" / "test-user", """{ "password": "secret" }""")
          _        <- request.run
          response <- request.run
          expectedResponseJson = Json.Obj(
            "id"            -> Json.Str(id.toString),
            "createdAt"     -> Json.Str("1973-11-29T21:33:09Z"),
            "username"      -> Json.Str("test-user"),
            "postCount"     -> Json.Num(0),
            "followerCount" -> Json.Num(0),
            "followeeCount" -> Json.Num(0),
            "followers"     -> Json.Obj(),
            "followees"     -> Json.Obj()
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
          _        <- TestClock.setTime(Instant.ofEpochSecond(123456789))
          (id, _)  <- createTestUser("test-user")
          response <- get(!! / "users" / "test-user").run
          expectedResponseJson = Json.Obj(
            "id"            -> Json.Str(id.toString),
            "createdAt"     -> Json.Str("1973-11-29T21:33:09Z"),
            "username"      -> Json.Str("test-user"),
            "postCount"     -> Json.Num(0),
            "followerCount" -> Json.Num(0),
            "followeeCount" -> Json.Num(0),
            "followers"     -> Json.Obj(),
            "followees"     -> Json.Obj()
          )
        yield assertTrue(response.status == Status.Ok && response.jsonBody == expectedResponseJson)
      },
      test("returns user with correct post count if username exists") {
        for
          _          <- TestClock.setTime(Instant.ofEpochSecond(123456789))
          (id, auth) <- createTestUser("test-user")
          _ <- post(!! / "posts", """{ "imageUrl": "https://example.org", "message": "test" }""").authed(auth).run
          response <- get(!! / "users" / "test-user").run
          expectedResponseJson = Json.Obj(
            "id"            -> Json.Str(id.toString),
            "createdAt"     -> Json.Str("1973-11-29T21:33:09Z"),
            "username"      -> Json.Str("test-user"),
            "postCount"     -> Json.Num(1),
            "followerCount" -> Json.Num(0),
            "followeeCount" -> Json.Num(0),
            "followers"     -> Json.Obj(),
            "followees"     -> Json.Obj()
          )
        yield assertTrue(response.status == Status.Ok && response.jsonBody == expectedResponseJson)
      },
      test("returns not found if username does not exist") {
        for
          response             <- get(!! / "users" / "test-user").run
          expectedResponseJson <- parseJson("""{ "message": "user not found" }""")
        yield assertTrue(response.status == Status.NotFound && response.jsonBody == expectedResponseJson)
      }
    ),
    suite("PUT /users/$followeeUsername/followers/$followerUsername")(
      test("adds the logged-in user to the followers of the followee") {
        for
          (followeeId, followeeAuth) <- createTestUser("followee-user")
          (followerId, followerAuth) <- createTestUser("follower-user")
          followeeResponseBefore     <- get(!! / "users" / "followee-user").run
          followerCountBefore <- ZIO.from(
            followeeResponseBefore.jsonBody.get(JsonCursor.field("followerCount").isNumber)
          )
          followersBefore <- ZIO.from(followeeResponseBefore.jsonBody.get(JsonCursor.field("followers").isObject))
          followerResponseBefore <- get(!! / "users" / "follower-user").run
          followeeCountBefore <- ZIO.from(
            followerResponseBefore.jsonBody.get(JsonCursor.field("followeeCount").isNumber)
          )
          followeesBefore <- ZIO.from(followerResponseBefore.jsonBody.get(JsonCursor.field("followees").isObject))
          followResponse <- put(!! / "users" / "followee-user" / "followers" / "follower-user").authed(followerAuth).run
          followeeResponseAfter <- get(!! / "users" / "followee-user").run
          followerCountAfter <- ZIO.from(
            followeeResponseAfter.jsonBody.get(JsonCursor.field("followerCount").isNumber)
          )
          followersAfter        <- ZIO.from(followeeResponseAfter.jsonBody.get(JsonCursor.field("followers").isObject))
          followerResponseAfter <- get(!! / "users" / "follower-user").run
          followeeCountAfter <- ZIO.from(
            followerResponseAfter.jsonBody.get(JsonCursor.field("followeeCount").isNumber)
          )
          followeesAfter <- ZIO.from(followerResponseAfter.jsonBody.get(JsonCursor.field("followees").isObject))
        yield assertTrue(
          followResponse.status == Status.Created &&
            followerCountBefore == Json.Num(0) &&
            followersBefore == Json.Obj() &&
            followerCountAfter == Json.Num(1) &&
            followersAfter == Json.Obj(followerId.toString -> Json.Str("follower-user")) &&
            followeeCountBefore == Json.Num(0) &&
            followeesBefore == Json.Obj() &&
            followeeCountAfter == Json.Num(1) &&
            followeesAfter == Json.Obj(followeeId.toString -> Json.Str("followee-user"))
        )
      }
    )
  ).provide(TestRouter.layer)
