package influencer2

import influencer2.HttpTestHelpers.{createTestUser, createTestUserAuth, parseJson}
import influencer2.TestRequest.{get, post}
import zio.ZIO
import zio.http.!!
import zio.http.model.Status
import zio.json.ast.JsonCursor
import zio.test.{Spec, TestClock, ZIOSpecDefault, assertTrue, suite, test}

import java.time.Instant
import java.util.UUID

object PostAppIntegrationSpec extends ZIOSpecDefault:
  override def spec: Spec[Any, Any] = suite(getClass.getSimpleName)(
    suite("POST /posts")(
      test("creates new post") {
        for
          _              <- TestClock.setTime(Instant.parse("2023-04-10T16:54:32.123Z"))
          (userId, auth) <- createTestUser("test-user")
          response <-
            post(!! / "posts", """{ "imageUrl": "https://example.org", "message": "test" }""").authed(auth).run
          id <- ZIO.from(response.jsonBody.get(JsonCursor.field("id").isString)).map(_.value)
          expectedResponseJson <- parseJson(s"""
            |{
            |  "id": "$id",
            |  "userId": "${userId.toString}",
            |  "username": "test-user",
            |  "createdAt": "2023-04-10T16:54:32.123Z",
            |  "imageUrl": "https://example.org",
            |  "message": "test"
            |}
            |""".stripMargin)
        yield assertTrue(response.status == Status.Created && response.jsonBody == expectedResponseJson)
      },
      test("rejects non-https media urls") {
        for
          auth                 <- createTestUserAuth("test-user")
          response             <- post(!! / "posts", """{ "imageUrl": "http://example.org" }""").authed(auth).run
          expectedResponseJson <- parseJson("""{ "message": "image url must be https" }""")
        yield assertTrue(response.status == Status.UnprocessableEntity && response.jsonBody == expectedResponseJson)
      },
      suite("trims message contents")(
        Seq("" -> None, "\\t" -> None, "\\tfoo " -> Some("foo")).map { case (input, expectedOutput) =>
          test(s"for input message \"$input\"") {
            for
              auth <- createTestUserAuth("test-user")
              response <-
                post(!! / "posts", s"""{ "imageUrl": "https://example.org", "message": "$input" }""").authed(auth).run
              messageOption = response.jsonBody.get(JsonCursor.field("message").isString).toOption.map(_.value)
            yield assertTrue(messageOption == expectedOutput)
          }
        }*
      )
    ),
    suite("GET /posts")(
      test("returns nothing if there are no posts") {
        for
          response             <- get(!! / "posts").run
          expectedResponseJson <- parseJson("""[]""")
        yield assertTrue(response.status == Status.Ok && response.jsonBody == expectedResponseJson)
      },
      test("returns all posts from latest to earliest") {
        for
          _                <- TestClock.setTime(Instant.parse("2023-01-01T00:00:00.999Z"))
          (userId1, auth1) <- createTestUser("user1")
          postResponse1_1  <- post(!! / "posts", """{ "imageUrl": "https://example.org/1_1" }""").authed(auth1).run
          _                <- TestClock.setTime(Instant.parse("2023-01-02T00:00:00.999Z"))
          postResponse1_2  <- post(!! / "posts", """{ "imageUrl": "https://example.org/1_2" }""").authed(auth1).run
          _                <- TestClock.setTime(Instant.parse("2023-02-01T00:00:00.999Z"))
          (userId2, auth2) <- createTestUser("user2")
          postResponse2_1  <- post(!! / "posts", """{ "imageUrl": "https://example.org/2_1" }""").authed(auth2).run
          postId1_1        <- ZIO.from(postResponse1_1.jsonBody.get(JsonCursor.field("id").isString)).map(_.value)
          postId1_2        <- ZIO.from(postResponse1_2.jsonBody.get(JsonCursor.field("id").isString)).map(_.value)
          postId2_1        <- ZIO.from(postResponse2_1.jsonBody.get(JsonCursor.field("id").isString)).map(_.value)
          response         <- get(!! / "posts").run
          expectedResponseJson <- parseJson(s"""
               |[{
               |  "id": "$postId2_1",
               |  "userId": "${userId2.toString}",
               |  "username": "user2",
               |  "createdAt": "2023-02-01T00:00:00.999Z",
               |  "imageUrl": "https://example.org/2_1"
               |}, {
               |  "id": "$postId1_2",
               |  "userId": "${userId1.toString}",
               |  "username": "user1",
               |  "createdAt": "2023-01-02T00:00:00.999Z",
               |  "imageUrl": "https://example.org/1_2"
               |}, {
               |  "id": "$postId1_1",
               |  "userId": "${userId1.toString}",
               |  "username": "user1",
               |  "createdAt": "2023-01-01T00:00:00.999Z",
               |  "imageUrl": "https://example.org/1_1"
               |}]
               |""".stripMargin)
        yield assertTrue(response.status == Status.Ok && response.jsonBody == expectedResponseJson)
      }
    )
  ).provide(TestRouter.layer)
