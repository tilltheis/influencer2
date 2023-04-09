package influencer2

import influencer2.HttpTestHelpers.parseJson
import influencer2.TestRequest.{delete, post, put}
import zio.Clock
import zio.http.!!
import zio.http.model.{Cookie, Status}
import zio.json.ast.JsonCursor
import zio.test.{Spec, ZIOSpecDefault, assertTrue, suite, test}

import java.util.concurrent.TimeUnit

object SessionAppIntegrationSpec extends ZIOSpecDefault:
  override def spec: Spec[Any, Any] = suite(getClass.getSimpleName)(
    suite("POST /sessions")(
      test("creates session token when given correct user credentials") {
        for
          _                    <- put(!! / "users" / "test-user", """{ "password": "secret" }""").run
          response             <- post(!! / "sessions", """{ "username": "test-user", "password": "secret" }""").run
          auth                 <- response.auth
          expectedResponseJson <- parseJson(s"""{ "token": "${auth.token}" }""")
          expectedMaxAge       <- Clock.currentDateTime.map(_.plusDays(30).toEpochSecond)
          expectedCookie =
            Cookie("jwt-signature", auth.signature, isSecure = false, isHttpOnly = true, maxAge = Some(expectedMaxAge))
        yield assertTrue(
          response.status == Status.Ok &&
            response.jsonBody == expectedResponseJson &&
            response.setCookieDecoded("jwt-signature") == Some(expectedCookie)
        )
      },
      test("reports unauthorized when password is wrong") {
        for
          _                    <- put(!! / "users" / "test-user", """{ "password": "secret" }""").run
          response             <- post(!! / "sessions", """{ "username": "test-user", "password": "wrong" }""").run
          expectedResponseJson <- parseJson("""{ "message": "invalid credentials" }""")
        yield assertTrue(response.status == Status.Unauthorized && response.jsonBody == expectedResponseJson)
      },
      test("reports unauthorized when user does not exist") {
        for
          response             <- post(!! / "sessions", """{ "username": "test-user", "password": "secret" }""").run
          expectedResponseJson <- parseJson("""{ "message": "invalid credentials" }""")
        yield assertTrue(response.status == Status.Unauthorized && response.jsonBody == expectedResponseJson)
      }
    ),
    suite("DELETE /sessions")(
      test("clears the token signature cookie when the user is logged in") {
        for
          _                    <- put(!! / "users" / "test-user", """{ "password": "secret" }""").run
          loginResponse        <- post(!! / "sessions", """{ "username": "test-user", "password": "secret" }""").run
          auth                 <- loginResponse.auth
          logoutResponse       <- delete(!! / "sessions").authed(auth).run
          expectedResponseJson <- parseJson("""{ "message": "logged out" }""")
          expectedCookie = Cookie("jwt-signature", "", isSecure = false, isHttpOnly = true)
        yield assertTrue(
          logoutResponse.status == Status.Ok &&
            logoutResponse.jsonBody == expectedResponseJson &&
            logoutResponse.setCookieDecoded("jwt-signature") == Some(expectedCookie)
        )
      }
    )
  ).provide(TestRouter.layer)
