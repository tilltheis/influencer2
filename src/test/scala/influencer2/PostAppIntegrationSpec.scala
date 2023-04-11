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
          (userId, auth) <- createTestUser
          response <-
            post(!! / "posts", """{ "imageUrl": "https://example.org", "message": "test" }""").authed(auth).run
          id <- ZIO.from(response.jsonBody.get(JsonCursor.field("id").isString)).map(_.value)
          expectedResponseJson <- parseJson(s"""
            |{
            |  "id": "$id",
            |  "author": "${userId.toString}",
            |  "createdAt": "2023-04-10T16:54:32.123Z",
            |  "imageUrl": "https://example.org",
            |  "message": "test"
            |}
            |""".stripMargin)
        yield assertTrue(response.status == Status.Created && response.jsonBody == expectedResponseJson)
      },
      test("rejects non-https media urls") {
        for
          auth                 <- createTestUserAuth
          response             <- post(!! / "posts", """{ "imageUrl": "http://example.org" }""").authed(auth).run
          expectedResponseJson <- parseJson("""{ "message": "image url must be https" }""")
        yield assertTrue(response.status == Status.UnprocessableEntity && response.jsonBody == expectedResponseJson)
      },
      suite("trims message contents")(
        Seq("" -> None, "\\t" -> None, "\\tfoo " -> Some("foo")).map { case (input, expectedOutput) =>
          test(s"for input message \"$input\"") {
            for
              auth <- createTestUserAuth
              response <-
                post(!! / "posts", s"""{ "imageUrl": "https://example.org", "message": "$input" }""").authed(auth).run
              messageOption = response.jsonBody.get(JsonCursor.field("message").isString).toOption.map(_.value)
            yield assertTrue(messageOption == expectedOutput)
          }
        }*
      )
    )
  ).provide(TestRouter.layer)
