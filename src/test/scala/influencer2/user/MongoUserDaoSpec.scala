package influencer2.user

import influencer2.TestDatabase
import zio.ZIO
import zio.test.*

import java.util.UUID

object MongoUserDaoSpec extends ZIOSpecDefault {
  override def spec: Spec[Any, Any] = suite(getClass.getSimpleName)(
    suite("createUser")(
      test("creates new user if username is available") {
        for
          userDao <- ZIO.service[MongoUserDao]
          userId  <- UserId.random
          inUser = User(userId, "name", "passwordHash")
          outUser <- userDao.createUser(inUser)
        yield assertTrue(outUser == inUser)
      },
      test("returns existing user if username is already taken") {
        for
          userDao <- ZIO.service[MongoUserDao]
          userId  <- UserId.random
          user = User(userId, "name", "passwordHash")
          _                 <- userDao.createUser(user)
          userAlreadyExists <- userDao.createUser(user).flip
        yield assertTrue(userAlreadyExists.user == user)
      }
    )
  ).provide(MongoUserDao.layer, TestDatabase.layer)
}
