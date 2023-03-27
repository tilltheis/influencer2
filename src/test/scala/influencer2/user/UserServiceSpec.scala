package influencer2.user

import influencer2.user.UserService.UserCreationConflict
import org.mindrot.jbcrypt.BCrypt
import zio.ZIO
import zio.test.*

import java.util.UUID

object UserServiceSpec extends ZIOSpecDefault {
  override def spec: Spec[Any, Any] = suite(UserServiceSpec.getClass.getSimpleName)(
    suite("createUser")(
      test("creates new user if username is not taken") {
        for
          userService           <- ZIO.service[UserService]
          expectedUuid          <- ZIO.random.flatMap(_.nextUUID)
          _                     <- TestRandom.feedUUIDs(expectedUuid)
          user                  <- userService.createUser(CreateUser("name", "password"))
          isCorrectPasswordHash <- ZIO.attemptBlocking(BCrypt.checkpw("password", user.passwordHash))
        yield assertTrue(user == User(UserId(expectedUuid), "name", user.passwordHash), isCorrectPasswordHash)
      },
      test("returns existing user if existing user is same as new user") {
        for
          userService <- ZIO.service[UserService]
          oldUser     <- userService.createUser(CreateUser("name", "password"))
          newUser     <- userService.createUser(CreateUser("name", "password"))
        yield assertTrue(newUser == oldUser)
      },
      test("reports conflict if existing user is different from new user") {
        for
          userService <- ZIO.service[UserService]
          _           <- userService.createUser(CreateUser("name", "password"))
          conflict    <- userService.createUser(CreateUser("name", "different password")).flip
        yield assertTrue(conflict == UserCreationConflict)
      }
    ).provide(UserService.layer, InMemoryUserDao.layer)
  )
}
